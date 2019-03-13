package ths.myvocaapp;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import ths.myvocaapp.define.AsyncResult;
import ths.myvocaapp.define.DownloadWebpageTask;
import ths.myvocaapp.define.EVWord;
import ths.myvocaapp.storage.XmlHelper;

/**
 * Created by HungS7 on 03/13/2019.
 */

public class SyncDriveActivity extends Activity {
    private TextView mOutputText;
    private Button btnDownload;
    private LinkedList<EVWord> llWordList;
    private static final String BUTTON_TEXT = "Download Vocabulary Sheet";
    private ContextWrapper context = this;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        llWordList = new LinkedList<EVWord>();

        btnDownload = new Button(this);
        btnDownload.setText(BUTTON_TEXT);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadWebpageTask(new AsyncResult() {
                    @Override
                    public void onResult(JSONObject object) {
                        processJson(object);
                    }
                }).execute("https://spreadsheets.google.com/tq?key=1wLzROI-ny6de_8ld3JZS5Ck7TLQowX6Rv4bNWMGN36s");
            }
        });
        activityLayout.addView(btnDownload);

        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Hit the \'" + BUTTON_TEXT +"\' button to download Vocabulary.");
        activityLayout.addView(mOutputText);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            btnDownload.setEnabled(true);
        } else {
            btnDownload.setEnabled(false);
        }

        setContentView(activityLayout);
    }

    private void processJson(JSONObject object) {
        try {
            JSONArray rows = object.getJSONArray("rows");
            String original, phonetic, vietnamese, example;

            llWordList.clear();

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                if (    !columns.isNull(0) &&
                        !columns.isNull(1) &&
                        !columns.isNull(2) &&
                        !columns.isNull(3)) {

                    original = columns.getJSONObject(0).getString("v");
                    phonetic = columns.getJSONObject(1).getString("v");
                    vietnamese = columns.getJSONObject(2).getString("v");
                    example = columns.getJSONObject(3).getString("v");

                    EVWord word = new EVWord(original, phonetic, vietnamese, example);
                    llWordList.add(word);
                }
            }

            XmlHelper.saveVocabulary_v2(context, llWordList);
            mOutputText.setText("Download successful.");

        } catch (JSONException e) {
            mOutputText.setText("Download failed.");
            e.printStackTrace();
        }
    }
}