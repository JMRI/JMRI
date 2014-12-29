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
public class ProgOpsModePane extends JPanel {

    // GUI member declarations

    /**
	 * 
	 */
	private static final long serialVersionUID = 165989491869394147L;
	ButtonGroup mModeGroup;
    JRadioButton mOpsByteButton  	= new JRadioButton();
    JTextField mAddrField           = new JTextField(4);
    JCheckBox mLongAddrCheck        = new JCheckBox("Long address");
    JComboBox<AddressedProgrammerManager>   progBox;

    public ProgOpsModePane(int direction) {
        this(direction, new javax.swing.ButtonGroup());
    }

    /**
     * @param direction
     * @param group allows grouping of buttons across panes
     */
    public ProgOpsModePane(int direction, javax.swing.ButtonGroup group) {

        // create the display combo box
        java.util.Vector<AddressedProgrammerManager> v = new java.util.Vector<AddressedProgrammerManager>();
        for (AddressedProgrammerManager e : InstanceManager.getList(jmri.AddressedProgrammerManager.class))
            v.add(e);
        add(progBox = new JComboBox<AddressedProgrammerManager>(v));
        // if only one, don't show
        if (progBox.getItemCount()<2) progBox.setVisible(false);
        progBox.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new selection
                setModes(progBox.getItemAt(progBox.getSelectedIndex()));
            }
        });
        progBox.setSelectedIndex(progBox.getItemCount()-1); // default is last

        // save the group to use
        mModeGroup = group;

        // configure buttons
        mOpsByteButton.setText(Bundle.getMessage("OpsByteMode"));
        mModeGroup.add(mOpsByteButton);
        mAddrField.setToolTipText(Bundle.getMessage("ToolTipEnterDecoderAddress"));
        mLongAddrCheck.setToolTipText(Bundle.getMessage("ToolTipCheckedLongAddress"));

        // if a programmer is available, disable buttons for unavailable modes
        setModes(InstanceManager.getDefault(jmri.AddressedProgrammerManager.class));

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // install items in GUI
        add(mOpsByteButton);
        
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.FlowLayout());
        panel.add(new JLabel("Addr:"));
        panel.add(mAddrField);
        add(panel);
        add(mLongAddrCheck);

    }

    void setModes(AddressedProgrammerManager p) {
        if (p!=null) {
            if (!p.isAddressedModePossible()) mOpsByteButton.setEnabled(false);
        } else {
            log.warn("No programmer available, so modes not set");
        }
    }
    
    /**
     * Get a configured programmer
     */
    public Programmer getProgrammer() {
        AddressedProgrammerManager pm = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class);
		if (pm != null) {
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
			Programmer pr = pm.getAddressedProgrammer(longAddr, address);
			pr.setMode(getMode());
			return pr;
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


    /**
     * Done with this pane
     */
    public void dispose() {
    }

    static Logger log = LoggerFactory.getLogger(ProgOpsModePane.class.getName());

}
