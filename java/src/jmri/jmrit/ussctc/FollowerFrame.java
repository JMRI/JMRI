// FollowerFrame.java
package jmri.jmrit.ussctc;

import javax.swing.BoxLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface frame for creating and editing "Follower" logic on USS CTC
 * machines.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version	$Revision$
 */
public class FollowerFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 6090025917852821775L;

    public FollowerFrame() {
        super();
    }

    public void initComponents() throws Exception {
        addHelpMenu("package.jmri.jmrit.ussctc.FollowerFrame", true);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new FollowerPanel());
        setTitle(FollowerPanel.rb.getString("TitleFollower"));

        // pack to cause display
        pack();
    }

    private final static Logger log = LoggerFactory.getLogger(FollowerFrame.class.getName());

}
