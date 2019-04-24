package com.gustavclausen.bikeshare.view.adapters

import android.support.v7.widget.RecyclerView
import android.view.View

/*
 * Source (accessed 2019-04-24): https://gist.github.com/sheharyarn/5602930ad84fa64c30a29ab18eb69c6e
 */
class RecyclerViewEmptyObserver(private val recyclerView: RecyclerView, private val emptyView: View) :
    RecyclerView.AdapterDataObserver() {

    init {
        checkIfEmpty()
    }

    /**
     * Check if Layout is empty and show the appropriate view
     */
    private fun checkIfEmpty() {
        if (recyclerView.adapter != null) {
            val emptyViewVisible = recyclerView.adapter.itemCount == 0
            emptyView.visibility = if (emptyViewVisible) View.VISIBLE else View.GONE
            recyclerView.visibility = if (emptyViewVisible) View.GONE else View.VISIBLE
        }
    }

    override fun onChanged() {
        checkIfEmpty()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }
}