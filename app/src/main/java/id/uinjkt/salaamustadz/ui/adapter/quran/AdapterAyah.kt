package id.uinjkt.salaamustadz.ui.adapter.quran

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.data.models.quran.ModelAyat
import id.uinjkt.salaamustadz.databinding.ItemAyahQuranBinding

class AdapterAyah(
    private var dataList: MutableList<ModelAyat>
): RecyclerView.Adapter<AdapterAyah.MenuHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return MenuHolder(
            ItemAyahQuranBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        val data = dataList[position]
        holder.apply {
            data.apply {
                tvNumber.text = nomor
                tvTranslate.text = indo
                tvArabic.text = " ".plus(arab).plus(" ")
            }
        }
    }

    inner class MenuHolder(binding: ItemAyahQuranBinding): RecyclerView.ViewHolder(binding.root) {
        val tvNumber: TextView = binding.tvNomorAyat
        val tvArabic: TextView = binding.tvArabic
        val tvTranslate: TextView = binding.tvTerjemahan
    }

}