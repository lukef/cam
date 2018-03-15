import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'package:scan/models/camera_error.dart';

typedef void CameraInitializationCallback(CameraError error);

class CameraView extends StatefulWidget {

  final CameraInitializationCallback onInitialized;
  CameraView({this.onInitialized});

  @override
  _CameraViewState createState() => new _CameraViewState();
}

class _CameraViewState extends State<CameraView> {
  // channels
  static final _cameraChannel = new MethodChannel("com.lukef.scan/camera");
  // members
  int _textureId; // created texture identifier
  CameraError _error; // any error from the platform or otherwise

  @override
  void initState() {
    _registerCameraChannelHandler();
    _initializeCamera();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    if (_error != null) return _errorStateWidget();
    return (_textureId != null ? _previewWidget() : _intializationWidget());
  }

  /// Initializes the camera view
  /// Flutter notifies the platform that it wants to set up the native components
  /// to return data from the camera and, if you have permission, the platform will
  /// set up the texture and return the newly created texture id. If it fails, you
  /// will be able to interrogate the error.
  _initializeCamera() async {
    CameraError error;
    try {
      final Map<dynamic, dynamic> response = await _cameraChannel.invokeMethod("create");
      final errorCode = response["errorCode"] as String;
      if (errorCode == null) {
        setState(() {
          _textureId = response["textureId"];
        });
      } else {
        error = new CameraError(response["errorMessage"] as String ?? "An error occurred during camera initialization",
            code: errorCode, data: null);
      }
    } catch (ex) {
      error = CameraError.unknown();
      if (ex is PlatformException) {
        error = new CameraError(ex.message, code: ex.code, data: ex.details);
      }
    }
    if (error != null) {
      setState(() {
        _error = error;
      });      
    }
    if (widget.onInitialized != null) {
      widget.onInitialized(error);
    }
  }


  /// The default preview widget. This widget will display the camera output.
  Widget _previewWidget() {
    return Transform(
      transform: Matrix4.diagonal3Values(1.0, 0.82, 1.0),
      child: Texture(textureId: _textureId),
    );
  }

  /// The default widget that is shown while the camera is initializing. By default
  /// the widget will just display a progress bar. It can be customized.
  Widget _intializationWidget() {
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

  /// Shows a generic error state becasue the camera could not be initialized due
  /// to an error of some kind
  Widget _errorStateWidget() =>
    Container(
      child: Column(
        mainAxisSize: MainAxisSize.max,
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Text("Aaargh"),
          Text("We hit a snag while creating the camera view. Please reach out and let us know."),
        ],
      ),
    );

  /// Registers a method call handler for all camera related events
  _registerCameraChannelHandler() {
    _cameraChannel.setMethodCallHandler((call) {});
  }
}
