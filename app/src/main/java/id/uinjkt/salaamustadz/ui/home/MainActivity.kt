package id.uinjkt.salaamustadz.ui.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.ActivityMainBinding
import id.uinjkt.salaamustadz.ui.auth.AuthActivity
import id.uinjkt.salaamustadz.ui.prayer.BootCompleteReceiver
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.dialog.ConfirmDialog
import id.uinjkt.salaamustadz.utils.toast
import timber.log.Timber
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    private var docRef: DocumentReference? = null
    private lateinit var auth: FirebaseAuth
    private val senderId: String? = FirebaseAuth.getInstance().uid
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNav.setupWithNavController(navController)

        preferenceManager = PreferenceManager(applicationContext)

        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)

        initRemoteConfig()
        auth = FirebaseAuth.getInstance()

        initUser()
        initOnlineStatus()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 225)
            }
        }

        val receiver = ComponentName(applicationContext, BootCompleteReceiver::class.java)

        applicationContext.packageManager?.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )


    }

    private fun initOnlineStatus() {

        if (auth.currentUser != null) {
            docRef = database.collection(Constants.KEY_COLLECTION_USER).document(auth.currentUser!!.uid)
            docRef?.update(Field.onlineStatus,true, Field.lastSeen, Timestamp(Date()))

        }
    }

    private fun initUser() {
        val userId = auth.currentUser?.uid

        if (userId.isNullOrEmpty()) {
            // User is not authenticated, sign out and navigate to AuthActivity
            signOutAndNavigateToAuthActivity()
            return
        }

        // Get the document reference for the user document
        docRef = database.collection(Constants.KEY_COLLECTION_USER).document(userId)

        // Check if the user document exists
        docRef!!.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // User document exists, proceed with the rest of the logic
                    // You can access the document data using document.data
                } else {
                    // User document does not exist, sign out and navigate to AuthActivity
                    signOutAndNavigateToAuthActivity()
                }
            } else {
                // Error occurred while fetching the document
                Timber.e("Error fetching user document: ${task.exception}")
            }
        }
    }

    private fun signOutAndNavigateToAuthActivity() {
        Firebase.auth.signOut()
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false)
        preferenceManager.clear()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }


    private fun updateToken(token: String) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).update(Field.fcmToken,token).addOnSuccessListener {
            Timber.tag("TOKEN UPDATE STATUS").d("Token Updated Successfully")
        }. addOnFailureListener {
            Timber.tag("TOKEN UPDATE STATUS").d("Failed to Update The Token")
        }
    }

    private fun initRemoteConfig(){
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (id.uinjkt.salaamustadz.BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    preferenceManager.putString(Constants.KEY_PHONE_NUMBER, remoteConfig.getString(Constants.KEY_PHONE_NUMBER))
                    preferenceManager.putString(Constants.KEY_URL_SALAAM_USTADZ, remoteConfig.getString(Constants.KEY_URL_SALAAM_USTADZ))
                    preferenceManager.putString(Constants.KEY_URL_DONATE, remoteConfig.getString(Constants.KEY_URL_DONATE))
                    preferenceManager.putString(Constants.KEY_URL_PRIVACY_POLICY, remoteConfig.getString(Constants.KEY_URL_PRIVACY_POLICY))

                    val latestVersionCode = remoteConfig.getLong(Constants.KEY_LATEST_VERSION_CODE)

                    // Compare with the version code of the installed app
                    val currentVersionCode = id.uinjkt.salaamustadz.BuildConfig.VERSION_CODE
                    if (latestVersionCode > currentVersionCode) {
                        // Show a dialog informing the user about the update
                        ConfirmDialog(this, Constants.KEY_LATEST_VERSION_CODE){
                            openPlayStore()
                        }.show(supportFragmentManager, "latestVersionCodeUstadzApp")
                    }
                } else {
                    toast("Terjadi Kesalahan")
                }
            }

    }

    private fun openPlayStore(){
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
        }
    }

}