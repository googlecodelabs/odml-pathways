// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.mysoundclassification

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


class MainActivity : AppCompatActivity() {
    var TAG = "MainActivity"

    // TODO 1: define your model name
    //var modelPath = "my_birds_model.tflite"

    var probabilityThreshold: Float = 0.3f

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val REQUEST_RECORD_AUDIO = 1337
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)

        textView = findViewById<TextView>(R.id.output)
        val recorderSpecsTextView = findViewById<TextView>(R.id.textViewAudioRecorderSpecs)

        val classifier = AudioClassifier.createFromFile(this, modelPath)

        val tensor = classifier.createInputTensorAudio()

        val format = classifier.requiredTensorAudioFormat
        val recorderSpecs = "Number Of Channels: ${format.channels}\n" +
                "Sample Rate: ${format.sampleRate}"
        recorderSpecsTextView.text = recorderSpecs

        val record = classifier.createAudioRecord()
        record.startRecording()

        Timer().scheduleAtFixedRate(1, 500) {

            val numberOfSamples = tensor.load(record)
            val output = classifier.classify(tensor)

            // TODO 2: Check if it's a bird sound.
            //var filteredModelOutput = output[0].categories.filter {
            //    it.label.contains("Bird") && it.score > probabilityThreshold
            //}

            // TODO 3: given there's a bird sound, which one is it?
            //if (filteredModelOutput.isNotEmpty()) {
            //    Log.i("Yamnet", "bird sound detected!")
            //    filteredModelOutput = output[1].categories.filter {
            //        it.score > probabilityThreshold
            //    }
            //}

            val outputStr =
                filteredModelOutput.sortedBy { -it.score }
                    .joinToString(separator = "\n") { "${it.label} -> ${it.score} " }

            if (outputStr.isNotEmpty())
                runOnUiThread {
                    textView.text = outputStr
                }
        }
    }
}