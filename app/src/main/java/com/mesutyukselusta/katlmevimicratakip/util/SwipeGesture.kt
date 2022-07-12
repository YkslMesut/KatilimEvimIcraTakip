package com.mesutyukselusta.katlmevimicratakip.util

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mesutyukselusta.katlmevimicratakip.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


abstract class SwipeGesture(context : Context) : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val deleteColor =  ContextCompat.getColor(context, R.color.white)
    private val deleteIcon = R.drawable.ic_baseline_delete_24

    private val documentStatusColor =  ContextCompat.getColor(context, R.color.soft_purple)
    private val documentStatusIcon = R.drawable.ic_baseline_swap_horizontal_circle_24

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return false
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        RecyclerViewSwipeDecorator.Builder(c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive)
            .addSwipeRightBackgroundColor(deleteColor)
            .addSwipeLeftBackgroundColor(documentStatusColor)
            .addSwipeRightActionIcon(deleteIcon)
            .addSwipeLeftActionIcon(documentStatusIcon)
            .create()
            .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}