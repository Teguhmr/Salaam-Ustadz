package id.uinjkt.salaamustadz.ui.home.ustadz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import id.uinjkt.salaamustadz.utils.emptyString
import timber.log.Timber

class UstadzViewModel: ViewModel() {
    private var usersLiveData: MutableLiveData<Result<List<User>?>> = MutableLiveData(Result.Loading())
    val userLiveData: LiveData<Result<List<User>?>>
        get() = usersLiveData

    fun fetchUserListUstadz(role1: String, role2: String? = emptyString()) {
        val database = FirebaseFirestore.getInstance()
        val users = mutableListOf<User>()

        val query1 = database.collection(Constants.KEY_COLLECTION_USER).whereEqualTo(Field.role, role1)

        query1.addSnapshotListener { documents1, error1 ->
            users.clear()
            if (error1 != null) {
                Timber.tag("Error").w("Listen Failed $error1")
                return@addSnapshotListener
            } else {
                if (documents1 != null && !documents1.isEmpty) {
                    for (document in documents1.documents) {
                        val user = document.toObject(User::class.java)
                        user?.let { users.add(it) }
                    }
                }
            }

            if (!role2.isNullOrEmpty()) {
                val query2 = database.collection(Constants.KEY_COLLECTION_USER).whereEqualTo(Field.role, role2)
                query2.addSnapshotListener { documents2, error2 ->
                    if (error2 != null) {
                        Timber.tag("Error").w("Listen Failed $error2")
                    } else {
                        if (documents2 != null && !documents2.isEmpty) {
                            for (document in documents2.documents) {
                                val user = document.toObject(User::class.java)
                                user?.let { users.add(it) }
                            }
                        }
                    }

                    usersLiveData.postValue(Result.Success(users))
                }
            } else {
                usersLiveData.postValue(Result.Success(users))
            }
        }
    }

}