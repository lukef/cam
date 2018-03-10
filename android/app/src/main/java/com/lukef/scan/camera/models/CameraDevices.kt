package com.lukef.scan.camera.models

class CameraDevices(val cameraDevices: List<CameraDeviceInfo>) {
	fun firstRearFacing() = cameraDevices.firstOrNull { di -> di.lensFacing == CameraDeviceInfo.LENS_FACING_REAR }
	fun firstFrontFacing() = cameraDevices.firstOrNull { di -> di.lensFacing == CameraDeviceInfo.LENS_FACING_REAR }
}