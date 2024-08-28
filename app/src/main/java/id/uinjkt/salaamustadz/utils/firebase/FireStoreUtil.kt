package id.uinjkt.salaamustadz.utils.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.utils.Constants
import java.util.Date

object FireStoreUtil {
    // Function to store the end time in Firestore
    val fireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun storeEndTimeInFireStore(conversationRef: DocumentReference, endTimeMillis: Long) {
        val conversationMap = hashMapOf<String, Any>(
            "endTime" to longToTimestamp(endTimeMillis)
        )
        conversationRef.update(conversationMap)
            .addOnSuccessListener {
                // End time successfully updated in Firestore
            }
            .addOnFailureListener { exception ->
                // Handle the failure to update the end time
            }
    }
    private fun longToTimestamp(milliseconds: Long): Timestamp {
        val date = Date(milliseconds)
        return Timestamp(date)
    }


    fun updateNewPassword(userId: String){
        val userUpdateData = mapOf(
            "updatedAt" to Timestamp(Date())
        )
        val userDocument = fireStore.collection(Constants.KEY_USERS).document(userId)
        userDocument.update(userUpdateData)
    }

    fun updateUserData(
        userId: String,
        name: String,
        birthDate: String,
        image: String?,
        imageBg: String?,
        dateUpdate: Timestamp,
        onSuccess: () -> Unit
    ) {
        val userUpdateData = mapOf(
            "name" to name,
            "birthDate" to birthDate,
            "image" to image,
            "imageBg" to imageBg,
            "updatedAt" to dateUpdate
        )

        val userDocument = fireStore.collection(Constants.KEY_USERS).document(userId)
        userDocument.update(userUpdateData).addOnSuccessListener {
            onSuccess.invoke()
        }


    }
}