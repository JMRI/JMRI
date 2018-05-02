package jmri.jmrit.throttle;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ThrottleListener;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.symbolicprog.ProgDefault;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.nce.consist.NceConsistRoster;
import jmri.jmrix.nce.consist.NceConsistRosterEntry;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JInternalFrame that provides a way for the user to enter a decoder address.
 * This class also store AddressListeners and notifies them when the user enters
 * a new address.
 *
 * @author glen Copyright (C) 2002
 * @author Daniel Boudreau Copyright (C) 2008 (add consist feature)
 */
public class AddressPanel extends JInternalFrame implements ThrottleListener, PropertyChangeListener {

    private DccThrottle throttle;
    private DccThrottle consistThrottle;

    private DccLocoAddressSelector addrSelector = new DccLocoAddressSelector();
    private DccLocoAddress currentAddress;
    private DccLocoAddress consistAddress;
    private ArrayList<AddressListener> listeners;

    private JPanel mainPanel;

    private JButton releaseButton;
    private JButton dispatchButton;
    private JButton progButton;
    private JButton setButton;
    private RosterEntrySelectorPanel rosterBox;
    private JComboBox<String> conRosterBox;

    private RosterEntry rosterEntry;

    /**
     * Constructor
     */
    public AddressPanel() {
        initGUI();
    }

    public void destroy() { // Handle disposing of the throttle
        if (throttle != null) {
            DccLocoAddress l = (DccLocoAddress) throttle.getLocoAddress();
            throttle.removePropertyChangeListener(this);
            InstanceManager.throttleManagerInstance().cancelThrottleRequest(l.getNumber(), this);
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            notifyListenersOfThrottleRelease();
            throttle = null;
        }
        if (consistThrottle != null) {
            InstanceManager.throttleManagerInstance().releaseThrottle(consistThrottle, this);
            notifyListenersOfThrottleRelease();
            consistThrottle = null;
        }
    }

    /**
     * Add an AddressListener. AddressListeners are notified when the user
     * selects a new address and when a Throttle is acquired for that address
     *
     */
    public void addAddressListener(AddressListener l) {
        if (listeners == null) {
            listeners = new ArrayList<AddressListener>(2);
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove an AddressListener.
     *
     */
    public void removeAddressListener(AddressListener l) {
        if (listeners == null) {
            return;
        }
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Gets the selected index of the roster combo box. Implemented to support
     * xboxThrottle.py
     *
     * @return the selected index of the roster combo box
     */
    public int getRosterSelectedIndex() {
        return getRosterEntrySelector().getRosterEntryComboBox().getSelectedIndex();
    }

    /**
     * Sets the selected index of the roster combo box. Implemented to support
     * xboxThrottle.py This method temporarily disables roster box actions so it
     * can change the selected index without triggering a cascade of events.
     *
     * @param index the index to select in the combo box
     */
    public void setRosterSelectedIndex(int index) {
        if (getRosterEntrySelector().isEnabled() && index >= 0 && index < getRosterEntrySelector().getRosterEntryComboBox().getItemCount()) {
            getRosterEntrySelector().getRosterEntryComboBox().setSelectedIndex(index);
        }
        if ((backgroundPanel != null) && (!(rosterBox.getSelectedRosterEntries().length != 0))) {
            backgroundPanel.setImagePath(null);
            String rosterEntryTitle = getRosterEntrySelector().getSelectedRosterEntries()[0].titleString();
            RosterEntry re = Roster.getDefault().entryFromTitle(rosterEntryTitle);
            if (re != null) {
                backgroundPanel.setImagePath(re.getImagePath());
            }
        }
    }

    private BackgroundPanel backgroundPanel;

    public void setBackgroundPanel(BackgroundPanel bp) {
        backgroundPanel = bp;
    }

    /**
     * "Sets" the current roster entry. Equivalent to the user pressing the
     * "Set" button. Implemented to support xboxThrottle.py
     */
    public void selectRosterEntry() {
        rosterItemSelected();
    }

    /**
     * Get notification that a throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        log.debug("Asked for " + currentAddress.getNumber() + " got " + t.getLocoAddress());
        if (consistAddress != null
                && ((DccLocoAddress) t.getLocoAddress()).getNumber() == consistAddress.getNumber()) {
            // notify the listeners that a throttle was found
            // for the consist address.
            log.debug("notifying that this is a consist");
            notifyConsistThrottleFound(t);
            return;
        }
        if (((DccLocoAddress) t.getLocoAddress()).getNumber() != currentAddress.getNumber()) {
            log.warn("Not correct address, asked for " + currentAddress.getNumber() + " got " + t.getLocoAddress() + ", requesting again...");
            boolean requestOK
                    = InstanceManager.throttleManagerInstance().requestThrottle(currentAddress.getNumber(), currentAddress.isLongAddress(), this);
            if (!requestOK) {
                JOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("AddressInUse"));
            }
            return;
        }

        throttle = t;
        releaseButton.setEnabled(true);
        currentAddress = (DccLocoAddress) t.getLocoAddress();
        addrSelector.setAddress(currentAddress);
        throttle.addPropertyChangeListener(this);

        // can we find a roster entry?
        if ((rosterEntry == null)
                && (InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isUsingExThrottle())
                && (InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isEnablingRosterSearch())
                && addrSelector.getAddress() != null) {
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null, "" + addrSelector.getAddress().getNumber(), null, null, null, null);
            if (l.size() > 0) {
                rosterEntry = l.get(0);
            }
        }

        // update GUI
        setButton.setEnabled(false);
        addrSelector.setEnabled(false);
        getRosterEntrySelector().setEnabled(false);
        conRosterBox.setEnabled(false);
        if (InstanceManager.throttleManagerInstance().hasDispatchFunction()) {
            dispatchButton.setEnabled(true);
        }
        // enable program button if programmer available
        // for ops-mode programming
        if ((rosterEntry != null) && (ProgDefault.getDefaultProgFile() != null)
                && (InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class) != null)
                && (InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).isAddressedModePossible())) {
            progButton.setEnabled(true);
        }
        // send notification of new address
        for (int i = 0; i < listeners.size(); i++) {
            AddressListener l = listeners.get(i);
            if (log.isDebugEnabled()) {
                log.debug("Notify address listener of throttle acquired " + l.getClass());
            }
            if (currentAddress != null) {
                l.notifyAddressThrottleFound(throttle);
            }
        }
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        javax.swing.JOptionPane.showMessageDialog(null, reason, Bundle.getMessage("FailedSetupRequestTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress address){
        InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, 
                InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isSilentSteal() || 
                ( javax.swing.JOptionPane.YES_OPTION == javax.swing.JOptionPane.showConfirmDialog(this, Bundle.getMessage("StealQuestionText",address.toString()), Bundle.getMessage("StealRequestTitle"), javax.swing.JOptionPane.YES_NO_OPTION)));
    }

    /**
     * Get notification that a consist throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyConsistThrottleFound(DccThrottle t) {
        this.consistThrottle = t;
        for (int i = 0; i < listeners.size(); i++) {
            AddressListener l = listeners.get(i);
            if (log.isDebugEnabled()) {
                log.debug("Notify address listener of address change " + l.getClass());
            }
            l.notifyConsistAddressThrottleFound(t);
        }

    }

    /**
     * Receive notification that an address has been release or dispatched.
     */
    public void notifyThrottleDisposed() {
        log.debug("notifyThrottleDisposed");
        dispatchButton.setEnabled(false);
        releaseButton.setEnabled(false);
        progButton.setEnabled(false);
        setButton.setEnabled(true);
        addrSelector.setEnabled(true);
        getRosterEntrySelector().setEnabled(true);
        conRosterBox.setEnabled(true);
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
        }
        throttle = null;
        rosterEntry = null;
        notifyListenersOfThrottleRelease();
    }

    /**
     * Get the RosterEntry if there's one for this throttle.
     *
     * @return RosterEntry or null
     */
    public RosterEntry getRosterEntry() {
        return rosterEntry;
    }

    /**
     * Set the RosterEntry for this throttle.
     */
    public void setRosterEntry(RosterEntry entry) {
        getRosterEntrySelector().setSelectedRosterEntry(entry);
        addrSelector.setAddress(entry.getDccLocoAddress());
        rosterEntry = entry;
        changeOfAddress();
    }

    /**
     * Create, initialize and place the GUI objects.
     */
    private void initGUI() {
        mainPanel = new JPanel();
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

        constraints.ipadx = -16;
        if (jmri.util.SystemType.isLinux()) {
            constraints.ipady = 0;
        } else {
            constraints.ipady = -16;
        }
        addrSelector.setVariableSize(true);
        mainPanel.add(addrSelector.getCombinedJPanel(), constraints);

        setButton = new JButton(Bundle.getMessage("ButtonSet"));
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        constraints.ipadx = constraints.ipady = 0;
        mainPanel.add(setButton, constraints);

        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consistAddress = null;
                changeOfAddress();
            }
        });

        rosterBox = new RosterEntrySelectorPanel();
        getRosterEntrySelector().setNonSelectedItem(Bundle.getMessage("NoLocoSelected"));
        getRosterEntrySelector().setToolTipText(Bundle.getMessage("SelectLocoFromRosterTT"));
        getRosterEntrySelector().addPropertyChangeListener("selectedRosterEntries", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                rosterItemSelected();
            }
        });

        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(getRosterEntrySelector(), constraints);

        conRosterBox = NceConsistRoster.instance().fullRosterComboBox();
        if (NceConsistRoster.instance().numEntries() > 0) {
            conRosterBox.insertItemAt(Bundle.getMessage("NoConsistSelected"), 0);  // empty entry
            conRosterBox.setSelectedIndex(0);
            conRosterBox.setToolTipText(Bundle.getMessage("SelectConsistFromRosterTT"));
            conRosterBox.addActionListener(new ActionListener() {
                @Override
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
        dispatchButton = new JButton(Bundle.getMessage("ButtonDispatch"));
        buttonPanel.add(dispatchButton);
        dispatchButton.setEnabled(false);
        dispatchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchAddress();
            }
        });

        releaseButton = new JButton(Bundle.getMessage("ButtonRelease"));
        buttonPanel.add(releaseButton);
        releaseButton.setEnabled(false);
        releaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                releaseAddress();
            }
        });

        progButton = new JButton(Bundle.getMessage("ButtonProgram"));
        buttonPanel.add(progButton);
        progButton.setEnabled(false);
        progButton.addActionListener(new ActionListener() {
            @Override
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
        if (getRosterEntrySelector().getSelectedRosterEntries().length != 0) {
            setRosterEntry(getRosterEntrySelector().getSelectedRosterEntries()[0]);
            consistAddress = null;
        }
    }

    private void consistRosterSelected() {
        if (!(conRosterBox.getSelectedItem().equals(Bundle.getMessage("NoConsistSelected")))) {
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
                JOptionPane.showMessageDialog(mainPanel,
                        Bundle.getMessage("ConsistNumberHasNotBeenAssigned"),
                        Bundle.getMessage("NeedsConsistNumber"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(mainPanel,
                    Bundle.getMessage("SendFunctionToLead"), Bundle.getMessage("NCEconsistThrottle"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                addrSelector.setAddress(consistAddress);
                consistAddress = null;
            }
            changeOfAddress();
        }
    }

    /**
     * The user has selected a new address. Notify all listeners.
     */
    private void changeOfAddress() {
        currentAddress = addrSelector.getAddress();
        if (currentAddress == null) {
            return; // no address
        }  // send notification of new address
        for (int i = 0; i < listeners.size(); i++) {
            AddressListener l = listeners.get(i);
            if (log.isDebugEnabled()) {
                log.debug("Notify address listener of address change " + l.getClass());
            }
            l.notifyAddressChosen(currentAddress);
        }
        log.debug("Requesting new slot for address "+currentAddress);
        boolean requestOK
                = InstanceManager.throttleManagerInstance().requestThrottle(getCurrentAddress(), rosterEntry, this);
        if (!requestOK) {
            JOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("AddressInUse"));
        }
    }

    private void changeOfConsistAddress() {
        if (consistAddress == null) {
            return; // no address
        }  // send notification of new address
        for (int i = 0; i < listeners.size(); i++) {
            AddressListener l = listeners.get(i);
            if (log.isDebugEnabled()) {
                log.debug("Notify address listener of address change " + l.getClass());
            }
            l.notifyConsistAddressChosen(consistAddress.getNumber(), consistAddress.isLongAddress());
        }

        boolean requestOK
                = InstanceManager.throttleManagerInstance().requestThrottle(consistAddress.getNumber(), consistAddress.isLongAddress(), this);
        if (!requestOK) {
            JOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("AddressInUse"));
        }
    }

    /**
     * Open a programmer for this address
     */
    protected void openProgrammer() {
        if (rosterEntry == null) {
            return;
        }

        java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle");
        String title = java.text.MessageFormat.format(rbt.getString("FrameOpsProgrammerTitle"), new Object[]{rosterEntry.getId()});
        // find the ops-mode programmer
        int address = Integer.parseInt(rosterEntry.getDccAddress());
        boolean longAddr = true;
        if (address < 100) {
            longAddr = false;
        }
        Programmer programmer = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(longAddr, address);
        // and created the frame
        JFrame p = new PaneOpsProgFrame(null, rosterEntry,
                title, "programmers" + File.separator + ProgDefault.getDefaultProgFile() + ".xml",
                programmer);
        p.pack();
        p.setVisible(true);
    }

    /**
     * Dispatch the current address for use by other throttles
     */
    public void dispatchAddress() {
        if (throttle != null) {
            InstanceManager.throttleManagerInstance().dispatchThrottle(throttle, this);
            if (consistThrottle != null) {
                InstanceManager.throttleManagerInstance().dispatchThrottle(consistThrottle, this);
                consistThrottle = null;
            }
            notifyThrottleDisposed();
        }
    }

    /**
     * Release the current address.
     */
    public void releaseAddress() {
        InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
        if (consistThrottle != null) {
            InstanceManager.throttleManagerInstance().releaseThrottle(consistThrottle, this);
            consistThrottle = null;
        }
        notifyThrottleDisposed();
    }

    private void notifyListenersOfThrottleRelease() {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                AddressListener l = listeners.get(i);
                if (log.isDebugEnabled()) {
                    log.debug("Notify address listener of release " + l.getClass());
                }
                l.notifyAddressReleased(currentAddress);
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
     * @return org.jdom2.Element for this objects preferences. Defined in
     *         DTD/throttle-config
     */
    public Element getXml() {
        Element me = new Element("AddressPanel");
        //Element window = new Element("window");
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);
        children.add(WindowPreferences.getPreferences(this));
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
     * @param e The Element containing prefs as defined in DTD/throttle-config
     */
    @SuppressWarnings("unchecked")
    public void setXml(Element e) {
        Element window = e.getChild("window");
        WindowPreferences.setPreferences(this, window);

        Element addressElement = e.getChild("address");
        if ((addressElement != null) && (this.getRosterEntry() == null)) {
            String address = addressElement.getAttribute("value").getValue();
            addrSelector.setAddress(new DccLocoAddress(Integer
                    .parseInt(address), false)); // guess at the short/long
            consistAddress = null;
            changeOfAddress();
        }

        List<Element> elementList = e.getChildren("locoaddress");
        if ((elementList.size() > 0) && (getThrottle() == null)) {
            log.debug("found " + elementList.size() + " locoaddress");
            currentAddress = (DccLocoAddress) (new jmri.configurexml.LocoAddressXml())
                    .getAddress(elementList.get(0));
            log.debug("Loaded address "+currentAddress+" from xml");
            addrSelector.setAddress(currentAddress);
            consistAddress = null;
            // if there are two locoaddress, the second is the consist address
            if (elementList.size() > 1) {
                DccLocoAddress tmpAdd = ((DccLocoAddress) (new jmri.configurexml.LocoAddressXml())
                        .getAddress(elementList.get(1)));
                if (tmpAdd !=null && ! currentAddress.equals(tmpAdd)) {                    
                    log.debug("and consist with "+tmpAdd);
                    consistAddress = tmpAdd;
                }
            }
            changeOfAddress();
        }
    }

    /**
     * @return the RosterEntrySelectorPanel
     */
    public RosterEntrySelectorPanel getRosterEntrySelector() {
        return rosterBox;
    }

    public DccThrottle getThrottle() {
        return throttle;
    }

    public DccLocoAddress getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(DccLocoAddress currentAddress) {
        if (log.isDebugEnabled()) {
            log.debug("Setting CurrentAddress to " + currentAddress);
        }
        this.addrSelector.setAddress(currentAddress);
        changeOfAddress();
    }

    public void setAddress(int consistNumber, boolean b) {
        setCurrentAddress(new DccLocoAddress(consistNumber, b));
    }

    public DccLocoAddress getConsistAddress() {
        return consistAddress;
    }

    public void setConsistAddress(DccLocoAddress consistAddress) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Consist Address to " + consistAddress);
        }
        this.consistAddress = consistAddress;
        changeOfConsistAddress();

    }

    private final static Logger log = LoggerFactory.getLogger(AddressPanel.class);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) {
            return;
        }
        if ("ThrottleConnected".compareTo(evt.getPropertyName()) == 0) {
            if ((true == (Boolean) evt.getOldValue()) && (false == (Boolean) evt.getNewValue())) {
                log.debug("propertyChange: ThrottleConnected to false");
                notifyThrottleDisposed();
            }
        }
    }
}
