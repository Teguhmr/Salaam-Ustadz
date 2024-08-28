package id.uinjkt.salaamustadz.ui.screen.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.ui.auth.AuthActivity
import id.uinjkt.salaamustadz.ui.home.MainActivity
import id.uinjkt.salaamustadz.ui.screen.walkthrough.WalkThroughActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.WalkthroughPreferenceManager

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var walkthroughPreferenceManager: WalkthroughPreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        preferenceManager = PreferenceManager(applicationContext)
        walkthroughPreferenceManager = WalkthroughPreferenceManager(applicationContext)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent: Intent = if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN) && walkthroughPreferenceManager.getBoolean(Constants.KEY_IS_FIRST_TIME, true)) {
                Intent(this@SplashScreenActivity, WalkThroughActivity::class.java)
            } else if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN) && !walkthroughPreferenceManager.getBoolean(Constants.KEY_IS_FIRST_TIME)) {
                Intent(this@SplashScreenActivity, AuthActivity::class.java)
            } else {
                Intent(this@SplashScreenActivity, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, delayStart.toLong())

    }

    companion object {
        private const val delayStart = 1000
    }
}