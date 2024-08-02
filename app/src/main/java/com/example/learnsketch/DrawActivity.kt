
package com.example.learnsketch

import ImageAdapter
import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.net.toUri
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import com.shashank.sony.fancytoastlib.FancyToast


class DrawActivity : AppCompatActivity() {

    lateinit var canvas: CanvasView
    lateinit var eraserbtn: ImageButton
    lateinit var colorbtn: ImageButton
    lateinit var savebtn: ImageButton
    lateinit var clearbtn: ImageButton
    lateinit var seekbar: SeekBar
    lateinit var backbtn: ImageButton
    lateinit var brushsizetv: TextView
    lateinit var brushbtn: ImageButton
    lateinit var imageURI: Uri

    lateinit var zoominbtn: FloatingActionButton
    lateinit var zoomoutbtn: FloatingActionButton
    lateinit var analyzebtn: AppCompatButton
    lateinit var toggleerrbtn: ToggleButton
    lateinit var spinnerid: Spinner
    lateinit var gridview: TextView


    //Python
    var py: Python? = null
    var module: PyObject? = null
    //---

    var learning: Boolean = false


    var defaultcolor: Int = Color.BLACK
    var colorwheelactive: Int = 0




    var path =
        File(Environment.getExternalStorageDirectory().absolutePath + "/LearnSketch/sketchedImages")

    var refpath =
        File(Environment.getExternalStorageDirectory().absolutePath + "/LearnSketch/refImages")

    companion object {
        lateinit var filename: String
        lateinit var bitmap: Bitmap
        lateinit var preview: ImageView
        lateinit var errorpreview: ImageView
        lateinit var ssimtv: TextView
        lateinit var grid3: ImageView
        lateinit var grid4: ImageView
        lateinit var savedsketchrefdir: String
        lateinit var savedsketchbmp: Bitmap
        var pulled_refimgpath: String = ""
        var grid: Int = 1

        var errordetectmode: Boolean = true
        lateinit var gridpreview: String
        lateinit var checkBox: CheckBox


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pentab_canvas)


        //Load Python
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }


        // Canvas
        canvas = findViewById<View>(R.id.canvas) as CanvasView
        canvas.brush?.size = 10

        seekbar = findViewById(R.id.brushSize)
        eraserbtn = findViewById(R.id.erasebtn)
        colorbtn = findViewById(R.id.colorbtn)
        savebtn = findViewById(R.id.savebtn)
        clearbtn = findViewById(R.id.clearcanvasbtn)
        brushbtn = findViewById(R.id.brushbtn)
        preview = findViewById(R.id.preview_img)
        errorpreview = findViewById(R.id.erroriv)
        brushsizetv = findViewById(R.id.tv_BrushSize)
        backbtn = findViewById(R.id.backbtn)
        zoominbtn = findViewById(R.id.btn_zoomin)
        zoomoutbtn = findViewById(R.id.btn_zoomout)

        toggleerrbtn = findViewById(R.id.toggleErrorDetection)
        spinnerid = findViewById(R.id.spinid)
        gridview = findViewById(R.id.textView4)
        grid3 = findViewById(R.id.grid3)
        grid4 = findViewById(R.id.grid4)
        checkBox = findViewById(R.id.checkBox)
        ssimtv = findViewById(R.id.ssim_header)

        val intent = getIntent().extras

        // To check if activity called from AnalyzeSketch
        if (HomeScreen.isSavedSketchClicked == false) {
            val previewrefbitmap = Analyze.Companion.preview.toUri()
            preview.setImageURI(previewrefbitmap)

        }

        //Spinner for grids view
        val grid_value = arrayOf("Select Grid Size","3 x 3", "4 x 4")
        val arrayadp = ArrayAdapter(this@DrawActivity,android.R.layout.simple_spinner_dropdown_item, grid_value)
        spinnerid.adapter = arrayadp

        spinnerid?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                gridview.setText(grid_value[p2])
                gridpreview = grid_value[p2]
                if (grid_value[p2] === "3 x 3")
                {
                    canvas.hideGrids()
                    grid = 3;
                    canvas.showGrids()
                    if (checkBox.isChecked == true){
                        grid4.setVisibility(View.INVISIBLE)
                        grid3.setVisibility(View.VISIBLE)
                    }
                    else{
                        grid3.setVisibility(View.INVISIBLE)
                        grid4.setVisibility(View.INVISIBLE)
                    }
                }
                else if (grid_value[p2] === "Select Grid Size") {
                    grid = 1
                    canvas.hideGrids()
                    grid3.setVisibility(View.INVISIBLE)
                    grid4.setVisibility(View.INVISIBLE)
                }
                else {
                    canvas.hideGrids()
                    grid = 4;
                    canvas.showGrids()
                    if (checkBox.isChecked == true){
                        grid3.setVisibility(View.INVISIBLE)
                        grid4.setVisibility(View.VISIBLE)
                    }
                    else {
                        grid3.setVisibility(View.INVISIBLE)
                        grid4.setVisibility(View.INVISIBLE)
                    }
                }



            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                canvas.hideGrids()
            }

        }

        askPermissions()
        eraserbtn.setOnClickListener(View.OnClickListener { useEraser() })
        brushbtn.setOnClickListener(View.OnClickListener {
            if (colorwheelactive == 0) {
                canvas.setColor(defaultcolor)
            } else {
                canvas.setColor(colorwheelactive)
            }

        })

//        HOMESCREEN SAVED SKETCH
        if (intent?.getString("classFrom").equals(ImageAdapter::class.java.toString())) {
            val imgpath = intent?.getString("imagepath").toString()
            pulled_refimgpath = intent?.getString("saved_refimagepath")!!
            preview.setImageURI(pulled_refimgpath.toUri())
            Log.d("DrawActivitySavedSketch", imgpath)
            Log.d("PulledRefImagepath", pulled_refimgpath)
            if (imgpath != null) {
                imageURI = imgpath.toUri();
                savedsketchbmp = BitmapFactory.decodeFile(imgpath)
                savedsketchbmp = savedsketchbmp.copy(Bitmap.Config.ARGB_8888, true)
                canvas.initBitmaps()
                canvas.postInvalidate()

            }
        }





        clearbtn.setOnClickListener(View.OnClickListener { execclearCanvas() })
        colorbtn.setOnClickListener(View.OnClickListener { openColorWheel() })
        backbtn.setOnClickListener(View.OnClickListener { gotoHome() })


        seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean,
            ) {
                brushsizetv.setText("${progress}dp")

                canvas.brush?.size = progress
//                signatureView.penSize = progress.toFloat()
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


        // for getting bitmap from savesketch screen
//        val callingintent = getIntent().extras
//        if (callingintent?.getString("classFrom").equals(SaveSketch::class.java.toString())) {
//            if (HomeScreen.isSavedSketchClicked == true) {
//                preview.setImageURI(pulled_refimgpath.toUri())
//            }
//            canvas.initBitmaps()
//            canvas.layerCanvas.setBitmap(SaveSketch.bmp)
//            canvas.invalidate()
//            triggerCanvasTouch()
//
//        }

        savebtn.setOnClickListener(View.OnClickListener {
            bitmap = canvas.layer
            val launchSaveSketch = Intent(this@DrawActivity, SaveSketch::class.java)
            startActivity(launchSaveSketch)
        })


        filename = "$path"
        savedsketchrefdir = "$refpath"

        if (!path.exists()) {
            path.mkdirs()
        }
        if (!refpath.exists()) {
            refpath.mkdirs()
        }

        Log.d("CWD_DIRECTORY_CANVAS", DrawActivity.filename)



            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked == true) {
                    preview.setVisibility(View.VISIBLE);
                    if (grid == 3) {

                        grid3.setVisibility(View.VISIBLE)
                    }else if (grid == 1){
                        grid3.setVisibility(View.INVISIBLE)
                        grid4.setVisibility(View.INVISIBLE)
                    }else {
                        grid4.setVisibility(View.VISIBLE)
                    }

                } else {
                    preview.setVisibility(View.INVISIBLE);
                    grid3.setVisibility(View.INVISIBLE)
                    grid4.setVisibility(View.INVISIBLE)

                }
                Log.d("Preview", "preview is checked: $isChecked")
            }


        toggleerrbtn.setOnClickListener(View.OnClickListener {

            errordetectmode = toggleerrbtn.isChecked
//            Toast.makeText(this, "Error Detection: ${errordetectmode}", Toast.LENGTH_SHORT).show()
            if (errordetectmode) {
                ssimtv.setVisibility(View.VISIBLE)
                errorpreview.setVisibility(View.VISIBLE)
                FancyToast.makeText(
                    this,
                    "Error Detection ON",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.SUCCESS,
                    false
                ).show()
            } else {
                ssimtv.setVisibility(View.INVISIBLE)
                errorpreview.setVisibility(View.INVISIBLE)
                FancyToast.makeText(
                    this,
                    "Error Detection OFF",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })


        zoominbtn.setOnClickListener(View.OnClickListener {

            canvas.zoomIn()
            CanvasView.Companion.transform.postScale(
                2f,
                2f
            )
        })
        zoomoutbtn.setOnClickListener(View.OnClickListener {
            CanvasView.Companion.transform.postScale(
                -2f,
                -2f
            )
        })
    }


    private fun zoomIn() {

        canvas.zoomIn()
    }

    private fun zoomOut() {

        canvas.zoomOut()
    }


    private fun useEraser() {

        canvas.setColor(Color.WHITE)
    }

    private fun execclearCanvas() {
        canvas.initBitmaps()
        canvas.brush?.size = 10
        triggerCanvasTouch()
    }

    private fun triggerCanvasTouch() {

        // This Function is to create a dummy touch on the Canvas, for clearing and initializing canvas procedure

        // Obtain MotionEvent object
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = 0.0f
        val y = 0.0f

        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState
        )

        // Dispatch touch event to view
        canvas.dispatchTouchEvent(motionEvent)
    }

    private fun openColorWheel() {
        val ambilwarnadialog = AmbilWarnaDialog(this, defaultcolor, object :
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {}
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {

                defaultcolor = color
                canvas.setColor(color)
                colorwheelactive = color

            }
        })
        ambilwarnadialog.show()
    }

    private fun gotoHome() {
        val launchHomeScreen = Intent(this@DrawActivity, HomeScreen::class.java)
        startActivity(launchHomeScreen)
    }



    public fun convertImgtoString(bmp: Bitmap): String {

        var bos: ByteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bos)

        var imgBytes: ByteArray = bos.toByteArray()
        var encodedImage: String = Base64.encodeToString(imgBytes, Base64.DEFAULT)

        return encodedImage

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
                                this@DrawActivity,
                                "All Permissions Granted!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                this@DrawActivity,
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