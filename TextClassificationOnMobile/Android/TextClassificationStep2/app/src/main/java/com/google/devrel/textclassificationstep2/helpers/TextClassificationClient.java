package com.google.devrel.textclassificationstep2.helpers;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.util.List;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier;

public class TextClassificationClient {
    private static final String MODEL_PATH = "model.tflite";
    private static final String TAG = "CommentSpam";
    private final Context context;

    NLClassifier classifier;

    public TextClassificationClient(Context context) {
        this.context = context;
    }

    public void load() {
        try {
            classifier = NLClassifier.createFromFile(context, MODEL_PATH);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void unload() {
        classifier.close();
        classifier = null;
    }

    public List<Category> classify(String text) {
        List<Category> apiResults = classifier.classify(text);
        return apiResults;
    }

}

