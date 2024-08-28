package id.uinjkt.salaamustadz.ui.adapter.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.user.ProfileUser
import id.uinjkt.salaamustadz.databinding.ItemDetailProfileBinding
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.show

class AdapterProfileUser(
    private val context: Context,
    private var dataList: ArrayList<ProfileUser>
): RecyclerView.Adapter<AdapterProfileUser.MenuHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return MenuHolder(
            ItemDetailProfileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        val data = dataList[position]

        holder.apply {
            data.apply {
                txtTitle.text = title
                txtSubtitle.text = subtitleData
                if (data.title == "Bidang Keilmuan"){
                    txtSubtitle.gone()
                    chipGroup.show()
                    setChipsFromArrayList(listKnowLedge, chipGroup)
                }
            }
        }

    }
    private fun setChipsFromArrayList(stringList:List<String>, chipGroup: ChipGroup) {
        for (text in stringList) {
            val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 50f)
                .build()
            val chip = Chip(context)
            chip.text = text
            chip.setTextColor(ContextCompat.getColor(context, R.color.gray))
            chip.textSize = 13f
            chip.shapeAppearanceModel = shapeAppearanceModel
            chip.setChipBackgroundColorResource(R.color.gray_chip)
            chip.chipStrokeWidth = 0f
            chip.isCloseIconVisible = false // Set to true if you want to show close icon
            chip.isClickable = false
            // Set any other properties for the chip as needed (e.g., chip background, text color, etc.)

            // Add the chip to the ChipGroup
            chipGroup.addView(chip)
        }
    }

    inner class MenuHolder(binding: ItemDetailProfileBinding): RecyclerView.ViewHolder(binding.root) {
        val txtTitle: TextView = binding.tvTitle
        val txtSubtitle: TextView = binding.tvSubtitle
        val chipGroup: ChipGroup = binding.chipGroup
    }


}