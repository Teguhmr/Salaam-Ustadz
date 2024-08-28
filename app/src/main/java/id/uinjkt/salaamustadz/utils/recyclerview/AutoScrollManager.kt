package id.uinjkt.salaamustadz.utils.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

class AutoScrollManager(private val recyclerView: RecyclerView) {


    private val timer = Timer()
    private var autoScrollTask: TimerTask? = null

    private val autoScrollRunnable  = object : TimerTask() {
        override fun run() {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val itemCount = layoutManager.itemCount

            if (lastVisibleItemPosition != RecyclerView.NO_POSITION) {
                val nextPosition = (lastVisibleItemPosition + 1) % itemCount
                recyclerView.smoothScrollToPosition(nextPosition)
            }
        }
    }

    companion object {
        private const val AUTO_SCROLL_DELAY = 3000L // Auto-scroll interval in milliseconds
    }

    fun startAutoScroll() {
        stopAutoScroll() // Stop any existing auto-scroll task
        autoScrollTask = autoScrollRunnable
        Timber.tag("timer").d("$autoScrollTask")
        timer.scheduleAtFixedRate(autoScrollTask, 0, AUTO_SCROLL_DELAY)
    }

    fun stopAutoScroll() {
        autoScrollTask?.cancel()
        autoScrollTask = null
    }
}
