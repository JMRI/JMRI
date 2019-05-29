package jmri.jmrit.ussctc;

import javax.swing.BoxLayout;

/**
 * User interface frame for creating and editing "Follower" logic on USS CTC
 * machines.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class FollowerFrame extends jmri.util.JmriJFrame {

    public FollowerFrame() {
        super();
    }

    @Override
    public void initComponents() {
        addHelpMenu("package.jmri.jmrit.ussctc.FollowerFrame", true);  //NOI18N

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new FollowerPanel());
        setTitle(Bundle.getMessage("TitleFollower"));  //NOI18N

        // pack to cause display
        pack();
    }

}
