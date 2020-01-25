package com.futuretech.nfmovies.Utils

import android.util.Base64

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtil {

    @Throws(Exception::class)
    fun decryptData(text: String, key: String, iv: String): String {

        val encryted_bytes = Base64.decode(text, Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val static_key = key.toByteArray()

        val keySpec = SecretKeySpec(static_key, "AES")
        val ivSpec = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val decrypted = cipher.doFinal(encryted_bytes)

        return String(decrypted)
    }
}