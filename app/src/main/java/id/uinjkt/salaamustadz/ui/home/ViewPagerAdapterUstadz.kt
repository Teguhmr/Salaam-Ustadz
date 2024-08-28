package id.uinjkt.salaamustadz.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.uinjkt.salaamustadz.ui.home.ustadz.UstadzFragment
import id.uinjkt.salaamustadz.ui.home.ustadz.UstadzahFragment

private const val NUM_TABS = 2

class ViewPagerAdapterUstadz (fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return if(position == 0){
            UstadzFragment()
        } else {
            UstadzahFragment()
        }
    }
}