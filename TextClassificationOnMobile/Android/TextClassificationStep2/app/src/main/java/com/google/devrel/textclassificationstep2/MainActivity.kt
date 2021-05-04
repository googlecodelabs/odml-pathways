package com.google.devrel.textclassificationstep2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.devrel.textclassificationstep2.helpers.TextClassificationClient
import org.tensorflow.lite.support.label.Category

class MainActivity : AppCompatActivity() {
    lateinit var txtInput: EditText
    lateinit var btnSendText: Button
    lateinit var txtOutput: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val client = TextClassificationClient(applicationContext)
        client.load()
        txtInput = findViewById(R.id.txtInput)
        txtOutput = findViewById(R.id.txtOutput)
        btnSendText = findViewById(R.id.btnSendText)
        btnSendText.setOnClickListener {
            var toSend:String = txtInput.text.toString()
            var results:List<Category> = client.classify(toSend)
            val score = results[1].score
            if(score>0.8){
                txtOutput.text = "Your message was detected as spam with a score of " + score.toString() + " and not sent!"
            } else {
                txtOutput.text = "Message sent! \nSpam score was:" + score.toString()
            }
            //txtInput.text.clear()
        }

    }
}