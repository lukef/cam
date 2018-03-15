class CameraError {
  CameraError(this.message, {this.code, this.data});
  CameraError.unknown() : message = "Unknown error occurred", code = "unknown";
  String message;
  String code;
  dynamic data;
}