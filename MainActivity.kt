package com.soulai.player

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Soul AI starter: local video playback + basic Hindi TTS demo.
 * This is not full automatic dubbing yet: it does not transcribe/translate
 * the video's English audio or replace the audio track.
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var player: ExoPlayer? = null
    private var tts: TextToSpeech? = null
    private lateinit var status: TextView
    private lateinit var playerView: PlayerView
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 11, 20)
        window.navigationBarColor = android.graphics.Color.rgb(9, 11, 20)

        tts = TextToSpeech(this, this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24)
            setBackgroundColor(android.graphics.Color.rgb(9, 11, 20))
        }

        val title = TextView(this).apply {
            text = "Soul AI"
            textSize = 30f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }
        val subtitle = TextView(this).apply {
            text = "Video Player • Free Hindi voice demo"
            textSize = 14f
            setTextColor(0xFFB8B8D0.toInt())
            setPadding(0, 4, 0, 20)
        }
        playerView = PlayerView(this).apply {
            useController = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        status = TextView(this).apply {
            text = "Choose a video from your phone to begin."
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 16, 0, 12)
        }
        val open = Button(this).apply {
            text = "Open video from Gallery"
            setOnClickListener { openVideoPicker() }
        }
        val testVoice = Button(this).apply {
            text = "Test free Hindi voice"
            setOnClickListener {
                if (ttsReady) {
                    tts?.language = Locale("hi", "IN")
                    tts?.speak(
                        "नमस्ते! यह सोल एआई की हिंदी आवाज़ का परीक्षण है।",
                        TextToSpeech.QUEUE_FLUSH, null, "soul-ai-test"
                    )
                    status.text = "Playing a Hindi voice sample. Voice quality depends on your phone's installed TTS engine."
                } else {
                    Toast.makeText(this@MainActivity, "Hindi voice engine is starting…", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val note = TextView(this).apply {
            text = "Note: automatic English-to-Hindi dubbing and YouTube dubbing are not included in this starter build yet."
            textSize = 12f
            setTextColor(0xFFFFD18A.toInt())
            setPadding(0, 10, 0, 8)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(playerView)
        root.addView(status)
        root.addView(open)
        root.addView(testVoice)
        root.addView(note)
        setContentView(root)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_VIDEO)
    }

    @Deprecated("Legacy result API kept for a compact starter sample")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            if (player == null) {
                player = ExoPlayer.Builder(this).build().also { playerView.player = it }
            }
            player?.setMediaItem(MediaItem.fromUri(uri))
            player?.prepare()
            player?.play()
            status.text = "Playing selected video. Use the Hindi voice button to test your phone's free TTS."
        }
    }

    override fun onInit(statusCode: Int) {
        ttsReady = statusCode == TextToSpeech.SUCCESS
        if (ttsReady) {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                       result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onDestroy() {
        playerView.player = null
        player?.release()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object { private const val REQUEST_VIDEO = 101 }
}
