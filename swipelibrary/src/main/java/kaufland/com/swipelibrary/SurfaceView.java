package kaufland.com.swipelibrary;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

/**
 * Created by sbra0902 on 06.03.17.
 */

public class SurfaceView extends FrameLayout {

    public SurfaceView(Context context) {
        super(context);
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
