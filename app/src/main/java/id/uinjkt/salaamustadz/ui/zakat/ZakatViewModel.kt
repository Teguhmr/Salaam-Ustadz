package id.uinjkt.salaamustadz.ui.zakat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.uinjkt.salaamustadz.state.Result
typealias Image = String

class ZakatViewModel: ViewModel() {

    private var imageSTFLiveData: MutableLiveData<Result<List<Image>>> = MutableLiveData(Result.Loading())
    val imagesSTFLiveData: LiveData<Result<List<Image>>>
        get() = imageSTFLiveData

    private val database = FirebaseDatabase.getInstance()
    private val imageRef = database.reference.child("zakat").child("dokumentasi_stf")

    init {
        fetchImageDocumentation()
    }

    private fun fetchImageDocumentation() {
        val imagesList: ArrayList<Image> = ArrayList()

        // Add a ValueEventListener to the reference
        imageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                imagesList.clear() // Clear the previous data
                for (articleSnapshot in dataSnapshot.children) {
                    val images = articleSnapshot.getValue(String::class.java)
                    images?.let {
                        imagesList.add(it)
                    }
                }

                imageSTFLiveData.postValue(Result.Success(imagesList))

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
                imageSTFLiveData.postValue(Result.Error(databaseError.message))
            }
        })
    }
}