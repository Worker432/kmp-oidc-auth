package io.github.zm.kmpauth.core.platform.securestorage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import kotlin.collections.iterator

@OptIn(ExperimentalForeignApi::class)
internal fun MemScope.cfString(s: String): CFStringRef =
    CFStringCreateWithCString(
        kCFAllocatorDefault,
        s,
        kCFStringEncodingUTF8,
    )!!

@OptIn(ExperimentalForeignApi::class)
internal fun cfDataFromNSData(nsData: NSData): CFDataRef =
    CFDataCreate(
        kCFAllocatorDefault,
        nsData.bytes?.reinterpret(),
        nsData.length.toLong(),
    )!!

@OptIn(ExperimentalForeignApi::class)
internal fun MemScope.cfDict(map: Map<CFStringRef, CFTypeRef?>): CFDictionaryRef {
    val n = map.size

    val keys = allocArray<COpaquePointerVar>(n)
    val values = allocArray<COpaquePointerVar>(n)

    var i = 0
    for ((k, v) in map) {
        keys[i] = k
        values[i] = v
        i++
    }

    return CFDictionaryCreate(
        kCFAllocatorDefault,
        keys,
        values,
        n.toLong(),
        kCFTypeDictionaryKeyCallBacks.ptr,
        kCFTypeDictionaryValueCallBacks.ptr,
    )!!
}

@OptIn(ExperimentalForeignApi::class)
internal fun String.toNSDataUtf8(): NSData =
    (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
        ?: error("Failed to encode string as UTF-8 NSData")

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun cfDataToUtf8String(data: CFDataRef): String? {
    val len = CFDataGetLength(data)
    if (len == 0L) return ""
    val bytes = CFDataGetBytePtr(data) ?: return null

    val ns = NSString.create(bytes = bytes, length = len.toULong(), encoding = NSUTF8StringEncoding)
    return ns as String?
}
