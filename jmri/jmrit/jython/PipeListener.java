// PipeListener.java

package jmri.jmrit.jython;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.io.*;

/**
 * Small service class to read characters from a pipe
 * and post them to a JTextArea for display
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.1 $
 */
 class PipeListener extends Thread {
    private PipedReader pr;
    private javax.swing.JTextArea ta;

    public PipeListener(PipedReader pr, javax.swing.JTextArea ta) {
        this.pr = pr;
        this.ta = ta;
    }

    public void run() {
        try {
            while (true) {
                ta.append(Character.toString((char)pr.read()));
            }
        } catch (IOException ex) {
            ta.append("PipeListener Exiting on IOException");
        }
    }
}

