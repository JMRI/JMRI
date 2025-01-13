package jmri.jmrix.openlcb.swing.lccpro;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * AbstractAction for the LccProFrane so that multiple windows can be opened
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012
 * @author Bob Jacobsen  Copyright (C) 2024
 */
public class LccProFrameAction extends JmriAbstractAction {

    public LccProFrameAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public LccProFrameAction(String s, WindowInterface wi, boolean allowQuit) {
        super(s, wi);
        this.allowQuit = allowQuit;
        checkAndSetEnabled();
    }

    public LccProFrameAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        checkAndSetEnabled();
    }

    /**
     * Default constructor used when instantiating via startup action or button
     * configured in user preferences
     */
    public LccProFrameAction() {
        super(Bundle.getMessage("LccProFrameAction")); // NOI18N
        allowQuit = false;
        checkAndSetEnabled();
    }

    /**
     * Method for opening a new window via the classic JMRI interface.
     * @param pName action name.
     * @param allowQuit Set state to either close JMRI or just the roster window.
     */
    public LccProFrameAction(String pName, boolean allowQuit) {
        super(pName);
        this.allowQuit = allowQuit;
        checkAndSetEnabled();
    }

    boolean allowQuit = true;

    void checkAndSetEnabled() {
        // if there's no connection, disable this
        var memo = jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class);
        setEnabled(memo != null);
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        var mainFrame = new LccProFrame("LCC Pro");
        UserPreferencesManager p = InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (!p.hasProperties(mainFrame.getWindowFrameRef())) {
            mainFrame.setSize(new Dimension(1024, 600));
            mainFrame.setPreferredSize(new Dimension(1024, 600));
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
