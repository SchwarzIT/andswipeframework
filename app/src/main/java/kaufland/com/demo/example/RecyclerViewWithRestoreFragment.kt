package kaufland.com.demo.example

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kaufland.com.demo.R
import kaufland.com.demo.example.dummy.DummyContent

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class RecyclerViewWithRestoreFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview_with_restore, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = LinearLayoutManager(context)
            view.adapter = MyItemRecyclerViewAdapter(DummyContent.ITEMS)
        }
        return view
    }

}