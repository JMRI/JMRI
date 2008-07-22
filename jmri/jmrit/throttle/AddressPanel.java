package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.File;

import jmri.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.nce.consist.NceConsistRoster;
import jmri.jmrix.nce.consist.NceConsistRosterEntry;

import org.jdom.Element;

/**
 * A JInternalFrame that provides a way for the user to enter a decoder address.
 * This class also store AddressListeners and notifies them when the user enters
 * a new address.
 * 
 * @author glen Copyright (C) 2002
 * @author Daniel Boudreau Copyright (C) 2008 (add consist feature)
 * @version $Revision: 1.30 $
 */
public class AddressPanel extends JInternalFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.throttle.ThrottleBundle");
	private DccThrottle throttle;
	private DccThrottle consistThrottle;

	private DccLocoAddressSelector addrSelector = new DccLocoAddressSelector();
	private DccLocoAddress currentAddress;
	private DccLocoAddress consistAddress;
	private ArrayList listeners;

	private JButton releaseButton;
	private JButton dispatchButton;
	private JButton progButton;
	private JButton setButton;
	private JComboBox rosterBox;
	private JComboBox conRosterBox;

	/**
	 * Constructor
	 */
	public AddressPanel() {
		initGUI();
	}

	public void destroy() {
		if (throttle != null)
			throttle.release();
		if (consistThrottle != null)
			consistThrottle.release();
	}

	/**
	 * Add an AddressListener. AddressListeners are notified when the user
	 * selects a new address.
	 * 
	 * @param l
	 */
	public void addAddressListener(AddressListener l) {
		if (listeners == null) {
			listeners = new ArrayList(2);
		}
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * Get notification that a throttle has been found as we requested.
	 * 
	 * @param t
	 *            An instantiation of the DccThrottle with the address
	 *            requested.
	 */
	public void notifyThrottleFound(DccThrottle t) {
		this.throttle = t;
		releaseButton.setEnabled(true);
		currentAddress = (DccLocoAddress) t.getLocoAddress();
		addrSelector.setAddress(currentAddress);

		if (InstanceManager.throttleManagerInstance().hasDispatchFunction()) {
			dispatchButton.setEnabled(true);
		}

		setButton.setEnabled(false);
		addrSelector.setEnabled(false);
		rosterBox.setEnabled(false);
		conRosterBox.setEnabled(false);
	}

	/**
	 * Get notification that a consist throttle has been found as we requested.
	 * 
	 * @param t
	 *            An instantiation of the DccThrottle with the address
	 *            requested.
	 */
	public void notifyConsistThrottleFound(DccThrottle t) {
		this.consistThrottle = t;
	}

	/**
	 * Receive notification that an address has been release or dispatched.
	 */
	public void notifyThrottleDisposed() {
		dispatchButton.setEnabled(false);
		releaseButton.setEnabled(false);
		progButton.setEnabled(false);
		setButton.setEnabled(true);
		addrSelector.setEnabled(true);
		rosterBox.setEnabled(true);
		conRosterBox.setEnabled(true);
		throttle = null;
	}

	/**
	 * Create, initialize and place the GUI objects.
	 */
	private void initGUI() {
		JPanel mainPanel = new JPanel();
		this.setContentPane(mainPanel);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;

		constraints.ipadx = constraints.ipady = -16;
		addrSelector.setVariableSize(true);
		mainPanel.add(addrSelector.getCombinedJPanel(), constraints);

		setButton = new JButton(rb.getString("ButtonSet"));
		constraints.gridx = GridBagConstraints.RELATIVE;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0;
		constraints.ipadx = constraints.ipady = 0;
		mainPanel.add(setButton, constraints);

		setButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consistAddress = null;
				changeOfAddress();
			}
		});

		rosterBox = Roster.instance().fullRosterComboBox();
		rosterBox.insertItemAt(new NullComboBoxItem(), 0);
		rosterBox.setSelectedIndex(0);
		rosterBox.setToolTipText(rb.getString("SelectLocoFromRosterTT"));
		rosterBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rosterItemSelected();
			}
		});
		constraints.gridx = 0;
		constraints.gridy = GridBagConstraints.RELATIVE;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 0;
		mainPanel.add(rosterBox, constraints);

		conRosterBox = NceConsistRoster.instance().fullRosterComboBox();
		if (NceConsistRoster.instance().numEntries() > 0) {
			conRosterBox.insertItemAt(new NullComboBoxConsist(), 0);
			conRosterBox.setSelectedIndex(0);
			conRosterBox.setToolTipText(rb.getString("SelectConsistFromRosterTT"));
			conRosterBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					consistRosterSelected();
				}
			});
			constraints.gridx = 0;
			constraints.gridy = GridBagConstraints.RELATIVE;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1;
			constraints.weighty = 0;
			mainPanel.add(conRosterBox, constraints);
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		dispatchButton = new JButton(rb.getString("ButtonDispatch"));
		buttonPanel.add(dispatchButton);
		dispatchButton.setEnabled(false);
		dispatchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispatchAddress();
			}
		});

		releaseButton = new JButton(rb.getString("ButtonRelease"));
		buttonPanel.add(releaseButton);
		releaseButton.setEnabled(false);
		releaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				releaseAddress();
			}
		});

		progButton = new JButton(rb.getString("ButtonProgram"));
		buttonPanel.add(progButton);
		progButton.setEnabled(false);
		progButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openProgrammer();
			}
		});

		constraints.gridx = 0;
		constraints.gridy = GridBagConstraints.RELATIVE;
		constraints.gridwidth = 2;
		constraints.weighty = 0;
		constraints.insets = new Insets(0, 0, 0, 0);
		mainPanel.add(buttonPanel, constraints);

		pack();
	}

	private void rosterItemSelected() {
		if (!(rosterBox.getSelectedItem() instanceof NullComboBoxItem)) {
			String rosterEntryTitle = rosterBox.getSelectedItem().toString();
			RosterEntry entry = Roster.instance().entryFromTitle(
					rosterEntryTitle);

			addrSelector.setAddress(entry.getDccLocoAddress());
			consistAddress = null;
			changeOfAddress();
		}
		progButton.setEnabled(true);
	}

	private void consistRosterSelected() {
		if (!(conRosterBox.getSelectedItem() instanceof NullComboBoxConsist)) {
			String rosterEntryTitle = conRosterBox.getSelectedItem().toString();
			NceConsistRosterEntry cre = NceConsistRoster.instance()
					.entryFromTitle(rosterEntryTitle);

			DccLocoAddress a = new DccLocoAddress(Integer.parseInt(cre
					.getLoco1DccAddress()), cre.isLoco1LongAddress());
			addrSelector.setAddress(a);
			consistAddress = null;
			int cA = 0;
			try {
				cA = Integer.parseInt(cre.getConsistNumber());
			} catch (NumberFormatException e) {

			}
			if (0 < cA && cA < 128) {
				consistAddress = new DccLocoAddress(cA, false);
			} else {
				log.warn("consist number missing " + cre.getLoco1DccAddress());
				JOptionPane.showMessageDialog(this,
						rb.getString("ConsistNumberHasNotBeenAssigned"),
						rb.getString("NeedsConsistNumber"),
						JOptionPane.ERROR_MESSAGE);
			}
			changeOfAddress();
		}
	}

	/**
	 * The user has selected a new address. Notify all listeners.
	 */
	public void changeOfAddress() {
		// send notification of new address
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				AddressListener l = (AddressListener) listeners.get(i);
				if (log.isDebugEnabled()) {
					log.debug("Notify address listener " + l);
				}
				currentAddress = addrSelector.getAddress();
				if (currentAddress != null) {
					l.notifyAddressChosen(currentAddress.getNumber(),
							currentAddress.isLongAddress());
				}
				if (consistAddress != null) {
					l.notifyAddressChosen(consistAddress.getNumber(),
							consistAddress.isLongAddress());
				}
			}
		}
	}

    /**
     * Open a programmer for this address
     */
    protected void openProgrammer() {
        RosterEntry re = Roster.instance().entryFromTitle((String)rosterBox.getSelectedItem());
        if (re == null) log.error("RosterEntry is null during open; that shouldnt be possible");

        java.util.ResourceBundle rbt 
                    = java.util.ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle");
        String title = java.text.MessageFormat.format(rbt.getString("FrameOpsProgrammerTitle"),
                                                new String[]{re.getId()});
        // find the ops-mode programmer
        int address = Integer.parseInt(re.getDccAddress());
        boolean longAddr = true;
        if (address<100) longAddr = false;
        Programmer programmer = InstanceManager.programmerManagerInstance()
                                    .getOpsModeProgrammer(longAddr, address);
        // and created the frame
        JFrame p = new PaneOpsProgFrame(null, re,
                                         title, "programmers"+File.separator+"Comprehensive.xml",
                                         programmer);
        p.pack();
        p.setVisible(true);
    }
    
	/**
	 * Dispatch the current address for use by other throttles
	 */
	private void dispatchAddress() {
		throttle.dispatch();
		if (consistThrottle != null) {
			consistThrottle.dispatch();
			consistThrottle = null;
		}
		notifyListenersOfThrottleRelease();
	}

	/**
	 * Release the current address.
	 */
	private void releaseAddress() {
		throttle.release();
		if (consistThrottle != null) {
			consistThrottle.release();
			consistThrottle = null;
		}
		notifyListenersOfThrottleRelease();
	}

	private void notifyListenersOfThrottleRelease() {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				AddressListener l = (AddressListener) listeners.get(i);
				if (log.isDebugEnabled()) {
					log.debug("Notify address listener " + l);
				}
				l.notifyAddressReleased(currentAddress.getNumber(),
						currentAddress.isLongAddress());
			}
		}
	}

	/**
	 * Create an Element of this object's preferences.
	 * <ul>
	 * <li> Window Preferences
	 * <li> Address value
	 * </ul>
	 * 
	 * @return org.jdom.Element for this objects preferences. Defined in
	 *         DTD/throttle-config
	 */
	public Element getXml() {
		Element me = new Element("AddressPanel");
		Element window = new Element("window");
		WindowPreferences wp = new WindowPreferences();
		java.util.ArrayList children = new java.util.ArrayList(1);
		children.add(wp.getPreferences(this));
		children.add((new jmri.configurexml.LocoAddressXml())
				.store(addrSelector.getAddress()));
		children.add((new jmri.configurexml.LocoAddressXml())
				.store(consistAddress));
		me.setContent(children);
		return me;
	}
	

	/**
	 * Use the Element passed to initialize based on user prefs.
	 * 
	 * @param e
	 *            The Element containing prefs as defined in DTD/throttle-config
	 */
	public void setXml(Element e) {
		Element window = e.getChild("window");
		WindowPreferences wp = new WindowPreferences();
		wp.setPreferences(this, window);

		Element addressElement = e.getChild("address");
		if (addressElement != null) {
			String address = addressElement.getAttribute("value").getValue();
			addrSelector.setAddress(new DccLocoAddress(Integer
					.parseInt(address), false)); // guess at the short/long
			consistAddress = null;
			changeOfAddress();
		}
		
		List elementList = e.getChildren("locoaddress");
		if (elementList.size() > 0){
			log.debug("found " + elementList.size() +" locoaddress");
			addrSelector.setAddress((DccLocoAddress) (new jmri.configurexml.LocoAddressXml())
					.getAddress((Element)elementList.get(0)));
			consistAddress = null;
			// if there are two locoaddress, the second is the consist address
			if (elementList.size() > 1){
				consistAddress = ((DccLocoAddress) (new jmri.configurexml.LocoAddressXml())
						.getAddress((Element)elementList.get(1)));
			}
			changeOfAddress();
		}
	}

	class NullComboBoxItem {
		public String toString() {
			return rb.getString("NoLocoSelected");
		}
	}

	class NullComboBoxConsist {
		public String toString() {
			return rb.getString("NoConsistSelected");
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(AddressPanel.class.getName());

}
