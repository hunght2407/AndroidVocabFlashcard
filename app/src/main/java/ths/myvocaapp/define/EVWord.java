package ths.myvocaapp.define;

/**
 * Created by HungS7 on 1/26/2018.
 */

public class EVWord {
    public final static String DF_ORIGINAL 		= "original";
    public final static String DF_PHONETIC 		= "phonetic";
    public final static String DF_VIETNAMESE 	= "vietnamese";
    public final static String DF_EXAMPLE 		= "example";
    public final static String DF_FLAG 			= "flag";

    private String original;
    private String phonetic;
    private String vietnamese;
    private String example;
    private boolean flag;

    public EVWord() {
        original 		= "";
        phonetic 		= "";
        vietnamese 		= "";
        example			= "";
        flag			= true;
    }

    public EVWord(String original, String phonetic, String vietnamese, String example) {
        this.original 	= original;
        this.phonetic 	= phonetic;
        this.vietnamese = vietnamese;
        this.example	= example;
        this.flag		= true;
    }

    public String getOriginal() {
        return original;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public String getVietnamese() {
        return vietnamese;
    }

    public String getExample() {
        return example;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public void setVietnamese(String vietnamese) {
        this.vietnamese = vietnamese;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}