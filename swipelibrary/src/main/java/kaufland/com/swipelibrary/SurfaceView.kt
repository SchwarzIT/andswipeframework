package kaufland.com.swipelibrary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.support.annotation.Px
import android.support.annotation.RequiresApi
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout

import org.androidannotations.annotations.EViewGroup

/**
 * Created by sbra0902 on 06.03.17.
 */
@EViewGroup
class SurfaceView : FrameLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }
}
