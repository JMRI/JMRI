// ProgOpsModePane.java

package jmri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.Programmer;
import jmri.ProgListener;

/**
 * Provide a JPanel to configure the ops programming mode.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */
public class ProgOpsModePane extends javax.swing.JPanel {

    // GUI member declarations

    javax.swing.ButtonGroup mModeGroup;
    javax.swing.JRadioButton mOpsByteButton  	= new javax.swing.JRadioButton();
    javax.swing.JTextField mAddrField           = new javax.swing.JTextField(4);
    javax.swing.JCheckBox mLongAddrCheck        = new javax.swing.JCheckBox("Long address");


    public ProgOpsModePane(int direction) {
        this(direction, new javax.swing.ButtonGroup());
    }

    /**
     * @param direction
     * @param group allows grouping of buttons across panes
     */
    public ProgOpsModePane(int direction, javax.swing.ButtonGroup group) {

        // save the group to use
        mModeGroup = group;

        // configure items for GUI
        mOpsByteButton.setText("Ops Byte Mode");
        mModeGroup.add(mOpsByteButton);
        mAddrField.setToolTipText("Enter the decoder numeric address");
        mLongAddrCheck.setToolTipText("If checked, use a long address, otherwise use a short address");

        // if a programmer is available, disable buttons for unavailable modes
        if (InstanceManager.programmerManagerInstance()!=null) {
            ProgrammerManager p = InstanceManager.programmerManagerInstance();
            if (!p.isOpsModePossible()) mOpsByteButton.setEnabled(false);
        } else {
            log.warn("No programmer available, so modes not set");
        }

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // install items in GUI
        add(mOpsByteButton);
        add(mAddrField);
        add(mLongAddrCheck);
    }

    /**
     * Get a configured programmer
     */
    public Programmer getProgrammer() {
        if (InstanceManager.programmerManagerInstance()!=null) {
            int address = Integer.parseInt(mAddrField.getText());
            boolean longAddr = mLongAddrCheck.isSelected();
            log.debug("ops programmer for address "+address+" long long address "+longAddr);
            Programmer p = InstanceManager.programmerManagerInstance()
                                .getOpsModeProgrammer(longAddr, address);
            p.setMode(getMode());
            return p;
        }
        else {
            log.warn("request for ops mode programmer with no ProgrammerManager configured");
            return null;
        }
    }

    public boolean isSelected() {
        return mOpsByteButton.isSelected();
    }

    public int getMode() {
        if (mOpsByteButton.isSelected())
            return jmri.Programmer.OPSBYTEMODE;
        else
            return 0;
    }

    // set the programmer to the current mode
    private void setProgrammerMode(int mode) {
        log.debug("Setting programmer to mode "+mode);
        if (InstanceManager.programmerManagerInstance() != null
            && InstanceManager.programmerManagerInstance().getServiceModeProgrammer() != null)
            InstanceManager.programmerManagerInstance().getServiceModeProgrammer().setMode(mode);
    }

    /**
     * Done with this pane
     */
    public void dispose() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgOpsModePane.class.getName());

}
