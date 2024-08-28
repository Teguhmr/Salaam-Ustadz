package id.uinjkt.salaamustadz.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.Constants
import timber.log.Timber

class HomeViewModel: ViewModel() {
    private var usersLiveData: MutableLiveData<Result<User?>> = MutableLiveData(Result.Loading())
    val userLiveData: LiveData<Result<User?>>
        get() = usersLiveData

    init {
        fetchUserDetail()
    }

    private fun fetchUserDetail() {
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER).document(FirebaseAuth.getInstance().uid!!).addSnapshotListener{ document, error ->
            if (error != null) {
                usersLiveData.postValue(Result.Error(error.message.toString()))
                Timber.tag("Error").w("Listen Failed $error")
                return@addSnapshotListener
            } else {
                val user = document?.toObject(User::class.java)
                usersLiveData.postValue(Result.Success(user))
            }
        }
    }
}