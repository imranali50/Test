package com.findmykids.tracker.panda.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ItemMenuBinding

class GridMenuAdapter : RecyclerView.Adapter<GridMenuAdapter.MenuViewHolder>() {

    var listener: GridMenuListener? = null

    private val menus = arrayListOf(
        /*Menu(
            "Video",
            R.drawable.ic_video
        ),*/
        Menu(
            "Camera",
            R.drawable.ic_camera
        ),
        Menu(
            "Gallery",
            R.drawable.ic_gallery
        ),
        /*Menu(
            "Audio",
            R.drawable.ic_volume
        ),
        Menu(
            "Location",
            R.drawable.ic_location
        ),
        Menu(
            "Document",
            R.drawable.ic_document
        ),
        Menu(
            "Contact",
            R.drawable.ic_contact
        )*/
    )

    interface GridMenuListener {
        fun dismissPopup()
        fun onCameraClick(name: String)
    }

    private val data = ArrayList<Menu>().apply {
        addAll(menus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder.create(
            parent,
            viewType
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(data[position], listener)
        holder.itemView.setOnClickListener {
            listener?.onCameraClick(data[position].name)
            listener?.dismissPopup()
        }
    }

    class MenuViewHolder(var binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            menu: Menu,
            listener: GridMenuListener?
        ) {
            with(itemView) {
                binding.tvTitle.text = menu.name
                binding.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, menu.drawable))
//                itemView.setOnClickListener {
//
//
//                }
            }
        }

        companion object {

            fun create(parent: ViewGroup, viewType: Int): MenuViewHolder {
                val binding: ItemMenuBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_menu,
                    parent,
                    false
                )
                return MenuViewHolder(binding)
            }
        }
    }

    data class Menu(val name: String, @DrawableRes val drawable: Int) {

    }
}