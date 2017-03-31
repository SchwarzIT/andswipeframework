# andswipeframework
just another swipe library 


## Demo

TODO


## Features

* Supports horizontal swipe (Left/Right).
* Configure swipe behaviour in XML. (SwipeLayout, DragView, SurfaceView)
* Define child of DragView as settling point. (xml attribute ```app:settleView="@+id/settle"```)
* Enable/Disable bouncing if swipe again in open state (xml attribute ```app:bouncePossible="true"```)
* Open/Close/... programmaticaly
* Recyclerview restore last swipe state in OnBindViewHolder ( ```SwipeLayout.markForRestoreOnDraw(State)```)
* SwipeListener for callbacks if swipe state changes.

## Implementation


1. Add it in your root build.gradle at the end of repositories:

	 ```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	```

2. Add gradle dependency

    ```
    compile 'com.github.Kaufland:andswipeframework:0.8.0'
    ```

3. Configure SwipeFramework 


  ``` xml
  <kaufland.com.swipelibrary.SwipeLayout_
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:weightSum="1"
        android:orientation="horizontal"
        android:id="@+id/swipe_example_2"
        android:layout_alignParentTop="true"
        android:layout_alignParentStart="true">

        <kaufland.com.swipelibrary.DragView_
            android:layout_width="match_parent"
            android:layout_weight="0.33"
            app:bouncePossible="true"
            android:id="@+id/swipe_example_2_right_drag"
            app:position="RIGHT_DRAG_VIEW"

            app:settleView="@+id/settle"
            android:background="@android:color/darker_gray"
            android:layout_height="match_parent">

            <TextView
                android:id="@+id/settle"
                android:layout_width="wrap_content"
                android:layout_height="match_parent"
                android:paddingRight="5dp"
                android:text="SettlePoint"/>
            <TextView
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="match_parent"
                android:text="Right Swipe"/>
        </kaufland.com.swipelibrary.DragView_>

        <kaufland.com.swipelibrary.SurfaceView_
            android:layout_width="match_parent"
            android:paddingTop="20dp"
            android:layout_weight="0.33"
            android:paddingBottom="20dp"
            android:background="@android:color/holo_red_light"
            android:layout_height="match_parent">
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Hello World!"/>

        </kaufland.com.swipelibrary.SurfaceView_>

        <kaufland.com.swipelibrary.DragView_
            android:layout_width="wrap_content"
            android:layout_weight="0.33"
            app:position="LEFT_DRAG_VIEW"
            android:background="@android:color/darker_gray"
            android:id="@+id/swipe_example_2_left_drag"
            app:bouncePossible="true"
            android:layout_height="match_parent">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:text="Left Swipe"/>
        </kaufland.com.swipelibrary.DragView_>
    </kaufland.com.swipelibrary.SwipeLayout_>
  
  ```
  
  ## Next Steps
  
  * Support vertical swipe
  * Support settlePoint swipe restore
