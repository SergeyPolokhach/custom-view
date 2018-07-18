package com.cleveroad.circlelayout

import android.graphics.Rect
import android.view.View


data class ChildViewWrapper(var view: View? = null, val childLayout: Rect = Rect())
