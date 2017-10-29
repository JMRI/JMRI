package apps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import jmri.util.FileUtil;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to display the JMRI license
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2010
 */
public class LicenseAction extends jmri.util.swing.JmriAbstractAction {

    public LicenseAction() {
        super("License");
    }

    public LicenseAction(String s, Icon i, WindowInterface w) {
        super(s, i, w);
    }

    public LicenseAction(String s, WindowInterface w) {
        super(s, w);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        jmri.util.swing.JmriPanel p = new JmriPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        JScrollPane jScrollPane = new JScrollPane();
        JTextPane textPane = new JTextPane();

        // get the file
        InputStream is = FileUtil.findInputStream("resources/COPYING", FileUtil.Location.INSTALLED); // NOI18N

        String t;

        try (   InputStreamReader isr = new InputStreamReader(is, "US-ASCII");    // file stored as ASCII // NOI18N
                BufferedReader r = new BufferedReader(isr);
            ){
            StringBuilder buf = new StringBuilder();
            while (r.ready()) {
                buf.append(r.readLine());
                buf.append("\n");
            }
            t = buf.toString();
        } catch (IOException ex) {
            t = "JMRI is distributed under a license. For license information, see the JMRI website http://jmri.org";
        }
        textPane.setText(t);

        // set up display
        textPane.setEditable(false);
        jScrollPane.getViewport().add(textPane);
        p.add(jScrollPane);

        // start scrolled to top
        JScrollBar b = jScrollPane.getVerticalScrollBar();
        b.setValue(b.getMaximum());

        return p;
    }
}

