package jmri.jmrit.revhistory.swing;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.jmrit.revhistory.FileHistory;
import jmri.util.JmriJFrame;

/**
 * Swing action to display the file revision history
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class FileHistoryAction extends AbstractAction {

    public FileHistoryAction(String s) {
        super(s);
    }

    public FileHistoryAction() {
        this("File History");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame frame = new JmriJFrame() {
        };  // JmriJFrame to ensure fits on screen

        JTextArea pane = new JTextArea();
        pane.append("\n"); // add a little space at top
        pane.setEditable(false);

        JScrollPane scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);

        FileHistory r = InstanceManager.getNullableDefault(FileHistory.class);
        if (r == null) {
            pane.append("<No History Found>\n");
        } else {
            pane.append(r.toString());
        }

        pane.append("\n"); // add a little space at bottom

        frame.pack();

        // start scrolled to top
        JScrollBar b = scroll.getVerticalScrollBar();
        b.setValue(b.getMaximum());

        // show
        frame.setVisible(true);
    }
}
