package com.progix.fridgex.light.fragment

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.activity.MainActivity.Companion.actionMode
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.adapter.cart.CartAdapter
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import com.progix.fridgex.light.helper.interfaces.ActionInterface
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CartFragment : Fragment(), ActionInterface {
    private var param1: String? = null
    private var param2: String? = null

    private val cartList: ArrayList<Pair<String, String>> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var dispose: Disposable? = null

    private lateinit var loading: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_cart, container, false)

        recycler = v.findViewById(R.id.fridgeRecycler)
        annotationCard = v.findViewById(R.id.annotationCard)
        loading = v.findViewById(R.id.loading)
        val swipeRefresh: SwipeRefreshLayout = v.findViewById(R.id.swipeRefresh)

        swipeRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.manualBackground
            )
        )
        swipeRefresh.setColorSchemeResources(R.color.checked, R.color.red, R.color.yellow)
        swipeRefresh.setOnRefreshListener {
            actionMode?.finish()
            dispose?.dispose()
            dispose = rxJava()
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.isNotEmpty()) {
                        adapter = CartAdapter(requireContext(), it)
                        adapter!!.init(forThis())
                        recycler.adapter = adapter
                    } else {
                        annotationCard.startAnimation(
                            loadAnimation(
                                requireContext(),
                                R.anim.item_animation_fall_down
                            )
                        )
                        annotationCard.visibility = View.VISIBLE
                        recycler.visibility = View.INVISIBLE
                    }
                    loading.visibility = View.GONE
                }
            swipeRefresh.isRefreshing = false

        }

        dispose?.dispose()
        dispose = rxJava()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.isNotEmpty()) {
                    adapter = CartAdapter(requireContext(), it)
                    adapter!!.init(forThis())
                    recycler.adapter = adapter
                } else {
                    annotationCard.startAnimation(
                        loadAnimation(
                            requireContext(),
                            R.anim.item_animation_fall_down
                        )
                    )
                    annotationCard.visibility = View.VISIBLE
                    recycler.visibility = View.INVISIBLE
                }
                loading.visibility = View.GONE
            }

        return v
    }


    private fun rxJava(): Observable<ArrayList<Pair<String, String>>> {
        return Observable.create { item ->
            cartList.clear()
            val cursor: Cursor =
                mDb.rawQuery("SELECT * FROM products WHERE is_in_cart = 1", null)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                cartList.add(Pair(cursor.getString(2), cursor.getString(1)))
                cursor.moveToNext()
            }
            cartList.sortBy { it.first }
            item.onNext(cartList)
            cursor.close()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        lateinit var recycler: RecyclerView
        lateinit var annotationCard: MaterialCardView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                if (recycler.adapter != null) {
                    MaterialAlertDialogBuilder(requireContext(), R.style.modeAlert)
                        .setTitle(getString(R.string.clearCart))
                        .setPositiveButton(
                            getString(R.string.cont)
                        ) { _, _ ->
                            val bottomNavigationView: BottomNavigationView =
                                requireActivity().findViewById(R.id.bottom_navigation)
                            val layoutParams =
                                bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
                            val behavior = layoutParams.behavior as HideBottomViewOnScrollBehavior
                            behavior.slideUp(bottomNavigationView)
                            CoroutineScope(Dispatchers.Main).launch {
                                annotationCard.startAnimation(
                                    loadAnimation(
                                        requireContext(),
                                        R.anim.item_animation_fall_down
                                    )
                                )
                                annotationCard.visibility = View.VISIBLE
                                recycler.visibility = View.INVISIBLE
                                recycler.adapter = null
                                undoOrDoActionCoroutine("do")
                            }

                            CustomSnackbar(requireContext())
                                .create(
                                    (context as MainActivity).findViewById(R.id.main_root),
                                    getString(R.string.clearSuccessCart),
                                    BaseTransientBottomBar.LENGTH_LONG
                                )
                                .setAction(getString(R.string.undo)) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        loading.visibility = View.VISIBLE
                                        annotationCard.visibility = View.GONE
                                        undoOrDoActionCoroutine("undo")
                                        recycler.visibility = View.VISIBLE
                                        adapter = CartAdapter(requireContext(), cartList)
                                        adapter!!.init(forThis())
                                        recycler.adapter = adapter
                                        behavior.slideUp(bottomNavigationView)
                                        loading.visibility = View.GONE
                                    }
                                }
                                .show()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.clearErrorCart),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            R.id.share -> {
                var sharing = ""

                sharing += getString(R.string.shopping)
                sharing += "\n\n"
                sharing += getString(R.string.buy)
                sharing += "\n"
                for (i in cartList) {
                    sharing += i.first.replaceFirstChar { it.titlecase() }
                    sharing += "\n"
                }

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    sharing + "\n" + getString(R.string.copiedFrom)
                )

                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share)))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun undoOrDoActionCoroutine(param: String) = withContext(Dispatchers.IO) {
        when (param) {
            "do" -> {
                val cursor: Cursor =
                    mDb.rawQuery("SELECT * FROM products WHERE is_in_cart = 1", null)
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val id = cursor.getInt(0)
                    if (cursor.getInt(5) == 1) crossList.add(id)
                    mDb.execSQL("UPDATE products SET is_in_cart = 0 WHERE id = $id")
                    mDb.execSQL("UPDATE products SET amount = 0 WHERE id = $id")
                    cursor.moveToNext()
                }
                cursor.close()
            }
            "undo" -> {
                for (i in cartList) {
                    mDb.execSQL(
                        "UPDATE products SET is_in_cart = 1 WHERE product = ?",
                        listOf(i.first).toTypedArray()
                    )
                }
                for (i in crossList) {
                    mDb.execSQL("UPDATE products SET amount = 1 WHERE id = $i")
                }
            }
        }
    }

    private val crossList: ArrayList<Int> = ArrayList()

    override fun onSelectedItemsCountChanged(count: Int) {
        val callback = ActionModeCallback()
        callback.init(adapter!!, R.id.nav_cart)
        if (actionMode == null) actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) actionMode?.title = "$count"
        else actionMode?.finish()
    }

    private fun forThis(): CartFragment {
        return this
    }

    private var adapter: CartAdapter? = null
}