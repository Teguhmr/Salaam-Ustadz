package id.uinjkt.salaamustadz.utils.audio.worker

interface AudioRecordListener {
    fun onAudioReady(audioUri: String?)
    fun onRecordFailed(errorMessage: String?)
    fun onReadyForRecord()
}