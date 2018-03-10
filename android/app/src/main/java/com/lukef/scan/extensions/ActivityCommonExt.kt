package com.lukef.scan.extensions

import android.app.Activity
import android.graphics.Point
import android.util.Size

fun Activity.screenSize() : Size {
	val point: Point = Point()
	windowManager.defaultDisplay.getSize(point)
	return Size(point.x, point.y)
}