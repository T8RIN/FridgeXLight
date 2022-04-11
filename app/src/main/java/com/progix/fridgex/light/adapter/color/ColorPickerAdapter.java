package com.progix.fridgex.light.adapter.color;

import static com.progix.fridgex.light.data.DataArrays.colorNames;
import static com.progix.fridgex.light.data.SharedPreferencesAccess.loadTheme;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.progix.fridgex.light.R;
import com.progix.fridgex.light.data.DataArrays;
import com.progix.fridgex.light.helper.interfaces.SettingsInterface;

import java.util.List;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {

    Context context;
    DialogFragment fragment;
    List<Pair<Integer, Integer>> colorList;
    SettingsInterface colorInterface;

    private int selectedItemPos;
    private int lastItemSelectedPos;

    public ColorPickerAdapter(Context context, DialogFragment fragment, List<Pair<Integer, Integer>> colorList, SettingsInterface colorInterface) {
        this.context = context;
        this.fragment = fragment;
        this.colorList = colorList;
        this.colorInterface = colorInterface;
        selectedItemPos = DataArrays.colorListNames.indexOf(loadTheme(context));
        lastItemSelectedPos = selectedItemPos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.image.setStrokeColorResource(colorList.get(position).first);
        holder.image.setImageResource(colorList.get(position).second);
        holder.text.setText(colorNames.get(position));
        String amplifier;
        switch (position) {
            case 0:
                amplifier = "red";
                break;
            case 1:
                amplifier = "pnk";
                break;
            case 2:
                amplifier = "vlt";
                break;
            case 3:
                amplifier = "ble";
                break;
            case 4:
                amplifier = "mnt";
                break;
            case 5:
                amplifier = "grn";
                break;
            case 6:
                amplifier = "yel";
                break;
            default:
                amplifier = "def";
                break;
        }

        MaterialCardView card = (MaterialCardView) holder.itemView;

        card.setChecked(position == selectedItemPos);

        holder.itemView.setOnClickListener(v -> {
            colorInterface.onPickColor(amplifier);
            selectedItemPos = holder.getAbsoluteAdapterPosition();
            if (lastItemSelectedPos != -1) {
                notifyItemChanged(lastItemSelectedPos);
            }
            lastItemSelectedPos = selectedItemPos;
            notifyItemChanged(selectedItemPos);
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        //MaterialCardView  ;
        ShapeableImageView image;
        TextView text;
        //View  ;

        public ViewHolder(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.shapeImage);
            text = view.findViewById(R.id.text);
        }
    }
}
