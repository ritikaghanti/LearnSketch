package com.example.learnsketch

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.provider.MediaStore.Video
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView

class VideoScreen : AppCompatActivity() {

    lateinit var startsketching: Button
    lateinit var replayvideo: Button
    lateinit var nextvideo: Button
    lateinit var playvideo: VideoView
    lateinit var text: TextView

    companion object {
        lateinit var link: String
        lateinit var value: String
        lateinit var loadvideo: Uri
        var counter: Int = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.videoscreen)

        startsketching = findViewById(R.id.startdrawing)
        playvideo = findViewById(R.id.videoview)
        replayvideo = findViewById(R.id.replay)
        nextvideo = findViewById(R.id.nextbt)
        text = findViewById(R.id.textView)

        playvideo()
//
//        playvideo.setOnPreparedListener(MediaPlayer.OnPreparedListener {
//            @Override
//            fun onPrepared(mp : MediaPlayer) {
//                mp.isLooping = true
//            }
//    })
        startsketching.setOnClickListener(View.OnClickListener { gotocanvas() })

        replayvideo.setOnClickListener(View.OnClickListener {
            playvideo.stopPlayback()
            playvideo.setVideoURI(loadvideo)
            playvideo.start()
        })

        nextvideo.setOnClickListener(View.OnClickListener {
            if(counter !== 4){
                link = value + counter++
            }else{
                counter = 1
                link = value + counter
            }



            Log.d("button","pressed")

            Log.d("counter", counter.toString())
            Log.d("new link",link)
            playvideo()

        })

        val back = findViewById<View>(R.id.goback2) as ImageView
        back.setOnClickListener(View.OnClickListener { gotoHome() })
    }


    private fun gotolearnmodule() {
        val learnmodule = Intent(this@VideoScreen, Learning::class.java)
        startActivity(learnmodule)
    }

    private fun gotocanvas() {
        val canvas = Intent(this@VideoScreen, ProSketchActivity::class.java)
        canvas.putExtra("classFrom", VideoScreen::class.java.toString())
        startActivity(canvas)
    }

    private fun gotoHome() {
        val launchMainScreen = Intent(this@VideoScreen, Learning::class.java)
        counter = 1
        startActivity(launchMainScreen)
    }

    private fun playvideo() {
        value = Learning.Companion.drawname
        if (value === "apple")
        {
            link = value
            Log.d("value",link)
            nextvideo.setVisibility(View.INVISIBLE)

            loadvideo = Uri.parse("android.resource://" + packageName + "/raw/" + link)
            playvideo.setVideoURI(loadvideo)
            playvideo.start()

        }else {
            text.setText("STAGE " + counter)
            link = value + counter
            Log.d("link", link)
            loadvideo = Uri.parse("android.resource://" + packageName + "/raw/" + link)
            Log.d("besthe", loadvideo.toString())

            playvideo.setVideoURI(loadvideo)
            playvideo.start()

        }
    }
}