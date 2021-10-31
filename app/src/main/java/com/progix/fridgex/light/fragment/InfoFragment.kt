package com.progix.fridgex.light.fragment

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.SecondActivity
import com.progix.fridgex.light.SecondActivity.Companion.adapter
import com.progix.fridgex.light.adapter.InfoAdapter
import com.progix.fridgex.light.model.InfoItem

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class InfoFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)

        val id = SecondActivity.id
        val recycler = v.findViewById<RecyclerView>(R.id.infoRecycler)

        val data: ArrayList<InfoItem> = ArrayList()
        val cursorData: Cursor = mDb.rawQuery(
            "SELECT * FROM recipes WHERE id = ?",
            listOf(id.toString()).toTypedArray()
        )
        cursorData.moveToFirst()
        data.add(
            InfoItem(
                getString(R.string.cookTime),
                cursorData.getString(6) + " " + getString(R.string.minutes),
                R.drawable.ic_baseline_av_timer_24
            )
        ) //0 время
        data.add(
            InfoItem(
                getString(R.string.cal),
                cursorData.getString(10) + " " + getString(R.string.calories),
                R.drawable.ic_calories_24
            )
        ) //1 калории
        data.add(
            InfoItem(
                getString(R.string.proteins),
                cursorData.getString(11) + " " + getString(R.string.gram),
                R.drawable.ic_proteins_24
            )
        ) //2 белки
        data.add(
            InfoItem(
                getString(R.string.fats),
                cursorData.getString(12) + " " + getString(R.string.gram),
                R.drawable.ic_fats_24
            )
        ) //3 жиры
        data.add(
            InfoItem(
                getString(R.string.carbohydrates),
                cursorData.getString(13) + " " + getString(R.string.gram),
                R.drawable.ic_carbohydrates_24
            )
        ) //4 углеводы
        data.add(
            InfoItem(
                getString(R.string.categories),
                cursorData.getString(2),
                R.drawable.ic_baseline_folder_24
            )
        ) //5 категория
        data.add(
            InfoItem(
                getString(R.string.source),
                cursorData.getString(9),
                R.drawable.ic_baseline_link_24
            )
        ) //6 ссылка
        data.add(
            InfoItem(
                getString(R.string.rating),
                id.toString(),
                R.drawable.ic_round_star_24
            ) //оценка пользователя
        )
        cursorData.close()

        adapter = InfoAdapter(requireContext(), data)

        recycler.adapter = adapter


        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}