package com.app.dixon.facorites.page.video.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.VIDEO_PATH
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.util.normalFont
import com.google.android.exoplayer2.Player
import com.jarvanmo.exoplayerview.media.SimpleMediaSource
import kotlinx.android.synthetic.main.activity_video_play.*
import kotlinx.android.synthetic.main.activity_video_play.videoView
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*


open class VideoPlayActivity : BaseActivity() {

    private lateinit var path: String
    private var playSeek = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideBottomUIMenu()
        setContentView(R.layout.activity_video_play)
        normalFont()
        Ln.i("VideoPlayActivity", "onCreate")

        intent.getStringExtra(VIDEO_PATH)?.let {
            path = it
        } ?: let {
            finish()
            return
        }

        videoView.changeWidgetVisibility(R.id.exo_player_enter_fullscreen, View.GONE)
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    private fun hideBottomUIMenu() {
        val decorView: View = window.decorView
        val uiOptions: Int = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }

    override fun onStart() {
        super.onStart()
        playVideo(path)
    }

    override fun onStop() {
        super.onStop()
        playSeek = videoView.player.currentPosition
        Ln.i("VideoPlayActivity", "记录：$playSeek")
        videoView.stop()
        videoView.releasePlayer()
    }

    private fun playVideo(path: String) {
        val mediaSource = SimpleMediaSource(Uri.parse(path)) //uri also supported
        Ln.i("VideoPlayActivity", "播放：$playSeek")
        videoView.play(mediaSource, playSeek) //play from a particular position
        videoView.player.repeatMode = Player.REPEAT_MODE_ALL
    }

    override fun statusBarColor(): Int = R.color.black
}