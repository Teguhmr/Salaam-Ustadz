package id.uinjkt.salaamustadz.ui.adapter.ustadz

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.ItemListUstadzBinding
import id.uinjkt.salaamustadz.ui.profile.UstadzProfileActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.capitalizeWords
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil

class MyUstadzRecyclerViewAdapter(
    private val context: Context,
    private val userUstadz: List<User>
) : RecyclerView.Adapter<MyUstadzRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemListUstadzBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = userUstadz[position]
        val role = item.role?.capitalizeWords()

        holder.contentView.text = role + " " + item.name
        holder.txtSpecialize.text = if (item.additionalInfo?.knowledgeField.isNullOrEmpty()) "Umum" else item.additionalInfo?.knowledgeField?.joinToString(", ")

        if (item.onlineStatus == true){
            holder.textStatus.text = "online"
            holder.textStatus.setTextColor(context.getColor(R.color.green_status))
        } else {
            holder.textStatus.text = "offline"
            holder.textStatus.setTextColor(context.getColor(R.color.color_text_orange))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, UstadzProfileActivity::class.java)
            intent.putExtra(Constants.KEY_USER, item)
            intent.putExtra(Constants.KEY_ROLE, role)
            context.startActivity(intent)
        }

        if (!item.image.isNullOrEmpty()) {
            Glide
                .with(context)
                .asBitmap()
                .load(StorageUtil.pathToReference(item.image.toString()))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(holder.imgProfile)
        } else {
            holder.imgProfile.setImageResource(R.drawable.profile_user)
        }

    }

    override fun getItemCount(): Int = userUstadz.size

    inner class ViewHolder(binding: ItemListUstadzBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtUsername
        val imgProfile: ImageView = binding.imgProfile
        val textStatus: TextView = binding.txtStatus
        val txtSpecialize: TextView = binding.txtSpecialize
    }

}