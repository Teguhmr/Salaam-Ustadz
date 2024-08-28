package id.uinjkt.salaamustadz.ui.dzikir

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.dzikir.Dzikir

class DzikirPagerAdapter(private val context: Context, private val items: List<Dzikir>) : PagerAdapter() {
    override fun getCount(): Int = items.size

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.item_dzikir, container, false)

        val data = items[position]

        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvCountOfReads: TextView = itemView.findViewById(R.id.tvCountOfReads)
        val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        val tvArabic: TextView = itemView.findViewById(R.id.tvArabic)
        val tvTranslate: TextView = itemView.findViewById(R.id.tvTerjemahan)

        tvNumber.text = position.plus(1).toString()

        data.apply {
            tvArabic.text = arabic
            tvTranslate.text = translate
            tvCountOfReads.text = countOfReads
            tvTitle.text = title
        }


        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}
