package id.uinjkt.salaamustadz.data.remote.di

import id.uinjkt.salaamustadz.data.remote.api.ApiClient.Companion.getSchedulePrayer
import id.uinjkt.salaamustadz.data.remote.repository.PrayerRepository
import id.uinjkt.salaamustadz.ui.prayer.PrayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val apiModule = module {
    single { getSchedulePrayer() }
}

val repositoryModule = module {
    single { PrayerRepository(get()) }
}

val viewModelModule = module {
    viewModel { PrayerViewModel(get()) }
}