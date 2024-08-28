package id.uinjkt.salaamustadz.ui.ibadah

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.pray.Pray
import id.uinjkt.salaamustadz.databinding.FragmentIbadahBinding
import id.uinjkt.salaamustadz.ui.adapter.ibadah.AdapterIbadah
import id.uinjkt.salaamustadz.ui.doa.DoaActivity
import id.uinjkt.salaamustadz.ui.dzikir.DzikirActivity
import id.uinjkt.salaamustadz.ui.prayer.PrayerActivity
import id.uinjkt.salaamustadz.ui.quran.QuranActivity
import id.uinjkt.salaamustadz.ui.zakat.ZakatActivity
import id.uinjkt.salaamustadz.utils.TuntunanIbadahFeatures
import id.uinjkt.salaamustadz.utils.TuntunanIbadahFeatures.DOA
import id.uinjkt.salaamustadz.utils.TuntunanIbadahFeatures.DZIKIR
import id.uinjkt.salaamustadz.utils.TuntunanIbadahFeatures.PRAYER
import id.uinjkt.salaamustadz.utils.TuntunanIbadahFeatures.QURAN
import id.uinjkt.salaamustadz.utils.TuntunanIbadahFeatures.ZAKAT

class IbadahFragment : Fragment() {
    private var _binding: FragmentIbadahBinding? = null
    private val binding get() = _binding as FragmentIbadahBinding
    private val prayList = ArrayList<Pray>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIbadahBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapterIbadah()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prayList.apply {
            add(
                Pray(
                    R.drawable.jadwal_sholat,
                    getString(R.string.prayer_schedule),
                    getString(R.string.label_body_prayer)
                )
            )
            add(
                Pray(
                    R.drawable.ic_quran,
                    getString(R.string.label_quran),
                    getString(R.string.label_body_quran)
                )
            )
            add(
                Pray(
                    R.drawable.ic_dzikir,
                    getString(R.string.label_dzikr),
                    getString(R.string.label_body_dzikr)
                )
            )
            add(
                Pray(
                    R.drawable.ic_doa_harian,
                    getString(R.string.label_daily_prayer),
                    getString(R.string.label_body_daily_prayer)
                )
            )
            add(
                Pray(
                    R.drawable.zakat,
                    getString(R.string.label_zakat),
                    getString(R.string.label_body_zakat)
                )
            )
        }
    }

    private fun adapterIbadah(){

        val adapterMyApp = AdapterIbadah(prayList) { position ->
            when (TuntunanIbadahFeatures.getPosition(position)) {
                PRAYER -> startActivity(Intent(requireContext(), PrayerActivity::class.java))
                QURAN -> startActivity(Intent(requireContext(), QuranActivity::class.java))
                DZIKIR -> startActivity(Intent(requireContext(), DzikirActivity::class.java))
                DOA -> startActivity(Intent(requireContext(), DoaActivity::class.java))
                ZAKAT -> startActivity(Intent(requireContext(), ZakatActivity::class.java))
            }

        }
        val rvIbadah = binding.rvIbadahGuide
        rvIbadah.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        rvIbadah.setHasFixedSize(true)
        rvIbadah.adapter = adapterMyApp
    }
}