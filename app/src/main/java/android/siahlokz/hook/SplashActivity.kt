package android.siahlokz.hook

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val imageView: ImageView = findViewById(R.id.gif_view)
        Glide.with(this@SplashActivity)
            .asGif()
            .load(R.drawable.hawktuah)
            .into(imageView)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (DeviceUtils().isInVirtualApp(this@SplashActivity) || DeviceUtils().checkIfDeviceIsRooted()) {
                val openMainActivity = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(openMainActivity)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }, 3900)
    }
}