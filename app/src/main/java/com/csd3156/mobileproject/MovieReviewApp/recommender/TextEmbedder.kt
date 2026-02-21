package com.csd3156.mobileproject.MovieReviewApp.recommender
import android.R.attr.text
import android.adservices.ondevicepersonalization.InferenceInput.Params.DELEGATE_CPU
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions
class TextEmbedder (val context : Context) {
    //Stores the text embedding model from google.
    private var textEmbedder : TextEmbedder;
    //Initializes the text embedding model.
    init{
        //Why does this not run but example demo can?
        val baseOptionsBuilder = BaseOptions.builder()
        baseOptionsBuilder.setModelAssetPath("universal_sentence_encoder.tflite");
        val baseOptions = baseOptionsBuilder.build()
        val optionsBuilder =
            TextEmbedderOptions.builder().setBaseOptions(baseOptions)
        val options = optionsBuilder.build()
        textEmbedder = TextEmbedder.createFromOptions(context, options)
    }

    //Uses the pre-trained text embedder to vectorize a string, and return it.
    suspend fun GetVectorFromText(text : String) : FloatArray
    {
        return textEmbedder.embed(text).embeddingResult().embeddings().first().floatEmbedding();
    }
}