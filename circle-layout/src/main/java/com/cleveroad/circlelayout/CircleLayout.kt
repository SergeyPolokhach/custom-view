package com.cleveroad.circlelayout

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import com.cleveroad.extensions.setClickListeners


class CircleLayout : ViewGroup, View.OnClickListener {

    companion object {
        private const val BAD_ANGLE = -1F
        private const val DEFAULT_VALUE = 0
        private const val HALF_DELIMITER = 2
        private const val DURATION = 1000L
        private const val START_VALUE = 0F
        private const val END_VALUE = 1F
        private const val DEGREES_IN_CICLE = 360.0
    }

    private var maxWidth = DEFAULT_VALUE
    private var maxHeight = DEFAULT_VALUE

    private var childRect = Rect()
    private var childCenter = Point()
    private var center = Point()
    private var centerLayout = Rect()
    private var angleInRadians = BAD_ANGLE
    private var inflatedChildCount = DEFAULT_VALUE
    private val viewOnCenter = ChildViewWrapper()
    private lateinit var viewOnCircle: View
    private lateinit var viewToCenterAnimator: ValueAnimator
    private lateinit var viewFromCenterAnimator: ValueAnimator

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
            getChildAt(i).run {
                if (visibility != View.GONE) {
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
                    if (id == View.NO_ID) id = View.generateViewId()
                    layoutChild(this)
                }
                setClickListeners(this)
            }
        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        inflatedChildCount = childCount
    }

    override fun onClick(view: View) {
        startAnimateFromCenter(view)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        context.obtainStyledAttributes(attrs, R.styleable.CircleLayout).run {
            getInteger(R.styleable.CircleLayout_capacity, DEFAULT_VALUE)
                    .takeIf { it != DEFAULT_VALUE }
                    ?.let { setCapacity(it) }
            getFloat(R.styleable.CircleLayout_angle, BAD_ANGLE)
                    .takeIf { it != BAD_ANGLE }
                    ?.let { setAngle(it.toDouble()) }
            recycle()
        }
        initValueAnimators()
    }

    private fun initValueAnimators() {
        viewToCenterAnimator = ValueAnimator.ofFloat(START_VALUE, END_VALUE).apply {
            interpolator = AccelerateInterpolator()
            duration = DURATION
            addUpdateListener({
                animateChildToCenter(it.animatedValue as Float)
                invalidate()
            })
            addListener(
                    object : SimpleAnimatorListener() {
                        override fun onAnimationEnd(animation: Animator?) {
                            invalidate()
                        }
                    }
            )
        }
        viewFromCenterAnimator = ValueAnimator.ofFloat(START_VALUE, END_VALUE).apply {
            interpolator = AccelerateInterpolator()
            duration = DURATION
            addUpdateListener({
                animateChildFromCenter(it.animatedValue as Float)
                invalidate()
            })
            addListener(
                    object : SimpleAnimatorListener() {
                        override fun onAnimationEnd(animation: Animator?) {
                            startAnimateToCenter(viewOnCircle)
                            invalidate()
                        }
                    }
            )
        }
    }

    private fun setAngle(degrees: Double) {
        angleInRadians = degreesToRadians(degrees)
        requestLayout()
    }

    private fun setCapacity(expectedViewsQuantity: Int) {
        angleInRadians = degreesToRadians(DEGREES_IN_CICLE / expectedViewsQuantity)
        requestLayout()
    }

    private fun maxPadding() =
            Math.max(Math.max(Math.max(paddingTop, paddingBottom), paddingLeft), paddingRight)

    private fun layoutChild(child: View) {
        val childWidth = child.measuredWidth
        val childHeight = child.measuredHeight
        childRect.apply {
            top = childCenter.y - childHeight / HALF_DELIMITER
            left = childCenter.x - childWidth / HALF_DELIMITER
            right = left + childWidth
            bottom = top + childHeight
            child.layout(left, top, right, bottom)
        }
    }

    private fun startAnimateToCenter(v: View) {
        viewOnCenter.apply {
            childLayout.apply {
                top = v.top
                bottom = v.bottom
                left = v.left
                right = v.right
            }
            centerLayout.apply {
                top = center.y - v.height / HALF_DELIMITER
                left = center.x - v.width / HALF_DELIMITER
                bottom = top + v.height
                right = left + v.width
            }
            viewToCenterAnimator.start()
            view = v
        }
    }

    private fun startAnimateFromCenter(v: View) {
        with(viewOnCenter) {
            if (view?.id == v.id || viewToCenterAnimator.isRunning || viewFromCenterAnimator.isRunning) {
                return
            }
            childRect.run {
                top = v.top
                bottom = v.bottom
                left = v.left
                right = v.right
            }
            centerLayout.run {
                viewOnCenter.view?.let {
                    top = center.y - it.height / HALF_DELIMITER
                    left = center.x - it.width / HALF_DELIMITER
                    bottom = top + it.height
                    right = left + it.width
                }
            }
            viewOnCircle = v
            viewOnCenter.view
                    ?.run { viewFromCenterAnimator.start() }
                    ?: run { startAnimateToCenter(v) }
        }
    }

    private fun animateChildToCenter(animateValue: Float) {
        childRect.run {
            val top = (top - (top - centerLayout.top) * animateValue)
            val left = (left - (left - centerLayout.left) * animateValue)
            val right = (right - (right - centerLayout.right) * animateValue)
            val bottom = (bottom - (bottom - centerLayout.bottom) * animateValue)
            viewOnCircle.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
    }

    private fun animateChildFromCenter(animateValue: Float) {
        viewOnCenter.run {
            centerLayout.run {
                val top = (top + (childLayout.top - top) * animateValue)
                val left = (left + (childLayout.left - left) * animateValue)
                val right = (right + (childLayout.right - right) * animateValue)
                val bottom = (bottom + (childLayout.bottom - bottom) * animateValue)
                view?.layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            }
        }
    }

    private fun degreesToRadians(angleInDegrees: Double) =
            Math.toRadians(angleInDegrees).toFloat()
}