package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.CartAdapter
import com.progix.fridgex.light.adapter.FridgeAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CartFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val cartList: ArrayList<Pair<String, String>> = ArrayList()
    private lateinit var recycler: RecyclerView
    private lateinit var annotationCard: MaterialCardView

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

    var dispose: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_cart, container, false)

        recycler = v.findViewById(R.id.fridgeRecycler)
        annotationCard = v.findViewById(R.id.annotationCard)
        val loading: CircularProgressIndicator = v.findViewById(R.id.loading)

//        CoroutineScope(Dispatchers.Main).launch{
//
//            val it = suspend()
//            if (it.isNotEmpty()) {
//                recycler.adapter = CartAdapter(requireContext(), it)
//            } else {
//                annotationCard.visibility = View.VISIBLE
//                recycler.visibility = View.INVISIBLE
//            }
//            loading.visibility = View.INVISIBLE
//        }

        dispose?.dispose()
        dispose = rxJava()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                if (it.isNotEmpty()) {
                    recycler.adapter = CartAdapter(requireContext(), it)
                } else {
                    annotationCard.visibility = View.VISIBLE
                    recycler.visibility = View.INVISIBLE
                }
                loading.visibility = View.GONE
            }

        return v
    }

//    private suspend fun suspend(): ArrayList<Pair<String, String>> = withContext(Dispatchers.IO){
//        cartList.clear()
//        val cursor: Cursor = MainActivity.mDb.rawQuery("SELECT * FROM products WHERE is_in_cart = 1", null)
//        cursor.moveToFirst()
//
//        while (!cursor.isAfterLast) {
//            cartList.add(Pair(cursor.getString(2), cursor.getString(1)))
//            cursor.moveToNext()
//        }
//        cartList.sortBy { it.first }
//        return@withContext cartList
//    }

    private fun rxJava() : Observable<ArrayList<Pair<String, String>>> {
        return Observable.create { item ->
            cartList.clear()
            val cursor: Cursor = MainActivity.mDb.rawQuery("SELECT * FROM products WHERE is_in_cart = 1", null)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                cartList.add(Pair(cursor.getString(2), cursor.getString(1)))
                cursor.moveToNext()
            }
            cartList.sortBy { it.first }
            item.onNext(cartList)
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }
}