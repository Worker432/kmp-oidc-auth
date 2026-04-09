@file:OptIn(ExperimentalForeignApi::class)

package io.github.zm.kmpauth.core.platform.securestorage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import kotlin.collections.mapOf

actual class DefaultSecureStorage : SecureStorage {
    private val service = "io.github.zm.kmpauth"

    actual override suspend fun putString(
        key: String,
        value: String,
    ) {
        val nsData = value.toNSDataUtf8()

        memScoped {
            val base = baseQuery(this, key)

            val exists = SecItemCopyMatching(base, null) == errSecSuccess

            val status =
                if (exists) {
                    val updateDict =
                        cfDict(
                            mapOf(
                                kSecValueData!! to cfDataFromNSData(nsData),
                            ),
                        )
                    SecItemUpdate(base, updateDict)
                } else {
                    val addDict =
                        cfDict(
                            mapOf(
                                kSecClass!! to (kSecClassGenericPassword as CFTypeRef?),
                                kSecAttrService!! to cfString(service),
                                kSecAttrAccount!! to cfString(key),
                                kSecValueData!! to cfDataFromNSData(nsData),
                                // доступно после первого анлока; и не синкается в iCloud
                                kSecAttrAccessible!! to (kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly as CFTypeRef?),
                            ),
                        )
                    SecItemAdd(addDict, null)
                }

            if (status != errSecSuccess) {
                throw IllegalStateException("Keychain putString failed: $status")
            }
        }
    }

    actual override suspend fun getString(key: String): String? =
        memScoped {
            val query =
                cfDict(
                    mapOf(
                        kSecClass!! to (kSecClassGenericPassword as CFTypeRef?),
                        kSecAttrService!! to cfString(service),
                        kSecAttrAccount!! to cfString(key),
                        kSecReturnData!! to (kCFBooleanTrue as CFTypeRef?),
                        kSecMatchLimit!! to (kSecMatchLimitOne as CFTypeRef?),
                    ),
                )

            val out = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, out.ptr)
            when (status) {
                errSecSuccess -> {
                    val data = out.value as CFDataRef
                    cfDataToUtf8String(data)
                }

                errSecItemNotFound -> null
                else -> throw IllegalStateException("Keychain getString failed: $status")
            }
        }

    actual override suspend fun remove(key: String) {
        memScoped {
            val query = baseQuery(this, key)
            val status = SecItemDelete(query)

            if (status != errSecSuccess && status != errSecItemNotFound) {
                throw IllegalStateException("Keychain remove failed: $status")
            }
        }
    }

    private fun baseQuery(
        scope: MemScope,
        key: String,
    ): CFDictionaryRef =
        scope.cfDict(
            mapOf(
                kSecClass!! to (kSecClassGenericPassword as CFTypeRef?),
                kSecAttrService!! to scope.cfString(service),
                kSecAttrAccount!! to scope.cfString(key),
            ),
        )
}
