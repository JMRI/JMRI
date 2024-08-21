package jmri.util;

import java.io.IOException;
import java.io.PipedReader;
import java.util.Arrays;
import javax.swing.JTextArea;

/**
 * Small service class to read characters from a pipe and post them to a
 * JTextArea for display.
 *
 * This expects the pipe to remain open, so has no code to handle
 * a broken pipe gracefully.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2023
 */
public class PipeListener extends Thread {

    private final PipedReader pr;
    private final JTextArea ta;

    public PipeListener(PipedReader pr, javax.swing.JTextArea ta) {
        this.pr = pr;
        this.ta = ta;
    }

    static final int BUFFER_SIZE = 120;
    
    @Override
    public void run() {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            while (true) {
                try {
                    int nRead = pr.read(cbuf, 0, BUFFER_SIZE);  // blocking read
                    String content = new String(Arrays.copyOf(cbuf, nRead)); // retain only filled chars

                    // The following used to be runOnGui (i.e. not "Eventually")
                    // but that occasionally caused the Swing/AWT thread to block
                    // with very large input strings.  Please don't change it back.
                    jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
                        ta.append(content);
                    });

                } catch (IOException ex) {
                    if ( "Write end dead".equals(ex.getMessage()) || "Pipe broken".equals(ex.getMessage())) {
                        // happens when the writer thread, possibly a script, terminates
                        synchronized (this) {
                            try {
                                wait(500);
                            } catch (InterruptedException exi) {
                                Thread.currentThread().interrupt(); // retain if needed later
                            }
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        } catch (IOException ex) {
            ta.append("PipeListener Exiting on IOException:" + ex);
        }
    }
}
