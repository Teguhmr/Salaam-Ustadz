package id.uinjkt.salaamustadz.utils.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.user.ProfileUser
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.DialogProfileUstadzBinding
import id.uinjkt.salaamustadz.ui.adapter.profile.AdapterProfileUser
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.capitalizeWords
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil

class ProfileUstadzDialog(private val context: Activity, private val userUstadz: User?): DialogFragment() {

    private lateinit var binding: DialogProfileUstadzBinding
    private lateinit var prefManager: PreferenceManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogProfileUstadzBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        prefManager = PreferenceManager(context)

        binding.btnClose.setOnClickListener {
           dismiss()
        }


        if (userUstadz?.image.isNullOrEmpty()){
            binding.imageProfile.setImageResource(R.drawable.profile_user)
        } else {
            Glide
                .with(this)
                .load(StorageUtil.pathToReference(userUstadz?.image!!))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(binding.imageProfile)
        }
        binding.txtUsername.text = userUstadz?.role?.capitalizeWords().plus(" ${userUstadz?.name}")

        val additionalUserInfo = userUstadz?.additionalInfo
        if (additionalUserInfo != null){
            val dataProfileList = ArrayList<ProfileUser>().apply {
                add(ProfileUser("Bidang Keilmuan", listKnowLedge = additionalUserInfo.knowledgeField!!))
                add(ProfileUser("Nama Lengkap", userUstadz?.name.toString()))
                add(ProfileUser("Tempat Tanggal Lahir", "${additionalUserInfo.birthPlace.toString()}, ${userUstadz?.birthDate.toString()}"))
                add(ProfileUser("Riwayat Pendidikan", additionalUserInfo.education.toString()))
                add(ProfileUser("Pengalaman Kerja Terkait", additionalUserInfo.lecturer.toString()))
                add(ProfileUser("Karya Tulis", additionalUserInfo.papers.toString()))

            }
            val adapterDetailUser = AdapterProfileUser(context, dataProfileList)

            val rvInfoProfile = binding.rvInfoProfile
            rvInfoProfile.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
            rvInfoProfile.setHasFixedSize(true)
            rvInfoProfile.adapter = adapterDetailUser

        }
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}