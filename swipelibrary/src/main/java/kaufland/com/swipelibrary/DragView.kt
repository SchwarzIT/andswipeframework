package kaufland.com.swipelibrary

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class DragView : LinearLayout {

    var settlePointResourceId: Int = 0
        private set

    var viewPosition: Int = 0
        private set

    var isDraggable = true

    internal val isInitialized: Boolean = false

    var isBouncePossible: Boolean = false

    internal constructor(context: Context) : super(context)

    internal constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initStyleable(context, attrs)
    }

    internal constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initStyleable(context, attrs)
    }

    private fun initStyleable(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragView, 0, 0)
        try {
            viewPosition = typedArray.getInt(R.styleable.DragView_position, 0)
            settlePointResourceId = typedArray.getResourceId(R.styleable.DragView_settleView, -1)
            isBouncePossible = typedArray.getBoolean(R.styleable.DragView_bouncePossible, false)
        } finally {
            typedArray.recycle()
        }
    }


}
