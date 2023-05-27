package jmri.util.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * Creates a simple counting progress bar.
 * <p>
 * After constructing one, call start() to display it.
 * Then call count(..) to update the progress count, and
 * finish() when the operation is done.
 *
 * @author   Mark Underwood Copyright (C) 2011
 * @author   Bob Jacobsen   Copyright (C) 2023
 *
 */
public class CountingBusyDialog extends JDialog {

    JFrame frame;
    JProgressBar pbar;
    int maxCount;

    public CountingBusyDialog(JFrame frame, String title, boolean modal, int maxCount) {
        super(frame, title, modal);
        this.frame = frame;
        this.maxCount = maxCount;
        initComponents();
    }

    public void initComponents() {

        setLocationRelativeTo(frame);
        setPreferredSize(new Dimension(200, 100));
        setMinimumSize(new Dimension(200, 100));
        setLayout(new BorderLayout(10, 10));

        pbar = new JProgressBar(0, maxCount);
        pbar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        //pbar.setBorderPainted(true);
        this.add(pbar, BorderLayout.CENTER);
    }

    public void start() {
        this.pack();
        this.setVisible(true);
        this.getContentPane().paintAll(pbar.getGraphics());
    }

    public void count(int now) {
        pbar.setValue(now);
    }

    public void finish() {
        this.dispose();

    }

    // Unused, for now.  Commented out to avoid the compiler warning.
    //private static final Logger log = LoggerFactory.getLogger(VSDecoderPane.class);
}
