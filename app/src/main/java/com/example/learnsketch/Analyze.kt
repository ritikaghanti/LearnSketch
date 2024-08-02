package com.example.learnsketch

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File
import java.io.InputStream
import java.net.URI

class Analyze : AppCompatActivity() {


    lateinit var upload_gallery_btn: ImageView
    lateinit var upload_camera_btn: ImageView
    lateinit var refuploadedimage: ImageView
    lateinit var proceedbtn: Button
    lateinit var back: ImageView

    lateinit var filename: String

    companion object {
        var refimfilepath: String = ""
        var preview: String = ""

    }

    var path =
        File(Environment.getExternalStorageDirectory().absolutePath + "/LearnSketch/refImages")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.anlyzesketch)

        filename = "$path"
        if (!path.exists()) {
            path.mkdirs()
        }
        Log.d("CWD_DIRECTORY_ANALYZE", filename)

        upload_gallery_btn = findViewById(R.id.upload_gallery_btn)
        upload_camera_btn = findViewById(R.id.upload_camera_btn)
        refuploadedimage = findViewById(R.id.ref_img)
        proceedbtn = findViewById(R.id.btn_proceed)

//
//        val getSelectedImage = registerForActivityResult(
//            ActivityResultContracts.GetContent()) {
//            imageURI = it!!
//            refuploadedimage.setImageURI(imageURI)
//
//        }

        back = findViewById(R.id.goback)
        back.setOnClickListener(View.OnClickListener { gotoHome() })
        proceedbtn.setOnClickListener(View.OnClickListener { gotoCanvas() })

        upload_camera_btn.setOnClickListener(View.OnClickListener {

            ImagePicker.with(this)
                .cameraOnly()
                .cropSquare()
//            .crop()	    			//Crop image(Optional), Check Customization for more option
//            .compress(1024)			//Final image size will be less than 1 MB(Optional)
//            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        })


        upload_gallery_btn.setOnClickListener(View.OnClickListener {

            ImagePicker.with(this)
                .galleryOnly()
                .cropSquare()
                .saveDir(getExternalFilesDir(Environment.getExternalStorageDirectory().absolutePath + "/LearnSketch/refImages")!!)
//            .crop()	    			//Crop image(Optional), Check Customization for more option
//            .compress(1024)			//Final image size will be less than 1 MB(Optional)
//            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()

        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageURI = data?.data
        Log.d("imageuri", imageURI.toString())
        refuploadedimage.setImageURI(imageURI)


        refimfilepath = imageURI?.path.toString()
        preview = imageURI.toString()

        Log.d("URI", imageURI?.path.toString())


    }

    private fun gotoHome() {
        val launchHomeScreen = Intent(this@Analyze, HomeScreen::class.java)
        startActivity(launchHomeScreen)
    }

    private fun saveRef(imageURI: Uri) {

        var sourceFilename = imageURI.getPath();
        var destinationFilename = "$path/refimage.png"


    }

    private fun gotoCanvas() {

        if (refimfilepath.isNotBlank()) {
            val launchCanvasActivity = Intent(this@Analyze, DrawActivity::class.java)
            launchCanvasActivity.putExtra("refURI", preview)
            Log.d("beforelauch", preview)
            startActivity(launchCanvasActivity)

        } else {
            Toast.makeText(
                this@Analyze,
                "ERROR!: Select a reference image before proceeding to sketch",
                Toast.LENGTH_SHORT
            ).show()
        }


    }


}