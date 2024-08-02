package com.example.learnsketch

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kyanogen.signatureview.SignatureView
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.learnsketch.SaveSketch.Companion.bmp

class CanvasMain : AppCompatActivity() {

    val TAG = "CanvasMain"

    lateinit var signatureView: SignatureView
    lateinit var eraserbtn: ImageButton
    lateinit var colorbtn: ImageButton
    lateinit var savebtn: ImageButton
    lateinit var clearbtn: ImageButton
    lateinit var seekbar: SeekBar
    lateinit var backbtn: ImageButton
    lateinit var brushsizetv: TextView
    lateinit var brushbtn: ImageButton
    lateinit var preview: ImageView

    var defaultcolor: Int = 0
    var colorwheelactive: Int = 0
    var erasercolor: Int = 0

    var path =
        File(Environment.getExternalStorageDirectory().absolutePath + "/LearnSketch/sketchedImages")


    companion object {
        lateinit var filename: String
        lateinit var bitmap: Bitmap
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.sketching_canvas)

        preview = findViewById(R.id.preview_img)

        signatureView = findViewById(R.id.signature_view)
        seekbar = findViewById(R.id.brushSize)
        eraserbtn = findViewById(R.id.erasebtn)
        colorbtn = findViewById(R.id.colorbtn)
        savebtn = findViewById(R.id.savebtn)
        clearbtn = findViewById(R.id.clearcanvasbtn)
        brushbtn = findViewById(R.id.brushbtn)

        brushsizetv = findViewById(R.id.tv_BrushSize)
        backbtn = findViewById(R.id.backbtn)

        val intent = intent
        val str = intent.getStringExtra("message_key")
        if (str != null) {
            preview.setImageURI(str.toUri())
        }
        Log.d("outside", str.toString())



        // for getting bitmap from savesketch screen
        val callingintent = getIntent().extras
        if (callingintent?.getString("classFrom").equals(SaveSketch::class.java.toString())) {
            signatureView.setBitmap(SaveSketch.Companion.bmp)
        }

        askPermissions()

        eraserbtn.setOnClickListener(View.OnClickListener { useEraser() })
        brushbtn.setOnClickListener(View.OnClickListener {
            signatureView.penColor = colorwheelactive
        })
        clearbtn.setOnClickListener(View.OnClickListener { execclearCanvas() })
        colorbtn.setOnClickListener(View.OnClickListener { openColorWheel() })
        backbtn.setOnClickListener(View.OnClickListener { gotoHome() })

//        savebtn.setOnClickListener(View.OnClickListener {
//            if(!signatureView.isBitmapEmpty()){
//                try {
//                    saveCanvasImage()
//                }
//                catch (e : IOException)
//                {
//                    Log.d(TAG, "${e.stackTrace}")
//                    println(e.stackTrace.toString())
//                    Toast.makeText(this@CanvasMain, "$e", Toast.LENGTH_SHORT).show()
//                }
//
//            }
//        })

        savebtn.setOnClickListener(View.OnClickListener {
            bitmap = signatureView.signatureBitmap
            val launchSaveSketch = Intent(this@CanvasMain, SaveSketch::class.java)
            startActivity(launchSaveSketch)
        })


        var format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        var date = format.format(object : Date() {})


//        filename = "$path/$date.png"
        filename = "$path"
        if (!path.exists()) {
            path.mkdirs()
        }

        Log.d("CWD_DIRECTORY_CANVAS", filename)


        defaultcolor = ContextCompat.getColor(this@CanvasMain, R.color.black)
        erasercolor = ContextCompat.getColor(this@CanvasMain, R.color.white)

        seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                brushsizetv.setText("${progress}dp")
                signatureView.penSize = progress.toFloat()
                seekbar.max = 50

            }

            override fun onStartTrackingTouch(seek: SeekBar) {

            }

            override fun onStopTrackingTouch(seek: SeekBar) {

//                Toast.makeText(this@CanvasMain,
//                    "Progress is: " + seek.progress + "%",
//                    Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<CheckBox>(R.id.checkBox2)
            .setOnCheckedChangeListener { buttonView, isChecked ->

                Log.d("hint", "hint is checked: $isChecked")
            }

        findViewById<CheckBox>(R.id.checkBox)
            .setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked == true){
                    preview.setVisibility(View.VISIBLE);
                }else{
                    preview.setVisibility(View.INVISIBLE);

                }
                Log.d("Preview", "preview is checked: $isChecked")
            }

    }

    private fun useEraser() {
        signatureView.penColor = erasercolor
    }

    private fun execclearCanvas() {
        signatureView.clearCanvas()
        signatureView.penColor = colorwheelactive
    }

    private fun openColorWheel() {
        val ambilwarnadialog = AmbilWarnaDialog(this, defaultcolor, object :
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {}
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {

                defaultcolor = color
                signatureView.penColor = color
                colorwheelactive = color

            }
        })
        ambilwarnadialog.show()
    }

    private fun gotoHome() {
        val launchHomeScreen = Intent(this@CanvasMain, HomeScreen::class.java)
        startActivity(launchHomeScreen)
    }

    private fun askPermissions() {

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    multiplePermissionsReport?.let {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            Toast.makeText(
                                this@CanvasMain,
                                "All Permissions Granted!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                this@CanvasMain,
                                "WARNING: Allow permissions!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }


                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken

                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }
}