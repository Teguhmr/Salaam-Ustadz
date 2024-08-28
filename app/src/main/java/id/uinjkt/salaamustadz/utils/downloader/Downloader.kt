package id.uinjkt.salaamustadz.utils.downloader

interface Downloader {
    fun downloadImage(url: String): Long
}