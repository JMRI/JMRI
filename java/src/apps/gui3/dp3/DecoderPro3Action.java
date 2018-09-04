package apps.gui3.dp3;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * AbstractAction for the DP3 window so that further windows can be opened
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class DecoderPro3Action extends JmriAbstractAction {

    DecoderPro3Window mainFrame;
    boolean allowQuit = true;

    public DecoderPro3Action(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DecoderPro3Action(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Method for opening a new window via the classic JMRI interface
     *
     * @param pName     the action name
     * @param allowQuit true if closing the {@link DecoderPro3Window} should
     *                  quit the application; false otherwise
     */
    public DecoderPro3Action(String pName, boolean allowQuit) {
        super(pName);
        this.allowQuit = allowQuit;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        mainFrame = new DecoderPro3Window(DecoderPro3.getMenuFile(), DecoderPro3.getToolbarFile());
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
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
