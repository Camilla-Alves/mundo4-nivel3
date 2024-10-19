package com.example.domawearapp

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

class AudioHelper(private val context: Context) {
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("pt", "BR"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                } else {
                    Log.d("TTS", "TextToSpeech initialized successfully in Portuguese")
                }
            } else {
                Log.e("TTS", "Failed to initialize TextToSpeech")
            }
        }
    }

    fun audioOutputAvailable(type: Int): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return false
        }
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { it.type == type }
    }

    fun speak(text: String) {
        Log.d("AudioHelper", "Falando: $text")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun release() {
        tts?.shutdown()
    }

    fun registerAudioDeviceCallback(onDeviceAdded: (Int) -> Unit) {
        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesAdded(addedDevices)
                addedDevices?.forEach { device ->
                    if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                        onDeviceAdded(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
                    }
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesRemoved(removedDevices)
                removedDevices?.forEach { device ->
                    if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                        Log.d("AudioHelper", "Fone de ouvido Bluetooth desconectado.")
                        Toast.makeText(context, "Fone de ouvido Bluetooth desconectado.", Toast.LENGTH_SHORT).show()

                        if (audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)) {
                            Log.d("AudioHelper", "Mudando para o alto-falante integrado.")
                            audioManager.mode = AudioManager.MODE_NORMAL
                        }
                    }
                }
            }
        }, null)
    }
}
