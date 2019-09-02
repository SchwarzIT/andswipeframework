package kaufland.com.demo.example

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import kaufland.com.demo.R
import kaufland.com.swipelibrary.SwipeLayout
import kaufland.com.swipelibrary.SwipeState
import kotlinx.android.synthetic.main.easy_example_fragment.*
import kotlinx.android.synthetic.main.match_parent_example.*

/**
 * Created by sbra0902 on 30.03.17.
 */

class EasyExample : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        return inflater.inflate(R.layout.easy_example_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExample2()

        btn_add.setOnClickListener { text_quantity.text = (Integer.valueOf(text_quantity.text.toString()) + 1).toString() }
        btn_minus.setOnClickListener { text_quantity.text = (Integer.valueOf(text_quantity.text.toString()) - 1).toString() }

    }

    private fun setupExample2() {
        swipe_example_2.setSwipeListener(object : SwipeLayout.SwipeListener {
            override fun onSwipeOpened(openedDragView: SwipeState.DragViewState, isFullSwipe: Boolean) {
                Toast.makeText(activity, "onSwipeOpened " + openedDragView.name + isFullSwipe.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onSwipeClosed(dragViewState: SwipeState.DragViewState) {
                Toast.makeText(activity, "onSwipeOpened " + dragViewState.name, Toast.LENGTH_SHORT).show()
            }

            override fun onBounce(dragViewState: SwipeState.DragViewState) {
                Toast.makeText(activity, "onBounce " + dragViewState.name, Toast.LENGTH_SHORT).show()
            }
        })
        btn_close.setOnClickListener { swipe_example_2.closeSwipe() }

        surface_view_example2.setOnClickListener { Toast.makeText(activity, "Clicked", Toast.LENGTH_SHORT).show() }
        btn_open_left.setOnClickListener { swipe_example_2.openSwipe(SwipeLayout.LEFT_DRAG_VIEW) }
        btn_open_right.setOnClickListener { swipe_example_2.openSwipe(SwipeLayout.RIGHT_DRAG_VIEW) }
        btn_disable_left_drag.setOnClickListener(object : View.OnClickListener {
            private var state = true
            override fun onClick(v: View) {
                if (state) {
                    state = false
                    (v as Button).text = "enable left drag"

                } else {
                    state = true
                    (v as Button).text = "disable left drag"
                }
                swipe_example_2_left_drag.isDraggable = state

            }
        })
        btn_disable_right_drag.setOnClickListener(object : View.OnClickListener {
            private var state = true
            override fun onClick(v: View) {
                if (state) {
                    state = false
                    (v as Button).text = "enable right drag"

                } else {
                    state = true
                    (v as Button).text = "disable right drag"
                }
                swipe_example_2_right_drag.isDraggable = state

            }
        })
    }
}
