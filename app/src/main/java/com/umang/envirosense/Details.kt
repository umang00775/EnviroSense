package com.umang.envirosense

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import java.io.ByteArrayOutputStream

class Details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setLogoAnimation()

        val temperature = intent.getIntExtra("temperature", 0)
        val humidity = intent.getIntExtra("humidity", 0)


        // Temperature
        var temp_c = temperature
        var temp_f = (temperature * (9/5)) + 32
        var temp_k = temp_c + 273.15

        // Dew point
        var dew_point = temp_c - ((100 - humidity)/5)
        var comfortLevel = comfortLevel(dew_point)

        // Heat index
        var heatIndex = heatIndex(temp_c.toDouble(), humidity.toDouble())


        // Hooks
        var tempC = this.findViewById<TextView>(R.id.temperature_c)
        var tempF = this.findViewById<TextView>(R.id.temperature_f)
        var tempK = this.findViewById<TextView>(R.id.temperature_k)
        var showHum = this.findViewById<TextView>(R.id.humidity)
        var showDew = this.findViewById<TextView>(R.id.dew_point)
        var showComfort = this.findViewById<TextView>(R.id.comfort_level)
        var showHI = this.findViewById<TextView>(R.id.heat_index)

        tempC.text = temp_c.toString() + " °C"
        tempF.text = temp_f.toString() + " °F"
        tempK.text = temp_k.toString() + " K"
        showHum.text = humidity.toString() + "% = " + (humidity.toDouble()/100)
        showDew.text = dew_point.toString()
        showComfort.text = comfortLevel
        showHI.text = String.format("%.2f", heatIndex) + " °F"


        val share = this.findViewById<RelativeLayout>(R.id.share)
        val Report = this.findViewById<RelativeLayout>(R.id.report)
        share.setOnClickListener {
            val bitmap = takeScreenshot(Report)
            shareScreenshot(bitmap)
        }

        val backBtn = this.findViewById<RelativeLayout>(R.id.back)
        backBtn.setOnClickListener {
            val intent = Intent(this,Dashboard::class.java)
            startActivity(intent)
            finish()
        }
    }


    fun setLogoAnimation() {
        val appName = findViewById<TextView>(R.id.app_name)
        val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE)
        val colorAnim = ObjectAnimator.ofInt(appName, "textColor", *colors)
        colorAnim.duration = 3000
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.repeatCount = ValueAnimator.INFINITE
        colorAnim.start()
    }

    private fun comfortLevel(dewPoint: Int): String {
        return if (dewPoint < 10) "A bit dry for some"
        else if (dewPoint < 16) "Dry and comfortable"
        else if (dewPoint < 18) "Getting sticky"
        else if (dewPoint < 21) "Unpleasant, lots of moisture in the air"
        else "Uncomfortable, oppressive"
    }

    private fun heatIndex(temperature: Double, humidity: Double): Double {
        val c1 = -8.784694
        val c2 = 1.61139411
        val c3 = 2.338548
        val c4 = -0.14611605
        val c5 = -0.012308094
        val c6 = -0.016424828
        val c7 = 0.002211732
        val c8 = 0.00072546
        val c9 = -0.000003582

        val hi = c1 + c2 * temperature + c3 * humidity + c4 * temperature * humidity
        + c5 * temperature * temperature + c6 * humidity * humidity
        + c7 * temperature * temperature * humidity
        + c8 * temperature * humidity * humidity
        + c9 * temperature * temperature * humidity * humidity
        return hi
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

}