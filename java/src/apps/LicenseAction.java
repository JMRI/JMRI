// LicenseAction.java

package apps;


import javax.swing.*;
import java.io.*;

import jmri.util.swing.*;

/**
 * Swing action to display the JMRI license
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004, 2010
 * @version         $Revision$
 */
public class LicenseAction extends jmri.util.swing.JmriAbstractAction {

    public LicenseAction() { super("License");}

    public LicenseAction(String s, Icon i, WindowInterface w) {
        super(s, i, w);
    }
    public LicenseAction(String s, WindowInterface w) {
        super(s, w);
    }

    public jmri.util.swing.JmriPanel makePanel() {
        jmri.util.swing.JmriPanel p = new JmriPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        
        JScrollPane jScrollPane = new JScrollPane();
        JTextPane textPane = new JTextPane();

        // get the file
        
        File file = new File("resources"+File.separator+"COPYING"); // NOI18N
        
        String t;
        
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader( new FileInputStream(file), "US-ASCII"));  // file stored as ASCII // NOI18N
            StringBuffer buf = new StringBuffer();
            while (r.ready()) {
                buf.append(r.readLine());
                buf.append("\n");
            }
            t = buf.toString();
            
            r.close();
        } catch (IOException ex) {
            t = "JMRI is distributed under a license. For license information, see the JMRI website http://jmri.sourceforge.net";
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

/* @(#)LicenseAction.java */
