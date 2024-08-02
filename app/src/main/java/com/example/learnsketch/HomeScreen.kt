package com.example.learnsketch

import ImageAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.io.File

class HomeScreen : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private var database: DatabaseReference? = null
    private var firebaseDatabase: FirebaseDatabase? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private lateinit var username: String
    companion object {
        var isSavedSketchClicked: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homescreen)
        val imageButton = findViewById<View>(R.id.analyzeBtn) as ImageButton
        val imageButton2 = findViewById<View>(R.id.learnsketchBtn) as ImageButton
        val imageButton3 = findViewById<View>(R.id.sketchBtn) as ImageButton
        val btn_Logout = findViewById<View>(R.id.btn_Logout) as Button
        val tv_welcome = findViewById<View>(R.id.textView_welcomeUser) as TextView
        imageButton.setOnClickListener(this)
        imageButton2.setOnClickListener(this)
        imageButton3.setOnClickListener(this)
        btn_Logout.setOnClickListener(this)
//        val intent = intent.extras
//        username = intent?.getString("username")!!
//
//        tv_welcome.setText("Welcome, " + username)
        tv_welcome.setText("Welcome, " + SignIn.Companion.fname)
//        tv_welcome.setText("Welcome, TestUser")

        recyclerView = findViewById(R.id.recentsketchRV)

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val imagePaths =
            retrieveImagePathsFromFolder("storage/emulated/0/LearnSketch/sketchedImages") // Implement this method to retrieve image file paths
        val imageList = createImageItemList(imagePaths)

        adapter = ImageAdapter(imageList)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        isSavedSketchClicked = false
    }

    private fun createImageItemList(imagePaths: List<String>): List<ImageItem> {
        return imagePaths.map { ImageItem(it) }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.analyzeBtn -> gotoAnalyze()
            R.id.btn_Logout -> signOut()
            R.id.learnsketchBtn -> gotoLearn()
            R.id.sketchBtn -> gotoSketch()
        }
    }

    override fun onPause() {
        super.onPause()
        println("onPause() called")
    }

    private fun signOut() {
        auth = Firebase.auth
        auth.signOut()
        val launchMainScreen = Intent(this@HomeScreen, MainActivity::class.java)
        startActivity(launchMainScreen)
    }

    private fun gotoAnalyze() {
        val launchMainScreen = Intent(this@HomeScreen, Analyze::class.java)
        startActivity(launchMainScreen)
    }

    private fun gotoLearn() {
        val launchMainScreen = Intent(this@HomeScreen, Learning::class.java)
        startActivity(launchMainScreen)
    }

    private fun gotoSketch() {
        val launchMainScreen = Intent(this@HomeScreen, ProSketchActivity::class.java)
        startActivity(launchMainScreen)
    }

    private fun retrieveImagePathsFromFolder(directoryPath: String): List<String> {
        val imagePaths = mutableListOf<String>()
        val imageTitles = mutableListOf<String>()

        // Create a File object for the directory
        val directory = File(directoryPath)

        // Check if the directory exists and is a directory
        if (directory.exists() && directory.isDirectory) {
            // Retrieve all files from the directory
            val files = directory.listFiles()

            // Iterate over each file
            files?.forEach { file ->
                // Check if the file is an image
                // Get the absolute path of the image file
                val imagePath = file.absolutePath
                // Add the image path to the list
                imagePaths.add(imagePath)
                Log.d("imgPath", imagePath)

            }
        }

        return imagePaths
    }

}