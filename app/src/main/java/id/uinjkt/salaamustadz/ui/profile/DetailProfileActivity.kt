package id.uinjkt.salaamustadz.ui.profile

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.models.user.ProfileUser
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.ActivityDetailProfileBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.profile.AdapterProfileUser
import id.uinjkt.salaamustadz.ui.home.HomeViewModel
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.emptyString
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.show
import id.uinjkt.salaamustadz.utils.toast

class DetailProfileActivity : BaseActivity<ActivityDetailProfileBinding>() {

    var userId: String? = emptyString()

    override fun getViewBinding(): ActivityDetailProfileBinding {
        return ActivityDetailProfileBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
        userId = intent.getStringExtra(Constants.KEY_USER_ID)

    }

    override fun setupUI() {
    }

    override fun setupAction() {
    }

    override fun setupProcess() {
        val homeViewModel: HomeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.userLiveData.observe(this){state ->
            when (state){
                is Result.Error -> {
                    dismissLoading()
                    toast(state.message.toString())
                }
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    dismissLoading()
                    setUpUi(state)
                    setUpAdapter(state.data as User)
                }
            }

        }

    }
    private fun setUpAdapter(user: User){
        val dataProfileList = ArrayList<ProfileUser>().apply {
            add(ProfileUser("Nama Lengkap", user.name.toString()))
            add(ProfileUser("Tanggal Lahir", "${user.birthDate}"))
        }
        val adapterDetailUser = AdapterProfileUser(this, dataProfileList)

        val rvInfoProfile = binding.rvInfoProfile
        rvInfoProfile.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        rvInfoProfile.setHasFixedSize(true)
        rvInfoProfile.adapter = adapterDetailUser
        val dataProfilePrivateList = ArrayList<ProfileUser>().apply {
            add(ProfileUser("Nomor Telepon", user.phone.toString()))
            add(ProfileUser("Email", user.email.toString()))
        }
        val adapterPrivateData = AdapterProfileUser(this, dataProfilePrivateList)

        val rvInfoProfilePrivate = binding.rvPrivateProfile
        rvInfoProfilePrivate.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        rvInfoProfilePrivate.setHasFixedSize(true)
        rvInfoProfilePrivate.adapter = adapterPrivateData


        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra(Constants.KEY_USER, user)
            startActivity(intent)
        }
    }

    private fun setUpUi(state: Result<User?>) {
        binding.txtUsername.text = state.data?.name
        if (state.data?.image.isNullOrEmpty()){
            Glide
                .with(this)
                .load(StorageUtil.pathToReference("/images/profile_user.png"))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(binding.imageProfile)
        } else {
            Glide
                .with(this)
                .load(StorageUtil.pathToReference(state.data?.image.toString()))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(binding.imageProfile)
        }

        if (state.data?.imageBg.isNullOrEmpty()){
            Glide.with(this)
                .load(StorageUtil.pathToReference("/images/bg_profile.png"))
                .into(binding.imgBackgroundProfile)
        } else {
            Glide.with(this)
                .load(StorageUtil.pathToReference(state.data?.imageBg!!))
                .into(binding.imgBackgroundProfile)
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        val upArrow = ResourcesCompat.getDrawable(resources, R.drawable.arrow_back, null)
        upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        var isShow: Boolean? = false
        var scrollRange = -1
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, offset ->
            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange
            }
            if (scrollRange + offset == 0) {
                //collapse map
                isShow = true
                upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.dark_blue_primary), PorterDuff.Mode.SRC_ATOP)
                binding.imageProfile.gone()

            } else if (isShow == true) {
                //expanded map
                isShow = false
                upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
                binding.imageProfile.show()

            }

        }
    }


    override fun setupObserver() {
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }
}