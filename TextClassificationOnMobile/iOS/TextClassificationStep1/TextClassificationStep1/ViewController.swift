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

