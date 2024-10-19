package com.example.domawearapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var audioHelper: AudioHelper
    private lateinit var speechRecognizer: SpeechRecognizer

    private val newsList = listOf(
        "O Brasil se prepara para as eleições de 2024 com novas propostas de candidatos.",
        "A tecnologia 5G começa a ser implementada em mais cidades brasileiras.",
        "Estudos recentes mostram que a vacinação continua a reduzir a transmissão do COVID-19.",
        "A economia brasileira cresce 2% no último trimestre, superando as expectativas.",
        "O governo anuncia novos investimentos em energia renovável para 2025."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioHelper = AudioHelper(this)

        val voiceCommandButton: Button = findViewById(R.id.voiceCommandButton)
        voiceCommandButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }

        val readMessageButton: Button = findViewById(R.id.readMessageButton)
        readMessageButton.setOnClickListener {
            val sender = "João Silva"
            val message = "Olá, como você está?"
            val date = "15 de outubro de 2024"
            val time = "14:30"

            val fullMessage = "Você recebeu uma nova mensagem de $sender em $date às $time: '$message'"

            if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)) {
                audioHelper.speak(fullMessage)
            } else {
                Toast.makeText(this, "Dispositivo de áudio não disponível.", Toast.LENGTH_SHORT).show()
            }
        }

        val helpButton: Button = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)) {
                audioHelper.speak("Você pode usar comandos de voz como 'notícias' para ouvir as últimas notícias.")
            } else {
                Toast.makeText(this, "Dispositivo de áudio não disponível.", Toast.LENGTH_SHORT).show()
            }
        }

        audioHelper.registerAudioDeviceCallback { type ->
            if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                Toast.makeText(this, "Fone de ouvido Bluetooth conectado!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startVoiceRecognition() {
        Log.d("VoiceRecognition", "Iniciando reconhecimento de voz")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this@MainActivity.packageName)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceRecognition", "Pronto para ouvir")
                Toast.makeText(this@MainActivity, "Pronto para ouvir", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceRecognition", "Início da fala detectado")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("VoiceRecognition", "Mudança de RMS: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("VoiceRecognition", "Buffer recebido")
            }

            override fun onEndOfSpeech() {
                Log.d("VoiceRecognition", "Fim da fala detectado")
            }

            override fun onError(error: Int) {
                Log.e("VoiceRecognition", "Erro de reconhecimento: $error")
                Toast.makeText(this@MainActivity, "Erro de reconhecimento: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    val command = it[0]
                    Log.d("VoiceRecognition", "Comando reconhecido: $command")
                    handleVoiceCommand(command)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("VoiceRecognition", "Resultados parciais recebidos")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("VoiceRecognition", "Evento recebido: $eventType")
            }
        })
        speechRecognizer.startListening(intent)
    }

    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("notícias", ignoreCase = true) -> {
                val randomNews = newsList[Random.nextInt(newsList.size)]
                if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)) {
                    audioHelper.speak(randomNews)
                } else {
                    Toast.makeText(this, "Dispositivo de áudio não disponível.", Toast.LENGTH_SHORT).show()
                }
            }
            command.contains("parar", ignoreCase = true) -> {
                audioHelper.stopSpeaking()
            }
            else -> {
                Toast.makeText(this, "Comando não reconhecido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                Toast.makeText(this, "Permissão para uso do microfone negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        audioHelper.release()
    }
}
