package com.lukef.scan

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.camera2.*
import android.os.Bundle
import android.util.Size
import android.view.Surface
import com.lukef.scan.camera.exceptions.CameraInitializationException
import com.lukef.scan.camera.helpers.CameraHelper
import com.lukef.scan.camera.models.CameraDeviceInfo
import com.lukef.scan.common.Channel
import com.lukef.scan.extensions.screenSize
import com.lukef.scan.flutter.SystemChromeHelper

import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import io.flutter.view.TextureRegistry
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.util.*
import android.support.annotation.NonNull
import com.lukef.scan.extensions.flipped
import kotlin.math.max
import kotlin.math.min
import android.hardware.camera2.CameraCharacteristics




class MainActivity : FlutterActivity() {

	private val logTag = "A:Main"

	// const
	private val errorCodeGeneral = "general"

	// scan channel methods
	private val scanMethodCreate: String = "create"

	// camera
	private val cameraLock = Semaphore(1)
	private var currentDeviceInfo: CameraDeviceInfo? = null
	private var currentCamera: CameraDevice? = null
	private var cameraInitResult: MethodChannel.Result? = null
	private var previewRequestBuilder: CaptureRequest.Builder? = null
	private var previewCaptureThread: HandlerThread? = null
	private var previewCaptureHandler: Handler? = null
	private var camMatrix = FloatArray(16)

	private var textureEntry: TextureRegistry.SurfaceTextureEntry? = null
	private var surface: Surface? = null
	private var scannerChannel: MethodChannel? = null
	private var statusBarHelper: SystemChromeHelper? = null
	private var previewCaptureRequest: CaptureRequest? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		registerChannelHandlers()
		GeneratedPluginRegistrant.registerWith(this)
	}

	// channel handlers
	private fun registerChannelHandlers() {
		statusBarHelper = SystemChromeHelper(this).register()
		scannerChannel = registerScanChannel()
	}

	private fun registerScanChannel() : MethodChannel {
		val channel = MethodChannel(flutterView, Channel.named(Channel.Scan))
		channel.setMethodCallHandler { call, result -> handleScanChannel(call, result) }
		return channel
	}

	// channel handlers
	private fun handleScanChannel(call: MethodCall?, result: MethodChannel.Result?) {
		if (call == null) return
		when(call.method) {
			scanMethodCreate -> {
				try {
					openCamera()
					cameraInitResult = result
				} catch (cie: CameraInitializationException) {
					result?.error(cie.code, cie.localizedMessage, null)
				} catch (e: Exception) {
					result?.error(errorCodeGeneral, e.localizedMessage, null)
				}
			}
		}
	}

	override fun onDestroy() {
		surface?.release()
		super.onDestroy()
	}

	private fun configureCameraPreview(camera: CameraDevice, cameraInfo: CameraDeviceInfo) {
		val texture = flutterView.createSurfaceTexture()

		// determine the preview size
		val optimalPreviewSize = cameraInfo.chooseOptimalOutputSize(screenSize())?.flipped()
				?: cameraInfo.largestCaptureSize?.flipped()
				?: throw CameraInitializationException("Could not define an appropriate capture size", "general")
		// configure the surface
		val surfaceTexture = texture.surfaceTexture()
		surfaceTexture.setDefaultBufferSize(screenSize().width, screenSize().height)
		val surface = Surface(surfaceTexture)
		this.textureEntry = texture
		this.surface = surface

		previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
		previewRequestBuilder?.addTarget(surface)

		val m = cameraInfo.characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
		val zoomCrop = Rect(0, 0, 3044, 3044)
		previewRequestBuilder?.set(CaptureRequest.SCALER_CROP_REGION, zoomCrop)

		camera.createCaptureSession(listOf(surface), captureStateCallback, null);
		cameraInitResult?.success(mapOf(
				"textureId" to texture.id()
		))
	}

	@SuppressLint("MissingPermission")
	private fun openCamera() {
		val devices = CameraHelper().getDevices(this)
		val cameraInfo = devices.firstRearFacing()
		if (cameraInfo != null) {
			if (!cameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
				throw CameraInitializationException("Could not open camera. Camera timed out.")
			}
			val cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
					?: throw CameraInitializationException("Could not open camera: failed to get camera manager.")
			try {
				this.currentDeviceInfo = cameraInfo
				cameraManager.openCamera(cameraInfo.cameraId, cameraStateCallback, null)
			} catch (e: Exception) {
				throw CameraInitializationException("Could not open camera: ${e.localizedMessage}")
			}
		} else {
			// TODO - error
		}
	}

	private val captureStateCallback:CameraCaptureSession.StateCallback = object : CameraCaptureSession.StateCallback() {
		override fun onConfigured(session: CameraCaptureSession) {
			previewRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
			val captureRequest = previewRequestBuilder?.build()

			previewCaptureThread = HandlerThread("previewUpdate");
			previewCaptureThread?.start();
			previewCaptureHandler = Handler(previewCaptureThread?.looper)
			session.setRepeatingRequest(captureRequest, null, previewCaptureHandler)
		}
		override fun onConfigureFailed(session: CameraCaptureSession) {}
	}

	private val cameraStateCallback = object : CameraDevice.StateCallback() {
		override fun onOpened(camera: CameraDevice?) {
			cameraLock.release()
			val cameraInfo = currentDeviceInfo
			if (camera != null && cameraInfo != null) {
				currentCamera = camera
				configureCameraPreview(camera, cameraInfo)
			}
			// TODO - error
		}

		override fun onDisconnected(camera: CameraDevice?) {
		}

		override fun onError(camera: CameraDevice?, error: Int) {
			Log.e(logTag, "Error: $error")
		}
	}

	private fun transformMatrix(originalMatrix: FloatArray, viewSize: Size, previewSize: Size) : Matrix? {
		if (viewSize.width == 0 || viewSize.height == 0) {
			Log.d(logTag, "Skipping transform. View size is invalid.")
			return null
		}
		val pivotPointX = viewSize.width / 2.0f
		val pivotPointY = viewSize.height / 2.0f
		val yScaleMultiplier = previewSize.width.toFloat() / previewSize.height.toFloat()
		val transformMatrix = Matrix()
		transformMatrix.setValues(originalMatrix)
		transformMatrix.setScale(1.0f, 1.0f * yScaleMultiplier, pivotPointX, pivotPointY)
		return transformMatrix
	}
}
