package id.uinjkt.salaamustadz.ui.adapter.ibadah

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.data.models.pray.Pray
import id.uinjkt.salaamustadz.databinding.ItemIbadahBinding

class AdapterIbadah(
    private var dataList: ArrayList<Pray>,
    private val onMenuSelected: (Int) -> Unit
): RecyclerView.Adapter<AdapterIbadah.MenuHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return MenuHolder(
            ItemIbadahBinding.inflate(
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
                icon.setImageResource(resImage)
                title.text = txtTitleHeader
                body.text = txtBody
            }
        }

    }


    inner class MenuHolder(binding: ItemIbadahBinding): RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.imgBg
        val title: TextView = binding.txtTitle
        val body: TextView = binding.txtBody

        init {
            binding.materialCardView.setOnClickListener {
                onMenuSelected.invoke(bindingAdapterPosition)
            }
        }
    }


}