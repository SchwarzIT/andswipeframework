package kaufland.com.swipelibrary;

import android.graphics.PointF;
import android.view.View;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sbra0902 on 21.06.17.
 */
public class LayoutCache {


    Map<View, PointF> mLayoutCache = new HashMap<>();


    public void restoreOnLayout(){

        for (View child : mLayoutCache.keySet()){

            PointF restorePoint = mLayoutCache.get(child);

            if(restorePoint != null){
                child.setX(restorePoint.x);
                child.setY(restorePoint.y);
            }

        }

    }



    public void captureChildrenBound(Collection<View> views){

        for (View child : views) {
            PointF point = mLayoutCache.get(child);
            if(point==null){
                point = new PointF();
                mLayoutCache.put(child, point);
            }

            point.x = child.getX();
            point.y = child.getY();
        }
    }
}
