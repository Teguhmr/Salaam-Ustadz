package id.uinjkt.salaamustadz.utils.firebase

import android.os.CountDownTimer
import com.google.firebase.Timestamp
import id.uinjkt.salaamustadz.utils.Constants
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ConversationListener(chat_room_id: String, private val endConversationListener:() -> Unit) {
    private var conversationTimer: CountDownTimer? = null
    private val conversationRef = FireStoreUtil.fireStore.collection(Constants.KEY_COLLECTION_CHAT_ROOMS)
        .document(chat_room_id)

    // Function to start listening to the conversation
    fun startListening() {
        conversationRef.collection("chat")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                // Check if there are new messages
                val hasNewMessages = snapshot?.documents?.isNotEmpty() ?: false

                if (hasNewMessages) {
                    // If there are new messages, update the end time based on the last message
                    updateEndTimeBasedOnLastMessage()
                } else {
                    // If there are no new messages, check if the conversation has ended
                    checkConversationEnded()
                }
            }
    }

    // Function to update the end time based on the last message
    private fun updateEndTimeBasedOnLastMessage() {
        conversationRef.collection("chat")
            .orderBy("datetime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val lastMessageTimestamp = querySnapshot.documents[0].getTimestamp("datetime") ?: return@addOnSuccessListener
                    val endTimeMillis = calculateEndTimeFromTimestamp(timestampToLong(lastMessageTimestamp))
                    FireStoreUtil.storeEndTimeInFireStore(conversationRef, endTimeMillis)
                    startTimer(endTimeMillis - Calendar.getInstance().timeInMillis)
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure
            }
    }

    private fun timestampToLong(timestamp: Timestamp): Long {
        return timestamp.toDate().time
    }

    // Function to calculate the end time based on the last message's timestamp
    private fun calculateEndTimeFromTimestamp(lastMessageTimestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = lastMessageTimestamp
        calendar.add(Calendar.DAY_OF_YEAR, 2) // Add one day to the last message's timestamp
//        calendar.set(Calendar.HOUR_OF_DAY, 13) // Set the desired end time (hour) for tomorrow
//        calendar.set(Calendar.MINUTE, 0) // Set the desired end time (minute) for tomorrow
//        calendar.set(Calendar.SECOND, 0) // Set the desired end time (second) for tomorrow
//        calendar.set(Calendar.MILLISECOND, 0) // Set the desired end time (millisecond) for tomorrow

        return calendar.timeInMillis
    }

    private fun checkConversationEnded() {
        conversationRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val endTimeMillis = documentSnapshot.getTimestamp("endTime") ?: return@addOnSuccessListener
                val endTimeTimestamp = Timestamp(timestampToLong(endTimeMillis) / 1000, (timestampToLong(endTimeMillis) % 1000).toInt() * 1000000)
                if (Timestamp.now() >= endTimeTimestamp) {
                    // End the conversation since the end time is reached
                    endConversationListener.invoke()
                } else {
                    // Start the timer to check again when the end time is reached
                    startTimer(timestampToLong(endTimeMillis) - Calendar.getInstance().timeInMillis)
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure
            }
    }

    // Function to end the conversation
    private fun endConversation() {
        // Perform actions to end the conversation
        // For example, you can update the UI, show a message, or close the activity/fragment.
    }

    // Function to start the countdown timer
    private fun startTimer(durationMillis: Long) {
        conversationTimer?.cancel() // Cancel the previous timer if running

        conversationTimer = object : CountDownTimer(durationMillis, 1000) { // 1000 milliseconds = 1 second
            override fun onTick(millisUntilFinished: Long) {
                // Do something on each tick (e.g., update UI with the remaining time)
                // For example: updateTextView.text = formatTimeRemaining(millisUntilFinished)
            }

            override fun onFinish() {
                // The timer has finished, end the conversation
//                endConversation()
                endConversationListener.invoke()
            }

            private fun formatTimeRemaining(millisUntilFinished: Long): String {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                return String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }

        conversationTimer?.start()
    }

    // Function to stop the countdown timer
    fun stopTimer() {
        conversationTimer?.cancel()
    }
}
