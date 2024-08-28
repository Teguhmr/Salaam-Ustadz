package id.uinjkt.salaamustadz

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.data.remote.di.apiModule
import id.uinjkt.salaamustadz.data.remote.di.repositoryModule
import id.uinjkt.salaamustadz.data.remote.di.viewModelModule
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber
import java.util.Date


class MyApplication: Application() {
    private var docRef: DocumentReference? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        FirebaseApp.initializeApp(this)
        database = FirebaseFirestore.getInstance()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        database.firestoreSettings.isPersistenceEnabled
        auth = FirebaseAuth.getInstance()
        initTimber()

        ProcessLifecycleOwner.get().lifecycle.addObserver(defaultLifecycleObserver)
        startKoin {
            androidContext(this@MyApplication)
            modules(listOf(
                apiModule,
                repositoryModule,
                viewModelModule
            ))
        }
    }

    private var defaultLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            auth.addAuthStateListener(authStateListener)
            Glide.with(applicationContext).resumeRequests()
            docRef?.update(Field.onlineStatus,true, Field.lastSeen, Timestamp(Date()))
            Timber.tag("MyApp").d("Online")

        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            auth.removeAuthStateListener(authStateListener)
            Glide.with(applicationContext).pauseRequests()
            docRef?.update(Field.onlineStatus,false)
            Timber.tag("MyApp").d("Offline")

        }

    }

    var authStateListener =
        AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                docRef = database.collection(Constants.KEY_COLLECTION_USER).document(auth.currentUser!!.uid)
            }
        }


    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "SalaamUstadz/${element.fileName}:${element.lineNumber})#${element.methodName}"
                }
            })
        }
    }


}