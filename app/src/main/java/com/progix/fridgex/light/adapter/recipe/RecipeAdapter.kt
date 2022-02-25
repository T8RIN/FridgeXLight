package com.progix.fridgex.light.adapter.recipe

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.SecondActivity
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.fragment.recipe.IngredsFragment.Companion.currentStep
import com.progix.fridgex.light.fragment.recipe.RecipeFragment
import com.progix.fridgex.light.functions.Functions.delayedAction

class RecipeAdapter(var fragment: RecipeFragment, var recipeList: List<String>) :
    RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_actions, parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.prodName.text = recipeList[position]

        holder.container.visibility = GONE
        if (currentStep == position) {
            holder.container.visibility = VISIBLE
            setOnClickListener(holder, position)
            holder.itemView.alpha = 1f
        } else if (currentStep > position) {
            holder.itemView.alpha = 1f
        } else {
            holder.itemView.alpha = 0.2f
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setOnClickListener(holder: RecipeAdapter.ViewHolder, position: Int) {
        holder.nextButton.setOnClickListener {
            if (currentStep + 1 >= recipeList.size) {
                CustomSnackbar(fragment.requireContext()).create(
                    364,
                    (fragment.requireActivity() as SecondActivity).mainRoot,
                    fragment.requireContext().getString(R.string.bonAppetit),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(fragment.requireContext().getString(R.string.repeat)) {
                        currentStep = 0
                        fragment.recycler.animate().alpha(0f).setDuration(200L).withEndAction {
                            notifyDataSetChanged()
                            fragment.recycler.scrollToPosition(0)
                            fragment.recycler.animate().alpha(1f).setDuration(200L).start()
                        }.start()
                    }
                    .show()
            }

            holder.nextButton.setOnClickListener {}
            delayedAction(500) { setOnClickListener(holder, position) }

            currentStep++
            notifyItemChanged(position)
            notifyItemChanged(position + 1)

            (fragment.recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                currentStep,
                0
            )
        }
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val prodName: TextView = view.findViewById(R.id.name)
        val container: View = view.findViewById(R.id.buttonContainer)
        val nextButton: MaterialCardView = view.findViewById(R.id.nextButton)
    }

}