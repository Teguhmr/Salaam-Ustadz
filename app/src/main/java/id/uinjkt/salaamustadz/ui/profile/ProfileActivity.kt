package id.uinjkt.salaamustadz.ui.profile

import id.uinjkt.salaamustadz.base.BaseActivity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.menu.ProfileMenu
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.ActivityProfileBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.about.AboutUsActivity
import id.uinjkt.salaamustadz.ui.adapter.menu.AdapterMenuProfile
import id.uinjkt.salaamustadz.ui.auth.AuthActivity
import id.uinjkt.salaamustadz.ui.auth.ChangePasswordActivity
import id.uinjkt.salaamustadz.ui.home.HomeViewModel
import id.uinjkt.salaamustadz.ui.image.ImagePreviewActivity
import id.uinjkt.salaamustadz.ui.notification.NotificationActivity
import id.uinjkt.salaamustadz.ui.prayer.ReminderScheduler
import id.uinjkt.salaamustadz.utils.AboutApp
import id.uinjkt.salaamustadz.utils.AboutApp.ABOUT_US
import id.uinjkt.salaamustadz.utils.AboutApp.CALL_US
import id.uinjkt.salaamustadz.utils.AboutApp.HELP
import id.uinjkt.salaamustadz.utils.AboutApp.LIKE
import id.uinjkt.salaamustadz.utils.AboutApp.PRIVACY_POLICY
import id.uinjkt.salaamustadz.utils.AccountSetting
import id.uinjkt.salaamustadz.utils.AccountSetting.CHANGE_PASSWORD
import id.uinjkt.salaamustadz.utils.AccountSetting.NOTIFICATION
import id.uinjkt.salaamustadz.utils.AccountSetting.PROFILE
import id.uinjkt.salaamustadz.utils.ConfirmType
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.Field
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.dialog.ConfirmDialog
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.show

class ProfileActivity : BaseActivity<ActivityProfileBinding>() {
    private var userId: String? = null
    private lateinit var prefManager: PreferenceManager
    private lateinit var reminderHandler: ReminderScheduler
    private var docRef: DocumentReference? = null

    override fun getViewBinding(): ActivityProfileBinding {
        return ActivityProfileBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {
    }

    override fun setupUI() {
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()

        prefManager = PreferenceManager(this)
        userId = intent.getStringExtra(Constants.KEY_USER_ID)
        docRef = database.collection(Constants.KEY_COLLECTION_USER).document(prefManager.getString(Constants.KEY_USER_ID).toString())

        binding.btnLogout.setOnClickListener {
            signOutDialog()
        }

        val versionName = id.uinjkt.salaamustadz.BuildConfig.VERSION_NAME
        binding.txtVersion.text = "Ver $versionName"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        menuProfileSettingAccount()
        menuProfileMyApp()
    }
    private fun signOutDialog() {
        ConfirmDialog(this, ConfirmType.CONFIRM_SIGN_OUT.type){
            docRef?.update(Field.onlineStatus,false)
            Firebase.auth.signOut()
            prefManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false)
            cancelReminderPrayer()
            prefManager.clear()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }.show(supportFragmentManager, "dialogSignOut")
    }

    private fun cancelReminderPrayer(){
        val getJsonString = prefManager.getString(Constants.KEY_REMINDERS_TIME)

        if (getJsonString != null){
            val mapType = object : TypeToken<ArrayList<Int>>() {}.type
            val retrievedMap: ArrayList<Int> = Gson().fromJson(getJsonString, mapType)

            for (time in retrievedMap) {
                reminderHandler.cancelRemindersNotification(time)
            }
        }
    }
    override fun setupAction() {
    }

    override fun setupProcess() {
        reminderHandler = ReminderScheduler(this)

    }

    override fun setupObserver() {
        val homeViewModel: HomeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.userLiveData.observe(this){state ->
            setUpUi(state)
        }
    }

    private fun setUpUi(state: Result<User?>) {
        prefManager.putString(Constants.KEY_EMAIL, state.data?.email.toString())
        when (state){
            is Result.Error -> {}
            is Result.Loading -> {}
            is Result.Success -> {
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
                    binding.imageProfile.setOnClickListener {
                        val intent = Intent(this@ProfileActivity, ImagePreviewActivity::class.java)
                        intent.putExtra(Constants.KEY_IMAGE_MESSAGE, state.data?.image.toString())
                        intent.putExtra(Constants.KEY_NAME_IMAGE_OWNER, "Anda")
                        startActivity(intent)
                    }
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
                        upArrow?.colorFilter = PorterDuffColorFilter(
                            ContextCompat.getColor(
                                this,
                                R.color.dark_blue_primary
                            ), PorterDuff.Mode.SRC_ATOP
                        )
                        binding.imageProfile.gone()

                    } else if (isShow == true) {
                        //expanded map
                        isShow = false
                        upArrow?.colorFilter = PorterDuffColorFilter(
                            ContextCompat.getColor(this, R.color.white),
                            PorterDuff.Mode.SRC_ATOP
                        )
                        binding.imageProfile.show()

                    }
                }
            }
        }
    }

    private fun menuProfileSettingAccount(){
        val profileSettingAccountList = ArrayList<ProfileMenu>().apply {
            add(ProfileMenu(R.drawable.ic_user, getString(R.string.my_profile)))
            add(ProfileMenu(R.drawable.ic_change_password, getString(R.string.change_password)))
            add(ProfileMenu(R.drawable.ic_notification_2, getString(R.string.label_setting_notif)))

        }
        val adapterSettingAccount = AdapterMenuProfile(profileSettingAccountList) { position, view ->
            when (AccountSetting.getPosition(position)) {
                PROFILE -> {
                    val intent = Intent(this, DetailProfileActivity::class.java)
                    intent.putExtra(userId, Constants.KEY_USER_ID)
                    startActivity(intent)
                }
                CHANGE_PASSWORD -> {
                    val intent = Intent(this, ChangePasswordActivity::class.java)
                    startActivity(intent)
                }
                NOTIFICATION -> {
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }
            }

        }
        val rvSettingAccount = binding.rvSettingAccount
        rvSettingAccount.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        rvSettingAccount.setHasFixedSize(true)
        rvSettingAccount.adapter = adapterSettingAccount
    }

    private fun menuProfileMyApp(){
        val profileMyAppList = ArrayList<ProfileMenu>().apply {
            add(ProfileMenu(R.drawable.ic_question, getString(R.string.label_about_us)))
            add(ProfileMenu(R.drawable.ic_information, getString(R.string.label_help)))
            add(ProfileMenu(R.drawable.ic_privacy_policy, getString(R.string.label_privacy_policy)))
            add(ProfileMenu(R.drawable.ic_rating_app, getString(R.string.label_rate)))
            add(ProfileMenu(R.drawable.ic_call_center, getString(R.string.label_call_us)))
        }
        val adapterMyApp = AdapterMenuProfile(profileMyAppList) { position, view ->
            when (AboutApp.getPosition(position)) {
                ABOUT_US -> startActivity(Intent(this, AboutUsActivity::class.java))
                HELP -> prefManager.getString(Constants.KEY_URL_SALAAM_USTADZ)?.let { openUrl(it) }
                LIKE -> openPlayStore()
                CALL_US -> openWhatsApp()
                PRIVACY_POLICY -> prefManager.getString(Constants.KEY_URL_PRIVACY_POLICY)?.let { openUrl(it) }
            }

        }
        val rvMyApp = binding.rvMyApp
        rvMyApp.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        rvMyApp.setHasFixedSize(true)
        rvMyApp.adapter = adapterMyApp
    }

    private fun openUrl(url: String){
        val uri: Uri = Uri.parse(url)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse(url)))
        }
    }

    private fun openPlayStore(){
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun openWhatsApp() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=${prefManager.getString(Constants.KEY_PHONE_NUMBER)}")
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            // Add more menu items handling here if needed
        }
        return super.onOptionsItemSelected(item)
    }

}