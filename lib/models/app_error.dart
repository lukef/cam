class AppError {
  AppError(this.message, {this.code, this.data});
  AppError.unknown() : message = "Unknown error occurred", code = "unknown";
  String message;
  String code;
  dynamic data;
}