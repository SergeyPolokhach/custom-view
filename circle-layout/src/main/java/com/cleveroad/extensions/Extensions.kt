package com.cleveroad.extensions

import android.view.View


fun View.OnClickListener.setClickListeners(vararg views: View) {
    views.forEach { it.setOnClickListener(this) }
}