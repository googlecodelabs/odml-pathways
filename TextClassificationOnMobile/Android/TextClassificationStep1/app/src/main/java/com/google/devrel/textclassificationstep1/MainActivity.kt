package com.google.devrel.textclassificationstep1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    lateinit var txtInput: EditText
    lateinit var btnSendText: Button
    lateinit var txtOutput: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtInput = findViewById(R.id.txtInput)
        txtOutput = findViewById(R.id.txtOutput)
        btnSendText = findViewById(R.id.btnSendText)
        btnSendText.setOnClickListener {
            var toSend:String = txtInput.text.toString()
            txtOutput.text = toSend
        }
    }
}