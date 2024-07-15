package com.findmykids.tracker.panda.util

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.findmykids.tracker.panda.R
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation

class CustomListBalloonFactory : Balloon.Factory() {

    override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {
        return Balloon.Builder(context)
            .setLayout(R.layout.layout_custom_list)
            .setWidth((context.resources.displayMetrics.widthPixels * 0.2f).toInt())
            .setHeight((context.resources.displayMetrics.widthPixels * 0.25f).toInt())
            .setArrowOrientation(ArrowOrientation.TOP)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowPosition(0.5f)
            .setArrowSize(20)
            .setTextSize(12f)
            .setCornerRadius(10f)
            .setMarginRight(12)
            .setElevation(6)
            .setBackgroundColorResource(R.color.white)
            .setBalloonAnimation(BalloonAnimation.FADE)
            .setIsVisibleOverlay(false)
            .setOverlayColorResource(R.color.text_black_color)
            .setOverlayPadding(12.5f)
            .setDismissWhenShowAgain(true)
            .setDismissWhenTouchOutside(true)
            .setDismissWhenOverlayClicked(true)
            .setDismissWhenClicked(true)
            .setLifecycleOwner(lifecycle)
            .build()
    }
}
