package com.progix.fridgex.light.adapter.measures;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.progix.fridgex.light.R;
import com.progix.fridgex.light.model.MeasureItem;

import java.util.List;


public class MeasureAdapter extends RecyclerView.Adapter<MeasureAdapter.ViewHolder> {

    private Context context;
    private final List<MeasureItem> measuresList;

    public MeasureAdapter(Context context, List<MeasureItem> measuresList) {
        this.context = context;
        this.measuresList = measuresList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.measure_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.product.setText(measuresList.get(position).getProduct());
        holder.cup250.setText(measuresList.get(position).getCup250());
        holder.cup200.setText(measuresList.get(position).getCup200());
        holder.tbsp.setText(measuresList.get(position).getTbsp());
        holder.tsp.setText(measuresList.get(position).getTsp());
        holder.onepcs.setText(measuresList.get(position).getOnepcs());
    }

    @Override
    public int getItemCount() {
        return measuresList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView product;
        TextView cup250;
        TextView cup200;
        TextView tbsp;
        TextView tsp;
        TextView onepcs;

        public ViewHolder(@NonNull View view) {
            super(view);
            product = view.findViewById(R.id.product);
            cup250 = view.findViewById(R.id.cup250);
            cup200 = view.findViewById(R.id.cup200);
            tbsp = view.findViewById(R.id.tbsp);
            tsp = view.findViewById(R.id.tsp);
            onepcs = view.findViewById(R.id.onepcs);
        }
    }

}
