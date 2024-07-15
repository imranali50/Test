package com.findmykids.tracker.panda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.database.Packages
import com.findmykids.tracker.panda.databinding.AdapterAppBinding
import com.findmykids.tracker.panda.model.AppListResponse
import java.util.Locale

class AppAdapter(
    val context: Context,
    var list: ArrayList<AppListResponse>,
    var clickListener: (packageName: String, appName: String) -> Unit
)  : RecyclerView.Adapter<AppAdapter.VieHolder>(),
    Filterable {

     var readPackageList: ArrayList<String> = ArrayList()
    private var cs: ItemFilter? = null
    lateinit var readPackage:Packages
    class VieHolder(val b: AdapterAppBinding) : RecyclerView.ViewHolder(b.root)  {

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VieHolder {
        val b = AdapterAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VieHolder(b)
    }

    override fun onBindViewHolder(holder: VieHolder, position: Int) {
        readPackage = Packages(context)

        holder.b.appName.text = list[position].name
        holder.b.appIcon.setImageDrawable(list[position].icon)
        holder.b.extra.text = list[position].extra
        readPackageList = readPackage.readPacks()
        if (!readPackageList.contains(list[position].packageName)) {
            holder.b.ConstraintLayout.setBackgroundResource(R.drawable.listview_theme)
        } else {
            holder.b.ConstraintLayout.setBackgroundResource(R.drawable.listview_select_theme)
        }

        holder.itemView.setOnClickListener {
            list[position].packageName?.let { it1 -> clickListener.invoke(it1,list[position].name) }

        }
    }

    override fun getFilter(): Filter {
        if (cs == null) {
            cs = ItemFilter()
        }
        return cs!!
    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            var constraint: CharSequence? = constraint
            val results = FilterResults()
            if (constraint != null && constraint.length > 0) {
                constraint = constraint.toString().uppercase(Locale.getDefault())
                val filters = ArrayList<AppListResponse>()
                var a: Int = 0
                while (a < list.size) {
                    if (list[a].name.uppercase(Locale.getDefault()).contains(constraint)) {
                        val appList = AppListResponse(
                            list[a].name,
                            list[a].packageName,
                            list[a].icon
                        )
                        filters.add(appList)
                    }
                    a++
                }
                results.count = filters.size
                results.values = filters
            } else {
                results.count = list.size
                results.values = list
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, filterResults: FilterResults) {
            list = filterResults.values as ArrayList<AppListResponse>
            notifyDataSetChanged()
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

}