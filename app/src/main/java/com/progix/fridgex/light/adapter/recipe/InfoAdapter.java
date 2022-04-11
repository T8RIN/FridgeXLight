package com.progix.fridgex.light.adapter.recipe;

import static android.provider.AlarmClock.ACTION_SET_TIMER;
import static android.provider.AlarmClock.EXTRA_LENGTH;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static android.provider.AlarmClock.EXTRA_SKIP_UI;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.progix.fridgex.light.activity.MainActivity.mDb;
import static com.progix.fridgex.light.activity.SecondActivity.name;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.progix.fridgex.light.R;
import com.progix.fridgex.light.activity.SecondActivity;
import com.progix.fridgex.light.model.InfoItem;

import java.util.ArrayList;
import java.util.Objects;


public class InfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<InfoItem> infoList;

    public InfoAdapter(Context context, ArrayList<InfoItem> infoList) {
        this.context = context;
        this.infoList = infoList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 7) return 2;
        else return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_info, parent, false)
            );
        } else {
            return new ViewHolder2(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_info_7, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderMain, int position) {
        if (holderMain.getItemViewType() == 1) {
            ViewHolder holder = (ViewHolder) holderMain;
            holder.itemView.setClickable(false);
            if (position == 0) {
                holder.itemView.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(context, R.style.modeAlert)
                            .setTitle(context.getString(R.string.timer))
                            .setMessage(context.getString(R.string.setTimer) + SecondActivity.name)
                            .setPositiveButton(context.getString(R.string.cont), (b, z) -> {
                                Intent intent = new Intent(ACTION_SET_TIMER);
                                intent.putExtra(
                                        EXTRA_LENGTH,
                                        Integer.parseInt(infoList.get(position).getValue().split(" ")[0]) * 60
                                );
                                intent.putExtra(EXTRA_SKIP_UI, false);
                                intent.putExtra(EXTRA_MESSAGE, SecondActivity.name);
                                context.startActivity(intent);
                            })
                            .setNegativeButton(context.getString(R.string.cancel), null)
                            .show();
                });
            }

            if (Objects.equals(infoList.get(position).getValue(), "0"))
                holder.itemView.setVisibility(GONE);
            holder.name.setText(infoList.get(position).getName());
            holder.value.setText(infoList.get(position).getValue());
            holder.image.setImageResource(infoList.get(position).getImage());
            if (Objects.equals(infoList.get(position).getName(), context.getString(R.string.source)) && !Objects.equals(infoList.get(position).getValue(), "Авторский")) {
                holder.itemView.setOnClickListener(v -> new MaterialAlertDialogBuilder(context, R.style.modeAlert)
                        .setTitle(context.getString(R.string.redirect))
                        .setMessage(context.getString(R.string.redirectMessage))
                        .setPositiveButton(context.getString(R.string.cont), (x, y) -> {
                            String url = "http://" + infoList.get(position).getValue();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            context.startActivity(browserIntent);
                        })
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show());
            } else if (Objects.equals(infoList.get(position).getValue(), "Авторский")) {
                holder.image.setImageResource(R.drawable.ic_round_edit_24);
            }
        } else {
            ViewHolder2 holder = (ViewHolder2) holderMain;
            holder.itemView.setClickable(false);

            if (position == 0) {
                holder.itemView.setOnClickListener(v -> new MaterialAlertDialogBuilder(context, R.style.modeAlert)
                        .setTitle(context.getString(R.string.timer))
                        .setMessage(context.getString(R.string.setTimer) + SecondActivity.name)
                        .setPositiveButton(context.getString(R.string.cont), (x, y) -> {
                            Intent intent = new Intent(ACTION_SET_TIMER);
                            intent.putExtra(
                                    EXTRA_LENGTH,
                                    Integer.parseInt(infoList.get(position).getValue().split(" ")[0]) * 60
                            );
                            intent.putExtra(EXTRA_SKIP_UI, false);
                            intent.putExtra(EXTRA_MESSAGE, SecondActivity.name);
                            context.startActivity(intent);
                        })
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show());
            }

            holder.name.setText(infoList.get(position).getName());
            holder.value.setText(infoList.get(position).getValue());
            holder.image.setImageResource(infoList.get(position).getImage());
            if (Objects.equals(infoList.get(position).getName(), context.getString(R.string.source)) && !Objects.equals(infoList.get(position).getValue(), "Авторский")) {
                holder.itemView.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(context, R.style.modeAlert)
                            .setTitle(context.getString(R.string.redirect))
                            .setMessage(context.getString(R.string.redirectMessage))
                            .setPositiveButton(
                                    context.getString(R.string.cont), (x, y) -> {
                                        String url = "http://" + infoList.get(position).getValue();
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        context.startActivity(browserIntent);
                                    })
                            .setNegativeButton(context.getString(R.string.cancel), null)
                            .show();
                });
            } else if (Objects.equals(infoList.get(position).getValue(), "Авторский")) {
                holder.image.setImageResource(R.drawable.ic_round_edit_24);
            }
            if (position == 7) {
                Cursor cursor = mDb.rawQuery("SELECT * FROM recipes WHERE id = ?", new String[]{infoList.get(position).getValue()});
                cursor.moveToFirst();
                boolean starred = cursor.getInt(7) == 1;
                boolean banned = cursor.getInt(14) == 1;
                if (starred && banned) {
                    holder.banLayout.setVisibility(VISIBLE);
                    holder.starLayout.setVisibility(VISIBLE);
                    holder.itemView.setVisibility(VISIBLE);
                    holder.value.setText(context.getString(R.string.inStarred));
                    holder.value2.setText(context.getString(R.string.inBanned));
                } else if (starred && !banned) {
                    holder.value.setText(context.getString(R.string.inStarred));
                    holder.banLayout.setVisibility(GONE);
                    holder.starLayout.setVisibility(VISIBLE);
                    holder.itemView.setVisibility(VISIBLE);
                } else if (!starred && banned) {
                    holder.starLayout.setVisibility(GONE);
                    holder.banLayout.setVisibility(VISIBLE);
                    holder.itemView.setVisibility(VISIBLE);
                    holder.value2.setText(context.getString(R.string.inBanned));
                } else if (!starred && !banned) holder.itemView.setVisibility(GONE);
                cursor.close();
            }
        }
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView value;
        ImageView image;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.name);
            value = view.findViewById(R.id.value);
            image = view.findViewById(R.id.image);
        }

    }

    public static class ViewHolder2 extends RecyclerView.ViewHolder {
        TextView name;
        TextView value;
        ImageView image;
        TextView value2;
        LinearLayout starLayout;
        LinearLayout banLayout;

        public ViewHolder2(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.name);
            value = view.findViewById(R.id.value);
            image = view.findViewById(R.id.star);
            value2 = view.findViewById(R.id.value2);
            starLayout = view.findViewById(R.id.star_layout);
            banLayout = view.findViewById(R.id.ban_layout);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.itemView.setOnClickListener(null);
    }

}