package com.gsnathan.pdfviewer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

/**
 * Created by Gokul Swaminathan on 2/23/2018.
 */

public class PrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        HtmlTextView htmlTextView = (HtmlTextView) findViewById(R.id.html_text);

        // loads html from raw resource, i.e., a html file in res/raw/,
        // this allows translatable resource (e.g., res/raw-de/ for german)
        htmlTextView.setHtml(R.raw.privacy_policy, new HtmlHttpImageGetter(htmlTextView));
    }
}