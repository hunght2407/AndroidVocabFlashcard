package ths.myvocaapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.LinkedList;

import ths.myvocaapp.define.EVWord;
import ths.myvocaapp.service.FloatingViewService;
import ths.myvocaapp.storage.XmlHelper;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private SharedPreferences _m_SharedPreferences;
    public static final String _g_PreferenceID = "_g_PreferenceID";
    public static final String _g_WordListIndex = "_g_WordListIndex";
    private final MainActivity mSelf = this;

    private ListView wordListView;
    private WordListAdapter wordListAdapter;
    private LinkedList<EVWord> llWordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();

            _m_SharedPreferences = getSharedPreferences(_g_PreferenceID, Context.MODE_PRIVATE);

            llWordList = new LinkedList<EVWord>();

//        ouWordList.add(new EVWord("love","/lʌv/","yêu","I _ you, Tracy."));
//        ouWordList.add(new EVWord("suppose","/səˈpəʊz/ -v","giả sử, giả thiết","There are many reasons to _ that Shakespeare was familiar with the stories of medieval Italy."));
//        ouWordList.add(new EVWord("role","/rəʊl/ -n","vai, vai trò","the _ of diet in the prevention of disease."));
//
//        XmlHelper.saveVocabulary_v2(this, ouWordList);

            XmlHelper.loadVocabulary_v2(this, llWordList);

            wordListView = (ListView) findViewById(R.id.lvWordList);
            wordListAdapter = new WordListAdapter(mSelf, _m_SharedPreferences, R.layout.layout_word_item, llWordList);
            wordListView.setAdapter(wordListAdapter);
            wordListView.setSelection(wordListAdapter.getPrefsIndex());
        }

        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 1000);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        XmlHelper.loadVocabulary_v2(this, llWordList);
        wordListAdapter.notifyDataSetChanged();
        wordListView.setSelection(wordListAdapter.getPrefsIndex());
    }

    /**
     * Set and initialize the view elements.
     */
    private void initializeView() {
        findViewById(R.id.btnStartStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMyServiceRunning(FloatingViewService.class)) {
                    stopService(new Intent(MainActivity.this, FloatingViewService.class));
                    //finish();
                }
                else {
                    startService(new Intent(MainActivity.this, FloatingViewService.class));
                }
            }
        });

        findViewById(R.id.btnSyncDrive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mSelf, SyncDriveActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mSelf.startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // *** private classes ***
    private class WordListAdapter extends ArrayAdapter<EVWord> {
        private final LinkedList<EVWord> llWordList;
        private final Activity context;
        private final WordListAdapter mSelf;
        private final SharedPreferences _m_prefs;
        private int currentIndex = 0;

        public WordListAdapter(Activity context, SharedPreferences prefs, int resource, LinkedList<EVWord> objects) {
            super(context, resource, objects);
            this.context = context;
            this.llWordList = objects;
            this.mSelf = this;
            this._m_prefs = prefs;

            this.currentIndex = getPrefsIndex();

        }

        public class ViewHolder {
            protected int iIndex;
            protected TextView tvOriginal;
            protected TextView tvVietnamese;
            protected ImageButton btnMarked;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                ViewHolder viewHolder = new ViewHolder();
                view = context.getLayoutInflater().inflate(R.layout.layout_word_item, null);

                viewHolder.tvOriginal = (TextView) view.findViewById(R.id.txtOriginal);
                viewHolder.tvVietnamese = (TextView) view.findViewById(R.id.txtVietnamese);
                viewHolder.btnMarked = (ImageButton) view.findViewById(R.id.btnMarked);

                view.setTag(viewHolder);
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ViewHolder holder = (ViewHolder)view.getTag();
                        currentIndex = holder.iIndex;

                        setPrefsIndex(currentIndex);

                        mSelf.notifyDataSetChanged();

                        return true;
                    }
                });
            } else {
                view = convertView;
            }

            ViewHolder holder = (ViewHolder)view.getTag();

            holder.iIndex = position;
            holder.tvOriginal.setText(llWordList.get(position).getOriginal());
            holder.tvVietnamese.setText(llWordList.get(position).getVietnamese());
            holder.btnMarked.setVisibility(View.GONE);

            if (position == currentIndex) {
                holder.btnMarked.setVisibility(View.VISIBLE);
            }
            else {
                holder.btnMarked.setVisibility(View.GONE);
            }

            return view;
        }

        public void setPrefsIndex(int idx) {
            SharedPreferences.Editor editor = _m_SharedPreferences.edit();
            editor.putInt(MainActivity._g_WordListIndex, idx);
            editor.commit();
        }

        public int getPrefsIndex() {
            if (_m_SharedPreferences.contains(MainActivity._g_WordListIndex)) {
                int prefsIndex = _m_SharedPreferences.getInt(MainActivity._g_WordListIndex, 0);

                if (prefsIndex < llWordList.size())
                    return prefsIndex;
            }

            return 0;
        }
    }
}
