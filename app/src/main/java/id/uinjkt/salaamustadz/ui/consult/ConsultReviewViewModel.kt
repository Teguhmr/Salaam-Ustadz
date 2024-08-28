package id.uinjkt.salaamustadz.ui.consult

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import id.uinjkt.salaamustadz.data.models.chat.ConsultReview
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field

class ConsultReviewViewModel: ViewModel() {
    private var database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var consultLiveData: MutableLiveData<Result<ConsultReview>> = MutableLiveData(Result.Loading())
    val consultReviewLiveData: LiveData<Result<ConsultReview>>
        get() = consultLiveData

    private var statConsultLiveData: MutableLiveData<Result<ConsultReview?>> = MutableLiveData(Result.Loading())
    val statusConsultReviewLiveData: LiveData<Result<ConsultReview?>>
        get() = statConsultLiveData

    private var privateMyReviewLiveData: MutableLiveData<Result<List<ConsultReview>>> = MutableLiveData(Result.Loading())
    val myReviewLiveData: LiveData<Result<List<ConsultReview>>>
        get() = privateMyReviewLiveData
    fun getReview(chatRoomId: String) {
        val consultReviewRef = database.collection(Constants.KEY_CONSULTATION_REVIEWS)
            .whereEqualTo(Field.chatRoomId, chatRoomId)
            .limit(1)

        consultReviewRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                 statConsultLiveData.value = Result.Error(error.message.toString())
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                // Process the snapshot or document changes here
                val consultReview = snapshot.documents[0].toObject(ConsultReview::class.java)
                 statConsultLiveData.value = Result.Success(consultReview)
            } else {
                statConsultLiveData.value = Result.Success(null)
            }
        }
    }

    fun sendReview(chatRoomId: String,
                   rating: Float,
                   titleQuestion: String,
                   question: String,
                   textReview: String,
                   senderId: String,
                   senderName: String,
                   ustadzId: String,
                   ustadzName: String
    ){
        val consultReview = ConsultReview(
            chatRoomId = chatRoomId,
            rating = rating,
            titleQuestion = titleQuestion,
            question = question,
            textReview = textReview,
            senderId = senderId,
            senderName = senderName,
            ustadzId = ustadzId,
            ustadzName = ustadzName
        )
        database.collection(Constants.KEY_CONSULTATION_REVIEWS).add(consultReview).addOnSuccessListener { it3 ->
            val hashmap: HashMap<String,Any> = HashMap()
            hashmap[Field.updatedAt] = FieldValue.serverTimestamp()
            hashmap[Field.createdAt] = FieldValue.serverTimestamp()
            hashmap[Field.id] = it3.id
            it3.update(hashmap)
            consultLiveData.value =  Result.Success(consultReview)
        }.addOnFailureListener {
            consultLiveData.value = Result.Error(it.message.toString())
        }
    }

    fun getMyReview(userId: String) {
        val consultReviewRef = database.collection(Constants.KEY_CONSULTATION_REVIEWS)
            .whereEqualTo(Field.ustadzId, userId)
            .orderBy(Field.createdAt, Query.Direction.DESCENDING)

        consultReviewRef.get().addOnSuccessListener { snapshot ->
            val reviewList = mutableListOf<ConsultReview>()

            for (doc in snapshot.documents) {
                val consultReview = doc.toObject(ConsultReview::class.java)
                consultReview?.let { reviewList.add(it) }
            }

            privateMyReviewLiveData.value = Result.Success(reviewList)
        }.addOnFailureListener { error ->
            privateMyReviewLiveData.value = Result.Error(error.message.toString())
        }
    }
}