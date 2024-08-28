package id.uinjkt.salaamustadz.ui.dzikir

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import id.uinjkt.salaamustadz.R

data class DotIndicator(
    val adapter: PagerAdapter,
    val dotsLayout: LinearLayout,
    val context: Context,
    val dotTransition: Int = R.drawable.dots_transition,
    val dotActive: Int = R.drawable.dot_active,
    val dotInactive: Int = R.drawable.dot_inactive
) {
    private val dots = mutableListOf<ImageView>()

    init {
        createDotIndicators()
    }

    private fun createDotIndicators() {
        dotsLayout.removeAllViews()

        val count = adapter.count
        for (i in 0 until count) {
            val dot = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            dot.setImageDrawable(ContextCompat.getDrawable(context, dotTransition))
            layoutParams.setMargins(8, 0, 8, 0)
            dotsLayout.addView(dot, layoutParams)
            dots.add(dot)
        }

        // Set the initial selected dot
        if (count > 0) {
            dots[0].isSelected = true
        }
    }

    fun updateDotIndicator(position: Int) {
        dots.forEachIndexed { index, dot ->
            dot.isSelected = index == position
            val drawableId =
                if (index == position) dotActive else dotInactive
            dot.setImageDrawable(ContextCompat.getDrawable(context, drawableId))
        }
    }
}
