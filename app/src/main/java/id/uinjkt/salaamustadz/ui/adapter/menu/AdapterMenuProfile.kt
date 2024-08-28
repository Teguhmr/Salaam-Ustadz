package id.uinjkt.salaamustadz.ui.adapter.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.data.models.menu.ProfileMenu
import id.uinjkt.salaamustadz.databinding.ItemListMenuProfileBinding

class AdapterMenuProfile(
    private var dataList: ArrayList<ProfileMenu>,
    private val onMenuSelected: (Int, View) -> Unit
): RecyclerView.Adapter<AdapterMenuProfile.MenuHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return MenuHolder(
            ItemListMenuProfileBinding.inflate(
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
                nameMenu.text = txtTitle
                iconMenu.setImageResource(resImage)

            }
            itemView.setOnClickListener {  view ->
                onMenuSelected.invoke(position, view)
            }
        }
    }


    inner class MenuHolder(binding: ItemListMenuProfileBinding): RecyclerView.ViewHolder(binding.root) {
        val iconMenu: ImageView = binding.iconMenu
        val nameMenu: TextView = binding.nameMenu
    }


}