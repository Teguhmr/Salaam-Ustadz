package id.uinjkt.salaamustadz.ui.adapter.doa

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.data.models.doa.Doa
import id.uinjkt.salaamustadz.databinding.ItemListDoaBinding

class AdapterDoa(
    private var dataList: List<Doa>,
    private val onMenuSelected: (Int, String) -> Unit
): RecyclerView.Adapter<AdapterDoa.MenuHolder>(), Filterable {
    private var filteredDataList: List<Doa> = dataList.toMutableList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return MenuHolder(
            ItemListDoaBinding.inflate(
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
                tvNumber.text = id
                tvTitleSurah.text = doa
                itemView.setOnClickListener {
                    onMenuSelected.invoke(id.toInt(), doa)
                }
            }
        }

    }


    inner class MenuHolder(binding: ItemListDoaBinding): RecyclerView.ViewHolder(binding.root) {
        val tvNumber: TextView = binding.tvNumber
        val tvTitleSurah: TextView = binding.tvTitle
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrBlank()) {
                    results.values = dataList
                } else {
                    val filteredList = dataList.filter { it.doa.contains(constraint, ignoreCase = true) }
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    filteredDataList = results.values as List<Doa>
                    notifyDataSetChanged()
                }
            }
        }
    }


}