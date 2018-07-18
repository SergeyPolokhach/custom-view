package com.cleveroad.circlelayout

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup


class CircleLayout : ViewGroup {

    companion object {
        private const val BAD_ANGLE = -1F
        private const val DEFAULT_VALUE = 0
        private const val HALF_DELIMITER = 2
    }

    private var maxWidth = DEFAULT_VALUE
    private var maxHeight = DEFAULT_VALUE

    private var childRect = Rect()
    private var childCenter = Point()
    private var center = Point()
    private var angleInRadians = BAD_ANGLE
    private var inflatedChildCount = DEFAULT_VALUE

    constructor (context: Context) : super(context)

    constructor (context: Context,
                 attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor (context: Context,
                 attrs: AttributeSet,
                 defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor (context: Context,
                 attrs: AttributeSet,
                 defStyleAttr: Int,
                 defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                maxWidth = Math.max(maxWidth, child.measuredWidth)
                maxHeight = Math.max(maxHeight, child.measuredHeight)
            }
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val childCount = childCount

        /*if angle hasn't been set, try to calculate it
        taking into account how many items declared
        in xml inside CircularLayout*/
        if (angleInRadians == BAD_ANGLE && inflatedChildCount > DEFAULT_VALUE) {
            setCapacity(childCount)
        }

        if (angleInRadians == BAD_ANGLE) {
            throw IllegalStateException("set angle or capacity first with " +
                    "setAngle(double degrees)/setCapacity(int expectedViewsQuantity) or " +
                    "with xml angle/capacity attrs.")
        }

        val width = right - left
        val height = bottom - top

        if (width != height) {
            throw IllegalStateException("width should be the same as height")
        }

        val radius = width / HALF_DELIMITER - Math.max(maxWidth / HALF_DELIMITER, maxHeight / HALF_DELIMITER) - maxPadding()
        center.set(width / HALF_DELIMITER, width / HALF_DELIMITER)

        var firstIsLaidOut = false
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                if (!firstIsLaidOut) {
                    childCenter.x = center.x
                    childCenter.y = center.y - radius
                    firstIsLaidOut = true
                } else {
                    val deltaX = childCenter.x - center.x
                    val deltaY = childCenter.y - center.y
                    val cos = Math.cos(angleInRadians.toDouble())
                    val sin = Math.sin(angleInRadians.toDouble())
                    childCenter.x = (center.x + deltaX * cos - deltaY * sin).toInt()
                    childCenter.y = (center.y + deltaX * sin + deltaY * cos).toInt()
                }
                layoutChild(child)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflatedChildCount = childCount
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val typedArray = context
                .obtainStyledAttributes(attrs, R.styleable.CircleLayout)

        val capacity = typedArray.getInteger(R.styleable.CircleLayout_capacity, DEFAULT_VALUE)
        if (capacity != DEFAULT_VALUE) {
            setCapacity(capacity)
        }
        /*angle attr always wins*/
        val angle = typedArray.getFloat(R.styleable.CircleLayout_angle, BAD_ANGLE)
        if (angle != BAD_ANGLE) {
            setAngle(angle.toDouble())
        }
        typedArray.recycle()
    }

    private fun setAngle(degrees: Double) {
        this.angleInRadians = degreesToRadians(degrees)
        requestLayout()
    }

    private fun setCapacity(expectedViewsQuantity: Int) {
        this.angleInRadians = degreesToRadians(360.0 / expectedViewsQuantity)
        requestLayout()
    }

    private fun maxPadding(): Int {
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        return Math.max(Math.max(Math.max(paddingTop, paddingBottom), paddingLeft), paddingRight)
    }

    private fun layoutChild(child: View) {
        val childWidth = child.measuredWidth
        val childHeight = child.measuredHeight
        childRect.top = childCenter.y - childHeight / HALF_DELIMITER
        childRect.left = childCenter.x - childWidth / HALF_DELIMITER
        childRect.right = childRect.left + childWidth
        childRect.bottom = childRect.top + childHeight
        child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom)
    }

    private fun degreesToRadians(angleInDegrees: Double) =
            Math.toRadians(angleInDegrees).toFloat()
}