package id.uinjkt.salaamustadz.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.yalantis.ucrop.UCrop
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.base.BaseActivity
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.ActivityEditProfileBinding
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.SuccessType
import id.uinjkt.salaamustadz.utils.compressImageToByteArray
import id.uinjkt.salaamustadz.utils.dialog.SuccessDialog
import id.uinjkt.salaamustadz.utils.emptyDash
import id.uinjkt.salaamustadz.utils.firebase.FireStoreUtil
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.formatDate
import id.uinjkt.salaamustadz.utils.parcelable
import id.uinjkt.salaamustadz.utils.toast
import io.reactivex.Flowable
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditProfileActivity : BaseActivity<ActivityEditProfileBinding>() {

    private var user: User? = null

    private val rcCrop = 100
    private val rcCropProfile = 200
    private var croppedImageUri: Uri? = null
    private var croppedImageUriProfile: Uri? = null
    private lateinit var birthDate: String

    override fun getViewBinding(): ActivityEditProfileBinding {
        return ActivityEditProfileBinding.inflate(layoutInflater)
    }

    override fun setupIntent() {

    }

    override fun setupUI() {
        user = intent.parcelable(Constants.KEY_USER)
        setUpUi(user)
        birthDate = user?.birthDate!!
        
        binding.btnChangeBackground.setOnClickListener {
            openImagePicker()
        }
        binding.imageProfile.setOnClickListener {
            openImagePickerProfile()
        }
        binding.btnDatePicker.setOnClickListener {
            datePicker(user?.birthDate)
        }
        with(binding){
            btnSave.setOnClickListener {
                if (edtName.text.isNullOrEmpty()){
                    applicationContext.toast("Field nama tidak boleh kosong")
                    return@setOnClickListener
                }

                else {
                    showLoading()
                    uploadDataUser()
                }
            }

        }

    }
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePicker.launch(intent)
    }
    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            // Perform cropping operation on the selected image
            selectedImageUri?.let { uri ->
                openCropActivity(uri, rcCrop)
            }
        }
    }
    private val imagePickerProfile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            // Perform cropping operation on the selected image
            selectedImageUri?.let { uri ->
                openCropActivity(uri, rcCropProfile)
            }
        }
    }

    private fun openImagePickerProfile() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerProfile.launch(intent)
    }
    private fun openCropActivity(imageUri: Uri, requestCode: Int) {
        // Start the crop activity using the uCrop library
        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(this, R.color.soft_yellow_green))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.hard_yellow_green))

        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        when (requestCode) {
            rcCrop -> {
                UCrop.of(imageUri, destinationUri)
                    .withAspectRatio(16F, 9F)
                    .withOptions(options)
                    .start(this, requestCode)
            }
            rcCropProfile -> {
                UCrop.of(imageUri, destinationUri)
                    .withAspectRatio(1F, 1F)
                    .withOptions(options)
                    .start(this, requestCode)
            }
        }
    }
    @SuppressLint("CheckResult")
    private fun uploadDataUser(){
        val selectedImageUriProfile = croppedImageUriProfile
        val selectedImageUriBackground = croppedImageUri
        if (selectedImageUriProfile != null && selectedImageUriBackground != null) {
            compressImagesToByteArrayPair(this, selectedImageUriProfile, selectedImageUriBackground)
                .subscribe { (selectedImageBytes, selectedImageBytesBackground) ->
                    StorageUtil.uploadProfilePhotoAndBackground(user?.id!!, selectedImageBytes, selectedImageBytesBackground){ imagePathProfile, imagePathBackground ->
                        updateProfileAndFinish(imagePathProfile, imagePathBackground)
                    }
                }

        }
        else if (selectedImageUriProfile != null) {
            compressImageToByteArray(this, selectedImageUriProfile)
                .subscribe { selectedImageBytes ->
                    StorageUtil.uploadProfilePhoto(user?.id!!, selectedImageBytes) { path ->
                        updateProfileAndFinish(path, null)
                    }
                }
        } else if (selectedImageUriBackground != null) {
            compressImageToByteArray(this, selectedImageUriBackground)
                .subscribe { selectedImageBytes ->
                    StorageUtil.uploadProfileBackground(user?.id!!, selectedImageBytes) { path ->
                        updateProfileAndFinish(null, path)
                    }
                }
        } else {
            updateProfileAndFinish(null, null)
        }
    }
    private fun updateProfileAndFinish(uriProfilePath: String?, uriBackgroundPath: String?) {
        with(binding) {
            FireStoreUtil.updateUserData(
                user?.id!!,
                edtName.text?.trim().toString(),
                birthDate,
                uriProfilePath ?: user?.image,
                uriBackgroundPath ?: user?.imageBg,
                Timestamp(Date())
            ) {
                if (uriProfilePath != null){
                    StorageUtil.deleteRefBucket(user?.image.toString())
                }
                if (uriBackgroundPath != null){
                    StorageUtil.deleteRefBucket(user?.imageBg.toString())
                }
                dismissLoading()
                SuccessDialog(this@EditProfileActivity, SuccessType.UPDATE_PROFILE.type) {
                    finish()
                }.show(supportFragmentManager, "successUpdateProfileDialog")
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun datePicker(initialDateString: String?){
        // Parse the initial date string
        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) // Use the appropriate locale
        if (initialDateString != emptyDash() && initialDateString?.isNotEmpty() == true){
            val initialDate = dateFormat.parse(initialDateString)
            initialDate?.let {
                calendar.time = it
            }
        }



        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val datePickerDialog = DatePickerDialog(
            this, R.style.my_dialog_theme,
            { _, years, monthOfYear, dayOfMoth ->
                calendar.set(Calendar.YEAR, years)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMoth)
                val myFormat = "dd MMMM yyyy" // mention the format you need
                val format = "dd MMM yyyy" // mention the format you need
                val sdf = SimpleDateFormat(format, Locale("id", "ID"))
                val sdf2 = SimpleDateFormat(myFormat, Locale("id", "ID"))
                binding.tvChoseDate.text = sdf.format(calendar.time)

                birthDate = sdf2.format(calendar.time)
                binding.tvChoseDate.error = null
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
    @SuppressLint("CheckResult")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rcCrop && resultCode == RESULT_OK) {
            croppedImageUri = UCrop.getOutput(data!!)
            // The cropped image URI is available here. You can use it as needed.
            Glide.with(this)
                .load(croppedImageUri)
                .into(binding.imgBackgroundProfile)

        } else if (requestCode == rcCropProfile && resultCode == RESULT_OK) {
            croppedImageUriProfile = UCrop.getOutput(data!!)
            Glide.with(this)
                .load(croppedImageUriProfile)
                .into(binding.imageProfile)

        }
    }

    private fun compressImagesToByteArrayPair(context: Context, imageUriProfile: Uri, imageUriBackground: Uri): Flowable<Pair<ByteArray, ByteArray>> {
        val flowable1 = compressImageToByteArray(context, imageUriProfile)
        val flowable2 = compressImageToByteArray(context, imageUriBackground)

        return Flowable.zip(flowable1, flowable2) { uri1, uri2 ->
            Pair(uri1, uri2)
        }
    }

    private fun setUpUi(user: User?) {
        if (user?.image.isNullOrEmpty()){
            Glide
                .with(this)
                .load(StorageUtil.pathToReference("/images/profile_user.png"))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(binding.imageProfile)
        } else {
            Glide
                .with(this)
                .load(StorageUtil.pathToReference(user?.image.toString()))
                .centerCrop()
                .placeholder(R.drawable.profile_user)
                .into(binding.imageProfile)
        }

        with(binding){
            edtName.setText(user?.name)
            tvChoseDate.text = formatDate(user?.birthDate!!)
            edtPhoneNumber.setText(user.phone)
            tvEmail.text = user.email

        }

        if (user?.imageBg.isNullOrEmpty()){
            Glide.with(this)
                .load(StorageUtil.pathToReference("/images/bg_profile.png"))
                .into(binding.imgBackgroundProfile)
        } else {
            Glide.with(this)
                .load(StorageUtil.pathToReference(user?.imageBg!!))
                .into(binding.imgBackgroundProfile)
        }
        setSupportActionBar(binding.toolbar)

    }
    override fun setupAction() {
    }

    override fun setupProcess() {
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