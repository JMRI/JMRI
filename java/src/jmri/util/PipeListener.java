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

    private long lastTime = System.currentTimeMillis();

    public long time() {
        long result = System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();
        return result;
    }

    @Override
    public void run() {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            while (true) {
                try {
                    System.out.format("%d: Read%n", time());
                    int nRead = pr.read(cbuf, 0, BUFFER_SIZE);  // blocking read
                    System.out.format("%d: Read: %d%n", time(), nRead);
                    String content = new String(Arrays.copyOf(cbuf, nRead)); // retain only filled chars
                    System.out.format("%d: Read: %d, %s%n", time(), nRead, content);

                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        System.out.format("%d: Read.append: start%n", time());
                        ta.append(content);
                        System.out.format("%d: Read.append: end%n", time());
                    });

                } catch (IOException ex) {
                    ex.printStackTrace();
                    if (ex.getMessage().equals("Write end dead") || ex.getMessage().equals("Pipe broken")) {
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
            ex.printStackTrace();
            ta.append("PipeListener Exiting on IOException:" + ex);
        }
    }
}
