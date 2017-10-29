package jmri.jmrit.progsupport;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JSeparator;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a JPanel to configure the programming mode.
 * <p>
 * The using code should get a configured programmer with getProgrammer.
 * <p>
 * This pane will only display ops mode options if ops mode is available, as
 * evidenced by an attempt to get an ops mode programmer at startup time.
 * <p>
 * For service mode, you can get the programmer either from the JPanel or direct
 * from the instance manager. For ops mode, you have to get it from here.
 * <p>
 * Note that you should call the dispose() method when you're really done, so
 * that a ProgModePane object can disconnect its listeners.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class ProgModePane extends ProgModeSelector {

    // GUI member declarations
    ProgOpsModePane mOpsPane;
    ProgServiceModePane mServicePane;
    ButtonGroup group = new ButtonGroup();

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or
     *                  BoxLayout.Y_AXIS
     */
    public ProgModePane(int direction) {

        if (log.isDebugEnabled()) {
            log.debug("AddressedProgrammerManager:");
            InstanceManager.getList(AddressedProgrammerManager.class).forEach((p) -> {
                log.debug("   " + p.toString());
            });
            log.debug("GlobalProgrammerManager:");
            InstanceManager.getList(GlobalProgrammerManager.class).forEach((p) -> {
                log.debug("   " + p.toString());
            });
        }

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        boolean addSep = false;

        // create the ops mode 1st, so the service is 2nd,
        // so it's the one that's selected
        mOpsPane = null;
        if (InstanceManager.getNullableDefault(AddressedProgrammerManager.class) != null
                && InstanceManager.getDefault(AddressedProgrammerManager.class).isAddressedModePossible()) {

            mOpsPane = new ProgOpsModePane(direction, group);
        }

        // service mode support, if present
        if (InstanceManager.getNullableDefault(GlobalProgrammerManager.class) != null) {

            mServicePane = new ProgServiceModePane(direction, group);
            add(mServicePane);
            addSep = true;
        }

        // ops mode support added if present
        if (mOpsPane != null) {

            if (addSep) {
                add(new JSeparator());
            }
            add(mOpsPane);
        }
    }

    /**
     * Default implementation of "isSelected" just returns true.
     *
     * @return Always true in this implementation
     */
    @Override
    public boolean isSelected() {
        return true;
    }

    /**
     * Get the configured programmer
     */
    @Override
    public Programmer getProgrammer() {
        if (mServicePane!=null && mServicePane.isSelected()) {
            return mServicePane.getProgrammer();
        } else if (mOpsPane != null && mOpsPane.isSelected()) {
            return mOpsPane.getProgrammer();
        } else {
            return null;
        }
    }

    @Override
    public void dispose() {
        if (mServicePane != null) {
            mServicePane.dispose();
        }
        mServicePane = null;
        if (mOpsPane != null) {
            mOpsPane.dispose();
        }
        mOpsPane = null;
    }

    private final static Logger log = LoggerFactory.getLogger(ProgModePane.class);

}
