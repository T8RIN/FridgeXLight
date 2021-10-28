package com.progix.fridgex.light.helper

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.progix.fridgex.light.MainActivity.Companion.actionMode
import com.progix.fridgex.light.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.CartAdapter
import com.progix.fridgex.light.adapter.FridgeAdapter

class ActionModeCallback : ActionMode.Callback {

    private var myFridgeAdapter: FridgeAdapter? = null
    private var myCartAdapter: CartAdapter? = null
//    private var myStarredProductsAdapter: StarredProductsAdapter? = null
//    private var myStarredRecipesAdapter: StarredRecipesAdapter? = null
//    private var myEditListAdapter: EditListAdapter? = null

    var fragmentId: Int? = null

    fun init(myAdapter: Any, fragmentId: Int) {
        this.fragmentId = fragmentId
        when (fragmentId) {
            R.id.nav_fridge -> {
                this.myFridgeAdapter = myAdapter as FridgeAdapter
            }
            R.id.nav_cart -> {
                this.myCartAdapter = myAdapter as CartAdapter
            }
//            R.id.nav_star_prod -> {
//                this.myStarredProductsAdapter = myAdapter as StarredProductsAdapter
//            }
//            R.id.nav_star_rec -> {
//                this.myStarredRecipesAdapter = myAdapter as StarredRecipesAdapter
//            }
//            R.id.nav_edit_list -> {
//                this.myEditListAdapter = myAdapter as EditListAdapter
//            }
        }
    }

    private var shouldResetRecyclerView = true

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (fragmentId) {
            R.id.nav_fridge -> {
                myFridgeAdapter?.tempPositions =
                    myFridgeAdapter?.selectedPositions?.clone() as ArrayList<Int>
                myFridgeAdapter?.tempList =
                    myFridgeAdapter?.selectedIds?.clone() as ArrayList<String>
                when (item?.itemId) {
                    android.R.id.home -> {
                        shouldResetRecyclerView = false
                        myFridgeAdapter?.selectedIds?.clear()
                        for (i in myFridgeAdapter?.selectedPositions!!) {
                            myFridgeAdapter?.notifyItemChanged(i)
                        }
                        myFridgeAdapter?.selectedPositions?.clear()
                        actionMode?.title = ""
                        actionMode?.finish()
                    }
                    R.id.tostar -> {
                        myFridgeAdapter?.doSomeAction("star")
                        actionMode?.finish()
                    }
                    R.id.tocart -> {
                        myFridgeAdapter?.doSomeAction("cart")
                        actionMode?.finish()
                    }
                    R.id.toban -> {
                        myFridgeAdapter?.doSomeAction("ban")
                        actionMode?.finish()
                    }
                    R.id.clear -> {
                        myFridgeAdapter?.doSomeAction("delete")
                        actionMode?.finish()
                    }
                }
            }
            R.id.nav_cart -> {
                myCartAdapter?.tempPositions =
                    myCartAdapter?.selectedPositions?.clone() as ArrayList<Int>
                myCartAdapter?.tempList =
                    myCartAdapter?.selectedIds?.clone() as ArrayList<String>
                when (item?.itemId) {
                    android.R.id.home -> {
                        shouldResetRecyclerView = false
                        myCartAdapter?.selectedIds?.clear()
                        for (i in myCartAdapter?.selectedPositions!!) {
                            myCartAdapter?.notifyItemChanged(i)
                        }
                        myCartAdapter?.selectedPositions?.clear()
                        actionMode?.title = ""
                        actionMode?.finish()
                    }
                    R.id.share -> {
                        myCartAdapter?.doSomeAction("share")
                        actionMode?.finish()
                    }
                    R.id.tostar -> {
                        myCartAdapter?.doSomeAction("star")
                        actionMode?.finish()
                    }
                    R.id.toban -> {
                        myCartAdapter?.doSomeAction("ban")
                        actionMode?.finish()
                    }
                    R.id.clear -> {
                        myCartAdapter?.doSomeAction("delete")
                        actionMode?.finish()
                    }
                }
            }
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        when (fragmentId) {
            R.id.nav_fridge -> {
                mode?.menuInflater?.inflate(R.menu.action_menu_fridge, menu)
            }
            R.id.nav_cart -> {
                mode?.menuInflater?.inflate(R.menu.action_menu_cart, menu)
            }
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDestroyActionMode(mode: ActionMode?) {
        when (fragmentId) {
            R.id.nav_fridge -> {
                if (shouldResetRecyclerView) {
                    myFridgeAdapter?.selectedIds?.clear()
                    for (i in myFridgeAdapter?.selectedPositions!!) {
                        myFridgeAdapter?.notifyItemChanged(i)
                    }
                    myFridgeAdapter?.selectedPositions?.clear()
                }
            }
            R.id.nav_cart -> {
                if (shouldResetRecyclerView) {
                    myCartAdapter?.selectedIds?.clear()
                    for (i in myCartAdapter?.selectedPositions!!) {
                        myCartAdapter?.notifyItemChanged(i)
                    }
                    myCartAdapter?.selectedPositions?.clear()
                }
            }
        }
        isMultiSelectOn = false
        actionMode = null
        shouldResetRecyclerView = true
    }
}