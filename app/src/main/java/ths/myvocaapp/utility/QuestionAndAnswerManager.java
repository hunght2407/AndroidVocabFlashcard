package ths.myvocaapp.utility;

import android.app.Service;
import android.text.Html;
import android.text.Spanned;

import java.util.LinkedList;

import ths.myvocaapp.define.EVWord;
import ths.myvocaapp.service.FloatingViewService;
import ths.myvocaapp.storage.XmlHelper;

/**
 * Created by HungS7 on 1/26/2018.
 */

public class QuestionAndAnswerManager {
    final static public int DEF_QUESTION  = 0;
    final static public int DEF_ANSWER    = 1;
    final private Service fviewService;

    private int curStatus;

    private EVWord mVocabulary;
    private String strExampleWithQuestion;
    private String strExampleWithAnswer;

    private int wordListIndex;
    private int suggestIndex;

    private LinkedList<EVWord> __vocaList = new LinkedList<EVWord>();

    public QuestionAndAnswerManager(Service service) {
        mVocabulary = new EVWord();
        strExampleWithQuestion = "";
        strExampleWithAnswer = "";
        wordListIndex = 0;
        suggestIndex = 0;
        fviewService = service;

//        setNextWordByID(0);
    }

    public int setNextWord() {
        if ( getVocaList().size() == 0) {
            return wordListIndex;
        }
        else if (wordListIndex < getVocaList().size() - 1) {
            wordListIndex++;
        }
        else {
            wordListIndex = 0;
        }
        suggestIndex = 0;
        mVocabulary = getVocaList().get(wordListIndex);

        strExampleWithAnswer = mVocabulary.getExample();
        strExampleWithQuestion = mVocabulary.getExample();

        strExampleWithAnswer = strExampleWithAnswer.replace("_", "<font color='#006600'><i>\"" + mVocabulary.getOriginal() + "\"</i></font>");
        strExampleWithQuestion = strExampleWithQuestion.replace("_", "<font color='#990000'><i>\"" + mVocabulary.getVietnamese() + "\"</i></font>");

        return wordListIndex;
    }

    public int setNextWordByID(int id) {
        if ((id < 0) || (id > getVocaList().size() - 1)) {
            return 0;
        }

        wordListIndex = id;
        suggestIndex = 0;
        mVocabulary = getVocaList().get(wordListIndex);

        strExampleWithAnswer = mVocabulary.getExample();
        strExampleWithQuestion = mVocabulary.getExample();

        strExampleWithAnswer = strExampleWithAnswer.replace("_", "<font color='#006600'><i>\"" + mVocabulary.getOriginal() + "\"</i></font>");
        strExampleWithQuestion = strExampleWithQuestion.replace("_", "<font color='#990000'><i>\"" + mVocabulary.getVietnamese() + "\"</i></font>");

        return id;
    }

    public Spanned getVietnamese() {
        return Html.fromHtml("<span>" + mVocabulary.getVietnamese() + "</span>");
    }

    public Spanned getExample(int status) {
        curStatus = status;

        if (status == DEF_QUESTION) {
            return Html.fromHtml("<span>" + strExampleWithQuestion + "</span>");
        }
        else {
            return Html.fromHtml("<span>" + strExampleWithAnswer + "</span>");
        }
    }

    public Spanned getPhonetic() {
        return Html.fromHtml("<span>" + mVocabulary.getPhonetic() + "</span>");
    }

    public String getOriginal() {
        return mVocabulary.getOriginal();
    }

    public String getSuggestion() {
        if (curStatus == DEF_QUESTION) {
            String wordSuggestion = mVocabulary.getOriginal();
            if (suggestIndex < wordSuggestion.length()) {
                suggestIndex++;
            }

            return wordSuggestion.substring(0, suggestIndex);
        }

        return "";
    }

    public boolean isCorrectAnswer(String ans) {
        if (mVocabulary.getOriginal().equals(ans))
            return true;
        return false;
    }

    public LinkedList<EVWord> getVocaList() {
        if (__vocaList.size() <= 0) {
            XmlHelper.loadVocabulary_v2(fviewService, __vocaList);
        }

        return __vocaList;
    }

    public void clearVocaList() {
        __vocaList.clear();
    }
}
