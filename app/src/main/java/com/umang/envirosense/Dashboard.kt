package com.umang.envirosense

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.PixelFormat.RGBA_8888
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.MediaPlayer
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate


class Dashboard : AppCompatActivity() {

    private var appContext: Context? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var humidityRef: DatabaseReference
    private lateinit var temperatureRef: DatabaseReference

    private var temp: Int = 0
    private var hum: Int = 0

    private val REQUEST_CODE_SCREENSHOT = 1
    private val SCREENSHOT_MIME_TYPE = "image/png"

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var imageReader: ImageReader

    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 202051154

    private var minT: Int = Int.MIN_VALUE
    private var maxT: Int = Int.MAX_VALUE
    private var minH: Int = Int.MIN_VALUE
    private var maxH: Int = Int.MAX_VALUE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        appContext = this

        //Glassmorphism
//        val blurView: FKBlurView = findViewById(R.id.blur)
//        // blurView.setBlur(this, blurView, 20)
//        blurView.setBlur(this, blurView, 2)


        val sharedPreferences = this.getSharedPreferences("EnviroSense", Context.MODE_PRIVATE)

        minT = sharedPreferences.getInt("min_temp", Int.MIN_VALUE)
        maxT = sharedPreferences.getInt("max_temp", Int.MAX_VALUE)
        minH = sharedPreferences.getInt("min_hum", Int.MIN_VALUE)
        maxH = sharedPreferences.getInt("max_hum", Int.MAX_VALUE)



        // Firebase realtime
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance()
        val rootRef = database.reference.child("DHT11")
        humidityRef = rootRef.child("Humidity")
        temperatureRef = rootRef.child("Temperature")

        //Show data
        Timer().scheduleAtFixedRate(0L, 2000L) {
            readData()
        }


        // Show day
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayString = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> ""
        }
        val showDay = findViewById<TextView>(R.id.day)
        showDay.text = dayString


        // Hooks
        val genPDF = this.findViewById<RelativeLayout>(R.id.generate_pdf)
        val viewStats = this.findViewById<RelativeLayout>(R.id.view_report)
        val openPopUP = this.findViewById<ImageView>(R.id.popup_opener)
        val popUp = this.findViewById<RelativeLayout>(R.id.popup)
        val dayAndBars = this.findViewById<RelativeLayout>(R.id.day_and_bars)
        val closePopUp = this.findViewById<ImageView>(R.id.close_popup)
        val openSetting = this.findViewById<RelativeLayout>(R.id.setting)
        popUp.visibility = View.INVISIBLE

        val hasPostNotificationPermission = PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)


        val wholeScreen = this.findViewById<RelativeLayout>(R.id.dashboard_whole)
        genPDF.setOnClickListener {
            val intent = Intent(this, Details::class.java)
            intent.putExtra("temperature", temp)
            intent.putExtra("humidity", hum)
            startActivity(intent)
            finish()
        }

        viewStats.setOnClickListener {
            val intent = Intent(this, Report::class.java)
            startActivity(intent)
            finish()
        }

        openPopUP.setOnClickListener{
            dayAndBars.visibility = View.INVISIBLE
            popUp.visibility = View.VISIBLE
        }

        closePopUp.setOnClickListener{
            dayAndBars.visibility = View.VISIBLE
            popUp.visibility = View.INVISIBLE
        }

        openSetting.setOnClickListener{
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            finish()
        }

    }


    private fun readData() {
        val showTemp = this.findViewById<TextView>(R.id.temperature)
        val showHum = this.findViewById<TextView>(R.id.humidity)
        humidityRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val humidity = snapshot.getValue(Double::class.java)
                Log.d("DashboardActivity", "Humidity: $humidity")
                if (humidity != null) {
                    showHum.text = "${humidity.toInt()}"
                    hum = humidity.toInt()

                    if (humidity < minH) appContext?.let { alertUser(it) }
                    else if (humidity > maxH) appContext?.let { alertUser(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardActivity", "Failed to read humidity value: ${error.toException()}")
            }
        })
        temperatureRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperature = snapshot.getValue(Double::class.java)
                Log.d("DashboardActivity", "Temperature: $temperature")
                if (temperature != null) {
                    showTemp.text = "${temperature.toInt()}"
                    temp = temperature.toInt()

                    if (temperature < minT) appContext?.let { alertUser(it) }
                    else if (temperature > maxT) appContext?.let { alertUser(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardActivity", "Failed to read temperature value: ${error.toException()}")
            }
        })
    }



    fun takeScreenshot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun shareScreenshot(bitmap: Bitmap) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, "Report", null)
        val uri = Uri.parse(path)
        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share Image"))
    }

    fun alertUser(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.sound)
        mediaPlayer.start()
        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer.stop()
            mediaPlayer.release()
        }, 5000)
    }


}