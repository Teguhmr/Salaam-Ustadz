package id.uinjkt.salaamustadz.utils.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File
import java.util.UUID


object StorageUtil {
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val imageUserRef: StorageReference
        get() = storageInstance.reference
            .child("images/")

    private val imageUserRefAudio: StorageReference
        get() = storageInstance.reference
            .child("audio/")
    fun uploadProfilePhotoAndBackground(userId:String, imageBytes: ByteArray, imageBytes2: ByteArray,
                                        onSuccess: (imagePathProfile: String, imagePathBackground: String) -> Unit) {
        val ref = imageUserRef.child("profilePictures/$userId/${UUID.nameUUIDFromBytes(imageBytes)}")
        val ref2 = imageUserRef.child("backgroundPictures/$userId/${UUID.nameUUIDFromBytes(imageBytes2)}")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                ref2.putBytes(imageBytes2)
                    .addOnSuccessListener {
                        onSuccess(ref.path, ref2.path)
                    }
            }
    }

    fun uploadProfileBackground(userId:String, imageBytes: ByteArray,
                                onSuccess: (imagePath: String) -> Unit) {
        val ref = imageUserRef.child("backgroundPictures/$userId/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }
    fun uploadProfilePhoto(userId:String, imageBytes: ByteArray,
                           onSuccess: (imagePath: String) -> Unit) {
        val ref = imageUserRef.child("profilePictures/$userId/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun uploadMessageImage(userId:String, imageUri: Uri,
                           onSuccess: (imagePath: String) -> Unit) {
        val ref = imageUserRef.child("messages/$userId/${UUID.randomUUID()}")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun uploadMessageCamera(userId:String, file: File,
                            onSuccess: (imagePath: String) -> Unit) {
        val ref = imageUserRef.child("messages/$userId/${UUID.randomUUID()}")
        ref.putFile(Uri.fromFile(file)).addOnSuccessListener {
            onSuccess(ref.path)
        }

    }
    var metadata = storageMetadata {
        contentType = "audio/m4a"
    }
    fun uploadMessageVoice(userId:String, audioUri: Uri,
                           onSuccess: (audioPath: String) -> Unit) {
        val filename: String = audioUri.toString().substring(audioUri.toString().lastIndexOf("/") + 1)
        val ref = imageUserRefAudio.child("messages/$userId/$filename")
        ref.putFile(audioUri, metadata).addOnSuccessListener {
            onSuccess(ref.path)
        }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
    fun downloadMessageImage(path: String, onSuccess: (imagePath: Uri) -> Unit) {
        storageInstance.getReference(path).downloadUrl.addOnSuccessListener {
            onSuccess(it)
        }
    }

    fun downloadMessageAudioPath(path: String, onSuccess: (imagePath: Uri) -> Unit) {
        storageInstance.getReference(path).downloadUrl.addOnSuccessListener {
            onSuccess(it)
        }
    }

    fun deleteRefBucket(path: String) {
        val fileToDeleteRef: StorageReference = storageInstance.getReferenceFromUrl(pathToReference(path)
            .toString())

        // Use delete() to delete the file from Firebase Storage
        fileToDeleteRef.delete()
    }
}