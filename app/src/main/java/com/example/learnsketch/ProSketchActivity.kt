package com.example.learnsketch

import ImageAdapter
import android.Manifest
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toBitmap
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
import java.lang.Exception


class ProSketchActivity : AppCompatActivity() {

    lateinit var canvas: ProSketchCanvas
    lateinit var eraserbtn: ImageButton
    lateinit var colorbtn: ImageButton
    lateinit var savebtn: ImageButton
    lateinit var clearbtn: ImageButton
    lateinit var seekbar: SeekBar
    lateinit var backbtn: ImageButton
    lateinit var brushsizetv: TextView
    lateinit var brushbtn: ImageButton
    lateinit var analyzebtn: AppCompatButton
    lateinit var grid3: ImageView
    lateinit var grid4: ImageView
    lateinit var preview: ImageView
    lateinit var zoominbtn: FloatingActionButton
    lateinit var zoomoutbtn: FloatingActionButton
    lateinit var spinnerid: Spinner
    lateinit var gridview: TextView
    lateinit var ssimtv: TextView
    lateinit var gridpreview: String
    lateinit var checkBox: CheckBox
    lateinit var errorprev: ImageView

    //Python
    var py: Python? = null
    var module: PyObject? = null
    //---

    var learning: Boolean = false


    var defaultcolor: Int = Color.BLACK
    var colorwheelactive: Int = 0
    var resourceid: Int = 0

    var path =
        File(Environment.getExternalStorageDirectory().absolutePath + "/LearnSketch/sketchedImages")


    companion object {
        lateinit var filename: String
        lateinit var proskbitmap: Bitmap


        var grid: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("new Activity", "Entered")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prosketch_canvas)

        spinnerid = findViewById(R.id.spinid)
        gridview = findViewById(R.id.textView4)


        //Load Python
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // Canvas
        canvas = findViewById<View>(R.id.procanvas) as ProSketchCanvas
        canvas.initBitmaps()

        var callingintent = getIntent()
        if (callingintent?.getStringExtra("classFrom")
                .equals(ImageAdapter::class.java.toString())
        ) {
            val imgpath = callingintent?.getStringExtra("imagepath").toString()
            Log.d("proimgpath", imgpath)
            if (imgpath != null) {
                proskbitmap = BitmapFactory.decodeFile(imgpath)
                proskbitmap = proskbitmap.copy(Bitmap.Config.ARGB_8888, true)

            }
        } else if (callingintent?.getStringExtra("classFrom")
                .equals(SaveSketch::class.java.toString())
        ) {
            if (HomeScreen.isSavedSketchClicked == true) {
                preview.setImageURI(DrawActivity.pulled_refimgpath.toUri())
            }
            canvas.initBitmaps()
            canvas.layerCanvas.setBitmap(SaveSketch.bmp)
            canvas.invalidate()

        }

        seekbar = findViewById(R.id.brushSize)
        eraserbtn = findViewById(R.id.erasebtn)
        colorbtn = findViewById(R.id.colorbtn)
        savebtn = findViewById(R.id.savebtn)
        clearbtn = findViewById(R.id.clearcanvasbtn)
        brushbtn = findViewById(R.id.brushbtn)
        analyzebtn = findViewById(R.id.analyzebtn)
        ssimtv = findViewById(R.id.ssim_header)
        brushsizetv = findViewById(R.id.tv_BrushSize)
        backbtn = findViewById(R.id.backbtn)
        zoominbtn = findViewById(R.id.btn_zoomin)
        zoomoutbtn = findViewById(R.id.btn_zoomout)
        grid3 = findViewById(R.id.progrid3)
        grid4 = findViewById(R.id.progrid4)
        preview = findViewById(R.id.preview_img_prosketch)
        checkBox = findViewById(R.id.checkBox)
        errorprev = findViewById(R.id.erroriv)


        if (intent?.getStringExtra("classFrom").equals(VideoScreen::class.java.toString())) {
            ssimtv.visibility = View.VISIBLE
            analyzebtn.visibility = View.VISIBLE
            checkBox.visibility = View.VISIBLE
            var ref = Learning.Companion.drawname
            when (ref) {
                "cake" -> {

                    preview.setImageResource(R.drawable.cake)
                    resourceid = R.drawable.cake
                }

                "cone" -> {
                    preview.setImageResource(R.drawable.cone)
                    resourceid = R.drawable.cone
                }

                "line" -> {
                    preview.setImageResource(R.drawable.lines)
                    resourceid = R.drawable.lines
                }

                "apple" -> {
                    preview.setImageResource(R.drawable.apple)
                    resourceid = R.drawable.apple
                }
            }

        }
        //Spinner for grids view
        val grid_value = arrayOf("Select Grid Size", "3 x 3", "4 x 4")
        val arrayadp = ArrayAdapter(
            this@ProSketchActivity,
            android.R.layout.simple_spinner_dropdown_item,
            grid_value
        )
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                preview.setVisibility(View.VISIBLE);
                if (grid == 3) {

                    grid3.setVisibility(View.VISIBLE)
                } else if (grid == 1) {
                    grid3.setVisibility(View.INVISIBLE)
                    grid4.setVisibility(View.INVISIBLE)
                } else {
                    grid4.setVisibility(View.VISIBLE)
                }

            } else {
                preview.setVisibility(View.INVISIBLE);
                grid3.setVisibility(View.INVISIBLE)
                grid4.setVisibility(View.INVISIBLE)

            }
            Log.d("Preview", "preview is checked: $isChecked")
        }
        spinnerid.adapter = arrayadp

        spinnerid?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                gridview.setText(grid_value[p2])
                gridpreview = grid_value[p2]
                if (grid_value[p2] === "3 x 3") {
                    canvas.hideGrids()
                    grid = 3;
                    canvas.showGrids()
                    if (checkBox.isChecked == true) {
                        grid4.setVisibility(View.INVISIBLE)
                        grid3.setVisibility(View.VISIBLE)

                    } else {
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
                    if (checkBox.isChecked == true) {
                        grid3.setVisibility(View.INVISIBLE)
                        grid4.setVisibility(View.VISIBLE)
                    } else {
                        grid3.setVisibility(View.INVISIBLE)
                        grid4.setVisibility(View.INVISIBLE)
                    }
                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                canvas.hideGrids()
            }

        }

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                preview.setVisibility(View.VISIBLE);
                if (grid == 3) {

                    grid3.setVisibility(View.VISIBLE)
                } else if (DrawActivity.grid == 1) {
                    grid3.setVisibility(View.INVISIBLE)
                    grid4.setVisibility(View.INVISIBLE)
                } else {
                    grid4.setVisibility(View.VISIBLE)
                }

            } else {
                preview.setVisibility(View.INVISIBLE);
                grid3.setVisibility(View.INVISIBLE)
                grid4.setVisibility(View.INVISIBLE)

            }
            Log.d("Preview", "preview is checked: $isChecked")
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


        clearbtn.setOnClickListener(View.OnClickListener { execclearCanvas() })
        colorbtn.setOnClickListener(View.OnClickListener { openColorWheel() })
        backbtn.setOnClickListener(View.OnClickListener { gotoHome() })
        analyzebtn.setOnClickListener(View.OnClickListener { gotoAnalyze() })
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

        savebtn.setOnClickListener(View.OnClickListener {
            proskbitmap = canvas.layer
            val launchSaveSketch = Intent(this@ProSketchActivity, SaveSketch::class.java)
            launchSaveSketch.putExtra("classFrom", ProSketchActivity::class.java.toString())
            startActivity(launchSaveSketch)
        })

        filename = "$path"

        if (!path.exists()) {
            path.mkdirs()
        }

        Log.d("CWD_DIRECTORY_CANVAS", ProSketchActivity.filename)


//        zoominbtn.setOnClickListener(View.OnClickListener {
//
//            canvas.zoomIn()
//            ProSketchCanvas.Companion.transform.postScale(
//                2f,
//                2f
//            )
//        })
//        zoomoutbtn.setOnClickListener(View.OnClickListener {
//            ProSketchCanvas.Companion.transform.postScale(
//                -2f,
//                -2f
//            )
//        })
    }

   /* private fun zoomIn() {

        canvas.zoomIn()
    }

    private fun zoomOut() {

        canvas.zoomOut()
    }*/


    private fun useEraser() {

        canvas.setColor(Color.WHITE)
    }

    private fun execclearCanvas() {
        canvas.initBitmaps()
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
        val launchHomeScreen = Intent(this@ProSketchActivity, HomeScreen::class.java)
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
                                this@ProSketchActivity,
                                "All Permissions Granted!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                this@ProSketchActivity,
                                "WARNING: Allow permissions!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }


                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken,

                    ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    private fun gotoAnalyze() {


        val handler = Handler(Looper.getMainLooper())

        val refbitmap: Bitmap = MediaStore.Images.Media.getBitmap(
            this.getContentResolver(),
            Uri.parse("android.resource://" + packageName + "/" + resourceid)
        )

        var refimgstring = convertImgtoString(refbitmap)
        var canvasimgstring = convertImgtoString(canvas.layer)
        Log.d("REF", refimgstring)
        Log.d("Sketch", canvasimgstring)
        Thread {
            try {
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(this))
                }
                val py = Python.getInstance()
                val module = py.getModule("DetectError")

                print("module: $module")
                print("analyze button clicked")

                val pyobj: List<PyObject> =
                    module.callAttr(
                        "detectError",
                        refimgstring,
                        canvasimgstring
                    )
                        .asList()
                var resultimgstring = pyobj.get(0).toString()
                var ssimscore = pyobj.get(1).toString()

                // Update UI elements on the main thread using Handler
                handler.post {

                    ssimtv.text = "SSIM Score: $ssimscore"
                    Log.d("SSIMSCORE", ssimscore)

                    var resultdata: ByteArray =
                        Base64.decode(resultimgstring, Base64.DEFAULT)
                    val resultbitmap =
                        BitmapFactory.decodeByteArray(resultdata, 0, resultdata.size)
                    errorprev.visibility = View.VISIBLE
                    errorprev.setImageBitmap(resultbitmap)
                }
            } catch (e: Exception) {
                print(e.message)
            }
        }.start()


    }

    public fun convertImgtoString(bmp: Bitmap): String {

        var bos: ByteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bos)

        var imgBytes: ByteArray = bos.toByteArray()
        var encodedImage: String = Base64.encodeToString(imgBytes, Base64.DEFAULT)

        return encodedImage

    }

}