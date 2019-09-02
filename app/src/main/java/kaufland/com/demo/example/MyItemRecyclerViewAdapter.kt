package kaufland.com.demo.example

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import kaufland.com.demo.R
import kaufland.com.demo.example.dummy.DummyContent.DummyItem
import kaufland.com.swipelibrary.SurfaceView
import kaufland.com.swipelibrary.SwipeLayout
import kaufland.com.swipelibrary.SwipeState

class MyItemRecyclerViewAdapter(private val mValues: List<DummyItem>) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    private val mStateReminder = SparseArray<SwipeState.DragViewState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_item_restore, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mSwipeLayout.markForRestoreOnDraw(if (mStateReminder.get(position) == null) SwipeState.DragViewState.CLOSED else mStateReminder.get(position))
        holder.mSwipeLayout.setSwipeListener(object : SwipeLayout.SwipeListener {
            override fun onSwipeOpened(openedDragView: SwipeState.DragViewState, isFullSwipe: Boolean) {
                mStateReminder.put(holder.adapterPosition, openedDragView)
            }

            override fun onSwipeClosed(dragViewState: SwipeState.DragViewState) {
                mStateReminder.put(holder.adapterPosition, dragViewState)
            }

            override fun onBounce(dragViewState: SwipeState.DragViewState) {

            }
        })

        holder.mSurfaceView.setOnClickListener { Toast.makeText(holder.mSwipeLayout.context, "Clicked SwipeLayout", Toast.LENGTH_LONG).show() }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var mSwipeLayout: SwipeLayout = view.findViewById(R.id.swipe_example_2) as SwipeLayout
        internal var mSurfaceView: SurfaceView = view.findViewById(R.id.surface_view_example2) as SurfaceView

    }
}