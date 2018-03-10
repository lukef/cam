package com.lukef.scan.extensions

import android.util.Size

fun Size.area() = this.width * this.height
fun Size.flipped() : Size = Size(this.height, this.width)