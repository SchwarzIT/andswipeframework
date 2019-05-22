package kaufland.com.demo.example;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import kaufland.com.demo.R;
import kaufland.com.swipelibrary.DragView;
import kaufland.com.swipelibrary.SwipeLayout;
import kaufland.com.swipelibrary.SwipeState;

/**
 * Created by sbra0902 on 30.03.17.
 */

public class EasyExample extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.easy_example_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupExample2(view);

        final TextView mQuantityTextView = (TextView) view.findViewById(R.id.txt_quantity);

        view.findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantityTextView.setText(String.valueOf(Integer.valueOf(mQuantityTextView.getText().toString()) +1));
            }
        });

        view.findViewById(R.id.btn_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantityTextView.setText(String.valueOf(Integer.valueOf(mQuantityTextView.getText().toString()) -1));
            }
        });


    }

    private void setupExample2(final View view) {
        final SwipeLayout mViewById = (SwipeLayout) view.findViewById(R.id.swipe_example_2);
        mViewById.setSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onSwipeOpened(SwipeState.DragViewState openedDragView, boolean isFullSwipe) {
                Toast.makeText(getActivity(), "onSwipeOpened " + openedDragView.name() + String.valueOf(isFullSwipe), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeClosed(SwipeState.DragViewState dragViewState) {
                Toast.makeText(getActivity(), "onSwipeOpened " + dragViewState.name(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBounce(SwipeState.DragViewState dragViewState) {
                Toast.makeText(getActivity(), "onBounce " + dragViewState.name(), Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewById.closeSwipe();
            }
        });

        view.findViewById(R.id.surface_view_example2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.btn_open_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewById.openSwipe(SwipeLayout.Companion.getLEFT_DRAG_VIEW());
            }
        });
        view.findViewById(R.id.btn_open_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewById.openSwipe(SwipeLayout.Companion.getRIGHT_DRAG_VIEW());
            }
        });
        view.findViewById(R.id.btn_disable_left_drag).setOnClickListener(new View.OnClickListener() {
            private boolean state = true;
            public void onClick(View v) {
                if ( state ) {
                    state = false;
                    ((Button)v).setText("enable left drag");

                } else {
                    state = true;
                    ((Button)v).setText("disable left drag");
                }
                ((DragView)view.findViewById(R.id.swipe_example_2_left_drag)).setDraggable(state);

            }
        });
        view.findViewById(R.id.btn_disable_right_drag).setOnClickListener(new View.OnClickListener() {
            private boolean state = true;
            public void onClick(View v) {
                if ( state ) {
                    state = false;
                    ((Button)v).setText("enable right drag");

                } else {
                    state = true;
                    ((Button)v).setText("disable right drag");
                }
                ((DragView)view.findViewById(R.id.swipe_example_2_right_drag)).setDraggable(state);

            }
        });
    }
}
