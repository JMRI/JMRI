package jmri.util.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * Creates a simple "indeterminate" busy spinner dialog...
 *
 * @author   Mark Underwood Copyright (C) 2011
 *
 */
public class BusyDialog extends JDialog {

    JFrame frame;
    JProgressBar pbar;

    public BusyDialog(JFrame frame, String title, boolean modal) {
        super(frame, title, modal);
        this.frame = frame;
        initComponents();
    }

    public void initComponents() {

        setLocationRelativeTo(frame);
        setPreferredSize(new Dimension(200, 100));
        setMinimumSize(new Dimension(200, 100));
        setLayout(new BorderLayout(10, 10));

        pbar = new JProgressBar();
        pbar.setIndeterminate(true);
        pbar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        //pbar.setBorderPainted(true);
        this.add(pbar, BorderLayout.CENTER);
    }

    public void start() {
        this.pack();
        this.setVisible(true);
        this.getContentPane().paintAll(pbar.getGraphics());
    }

    public void finish() {
        this.dispose();

    }

    // Unused, for now.  Commented out to avoid the compiler warning.
    //private static final Logger log = LoggerFactory.getLogger(VSDecoderPane.class);
}
