package com.findmykids.tracker.panda.model

import android.graphics.drawable.Drawable


class AppListResponse {  val name: String
    var icon: Drawable
    var packageName: String? = null
        private set
    var extra: String? = null
        private set

    constructor(name: String, icon: Drawable, packageName: String?) {
        this.name = name
        this.icon = icon
        this.packageName = packageName
    }

    constructor(name: String, icon: Drawable) {
        this.name = name
        this.icon = icon
    }

    constructor(name: String, packageName: String?, icon: Drawable) {
        this.name = name
        this.icon = icon
        this.packageName = packageName
    }

    constructor(name: String, packageName: String?, icon: Drawable, extra: String?) {
        this.name = name
        this.icon = icon
        this.packageName = packageName
        this.extra = extra
    }}




