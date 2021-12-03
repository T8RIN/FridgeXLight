package com.progix.fridgex.light.helper.callbacks

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.actionMode
import com.progix.fridgex.light.activity.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.adapter.banned.BannedProductsAdapter
import com.progix.fridgex.light.adapter.banned.BannedRecipesAdapter
import com.progix.fridgex.light.adapter.cart.CartAdapter
import com.progix.fridgex.light.adapter.fridge.FridgeAdapter
import com.progix.fridgex.light.adapter.starred.StarProductsAdapter
import com.progix.fridgex.light.adapter.starred.StarRecipesAdapter

class ActionModeCallback : ActionMode.Callback {

    private var myFridgeAdapter: FridgeAdapter? = null
    private var myCartAdapter: CartAdapter? = null
    private var myStarredProductsAdapter: StarProductsAdapter? = null
    private var myStarredRecipesAdapter: StarRecipesAdapter? = null
    private var myBannedRecipesAdapter: BannedRecipesAdapter? = null
    private var myBannedProductsAdapter: BannedProductsAdapter? = null


    private var fragmentId: Int? = null

    fun attachAdapter(myAdapter: Any, fragmentId: Int) {
        this.fragmentId = fragmentId
        when (fragmentId) {
            R.id.nav_fridge -> {
                this.myFridgeAdapter = myAdapter as FridgeAdapter
            }
            R.id.nav_cart -> {
                this.myCartAdapter = myAdapter as CartAdapter
            }
            4 -> {
                this.myStarredProductsAdapter = myAdapter as StarProductsAdapter
            }
            5 -> {
                this.myStarredRecipesAdapter = myAdapter as StarRecipesAdapter
            }
            6 -> {
                this.myBannedRecipesAdapter = myAdapter as BannedRecipesAdapter
            }
            7 -> {
                this.myBannedProductsAdapter = myAdapter as BannedProductsAdapter
            }
        }
    }

    private var shouldResetRecyclerView = true

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        @Suppress("UNCHECKED_CAST")
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
            4 -> {
                myStarredProductsAdapter?.tempPositions =
                    myStarredProductsAdapter?.selectedPositions?.clone() as ArrayList<Int>
                myStarredProductsAdapter?.tempList =
                    myStarredProductsAdapter?.selectedIds?.clone() as ArrayList<String>
                when (item?.itemId) {
                    android.R.id.home -> {
                        shouldResetRecyclerView = false
                        myStarredProductsAdapter?.selectedIds?.clear()
                        for (i in myStarredProductsAdapter?.selectedPositions!!) {
                            myStarredProductsAdapter?.notifyItemChanged(i)
                        }
                        myStarredProductsAdapter?.selectedPositions?.clear()
                        actionMode?.title = ""
                        actionMode?.finish()
                    }
                    R.id.fridge -> {
                        myStarredProductsAdapter?.doSomeAction("fridge")
                        actionMode?.finish()
                    }
                    R.id.cart -> {
                        myStarredProductsAdapter?.doSomeAction("cart")
                        actionMode?.finish()
                    }
                    R.id.ban -> {
                        myStarredProductsAdapter?.doSomeAction("ban")
                        actionMode?.finish()
                    }
                    R.id.clear -> {
                        myStarredProductsAdapter?.doSomeAction("delete")
                        actionMode?.finish()
                    }
                }
            }
            5 -> {
                myStarredRecipesAdapter?.tempPositions =
                    myStarredRecipesAdapter?.selectedPositions?.clone() as ArrayList<Int>
                myStarredRecipesAdapter?.tempList =
                    myStarredRecipesAdapter?.selectedIds?.clone() as ArrayList<String>
                when (item?.itemId) {
                    android.R.id.home -> {
                        shouldResetRecyclerView = false
                        myStarredRecipesAdapter?.selectedIds?.clear()
                        for (i in myStarredRecipesAdapter?.selectedPositions!!) {
                            myStarredRecipesAdapter?.notifyItemChanged(i)
                        }
                        myStarredRecipesAdapter?.selectedPositions?.clear()
                        actionMode?.title = ""
                        actionMode?.finish()
                    }
                    R.id.clear -> {
                        myStarredRecipesAdapter?.doSomeAction("delete")
                        actionMode?.finish()
                    }
                }
            }
            6 -> {
                myBannedRecipesAdapter?.tempPositions =
                    myBannedRecipesAdapter?.selectedPositions?.clone() as ArrayList<Int>
                myBannedRecipesAdapter?.tempList =
                    myBannedRecipesAdapter?.selectedIds?.clone() as ArrayList<String>
                when (item?.itemId) {
                    android.R.id.home -> {
                        shouldResetRecyclerView = false
                        myBannedRecipesAdapter?.selectedIds?.clear()
                        for (i in myBannedRecipesAdapter?.selectedPositions!!) {
                            myBannedRecipesAdapter?.notifyItemChanged(i)
                        }
                        myBannedRecipesAdapter?.selectedPositions?.clear()
                        actionMode?.title = ""
                        actionMode?.finish()
                    }
                    R.id.clear -> {
                        myBannedRecipesAdapter?.doSomeAction("delete")
                        actionMode?.finish()
                    }
                }
            }
            7 -> {
                myBannedProductsAdapter?.tempPositions =
                    myBannedProductsAdapter?.selectedPositions?.clone() as ArrayList<Int>
                myBannedProductsAdapter?.tempList =
                    myBannedProductsAdapter?.selectedIds?.clone() as ArrayList<String>
                when (item?.itemId) {
                    android.R.id.home -> {
                        shouldResetRecyclerView = false
                        myBannedProductsAdapter?.selectedIds?.clear()
                        for (i in myBannedProductsAdapter?.selectedPositions!!) {
                            myBannedProductsAdapter?.notifyItemChanged(i)
                        }
                        myBannedProductsAdapter?.selectedPositions?.clear()
                        actionMode?.title = ""
                        actionMode?.finish()
                    }
                    R.id.clear -> {
                        myBannedProductsAdapter?.doSomeAction("delete")
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
            4 -> {
                mode?.menuInflater?.inflate(R.menu.action_menu_starred, menu)
            }
            5 -> {
                mode?.menuInflater?.inflate(R.menu.menu_with_only_delete_option, menu)
            }
            6 -> {
                mode?.menuInflater?.inflate(R.menu.menu_with_only_delete_option, menu)
            }
            7 -> {
                mode?.menuInflater?.inflate(R.menu.menu_with_only_delete_option, menu)
            }
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDestroyActionMode(mode: ActionMode?) {

        isMultiSelectOn = false
        actionMode = null
        shouldResetRecyclerView = true

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
            4 -> {
                if (shouldResetRecyclerView) {
                    myStarredProductsAdapter?.selectedIds?.clear()
                    for (i in myStarredProductsAdapter?.selectedPositions!!) {
                        myStarredProductsAdapter?.notifyItemChanged(i)
                    }
                    myStarredProductsAdapter?.selectedPositions?.clear()
                }
            }
            5 -> {
                if (shouldResetRecyclerView) {
                    myStarredRecipesAdapter?.selectedIds?.clear()
                    for (i in myStarredRecipesAdapter?.selectedPositions!!) {
                        myStarredRecipesAdapter?.notifyItemChanged(i)
                    }
                    myStarredRecipesAdapter?.selectedPositions?.clear()
                }
            }
            6 -> {
                if (shouldResetRecyclerView) {
                    myBannedRecipesAdapter?.selectedIds?.clear()
                    for (i in myBannedRecipesAdapter?.selectedPositions!!) {
                        myBannedRecipesAdapter?.notifyItemChanged(i)
                    }
                    myBannedRecipesAdapter?.selectedPositions?.clear()
                }
            }
            7 -> {
                if (shouldResetRecyclerView) {
                    myBannedProductsAdapter?.selectedIds?.clear()
                    for (i in myBannedProductsAdapter?.selectedPositions!!) {
                        myBannedProductsAdapter?.notifyItemChanged(i)
                    }
                    myBannedProductsAdapter?.selectedPositions?.clear()
                }
            }
        }

    }
}