package com.futuretech.nfmovies.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import com.futuretech.nfmovies.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isTaskRoot) {
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)

        val o = findViewById<ImageView>(R.id.splash_o)
        val rotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
        val rotate = LinearInterpolator()
        rotateAnimation.interpolator = rotate
        rotateAnimation.duration = 1000
        rotateAnimation.repeatCount = -1
        rotateAnimation.fillAfter = true
        rotateAnimation.startOffset = 0
        o.animation = rotateAnimation

        Handler().postDelayed({
            val intent = Intent()
            intent.setClass(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, (1000 * 3).toLong()
        )

    }

}
