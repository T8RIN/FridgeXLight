package com.progix.fridgex.light.fragment.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialFadeThrough
import com.progix.fridgex.light.BuildConfig
import com.progix.fridgex.light.R
import com.progix.fridgex.light.adapter.settings.SettingsAdapter


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.anim_duration).toLong()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val settingsList: ArrayList<String> =
            ArrayList(
                listOf(
                    getString(R.string.nightMode),
                    getString(R.string.themeChooser),
                    getString(R.string.fontSize),
                    getString(R.string.cartSettings),
                    getString(R.string.guide)
                )
            )
        val recyclerView: RecyclerView = v.findViewById(R.id.settingsRecycler)

        val imageView: ImageView = v.findViewById(R.id.imageView2)
        imageView.setOnLongClickListener {
            Toast.makeText(requireContext(), getString(R.string.codeLines), Toast.LENGTH_SHORT)
                .show()
            true
        }

        view?.findViewById<TextView>(R.id.version)!!.text =
            "${getString(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        adapter = SettingsAdapter(requireContext(), settingsList)

        recyclerView.adapter = adapter
    }

    private var adapter: SettingsAdapter? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}