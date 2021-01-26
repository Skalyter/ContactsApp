package com.tiberiugaspar.tpjadcontactsapp.utils;

/**
 * Abstract class declaring the methods for leftClick and rightClick
 * to hold the implementation logic of the click actions for each button
 */
public abstract class SwipeControllerActions {
    /**
     * @param position the position of the current viewHolder inside the recyclerView
     */
    public void onLeftClicked(int position) {
    }

    /**
     * @param position the position of the current viewHolder inside the recyclerView
     */
    public void onRightClicked(int position) {
    }
}
