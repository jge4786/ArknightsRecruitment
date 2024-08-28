package com.jge.testapp2

import android.graphics.drawable.GradientDrawable

class Drawer {
    companion object {
        fun setBorder(bgColor: Int, bgRadius: Float, borderColor: Int, borderWidth: Int): GradientDrawable {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.setColor(bgColor)
            gradientDrawable.cornerRadius = bgRadius
            gradientDrawable.setStroke(borderWidth, borderColor)
            return gradientDrawable
        }
    }
}

data class OperatorColor (
    var background: Int,
    var stroke: Int
)