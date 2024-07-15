package com.findmykids.tracker.panda.util

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        outRect.left = space
        Log.e("TAG", "getItemOffsets: >>>>>>>>>>" + parent.getChildAdapterPosition(view))
        if (parent.getChildAdapterPosition(view) != (parent.adapter?.itemCount!! - 1)) {
            outRect.right = space
        }
//        outRect.bottom = space

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) != 0) {
//            outRect.top = space
            outRect.left = space
        }
    }
}

class TopBottomItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        outRect.left = space
        Log.e("TAG", "getItemOffsets: >>>>>>>>>>" + parent.getChildAdapterPosition(view))
        if (parent.getChildAdapterPosition(view) != (parent.adapter?.itemCount!! - 1)) {
            outRect.bottom = space
        }
//        outRect.bottom = space

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) != 0) {
//            outRect.top = space
            outRect.top = space
        }
    }
}
class AdaptiveSpacingItemDecoration(
    private val size: Int,
    private val edgeEnabled: Boolean = false
) : ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // Basic item positioning
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        val isLastPosition = position == (itemCount - 1)
        val isFirstPosition = position == 0

        // Saved size
        val sizeBasedOnEdge = if (edgeEnabled) size else 0
        val sizeBasedOnFirstPosition = if (isFirstPosition) sizeBasedOnEdge else size
        val sizeBasedOnLastPosition = if (isLastPosition) sizeBasedOnEdge else 0 // NO_SPACING means zero

        // Update properties
        with(outRect) {
            left = sizeBasedOnEdge
            top = sizeBasedOnFirstPosition
            right = sizeBasedOnEdge
            bottom = sizeBasedOnLastPosition
        }
    }
}