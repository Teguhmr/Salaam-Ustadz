package id.uinjkt.salaamustadz.utils.recyclerview

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.R

class SetUpIndicator(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val snapHelper: PagerSnapHelper,
    private val layoutManager: LinearLayoutManager,
    private val dotLayout: LinearLayout,
    private val dataList:List<*>) {

    init {
        setupDotIndicators()
    }

    // Method to set up dot indicators
    private fun setupDotIndicators() {
        dotLayout.removeAllViews()
        val dotCount = if (dataList.size < 6){
            dataList.size
        } else {
            6
        }
        val dots = arrayOfNulls<ImageView>(dotCount)

        for (i in 0 until dotCount) {
            dots[i] = ImageView(context)
            dots[i]?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dots_transition))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 8, 8, 8)
            dotLayout.addView(dots[i], params)

        }

        // Set the initial active dot
        updateDotIndicators(0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val centerView = snapHelper.findSnapView(layoutManager)
                if (centerView!=null){
                    val pos = layoutManager.getPosition(centerView)
                    updateDotIndicators(pos)
                }

            }

        })
    }

    // Method to update the dot indicators
    private fun updateDotIndicators(currentPosition: Int) {
        for (i in 0 until dotLayout.childCount) {
            val dot = dotLayout.getChildAt(i) as ImageView
            val drawableId =
                if (i == currentPosition) R.drawable.dot_active else R.drawable.dot_inactive
            dot.setImageDrawable(ContextCompat.getDrawable(context, drawableId))
        }
    }
}