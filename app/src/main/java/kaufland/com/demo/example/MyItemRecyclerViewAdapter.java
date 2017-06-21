package kaufland.com.demo.example;

import android.inputmethodservice.Keyboard;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import kaufland.com.demo.R;
import kaufland.com.demo.example.RecylerViewWithRestoreFragment.OnListFragmentInteractionListener;
import kaufland.com.demo.example.dummy.DummyContent.DummyItem;
import kaufland.com.swipelibrary.SurfaceView;
import kaufland.com.swipelibrary.SwipeLayout;
import kaufland.com.swipelibrary.SwipeState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    private Map<Integer, SwipeState.DragViewState> mStateReminder = new HashMap<>();

    public MyItemRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_restore, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.mSwipeLayout.markForRestoreOnDraw(mStateReminder.containsKey(position) ? mStateReminder.get(position) : SwipeState.DragViewState.CLOSED);
        holder.mSwipeLayout.setSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onSwipeOpened(SwipeState.DragViewState openedDragView, boolean isFullSwipe) {
                mStateReminder.put(position, openedDragView);
            }

            @Override
            public void onSwipeClosed(SwipeState.DragViewState dragViewState) {
                mStateReminder.put(position, dragViewState);
            }

            @Override
            public void onBounce(SwipeState.DragViewState dragViewState) {

            }
        });

        holder.mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(holder.mSwipeLayout.getContext(), "Clicked SwipeLayout", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SwipeLayout mSwipeLayout;
        SurfaceView mSurfaceView;

        public ViewHolder(View view) {
            super(view);
            mSwipeLayout = (SwipeLayout) view.findViewById(R.id.swipe_example_2);
            mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view_example2);
        }

    }
}
