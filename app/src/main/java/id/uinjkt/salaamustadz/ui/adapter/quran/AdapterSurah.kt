package id.uinjkt.salaamustadz.ui.adapter.quran

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.databinding.ItemListSurahBinding
import id.uinjkt.salaamustadz.utils.capitalizeWords
import id.uinjkt.ustadzapp.data.models.quran.ModelSurah

class AdapterSurah(
    private var dataList: List<ModelSurah>,
    private val onMenuSelected: (Int, String) -> Unit
): RecyclerView.Adapter<AdapterSurah.MenuHolder>(), Filterable {
    private var filteredDataList: List<ModelSurah> = dataList.toMutableList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return MenuHolder(
            ItemListSurahBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return filteredDataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        val data = filteredDataList[position]
        holder.apply {
            data.apply {
                tvNumber.text = nomor
                tvTitleSurah.text = nama
                tvArab.text = asma
                tvArtiSurah.text = "($arti)"
                tvInformation.text = "${type.capitalizeWords()} - $ayat Ayat"
                itemView.setOnClickListener {
                    onMenuSelected.invoke(nomor.toInt(), nama)
                }
            }
        }

    }


    inner class MenuHolder(binding: ItemListSurahBinding): RecyclerView.ViewHolder(binding.root) {
        val tvNumber: TextView = binding.tvNumber
        val tvTitleSurah: TextView = binding.tvSurah
        val tvArtiSurah: TextView = binding.tvArti
        val tvArab: TextView = binding.tvName
        val tvInformation: TextView = binding.tvInfo
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrBlank()) {
                    results.values = dataList
                } else {
                    val filteredList = dataList.filter { it.nama.contains(constraint, ignoreCase = true) }
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    filteredDataList = results.values as List<ModelSurah>
                    notifyDataSetChanged()
                }
            }
        }
    }


}