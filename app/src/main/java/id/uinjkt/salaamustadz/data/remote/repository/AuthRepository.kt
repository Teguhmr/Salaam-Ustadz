package id.uinjkt.salaamustadz.data.remote.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.emptyDash
import id.uinjkt.salaamustadz.utils.toast
import timber.log.Timber
import java.util.Date


class AuthRepository (private val context: Context) {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()

    fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential): MutableLiveData<User> {
        val authenticatedUserMutableLiveData = MutableLiveData<User>()
        firebaseAuth.signInWithCredential(googleAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("signInWithCredential:success")
                // Sign in success, update UI with the signed-in user's information
                val firebaseUser = firebaseAuth.currentUser
                val uid = firebaseUser!!.uid
                val name = firebaseUser.displayName!!
                val email = firebaseUser.email!!
                val user = User(
                    uid, name, email, null, "jamaah", emptyDash(), emptyDash(), Timestamp(
                        Date()
                    ), Timestamp(Date())
                )

                authenticatedUserMutableLiveData.value = user
                if (task.result.additionalUserInfo!!.isNewUser) {
                    createUserInFireStoreIfNotExists(user)
                }
            } else {
                // If sign in fails, display a message to the user.
                context.toast("Tidak dapat masuk")
                Timber.w("signInWithCredential:failure", task.exception)
            }
        }

        return authenticatedUserMutableLiveData
    }

    private fun createUserInFireStoreIfNotExists(authenticatedUser: User): MutableLiveData<User> {
        val newUserMutableLiveData = MutableLiveData<User>()
        database.collection(Constants.KEY_COLLECTION_USER).document(authenticatedUser.id!!)
            .set(authenticatedUser, SetOptions.merge()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                newUserMutableLiveData.value = authenticatedUser
                context.toast("Masuk sebagai ${authenticatedUser.email}")
                Timber.d("UserCreated: success")
            } else {
                context.toast("Tidak dapat masuk")
                Timber.d("unableToAddFireStore: failed")

            }
        }
        return newUserMutableLiveData
    }

    fun checkRoleUser(authID: String): MutableLiveData<User> {
        val userValue = MutableLiveData<User>()
        database.collection(Constants.KEY_COLLECTION_USER).document(authID).get()
            .addOnSuccessListener { data ->
                val user = data.toObject(User::class.java)
                userValue.value = user!!
            }
        return userValue
    }

}