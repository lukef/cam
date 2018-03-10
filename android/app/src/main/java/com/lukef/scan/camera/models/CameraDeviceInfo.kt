package com.lukef.scan.camera.models

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.params.StreamConfigurationMap
import android.graphics.ImageFormat
import java.util.Arrays.asList
import android.graphics.SurfaceTexture
import android.util.Size
import com.lukef.scan.extensions.area
import java.util.*


typealias LensFacing = Int

class CameraDeviceInfo(
		val cameraId: String,
		val characteristics: CameraCharacteristics,
		val index: Int,
		val lensFacing: LensFacing,
		val streamConfigurationMap: StreamConfigurationMap
) {

	val outputSizes: Array<Size> = streamConfigurationMap.getOutputSizes(SurfaceTexture::class.java)
	val captureSizes: Array<Size> = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG)
	val largestCaptureSize: Size? = captureSizes.maxWith(kotlin.Comparator { o1, o2 ->
		o1.area() - o2.area()
	})

	fun chooseOptimalOutputSize(maxSize: Size) : Size? {
		var bestSize: Size? = null
		outputSizes.forEach { size ->
			if (size.area() == maxSize.area()) return maxSize
			if (size.area() < maxSize.area() && size.area() > bestSize?.area() ?: -1) {
				bestSize = size
			}
		}
		return bestSize
	}

	companion object {
		val LENS_FACING_UNKNOWN: LensFacing = 0
		val LENS_FACING_REAR: LensFacing = 1
		val LENS_FACING_FRONT: LensFacing = 2
	}
}