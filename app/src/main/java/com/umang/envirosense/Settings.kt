package com.umang.envirosense

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setLogoAnimation()

        val backBtn = this.findViewById<RelativeLayout>(R.id.back)
        backBtn.setOnClickListener {
            val intent = Intent(this,Dashboard::class.java)
            startActivity(intent)
            finish()
        }



        val saveBtn = this.findViewById<RelativeLayout>(R.id.save)
        saveBtn.setOnClickListener {
            val minT = this.findViewById<TextView>(R.id.minTemp)
            val maxT = this.findViewById<TextView>(R.id.maxTemp)
            val minH = this.findViewById<TextView>(R.id.minHum)
            val maxH = this.findViewById<TextView>(R.id.maxHum)


            val sharedPreferences = this.getSharedPreferences("EnviroSense", Context.MODE_PRIVATE)

            // Default values
            var minTStored = sharedPreferences.getInt("min_temp", Int.MIN_VALUE)
            var maxTStored = sharedPreferences.getInt("max_temp", Int.MAX_VALUE)
            var minHStored = sharedPreferences.getInt("min_hum", Int.MIN_VALUE)
            var maxHStored = sharedPreferences.getInt("max_hum", Int.MAX_VALUE)

            // Editing
            val editor = sharedPreferences.edit()
//            val value1 = minT.text.toString().toInt()
//            val value2 = maxT.text.toString().toInt()
//            val value3 = minH.text.toString().toInt()
//            val value4 = maxH.text.toString().toInt()
            var value1 = minT.text.toString()
            var value2 = maxT.text.toString()
            var value3 = minH.text.toString()
            var value4 = maxH.text.toString()

            var newMinTemp: Int
            var newMaxTemp: Int
            var newMinHum: Int
            var newMaxHum: Int

            newMinTemp = if (value1.isEmpty()) minTStored else value1.toInt()
            newMaxTemp = if (value2.isEmpty()) maxTStored else value2.toInt()
            newMinHum = if (value3.isEmpty()) minHStored else value3.toInt()
            newMaxHum = if (value4.isEmpty()) maxHStored else value4.toInt()

            editor.putInt("min_temp", newMinTemp)
            editor.putInt("max_temp", newMaxTemp)
            editor.putInt("min_hum", newMinHum)
            editor.putInt("max_hum", newMaxHum)

            editor.apply()

            Toast.makeText(this, "Saved successfully", Toast.LENGTH_LONG).show()

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
}