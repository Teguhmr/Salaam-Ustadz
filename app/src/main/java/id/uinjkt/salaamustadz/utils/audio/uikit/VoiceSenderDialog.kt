package id.uinjkt.salaamustadz.utils.audio.uikit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.utils.audio.worker.AudioRecordListener
import id.uinjkt.salaamustadz.utils.audio.worker.MediaPlayListener
import id.uinjkt.salaamustadz.utils.audio.worker.Player
import id.uinjkt.salaamustadz.utils.audio.worker.Recorder
import java.io.File

class VoiceSenderDialog(audioRecordList: AudioRecordListener) : BottomSheetDialogFragment(), View.OnClickListener, View.OnTouchListener, AudioRecordListener, MediaPlayListener {
    private var audioRecordListener: AudioRecordListener? = null

    init {
        audioRecordListener = audioRecordList
    }
    private var fileName: String? = null

    private var beepEnabled = false
    private var permissionToRecordAccepted = false
    private var mStartRecording = true
    private var mStartPlaying = true
    private var langObj = LangObj()
    private var iconsObj = IconsObj()
    private lateinit var recordButton: ImageView

    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var recordDuration: Chronometer
    private lateinit var recordInformation: TextView
    private lateinit var closeRecordPanel: TextView
    private lateinit var audioActionInfo: TextView
    private lateinit var audioDelete: ImageView
    private lateinit var audioSend: ImageView
    private var readyToStop = false
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val REQUEST_PERMISSIONS = 200

    private var recorder: Recorder? = null
    private var player: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_voice_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_PERMISSIONS)
        setViews(view)
        closeRecordPanel.setOnClickListener(this)

        val options = Permissions.Options()
                .setSettingsDialogTitle("Izin Diperlukan")
                .setSettingsDialogMessage("Izinkan aplikasi untuk mengakses mikrofon. Silahkan ubah di Info Aplikasi > Izin")
        Permissions.check(requireContext(), permissions, null, options, object : PermissionHandler() {
            override fun onGranted() {
                setListeners()
            }
        })
    }

    private fun setViews(v: View) {
        recordDuration = v.findViewById(R.id.chr_record_duration)
        recordInformation = v.findViewById(R.id.txt_record_info)
        recordButton = v.findViewById(R.id.btn_record)
        lottieAnimationView = v.findViewById(R.id.lottie_animation_bg_circle)
        closeRecordPanel = v.findViewById(R.id.close_record_panel)
        audioDelete = v.findViewById(R.id.audio_delete)
        audioSend = v.findViewById(R.id.audio_send)
        audioActionInfo = v.findViewById(R.id.audio_action_info)

        audioActionInfo.text = langObj.hold_for_record_string
        recordInformation.text = langObj.record_audio_string

        recordButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_start_record))
        audioDelete.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_audio_delete))
        audioSend.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_send_circle))
    }

    fun setFileName(fileName: String) {
        recorder?.fileName = fileName
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        recorder = Recorder(this, requireContext())
        player = Player(this)
        audioDelete.setOnClickListener(this)
        audioSend.setOnClickListener(this)
        recordButton.setOnTouchListener(this)
    }

    fun deleteCurrentFile() {
        try {
            val file = File(fileName)
            file.delete()
            if (file.exists()) {
                file.canonicalFile.delete()
                if (file.exists()) {
                    requireActivity().deleteFile(file.name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            recordButton.id -> onPlay(mStartPlaying)
            audioDelete.id -> if (audioDelete.visibility == View.VISIBLE) {
                deleteCurrentFile()
                resetFragment()
                recordDuration.base = SystemClock.elapsedRealtime()
                recordDuration.stop()
            }
            audioSend.id -> if (audioSend.visibility == View.VISIBLE && !TextUtils.isEmpty(fileName)) {
                reflectRecord(fileName!!)
                dismiss()
            }
            closeRecordPanel.id -> dismiss()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!readyToStop) {
                    onRecord(true)
                    closeRecordPanel.visibility = View.INVISIBLE
                    closeRecordPanel.isEnabled = false
                    recordDuration.base = SystemClock.elapsedRealtime()
                    recordDuration.stop()
                    recordDuration.start()
                    recordInformation.text = langObj.stop_record_string
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (readyToStop) {
                    onRecord(false)
                    closeRecordPanel.visibility = View.VISIBLE
                    closeRecordPanel.isEnabled = true
                    recordDuration.stop()
                    recordInformation.text = langObj.send_record_string
                }
                return true
            }
        }
        return false
    }

    private fun beep() {
        if (beepEnabled) ToneGenerator(AudioManager.STREAM_MUSIC, 70).startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    private fun startRecording() {
        lottieAnimationView.visibility = View.VISIBLE
        audioActionInfo.text = langObj.release_for_end_string
        recordButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_stop_record))
        lottieAnimationView.playAnimation()
        beep()
        Handler().postDelayed({
            recorder?.startRecord()
        }, 50)
    }

    override fun dismiss() {
        super.dismiss()
        resetFragment()
        recordDuration.base = SystemClock.elapsedRealtime()
        recordDuration.stop()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun stopRecording() {
        if (activity != null) {
            lottieAnimationView.cancelAnimation()
            lottieAnimationView.visibility = View.GONE
            audioDelete.visibility = View.VISIBLE
            audioSend.visibility = View.VISIBLE
            audioActionInfo.text = langObj.listen_record_string
            recordDuration.stop()
            recordButton.setOnTouchListener(null)
            recordButton.setOnClickListener(this)
            recordButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_play_record))
            recorder?.stopRecording()
            readyToStop = false
        }
    }

    private fun onRecord(start: Boolean) {
        if (start) {
            readyToStop = false
            startRecording()
        } else {
            if (readyToStop) stopRecording()
            else Handler().postDelayed({
                stopRecording()
                recordDuration.text = "00:01"
            }, 1000)
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) startPlaying() else stopPlaying()
    }

    override fun onAudioReady(audioUri: String?) {
        fileName = audioUri
        player?.injectMedia(fileName)
    }

    override fun onRecordFailed(errorMessage: String?) {
        fileName = null
        reflectError(errorMessage.toString())
        dismiss()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun resetFragment() {
        mStartRecording = true
        mStartPlaying = true
        resetWorkers()
        recordButton.setOnClickListener(null)
        recordButton.setOnTouchListener(this)
        audioDelete.visibility = View.INVISIBLE
        audioSend.visibility = View.INVISIBLE
        recordButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_start_record))
        recordInformation.text = langObj.record_audio_string
        audioActionInfo.text = langObj.hold_for_record_string
        recordDuration.text = "00:00"
    }

    private fun startPlaying() {
        player?.startPlaying()
    }

    private fun stopPlaying() {
        player?.stopPlaying()
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        mStartRecording = true
        mStartPlaying = true
        resetWorkers()
        return super.show(transaction, tag)
    }

    private fun resetWorkers() {
        recorder?.let {
            it.reset()
            recorder = null
            recorder = Recorder(this, requireContext())
        }

        player?.let {
            it.reset()
            player = null
            player = Player(this)
        }
    }
    private fun reflectError(error: String) {
        audioRecordListener?.onRecordFailed(error)
    }

    private fun reflectRecord(uri: String) {
        audioRecordListener?.onAudioReady(uri)
    }

    fun setBeepEnabled(beepEnabled: Boolean) {
        this.beepEnabled = beepEnabled
    }

    private fun letsCheckPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            permissionToRecordAccepted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            } else {
                grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            }
            setListeners()
        }
        if (!permissionToRecordAccepted) dismiss()
    }

    override fun onStartMedia() {
        recordInformation.text = langObj.stop_listen_record_string
        recordButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_stop_play))
        mStartPlaying = !mStartPlaying
    }

    override fun onStopMedia() {
        recordInformation.text = langObj.listen_record_string
        recordButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(), iconsObj.ic_play_record))
        mStartPlaying = !mStartPlaying
    }

    override fun onReadyForRecord() {
        Handler().postDelayed({
            readyToStop = true
        }, 700)
    }

}
