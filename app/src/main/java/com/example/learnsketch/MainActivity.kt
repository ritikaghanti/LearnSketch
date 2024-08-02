package com.example.learnsketch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learnsketch.R
import android.content.Intent
import android.view.View
import android.widget.Button
import com.example.learnsketch.SignIn
import com.example.learnsketch.SignUp

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn_Login = findViewById<View>(R.id.btn_Login) as Button
        val btn_SignUp = findViewById<View>(R.id.btn_SignUp) as Button
        btn_Login.setOnClickListener(this)
        btn_SignUp.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_SignUp -> gotoSign_up()
            R.id.btn_Login -> gotoSign_in()
        }
    }

    override fun onPause() {
        super.onPause()
        println("onPause() called")
    }

    private fun gotoSign_in() {
        val launchHomeIntent = Intent(this@MainActivity, SignIn::class.java)
        startActivity(launchHomeIntent)
    }

    private fun gotoSign_up() {
        val launchHomeIntent = Intent(this@MainActivity, SignUp::class.java)
        startActivity(launchHomeIntent)
    }
}