package com.progix.fridgex.light.fragment.recipe;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.progix.fridgex.light.R;
import com.progix.fridgex.light.activity.MainActivity;
import com.progix.fridgex.light.activity.SecondActivity;
import com.progix.fridgex.light.adapter.recipe.RecipeAdapter;

import java.util.Arrays;
import java.util.List;

public class RecipeFragment extends Fragment {

    public RecipeFragment() {
        super(R.layout.fragment_recipe);
    }

    public RecyclerView recycler;

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        int id = SecondActivity.id;
        recycler = v.findViewById(R.id.recipeRecycler);

        Cursor cursor = MainActivity.mDb.rawQuery(
                "SELECT * FROM recipes WHERE id = ?", new String[]{Integer.toString(id)}
        );
        cursor.moveToFirst();

        List<String> list = Arrays.asList(cursor.getString(8).split("\n"));
        cursor.close();
        recycler.setAdapter(new RecipeAdapter(this, list));
    }

}