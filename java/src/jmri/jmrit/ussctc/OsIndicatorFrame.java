package jmri.jmrit.ussctc;

import javax.swing.BoxLayout;

/**
 * User interface frame for creating and editing "OS Indicator" logic on USS CTC
 * machines.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class OsIndicatorFrame extends jmri.util.JmriJFrame {

    public OsIndicatorFrame() {
        super();
    }

    @Override
    public void initComponents() {
        addHelpMenu("package.jmri.jmrit.ussctc.OsIndicatorFrame", true);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new OsIndicatorPanel());
        setTitle(OsIndicatorPanel.rb.getString("TitleOsIndicator"));

        // pack to cause display
        pack();
    }

}
