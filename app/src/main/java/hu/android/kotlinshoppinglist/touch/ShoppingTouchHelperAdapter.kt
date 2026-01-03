package hu.android.kotlinshoppinglist.touch

interface ShoppingTouchHelperAdapter {

    fun onItemDismissed(position: Int)

    fun onItemMoved(fromPosition: Int, toPosition: Int)
}