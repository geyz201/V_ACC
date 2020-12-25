package com.lncp.speed

import threeDvector.pvat
import java.lang.Float.floatToIntBits
import java.nio.ByteBuffer

fun toByteArray(source: Long) = ByteBuffer.allocate(java.lang.Long.BYTES)
    .putLong(source).array()

fun toByteArray(source: Double): ByteArray = toByteArray(java.lang.Double.doubleToLongBits(source))

fun toByteArray(source: pvat): ByteArray {
    val tmp =
        ByteBuffer.allocate(java.lang.Double.BYTES * 6 + java.lang.Float.BYTES + java.lang.Long.BYTES)
    with(source) {
        for (i in 0 until 3) tmp.putLong(java.lang.Double.doubleToLongBits(position[i]))
        tmp.putInt(java.lang.Float.floatToIntBits(velocity))
        for (i in 0 until 3) tmp.putLong(java.lang.Double.doubleToLongBits(acceleration[i]))
        tmp.putLong(time)
    }
    return tmp.array()
}