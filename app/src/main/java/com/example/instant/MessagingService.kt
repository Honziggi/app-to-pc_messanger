package com.example.instant

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class MessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("FCM", "Token received: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_SERVICE", "Message received from script!")

        val encryptedBody = remoteMessage.notification?.body ?: return
        val key = getOrGenerateKey(this)
        val decryptedBody = decrypt(encryptedBody, key)

        ChatViewModel.addMessage(decryptedBody)


    }
    fun decrypt(encryptedBase64: String, keyBase64: String): String {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)

            val iv = combined.sliceArray(0 until 12)
            val cipherText = combined.sliceArray(12 until combined.size)

            val secretKey = SecretKeySpec(Base64.decode(keyBase64, Base64.DEFAULT), "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            String(cipher.doFinal(cipherText))
        } catch (e: Exception) {
            Log.d("DECRYPT ERROR", "FAILED: ${e.message}")
            "Decryption error"
        }
    }

    companion object {
        fun getOrGenerateKey(context: Context): String {
            val pref = context.getSharedPreferences("security_pref", Context.MODE_PRIVATE)
            var key = pref.getString("terminal_key", null)

            if (key == null) {
                val keyGenerator = KeyGenerator.getInstance("AES")
                keyGenerator.init(256)
                val secretKey = keyGenerator.generateKey()
                key = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)

                pref.edit().putString("terminal_key", key).apply()
            }
            return key!!
        }
    }
}
