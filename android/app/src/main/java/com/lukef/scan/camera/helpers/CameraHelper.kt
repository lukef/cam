package com.lukef.scan.camera.helpers

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.lukef.scan.camera.exceptions.CameraInitializationException
import com.lukef.scan.camera.models.CameraDeviceInfo
import com.lukef.scan.camera.models.CameraDevices

class CameraHelper {

	/**
	 * Get a list of all available camera devices that were detected
	 *
	 * @return a list of useable cameras
	 * @throws CameraInitializationException if the camera list could not be collected
	 */
	@Throws(CameraInitializationException::class)
	fun getDevices(context: Context): CameraDevices {
		val cameraDeviceInfoList = mutableListOf<CameraDeviceInfo>()
		val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
				?: throw CameraInitializationException("Could not query CameraManager.")
		try {
			val cameraIDList = cameraManager.cameraIdList
			val totalCameras = cameraIDList.size
			if (cameraIDList.isEmpty()) {
				throw CameraInitializationException("Could not fetch hardware info: No cameras found.")
			}
			for (idx in 0 until totalCameras) {
				val cameraID = cameraIDList[idx]
				val characteristics = cameraManager.getCameraCharacteristics(cameraID)
				val lensDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
				var lensFacing = CameraDeviceInfo.LENS_FACING_UNKNOWN
				if (lensDirection != null) {
					lensFacing = when(lensDirection) {
						CameraCharacteristics.LENS_FACING_FRONT -> CameraDeviceInfo.LENS_FACING_FRONT
						else -> CameraDeviceInfo.LENS_FACING_REAR
					}
				}
				val streamConfigMap = characteristics.get(
						CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
				)
				cameraDeviceInfoList.add(
						CameraDeviceInfo(cameraID, characteristics, idx,
								lensFacing, streamConfigMap)
				)
			}
		} catch (cae: CameraAccessException) {
			throw CameraInitializationException("Could not initialize camera: ${cae.localizedMessage}")
		}
		return CameraDevices(cameraDeviceInfoList)
	}
}