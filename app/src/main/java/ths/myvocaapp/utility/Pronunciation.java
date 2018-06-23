package ths.myvocaapp.utility;

import android.content.ContextWrapper;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import java.io.IOException;

/**
 * Created by HungS7 on 1/30/2018.
 */

public class Pronunciation {
    private static Pronunciation instance = null;
    private final MediaPlayer mPlayer;
    private Uri mUri;
    private final static String LONGMAN_PATH = "/Android/data/ths.myvocaapp/files/data/Longman/";
//    private final static String LONGMAN_PATH = "/Android/Longman/";

    private Pronunciation(){
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public static Pronunciation getInstance() {
        if (instance == null) {
            instance = new Pronunciation();
        }

        return instance;
    }

    public void start(ContextWrapper context, String word) {
        String prefix = word.substring(0, 1).toUpperCase() + "/";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + LONGMAN_PATH + prefix + word + ".mp3";

        mUri = Uri.parse(path);

        try {
            mPlayer.reset();
            mPlayer.setDataSource(context.getApplicationContext(), mUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
