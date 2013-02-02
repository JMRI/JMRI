// OsIndicatorFrame.java

package jmri.jmrit.ussctc;

import org.apache.log4j.Logger;
import javax.swing.*;

/**
 * User interface frame for creating and editing "OS Indicator" logic
 * on USS CTC machines.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision$
 */
public class OsIndicatorFrame extends jmri.util.JmriJFrame {

    public OsIndicatorFrame() {
        super();
    }

    public void initComponents() throws Exception {
        addHelpMenu("package.jmri.jmrit.ussctc.OsIndicatorFrame", true);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new OsIndicatorPanel());
        setTitle(OsIndicatorPanel.rb.getString("TitleOsIndicator"));
        
        // pack to cause display
        pack();
    }

    static Logger log = Logger.getLogger(OsIndicatorFrame.class.getName());

}
