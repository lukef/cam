package com.lukef.scan.flutter

import android.graphics.Color
import android.support.annotation.RequiresApi
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodChannel

@RequiresApi(21)
class SystemChromeHelper(private val flutterActivity: FlutterActivity) {
	private val statusBarChannel = MethodChannel(flutterActivity.flutterView, "com.lukef.scan/systemChrome")
	fun register() : SystemChromeHelper {
		statusBarChannel.setMethodCallHandler { call, _ ->
			if (call.method == "update") {
				val statusBarColor = call.argument<String>("statusBarColor")
				if (statusBarColor != null) {
					flutterActivity.window.statusBarColor = Color.parseColor(statusBarColor)
				}
				val navigationBarColor = call.argument<String>("navigationBarColor")
				if (navigationBarColor != null) {
					flutterActivity.window.navigationBarColor = Color.parseColor(navigationBarColor)
				}
			}
		}
		return this
	}
}