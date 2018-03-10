package com.lukef.scan.camera.exceptions

class CameraInitializationException(message: String, val code: String = "general") : Exception(message)