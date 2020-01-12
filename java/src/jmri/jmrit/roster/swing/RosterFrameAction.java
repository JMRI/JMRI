package jmri.jmrit.roster.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * AbstractAction for the RosterFrane so that multiple windows can be opened
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012
 */
public class RosterFrameAction extends JmriAbstractAction {

    public RosterFrameAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RosterFrameAction(String s, WindowInterface wi, boolean allowQuit) {
        super(s, wi);
        this.allowQuit = allowQuit;
    }

    public RosterFrameAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Default constructor used when instantiating via startup action or button
     * configured in user preferences
     */
    public RosterFrameAction() {
        super(Bundle.getMessage("RosterFrameAction")); // NOI18N
        allowQuit = false;
    }

    /**
     * Method for opening a new window via the classic JMRI interface
     */
    public RosterFrameAction(String pName, boolean allowQuit) {
        super(pName);
        this.allowQuit = allowQuit;
    }

    boolean allowQuit = true;

    @Override
    public void actionPerformed(ActionEvent event) {
        RosterFrame mainFrame = new RosterFrame();
        UserPreferencesManager p = InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (!p.hasProperties(mainFrame.getWindowFrameRef())) {
            mainFrame.setSize(new Dimension(1024, 600));
            mainFrame.setPreferredSize(new Dimension(1024, 600));
        }
        if (wi instanceof RosterGroupSelector) {
            mainFrame.setSelectedRosterGroup(((RosterGroupSelector) wi).getSelectedRosterGroup());
        }
        mainFrame.setVisible(true);
        mainFrame.setAllowQuit(allowQuit);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
