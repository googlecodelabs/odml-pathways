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

//
//  ViewController.swift
//  TextClassificationStep1
//
//  Created by Laurence Moroney on 3/19/21.
//

import UIKit

class ViewController: UIViewController, UITextViewDelegate {
    @IBOutlet weak var txtInput: UITextView!
    @IBOutlet weak var txtOutput: UILabel!
    @IBAction func btnSendText(_ sender: Any) {
        txtOutput.text = "Sent :" + txtInput.text
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        txtInput.delegate = self
        // Do any additional setup after loading the view.
    }
    
    // hides text views
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if (text == "\n") {
            textView.resignFirstResponder()
            return false
        }
        return true
    }

}

