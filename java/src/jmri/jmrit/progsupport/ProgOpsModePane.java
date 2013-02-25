// ProgOpsModePane.java

package jmri.jmrit.progsupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import jmri.*;

import jmri.Programmer;

/**
 * Provide a JPanel to configure the ops programming mode.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision$
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
        mOpsByteButton.setText(Bundle.getMessage("OpsByteMode"));
        mModeGroup.add(mOpsByteButton);
        mAddrField.setToolTipText(Bundle.getMessage("ToolTipEnterDecoderAddress"));
        mLongAddrCheck.setToolTipText(Bundle.getMessage("ToolTipCheckedLongAddress"));

        // if a programmer is available, disable buttons for unavailable modes
        if (InstanceManager.programmerManagerInstance()!=null) {
            ProgrammerManager p = InstanceManager.programmerManagerInstance();
            if (!p.isAddressedModePossible()) mOpsByteButton.setEnabled(false);
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
		if (InstanceManager.programmerManagerInstance() != null) {
			int address;
			try {
				address = Integer.parseInt(mAddrField.getText());
			} catch (java.lang.NumberFormatException e) {
				log.error("loco address not correct");
				return null;
			}
			boolean longAddr = mLongAddrCheck.isSelected();
			log.debug("ops programmer for address " + address
					+ ", long address " + longAddr);
			Programmer p = InstanceManager.programmerManagerInstance()
					.getAddressedProgrammer(longAddr, address);
			p.setMode(getMode());
			return p;
		} else {
			log.warn("request for ops mode programmer with no ProgrammerManager configured");
			return null;
		}
	}

    public boolean isSelected() {
        return mOpsByteButton.isSelected();
    }

    private int getMode() {
        if (mOpsByteButton.isSelected())
            return jmri.Programmer.OPSBYTEMODE;
        else
            return 0;
    }

    // set the programmer to the current mode
    @SuppressWarnings("unused")
	private void setProgrammerMode(int mode) {
        log.debug("Setting programmer to mode "+mode);
        if (InstanceManager.programmerManagerInstance() != null
            && InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null)
            InstanceManager.programmerManagerInstance().getGlobalProgrammer().setMode(mode);
    }

    /**
     * Done with this pane
     */
    public void dispose() {
    }

    static Logger log = LoggerFactory.getLogger(ProgOpsModePane.class.getName());

}
