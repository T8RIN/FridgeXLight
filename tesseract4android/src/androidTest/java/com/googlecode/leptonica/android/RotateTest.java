/*
 * Copyright (C) 2019 Adaptech s.r.o., Robert Pösel
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.leptonica.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RotateTest {
	@Test
	public void testRotate() {
		Bitmap bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();

		// Paint the background white
		canvas.drawColor(Color.WHITE);

		// Paint a black circle in the center
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		canvas.drawCircle(50, 50, 10, paint);

		Pix pixs = ReadFile.readBitmap(bmp);
		Pix pixd = Rotate.rotate(pixs, 180);
		pixs.recycle();

		Bitmap rotated = WriteFile.writeBitmap(pixd);
		pixd.recycle();

		assertNotNull(rotated);

		float match = TestUtils.compareBitmaps(bmp, rotated);
		bmp.recycle();
		rotated.recycle();

		assertTrue("Bitmaps do not match.", (match > 0.99f));
	}

	@Test
	public void testRotateOrth() {
		Bitmap bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();

		// Paint the background white
		canvas.drawColor(Color.WHITE);

		// Paint a black circle in the center
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		canvas.drawCircle(50, 50, 10, paint);

		Pix pixs = ReadFile.readBitmap(bmp);
		Pix pixd = Rotate.rotateOrth(pixs, 1);
		pixs.recycle();

		Bitmap rotated = WriteFile.writeBitmap(pixd);
		pixd.recycle();

		assertNotNull(rotated);

		float match = TestUtils.compareBitmaps(bmp, rotated);
		bmp.recycle();
		rotated.recycle();

		assertTrue("Bitmaps do not match.", (match > 0.99f));
	}

	@Test
	public void testRotateResize() {
		Bitmap bmp = Bitmap.createBitmap(100, 10, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();

		// Paint the background white
		canvas.drawColor(Color.BLACK);

		// Paint a black circle in the center
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		canvas.drawCircle(50, 50, 10, paint);

		Pix pixs = ReadFile.readBitmap(bmp);
		Pix pixd = Rotate.rotate(pixs, 180);
		pixs.recycle();
		bmp.recycle();

		assertTrue("Rotated width is not 100.", (pixd.getWidth() == 100));
		pixd.recycle();
	}
}