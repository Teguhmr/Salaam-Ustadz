package id.uinjkt.salaamustadz.ui.prayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.LayoutCustomJadwalSholatBinding

class CustomJadwalView constructor(
    context: Context,
    attrs: AttributeSet,
) : LinearLayout(context, attrs) {

    private var binding: LayoutCustomJadwalSholatBinding

    init {
        binding = LayoutCustomJadwalSholatBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setTitle(title:String) {
        binding.tvTitle.text = title
    }

    fun setTime(time:String) {
        binding.tvTime.text = time
    }
    fun setBackground(strokeWidth: Int = 3, strokeColor: Int = R.color.dark_blue_primary, cardBackground: Int = R.color.soft_yellow_green){
        binding.cardViewRoot.strokeWidth = strokeWidth
        binding.cardViewRoot.strokeColor = context.getColor(strokeColor)
        binding.cardViewRoot.setCardBackgroundColor(context.getColor(cardBackground))
    }
}