package id.uinjkt.salaamustadz.ui.screen.walkthrough

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.auth.WalkThrough

class WalkThroughPagerAdapter(private val context: Context, private val items: List<WalkThrough>) : PagerAdapter() {
    override fun getCount(): Int = items.size

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.item_walk_through, container, false)

        val data = items[position]

        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        val imageHeader: ImageView = itemView.findViewById(R.id.imageViewHeader)


        data.apply {
            tvTitle.text = title
            tvSubtitle.text = subtitle
            imageHeader.setImageResource(image)
        }


        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}
