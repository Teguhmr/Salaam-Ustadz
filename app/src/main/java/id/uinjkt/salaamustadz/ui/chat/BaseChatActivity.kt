package id.uinjkt.salaamustadz.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field

open class BaseChatActivity : AppCompatActivity() {
   private lateinit var docRef: DocumentReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        docRef = database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!)
        docRef.update(Field.onChatStatus,true)
    }

    override fun onPause() {
        super.onPause()
        Glide.with(this).pauseRequests()
        docRef.update(Field.onChatStatus,false)
    }

    override fun onResume() {
        super.onResume()
        Glide.with(this).resumeRequests()
        docRef.update(Field.onChatStatus,true)
    }

    override fun onDestroy() {
        super.onDestroy()
        docRef.update(Field.onChatStatus,false)

    }
}