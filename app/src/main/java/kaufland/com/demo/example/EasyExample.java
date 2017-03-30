package kaufland.com.demo.example;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import kaufland.com.demo.R;
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SwipeLayout mViewById = (SwipeLayout) view.findViewById(R.id.swipe_example_2);
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
    }
}
