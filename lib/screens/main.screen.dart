import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:scan/models/app_error.dart';

class MainScreen extends StatefulWidget {
  @override
  _MainScreenState createState() => new _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  
  // consts
  final backgroundColor = const Color(0xFF222222);

  // channels
  static final scanChannel = new MethodChannel("com.lukef.scan/scan");
  static final systemChromeChannel = new MethodChannel("com.lukef.scan/systemChrome");

  // members
  int _textureId;
  AppError _error;

  @override
  void initState() {
    _registerScannerChannel();
    _initializeScanner();    
    super.initState();
  }

  _initializeScanner() async {
    try {
      final Map<dynamic, dynamic> response = await scanChannel.invokeMethod("create");
      setState(() {
        _textureId = response["textureId"];
      });
    } catch(ex) {
      var error = new AppError.unknown();
      if (ex is PlatformException) {
        error = new AppError(ex.message, code: ex.code, data: ex.details);
      }
      setState((){
        _error = error;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    _updateSystemChrome();
    return new Material(
      color: backgroundColor,
      child: _contentWidget(),
    );
  }

  // ui
  Widget _contentWidget() {
    if (_error != null) return _fullscreenErrorStateWidget();
    return (_textureId == null) ? _initializingStateWidget() : _readyStateWidget();
  }

  _readyStateWidget() {
    return new Stack(
      children: <Widget>[
        new Positioned.fill(child: new Texture(textureId: _textureId)),
        new Positioned(
          bottom: 0.0,
          left: 0.0,
          right: 0.0,
          height: 140.0,
          child: new Container(
            color: const Color(0xAA000000),
            child: new Center(
              child: new Container(
                width: 80.0, height: 80.0,
                child: new FlatButton(
                  color: const Color(0xFF42f498),
                  highlightColor: const Color(0x22000000),
                  splashColor: const Color(0x44000000),
                  shape: new CircleBorder(),
                  child: new Image.asset(
                    "assets/images/ic_camera_lens_black.png",
                    color: const Color(0x88000000),
                    height: 36.0,
                    width: 36.0,
                  ),
                  onPressed: () {},
                ),
              ),
            ),
          ),
        )
      ],
    );
  }

  _initializingStateWidget() {
    return new Container(
      child: new Center(
        child: new Container(
          width: 140.0,
          height: 1.0,
          child: new LinearProgressIndicator(
            backgroundColor: const Color(0x22000000),
            valueColor: new AlwaysStoppedAnimation<Color>(const Color(0xFFAAAAAA)),
            value: null,
          ),
        ),
      ),
    );
  }

  _fullscreenErrorStateWidget() {
      systemChromeChannel.invokeMethod("update", {
        "statusBarColor" : "#f44256",
        "navigationBarColor" : "#f44256",
      });
    return new Container(
      color: const Color(0xFFf44256),
      child: new Center(
        child: new Padding(
          padding: const EdgeInsets.only(left: 60.0, right: 60.0, top: 20.0, bottom: 20.0),
          child: new Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              new Text(
                "AARGH!",
                style: const TextStyle(
                  color: const Color(0xFFFFFFFF),
                  fontSize: 60.0,
                  fontFamily: "Roboto Condensed",
                  fontWeight: FontWeight.bold,
                  letterSpacing: -2.5,
                ),
              ),
              new Padding(
                padding: const EdgeInsets.only(top: 10.0),
                child: new Text(
                  "Apps are tricky and a bug slipped through somewhere. Don't worry though, we'll be looking into it.\n\nAt this point it's probably best to restart the app and email us if the problem persists.",
                  style: const TextStyle(
                    color: const Color(0xFFFFFFFF),
                    fontSize: 16.0,
                  ),
                ),
              ),
              new Padding(
                padding: const EdgeInsets.only(top: 50.0),
                child: new Text(
                  "TECHNICAL MUMBO JUMBO (what happened)",
                  style: const TextStyle(
                    color: const Color(0xFFFFFFFF),
                    fontSize: 12.0,
                    fontFamily: "Roboto Condensed",
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              new Padding(
                padding: const EdgeInsets.only(top: 10.0),
                child: new Text(
                  "${_error.message}",
                  style: const TextStyle(
                    color: const Color(0xFFFFFFFF),
                    fontFamily: "Roboto Mono",
                    fontSize: 13.0,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );    
  }

  _updateSystemChrome() {
    systemChromeChannel.invokeMethod("update", {
      "statusBarColor" : _textureId == null ? "#222222" : "#00FFFFFF",
      "navigationBarColor" : _textureId == null ? "#222222" : "#000000",
    });
  }

  // channel registration
  _registerScannerChannel() {
    scanChannel.setMethodCallHandler((call) {

    });
  }
}