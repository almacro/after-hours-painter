package org.example;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author almacro
 */
public class OkCancelDialog extends Dialog implements ActionListener {
    
    Button ok, cancel;
    TextField text;
    String data;
    
    OkCancelDialog(Frame hostFrame, String title, boolean dModal) {
        super(hostFrame, title, dModal);
        setSize(280,100);
        setLayout(new FlowLayout());
        text = new TextField(30);
        add(text);
        ok = new Button("OK");
        add(ok);
        ok.addActionListener(this);
        cancel = new Button("Cancel");
        add(cancel);
        cancel.addActionListener(this);
        data = "";
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == ok) {
            data = text.getText();
        } else {
            data = "";
        }
        setVisible(false);
    }
}