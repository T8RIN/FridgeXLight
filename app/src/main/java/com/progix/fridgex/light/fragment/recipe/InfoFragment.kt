package com.progix.fridgex.light.fragment.recipe

import android.database.Cursor
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity.Companion.mDb
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.activity.SecondActivity.Companion.adapter
import com.progix.fridgex.light.adapter.recipe.InfoAdapter
import com.progix.fridgex.light.model.InfoItem

class InfoFragment : Fragment(R.layout.fragment_info) {

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
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
    }

}