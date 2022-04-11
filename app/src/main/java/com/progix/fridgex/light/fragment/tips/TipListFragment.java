package com.progix.fridgex.light.fragment.tips;

import static androidx.navigation.Navigation.findNavController;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.progix.fridgex.light.R;
import com.progix.fridgex.light.activity.MainActivity;
import com.progix.fridgex.light.adapter.tips.TipListAdapter;

import java.util.ArrayList;

public class TipListFragment extends Fragment {

    public TipListFragment() {
        super(R.layout.fragment_tip_list);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        MaterialFadeThrough t = new MaterialFadeThrough();
        t.setDuration(getResources().getInteger(R.integer.anim_duration));
        setEnterTransition(t);
        setExitTransition(t);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        RecyclerView recyclerView = v.findViewById(R.id.tipRecycler);
        ArrayList<Pair<Integer, String>> tipList = new ArrayList<>();
        Cursor cursor = MainActivity.mDb.rawQuery("SELECT * FROM advices", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            tipList.add(new Pair(cursor.getInt(0), cursor.getString(1)));
            cursor.moveToNext();
        }
        cursor.close();
        recyclerView.setAdapter(new TipListAdapter(requireContext(), tipList, findNavController(requireView())));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tips_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}