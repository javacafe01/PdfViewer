/*
 * MIT License
 *
 * Copyright (c) 2018 Gokul Swaminathan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gsnathan.pdfviewer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import io.github.tonnyl.whatsnew.WhatsNew;
import io.github.tonnyl.whatsnew.item.WhatsNewItem;

public class Utils {

    static void showLog(AppCompatActivity context) {

        WhatsNew log = WhatsNew.newInstance(
                new WhatsNewItem("Translations", "Pdf Viewer Plus is now translated to German.", R.drawable.star_icon),
                new WhatsNewItem("F-Droid", "Pdf Viewer Plus is now on F-Droid!", R.drawable.star_icon),
                new WhatsNewItem("About Page", "The about page got a revamp.", R.drawable.thumbs_icon));
        log.setTitleColor(ContextCompat.getColor(context, R.color.colorAccent));
        log.setButtonBackground(ContextCompat.getColor(context, R.color.colorPrimary));
        log.setButtonTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        log.setItemTitleColor(ContextCompat.getColor(context, R.color.colorAccent));
        log.setItemContentColor(Color.parseColor("#808080"));

        log.show(context.getSupportFragmentManager(), "Log");
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    static Intent emailIntent(String emailAddress, String subject, String text, String title) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("text/email");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, text);
        return Intent.createChooser(email, title);
    }

    static Intent emailIntent(String subject, String text, String title, Uri filePath) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("text/email");
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, text);
        email.putExtra(Intent.EXTRA_STREAM, filePath);
        return Intent.createChooser(email, title);
    }

    static Intent linkIntent(String url) {
        Intent link = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        return link;
    }

    static Intent navIntent(Context context, Class activity) {
        Intent navigate = new Intent(context, activity);
        return navigate;
    }

    static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


}
