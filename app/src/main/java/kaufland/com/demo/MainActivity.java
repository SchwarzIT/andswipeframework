package kaufland.com.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import kaufland.com.swipelibrary.SwipeLayout;
import kaufland.com.swipelibrary.SwipeState;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeLayout mViewById = (SwipeLayout) findViewById(R.id.swipe_example_2);
        mViewById.setSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onSwipeOpened(SwipeState.DragViewState openedDragView, boolean isFullSwipe) {
                Toast.makeText(MainActivity.this, "onSwipeOpened " + openedDragView.name() + String.valueOf(isFullSwipe), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeClosed(SwipeState.DragViewState dragViewState) {
                Toast.makeText(MainActivity.this, "onSwipeOpened " + dragViewState.name(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBounce(SwipeState.DragViewState dragViewState) {
                Toast.makeText(MainActivity.this, "onBounce " + dragViewState.name(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
