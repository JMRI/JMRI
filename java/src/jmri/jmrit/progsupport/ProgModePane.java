// ProgModePane.java

package jmri.jmrit.progsupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import jmri.*;

import jmri.Programmer;

/**
 * Provide a JPanel to configure the programming mode.
 * <P>
 * The using code should get a configured programmer with getProgrammer.
 * <P>
 * This pane will only display ops mode options if ops mode is available,
 * as evidenced by an attempt to get an ops mode programmer at startup time.
 * <P>
 * For service mode, you can get the programmer either from the JPanel
 * or direct from the instance manager.  For ops mode, you have to
 * get it from here.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision$
 */
public class ProgModePane extends ProgModeSelector {

    // GUI member declarations
    ProgOpsModePane     mOpsPane;
    ProgServiceModePane mServicePane;
    ButtonGroup group = new ButtonGroup();

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or BoxLayout.Y_AXIS
     */
    public ProgModePane(int direction) {

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // service mode support, always present
        mServicePane = new ProgServiceModePane(direction, group);
        add(mServicePane);

        // ops mode support
        mOpsPane = null;
        if (InstanceManager.programmerManagerInstance()!=null &&
            InstanceManager.programmerManagerInstance().isAddressedModePossible()) {

            add(new JSeparator());
            mOpsPane = new ProgOpsModePane(direction, group);
            add(mOpsPane);
        }
    }

    /**
     * Default implementation of "isSelected" just returns true.
     * @return Always true in this implementation
     */
    public boolean isSelected() { return true; }

    /**
     * Get the configured programmer
     */
    public Programmer getProgrammer() {
        if (InstanceManager.programmerManagerInstance()==null) {
            log.warn("request for programmer with no ProgrammerManager configured");
            return null;
        } else if (mServicePane.isSelected()) {
            return mServicePane.getProgrammer();
        } else if (mOpsPane!=null && mOpsPane.isSelected()) {
            return mOpsPane.getProgrammer();
        } else return null;
    }

    public void dispose() {
        if (mServicePane != null)
            mServicePane.dispose();
        mServicePane = null;
        if (mOpsPane != null)
            mOpsPane.dispose();
        mOpsPane = null;
    }

    public void setDefaultMode() {
        mServicePane.setButtonMode(Programmer.PAGEMODE);
    }

    static Logger log = LoggerFactory.getLogger(ProgModePane.class.getName());

}
