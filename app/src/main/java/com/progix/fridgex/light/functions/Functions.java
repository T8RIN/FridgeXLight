package com.progix.fridgex.light.functions;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.progix.fridgex.light.data.DataArrays;
import com.progix.fridgex.light.model.RecipeItem;
import com.progix.fridgex.light.model.RecyclerSortItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public final class Functions {

    public static int strToInt(String txt) {
        int d = 256;
        int ida = 0;
        int cnt = 0;
        for (int i = 0; i < txt.length(); i++) {
            ida += Character.codePointAt(new char[]{txt.charAt(i)}, 0);
            cnt++;
        }
        if (ida < 200) {
            ida += d * 2.8;
        } else if (ida < 1000) {
            ida += 1000;
        }
        return ida * cnt;
    }

    public static void saveToInternalStorage(Context applicationContext, Bitmap bitmapImage, String name) {
        ContextWrapper cw = new ContextWrapper(applicationContext);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File path = new File(directory, name);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap loadImageFromStorage(Context context, String name) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        try {
            File f = new File(directory, name);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addItemToList(
            Integer id,
            ArrayList<RecyclerSortItem> pairList,
            Double percentage,
            Integer time,
            Double cal,
            Double prot,
            Double fats,
            Double carboh,
            Integer indicator,
            String name,
            String xOfY
    ) {
        if (id + 1 <= DataArrays.recipeImages.size()) {
            pairList.add(
                    new RecyclerSortItem(
                            percentage, time, cal, prot, fats, carboh,
                            new RecipeItem(
                                    DataArrays.recipeImages.get(id),
                                    indicator,
                                    name,
                                    time.toString(),
                                    xOfY
                            )
                    )
            );
        } else {
            pairList.add(
                    new RecyclerSortItem(
                            percentage, time, cal, prot, fats, carboh,
                            new RecipeItem(
                                    -1,
                                    indicator,
                                    name,
                                    time.toString(),
                                    xOfY
                            )
                    )
            );
        }
    }

    public static int search(String pat, String txt) {
        int q = 101, h = 1, i, j, p = 0, t = 0, d = 256;
        int M = pat.length(), N = txt.length();
        if (M <= N) {
            for (i = 0; i < M - 1; ++i)
                h = (h * d) % q;
            for (i = 0; i < M; ++i) {
                p = (d * p + pat.charAt(i)) % q;
                t = (d * t + txt.charAt(i)) % q;
            }
            for (i = 0; i <= N - M; ++i) {
                if (p == t) {
                    for (j = 0; j < M; ++j) {
                        if (txt.charAt(i + j) != pat.charAt(j))
                            break;
                    }
                    if (j == M)
                        return i;
                }
                if (i < N - M) {
                    t = (d * (t - txt.charAt(i) * h) + txt.charAt(i + M)) % q;
                    if (t < 0)
                        t = (t + q);
                }
            }
        } else return q;
        return q;
    }

}