package com.example.learnsketch

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import com.example.learnsketch.DrawActivity.Companion.bitmap
import com.example.learnsketch.DrawActivity.Companion.filename

import java.io.IOException


class SaveSketch : AppCompatActivity() {

    lateinit var saveImagebtn: Button
    lateinit var resumeSketching: Button
    lateinit var canvaspreview: ImageView
    lateinit var sketchTitle: EditText
    lateinit var homebtn: Button
    lateinit var filetitle: String
    lateinit var it: Intent

    companion object {
        var is_BackToCanvas: Boolean = false
        lateinit var bmp: Bitmap
        var refbmp: Bitmap? = null

    }


    val TAG = "SaveSketch"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.savesketch)


        saveImagebtn = findViewById(R.id.btn_saveimage)
        resumeSketching = findViewById(R.id.btn_resumesketch)
        homebtn = findViewById(R.id.btn_home)
        sketchTitle = findViewById(R.id.edit_sketchtitle)
        canvaspreview = findViewById(R.id.iv_previewSketch)



        it = intent
        if (it?.getStringExtra("classFrom").equals(ProSketchActivity::class.java.toString())) {
            bmp = ProSketchActivity.proskbitmap!!
            canvaspreview.setImageBitmap(ProSketchActivity.proskbitmap)


        } else {
            bmp = DrawActivity.Companion.bitmap
            canvaspreview.setImageBitmap(bmp)
            if (!HomeScreen.isSavedSketchClicked) {
                refbmp = BitmapFactory.decodeFile(Analyze.refimfilepath)
            } else {
                refbmp = BitmapFactory.decodeFile(DrawActivity.pulled_refimgpath)
            }
        }


        resumeSketching.setOnClickListener(View.OnClickListener {

            finish()

        })

        homebtn.setOnClickListener(View.OnClickListener {

            val launchHomeScreen = Intent(this@SaveSketch, HomeScreen::class.java)
            startActivity(launchHomeScreen)

        })
        saveImagebtn.setOnClickListener(View.OnClickListener {


                try {
                    saveCanvasImage()
                } catch (e: IOException) {
                    Log.d(TAG, "${e.stackTrace}")
                    println(e.stackTrace.toString())
                    Toast.makeText(this@SaveSketch, "$e", Toast.LENGTH_SHORT).show()
                }


        })

    }


    private fun saveCanvasImage() {

        filetitle = sketchTitle.text.toString()
        if (filetitle.isBlank()) {
            Toast.makeText(
                this@SaveSketch,
                "ERROR: Enter A Sketch Title Before Saving!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (it?.getStringExtra("classFrom").equals(ProSketchActivity::class.java.toString())) {
                var completefilepath = "${ProSketchActivity.filename}/$filetitle@pro.png"
                var file = File(completefilepath)

                var bos: ByteArrayOutputStream = ByteArrayOutputStream()

                bmp.compress(Bitmap.CompressFormat.PNG, 0, bos)


                var bitmapData: ByteArray = bos.toByteArray()


                var fos: FileOutputStream = FileOutputStream(file)

                fos.write(bitmapData)

                fos.flush()

                Toast.makeText(
                    this@SaveSketch,
                    "Success: Canvas Sketch Saved successfully in $file",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                var completefilepath = "${DrawActivity.filename}/$filetitle.png"
                var file = File(completefilepath)

                var bos: ByteArrayOutputStream = ByteArrayOutputStream()

                bmp.compress(Bitmap.CompressFormat.PNG, 0, bos)


                var bitmapData: ByteArray = bos.toByteArray()


                var fos: FileOutputStream = FileOutputStream(file)

                fos.write(bitmapData)

                fos.flush()

                saveReferenceImage()

                Toast.makeText(
                    this@SaveSketch,
                    "Success: Canvas Sketch Saved successfully in $file",
                    Toast.LENGTH_SHORT
                ).show()

            }


        }
    }

    private fun saveReferenceImage() {

        var refcompletefilepath = "${DrawActivity.Companion.savedsketchrefdir}/$filetitle.png"
        //        var file = File(CanvasMain.filename)
        var reffile = File(refcompletefilepath)
        var refbos: ByteArrayOutputStream = ByteArrayOutputStream()
        Log.d("Reference Image Filepath", Analyze.Companion.refimfilepath)
        refbmp!!.compress(Bitmap.CompressFormat.PNG, 0, refbos)
        var bitmapData2: ByteArray = refbos.toByteArray()
        var fos2: FileOutputStream = FileOutputStream(reffile)
        fos2.write(bitmapData2)
        fos2.close()
        Log.d("Reference Image Save Sucess!", "Reference image saved to phone successfully!")
    }
}