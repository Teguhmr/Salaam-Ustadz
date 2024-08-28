package id.uinjkt.salaamustadz.data.models.user

import id.uinjkt.salaamustadz.utils.emptyString


data class ProfileUser(
    val title: String = emptyString(),
    val subtitleData: String = emptyString(),
    val listKnowLedge: List<String> = emptyList()
)
