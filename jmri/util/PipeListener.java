// PipeListener.java

package jmri.util;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.io.*;

/**
 * Small service class to read characters from a pipe
 * and post them to a JTextArea for display
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.2 $
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
            char[] c = new char[1];
            while (true) {
                c[0] = (char)pr.read();
                ta.append(new String(c));  // odd way to do this, but only
                                            // way I could think of with only one
                                            // new object created
            }
        } catch (IOException ex) {
            ta.append("PipeListener Exiting on IOException");
        }
    }
}

