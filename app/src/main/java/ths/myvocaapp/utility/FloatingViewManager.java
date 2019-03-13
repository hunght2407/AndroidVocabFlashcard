package ths.myvocaapp.utility;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import ths.myvocaapp.MainActivity;
import ths.myvocaapp.R;

/**
 * Created by HungS7 on 1/26/2018.
 */
public class FloatingViewManager {
    final private Service fviewService;
    final WindowManager.LayoutParams fviewParams;

    private WindowManager mWindowManager;
    private View vFloatingView;
    private View vCollapsedView;
    private View vExpandedView;

    private TextView txtVietnamese;
    private TextView txtExample;
    private TextView txtPhonetic;
    private ImageButton btnSpeak;
    private ImageButton btnCorrect;
    private EditText edtAnswer;

    private QuestionAndAnswerManager qaManager;

    private Timer timerExpandedView;
    private TimerTask timerTaskExpandedView;
    final Handler handlerTimer;
    final int     timerTaskCount = 600000; // 10 minutes

    private SharedPreferences _m_SharedPreferences;

    public FloatingViewManager(Service service) {
        fviewService = service;
        vFloatingView = LayoutInflater.from(fviewService).inflate(R.layout.layout_floating_view, null);

//        XmlHelper.loadVocabulary_v2(fviewService, __llWordList);

//        if (__llWordList.size() == 0) {
//            EVWord voca = new EVWord(
//                    "vocabulary",
//                    "/v?'k?bj?l?ri/",
//                    "t? v?ng",
//                    "Teachers were impressed by his _.");
//            __llWordList.add(voca);
//        }

        qaManager = new QuestionAndAnswerManager(service);

        //Add the view to the window.
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        fviewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        fviewParams.gravity = Gravity.TOP | Gravity.LEFT;
        fviewParams.x = 0;
        fviewParams.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) fviewService.getSystemService(fviewService.WINDOW_SERVICE);
        mWindowManager.addView(vFloatingView, fviewParams);

        //The root element of the collapsed view layout
        vCollapsedView = vFloatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
        vExpandedView = vFloatingView.findViewById(R.id.expanded_container);

        //Drag and move floating view using user's touch action.
        setTouchListenerOnFView();

        // --- Init buttom bar.
        Button btnSuggest = (Button) vFloatingView.findViewById(R.id.btnSuggest);
        btnSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSuggest();
            }
        });

        Button btnNext = (Button) vFloatingView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickNext();
            }
        });

        Button btnClose = (Button) vFloatingView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickClose();
            }
        });

        Button btnOpenApp = (Button) vFloatingView.findViewById(R.id.btnOpenApp);
        btnOpenApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickOpenApp();
            }
        });

        // --- init vietnamese label
        txtVietnamese =(TextView) vFloatingView.findViewById(R.id.txtVietnamese);
        txtVietnamese.setText(qaManager.getVietnamese());

        // --- init example label
        txtExample =(TextView) vFloatingView.findViewById(R.id.txtExample);
        txtExample.setText(qaManager.getExample(QuestionAndAnswerManager.DEF_QUESTION));

        // --- init phonetic label
        txtPhonetic = (TextView) vFloatingView.findViewById(R.id.txtPhonetic);
        txtPhonetic.setText(qaManager.getPhonetic());

        // --- init answer bar
        btnSpeak = (ImageButton) vFloatingView.findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSpeak();
            }
        });
        btnCorrect = (ImageButton) vFloatingView.findViewById(R.id.btnCorrect);
        btnCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCorrect();
            }
        });
        edtAnswer = (EditText) vFloatingView.findViewById(R.id.edtAnswer);

        // Timer task ----
        handlerTimer = new Handler();
        startTimerExpandedView();

        // Preference ----
        _m_SharedPreferences = fviewService.getSharedPreferences(MainActivity._g_PreferenceID,
                Context.MODE_PRIVATE);

        qaManager.setNextWordByID(getPrefsIndex());
    }

    private void initTimerTask() {
        timerTaskExpandedView = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handlerTimer.post(new Runnable() {
                    public void run() {
                        showExpandedView();
                    }
                });
            }
        };
    }

    private void startTimerExpandedView() {
        //set a new Timer
        timerExpandedView = new Timer();

        //initialize the TimerTask's job
        initTimerTask();

        //schedule the timer
        timerExpandedView.schedule(timerTaskExpandedView, timerTaskCount, timerTaskCount); //
    }

    public void stopTimerExpandedView() {
        //stop the timer, if it's not already null
        if (timerExpandedView != null) {
            timerExpandedView.cancel();
            timerExpandedView = null;
        }
    }

    private void setTouchListenerOnFView() {
        vFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //remember the initial position.
                        initialX = fviewParams.x;
                        initialY = fviewParams.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff < 3 && YDiff< 3 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 3 && Ydiff < 3) {
                            if (isViewCollapsed()) {
                                showExpandedView();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        fviewParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        fviewParams.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(vFloatingView, fviewParams);
                        return true;
                }
                return false;
            }
        });
    }

    private boolean isViewCollapsed() {
        return vFloatingView == null || vFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    private void showCollapsedView() {
        fviewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        vCollapsedView.setVisibility(View.VISIBLE);
        vExpandedView.setVisibility(View.GONE);
        mWindowManager.updateViewLayout(vFloatingView, fviewParams);
        edtAnswer.setText("");
        txtVietnamese.setText(qaManager.getVietnamese());
        txtExample.setText(qaManager.getExample(QuestionAndAnswerManager.DEF_QUESTION));
        txtPhonetic.setText(qaManager.getPhonetic());

        startTimerExpandedView();
    }

    private void showExpandedView() {
        fviewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        vCollapsedView.setVisibility(View.GONE);
        vExpandedView.setVisibility(View.VISIBLE);
        mWindowManager.updateViewLayout(vFloatingView, fviewParams);

        stopTimerExpandedView();

        qaManager.setNextWordByID(getPrefsIndex());
        edtAnswer.setText("");
        txtVietnamese.setText(qaManager.getVietnamese());
        txtExample.setText(qaManager.getExample(QuestionAndAnswerManager.DEF_QUESTION));
        txtPhonetic.setText(qaManager.getPhonetic());
    }

    public void removeFloatingView() {
        if (vFloatingView != null) mWindowManager.removeView(vFloatingView);
    }

    // --- onClick functions
    private void onClickSuggest() {
        edtAnswer.setText(qaManager.getSuggestion());
    }

    private void onClickClose() {
        showCollapsedView();
    }

    private void onClickNext() {
        int wordid;
        wordid = qaManager.setNextWord();

        edtAnswer.setText("");
        txtVietnamese.setText(qaManager.getVietnamese());
        txtExample.setText(qaManager.getExample(QuestionAndAnswerManager.DEF_QUESTION));
        txtPhonetic.setText(qaManager.getPhonetic());

        setPrefsIndex(wordid);
    }

    private void onClickOpenApp() {
        //Open the application  click.
        Intent intent = new Intent(fviewService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fviewService.startActivity(intent);
        showCollapsedView();
    }

    private void onClickSpeak() {
        Pronunciation.getInstance().start(fviewService, qaManager.getOriginal());
    }

    private void onClickCorrect() {
        if (qaManager.isCorrectAnswer(edtAnswer.getText().toString())) {
            txtExample.setText(qaManager.getExample(QuestionAndAnswerManager.DEF_ANSWER));
        }
    }

    // --- Preference ------
    private void setPrefsIndex(int idx) {
        SharedPreferences.Editor editor = _m_SharedPreferences.edit();
        editor.putInt(MainActivity._g_WordListIndex, idx);
        editor.commit();
    }

    private int getPrefsIndex() {
        if (_m_SharedPreferences.contains(MainActivity._g_WordListIndex)) {
            int prefsIndex = _m_SharedPreferences.getInt(MainActivity._g_WordListIndex, 0);
            return prefsIndex;
        }

        return 0;
    }

    public void clearVocaList() {
        qaManager.clearVocaList();
    }
}
