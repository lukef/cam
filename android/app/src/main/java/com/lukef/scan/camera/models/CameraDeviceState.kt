package com.lukef.scan.camera.models

import android.hardware.camera2.CameraDevice

class CameraDeviceState(val deviceInfo: CameraDeviceInfo, val stateCallback: CameraDevice.StateCallback) {
}