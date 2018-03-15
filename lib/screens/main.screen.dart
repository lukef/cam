import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:scan/models/camera_error.dart';
import 'package:scan/widgets/camera_view.dart';

class MainScreen extends StatefulWidget {
  @override
  _MainScreenState createState() => new _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  // consts
  final backgroundColor = const Color(0xFF222222);
  // channels
  static final _systemChromeChannel = new MethodChannel("com.lukef.scan/systemChrome");
  // members
  CameraError _cameraError;
  bool _isCameraInitialized = false;

  @override
  Widget build(BuildContext context) {
    _updateSystemChrome();
    return new Material(
      color: backgroundColor,
      child: (_cameraError == null ? _readyStateWidget() : _fullscreenErrorStateWidget()),
    );
  }

  _readyStateWidget() => Stack(
    children: <Widget>[
      Positioned.fill(
        // child: new Texture(textureId: _textureId),
        child: CameraView(
          onInitialized: (err) {
            _isCameraInitialized = (err == null);
            if (err != null) {
              _cameraError = err;
            }
            setState((){});
          },
        ),
      ),
      Positioned(
        bottom: 0.0,
        left: 0.0,
        right: 0.0,
        height: 140.0,
        child: Container(
          color: Color(0xAA000000),
          child: Center(
            child: Container(
              width: 80.0, height: 80.0,
              child: FlatButton(
                color: Color(0xFF42f498),
                highlightColor: Color(0x22000000),
                splashColor: Color(0x44000000),
                shape: CircleBorder(),
                child: Image.asset(
                  "assets/images/ic_camera_lens_black.png",
                  color: Color(0x88000000),
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

  _fullscreenErrorStateWidget() {
      _systemChromeChannel.invokeMethod("update", {
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
                  "${_cameraError.message}",
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
    _systemChromeChannel.invokeMethod("update", {
      "statusBarColor" : !_isCameraInitialized ? "#222222" : "#00FFFFFF",
      "navigationBarColor" : !_isCameraInitialized ? "#222222" : "#000000",
    });
  }

}