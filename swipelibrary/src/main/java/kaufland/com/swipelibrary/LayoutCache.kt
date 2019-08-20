package kaufland.com.swipelibrary

import android.graphics.PointF
import android.view.View

import java.util.HashMap

/**
 * Created by sbra0902 on 21.06.17.
 */
class LayoutCache {


    internal var mLayoutCache: MutableMap<View, PointF> = HashMap()


    fun restoreOnLayout() {

        for (child in mLayoutCache.keys) {

            val restorePoint = mLayoutCache[child]

            if (restorePoint != null) {
                child.x = restorePoint.x
                child.y = restorePoint.y
            }

        }

    }


    fun captureChildrenBound(views: Collection<View>) {

        for (child in views) {
            var point: PointF? = mLayoutCache[child]
            if (point == null) {
                point = PointF()
                mLayoutCache[child] = point
            }

            point.x = child.x
            point.y = child.y
        }
    }
}
