package id.uinjkt.salaamustadz.data.models.dzikir

import id.uinjkt.salaamustadz.utils.emptyString


data class Dzikir(
    val title: String = emptyString(),
    val countOfReads: String = emptyString(),
    val arabic: String = emptyString(),
    val translate: String = emptyString()
)
