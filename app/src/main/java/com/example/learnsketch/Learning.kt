package com.example.learnsketch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learnsketch.R
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.example.learnsketch.HomeScreen

class Learning : AppCompatActivity() {

    lateinit var applebtn : Button
    lateinit var cakebtn : Button
    lateinit var conebtn : Button
    lateinit var linesbtn : Button


    companion object {
        lateinit var drawname: String
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learning)

        cakebtn = findViewById(R.id.cake)
        conebtn = findViewById(R.id.cone)
        linesbtn = findViewById(R.id.lines)
        applebtn = findViewById(R.id.apple)

        cakebtn.setOnClickListener(View.OnClickListener {
            drawname = "cake"
            gotovideo() })

        conebtn.setOnClickListener(View.OnClickListener {
            drawname = "cone"
            gotovideo() })

        linesbtn.setOnClickListener(View.OnClickListener {
            drawname = "line"
            gotovideo() })

        applebtn.setOnClickListener(View.OnClickListener {
            drawname = "apple"
            gotovideo() })

        val back = findViewById<View>(R.id.goback3) as ImageView
        back.setOnClickListener(View.OnClickListener { gotoHome() })
    }

    private fun gotoHome() {
        val launchMainScreen = Intent(this@Learning, HomeScreen::class.java)
        startActivity(launchMainScreen)
    }
    private fun gotovideo() {
        val modulevideoscreen = Intent(this@Learning, VideoScreen::class.java)
        startActivity(modulevideoscreen)
    }
}