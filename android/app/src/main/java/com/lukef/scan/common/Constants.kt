package com.lukef.scan.common

object Channel {
	private const val ChannelPrefix = "com.lukef.scan"
	const val Scan = "scan"

	fun named(name: String) : String = "$ChannelPrefix/$name"
}