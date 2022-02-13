package jmri.jmrit.throttle;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Programmer;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.symbolicprog.ProgDefault;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.nce.consist.NceConsistRoster;
import jmri.jmrix.nce.consist.NceConsistRosterEntry;
import jmri.util.swing.WrapLayout;

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
 * @author Lionel Jeanson 2009-2021
 */
public class AddressPanel extends JInternalFrame implements ThrottleListener, PropertyChangeListener {

    private final ThrottleManager throttleManager;

    private DccThrottle throttle;
    private DccThrottle consistThrottle;

    private final DccLocoAddressSelector addrSelector = new DccLocoAddressSelector();
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
    public AddressPanel(ThrottleManager throttleManager) {
        this.throttleManager = throttleManager;
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        initGUI();
        applyPreferences();
    }

    public void destroy() { // Handle disposing of the throttle
        if (throttle != null) {
            DccLocoAddress l = (DccLocoAddress) throttle.getLocoAddress();
            throttle.removePropertyChangeListener(this);
            throttleManager.cancelThrottleRequest(l, this);
            throttleManager.releaseThrottle(throttle, this);
            notifyListenersOfThrottleRelease();
            throttle = null;
        }
        if (consistThrottle != null) {
            throttleManager.releaseThrottle(consistThrottle, this);
            notifyListenersOfThrottleRelease();
            consistThrottle = null;
        }
    }

    /**
     * Add an AddressListener.
     * AddressListeners are notified when the user
     * selects a new address and when a Throttle is acquired for that address
     * @param l listener to add.
     *
     */
    public void addAddressListener(AddressListener l) {
        if (listeners == null) {
            listeners = new ArrayList<>(2);
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove an AddressListener.
     *
     * @param l listener to remove.
     */
    public void removeAddressListener(AddressListener l) {
        if (listeners == null) {
            return;
        }
        listeners.remove(l);
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
        if ((backgroundPanel != null) && (rosterBox.getSelectedRosterEntries().length == 0)) {
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
        log.debug("Asked for {} got {}", currentAddress.getNumber(), t.getLocoAddress());
        if (consistAddress != null
                && t.getLocoAddress().getNumber() == consistAddress.getNumber()) {
            // notify the listeners that a throttle was found
            // for the consist address.
            log.debug("notifying that this is a consist");
            notifyConsistThrottleFound(t);
            return;
        }
        if (t.getLocoAddress().getNumber() != currentAddress.getNumber()) {
            log.warn("Not correct address, asked for {} got {}, requesting again...", currentAddress.getNumber(), t.getLocoAddress());
            boolean requestOK
                    = throttleManager.requestThrottle(currentAddress, this, true);
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
                && (InstanceManager.getDefault(ThrottlesPreferences.class).isUsingExThrottle())
                && (InstanceManager.getDefault(ThrottlesPreferences.class).isEnablingRosterSearch())
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
        if (throttleManager.hasDispatchFunction()) {
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
        listeners.stream().filter((l) -> (currentAddress != null)).forEachOrdered((l) -> {
            l.notifyAddressThrottleFound(throttle);
        });
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        javax.swing.JOptionPane.showMessageDialog(null, reason, Bundle.getMessage("FailedSetupRequestTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
    }

    /**
    * A decision is required for Throttle creation to continue.
    * <p>
    * Steal / Cancel, Share / Cancel, or Steal / Share Cancel
    */
    @Override
    public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
        if ( null != question )  {
            switch (question) {
                case STEAL:
                    if (InstanceManager.getDefault(ThrottlesPreferences.class).isSilentSteal() ){
                        throttleManager.responseThrottleDecision(address, this, DecisionType.STEAL );
                        return;
                    }
                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        if ( javax.swing.JOptionPane.YES_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                                this, Bundle.getMessage("StealQuestionText",address.toString()),
                                Bundle.getMessage("StealRequestTitle"), javax.swing.JOptionPane.YES_NO_OPTION)) {
                            throttleManager.responseThrottleDecision(address, this, DecisionType.STEAL );
                        } else {
                            throttleManager.cancelThrottleRequest(address, this);
                        }
                    });
                    break;
                case SHARE:
                    if (InstanceManager.getDefault(ThrottlesPreferences.class).isSilentShare() ){
                        throttleManager.responseThrottleDecision(address, this, DecisionType.SHARE );
                        return;
                    }
                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        if ( javax.swing.JOptionPane.YES_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                                this, Bundle.getMessage("ShareQuestionText",address.toString()),
                                Bundle.getMessage("ShareRequestTitle"), javax.swing.JOptionPane.YES_NO_OPTION)) {
                            throttleManager.responseThrottleDecision(address, this, DecisionType.SHARE );
                        } else {
                            throttleManager.cancelThrottleRequest(address, this);
                        }
                    });
                    break;
                case STEAL_OR_SHARE:
                    if ( InstanceManager.getDefault(ThrottlesPreferences.class).isSilentSteal() ){
                        throttleManager.responseThrottleDecision(address, this, DecisionType.STEAL );
                        return;
                    }
                    if ( InstanceManager.getDefault(ThrottlesPreferences.class).isSilentShare() ){
                        throttleManager.responseThrottleDecision(address, this, DecisionType.SHARE );
                        return;
                    }
                    String[] options = new String[] {Bundle.getMessage("StealButton"), Bundle.getMessage("ShareButton"), Bundle.getMessage("CancelButton")};
                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        int response = javax.swing.JOptionPane.showOptionDialog(AddressPanel.this,
                                Bundle.getMessage("StealShareQuestionText",address.toString()), Bundle.getMessage("StealShareRequestTitle"),
                                javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                        switch (response) {
                            case 0:
                                log.debug("steal clicked");
                                throttleManager.responseThrottleDecision(address, AddressPanel.this, DecisionType.STEAL);
                                break;
                            case 1:
                                log.debug("share clicked");
                                throttleManager.responseThrottleDecision(address, AddressPanel.this, DecisionType.SHARE);
                                break;
                            default:
                                log.debug("cancel clicked");
                                throttleManager.cancelThrottleRequest(address, AddressPanel.this);
                                break;
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Get notification that a consist throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyConsistThrottleFound(DccThrottle t) {
        this.consistThrottle = t;
        listeners.forEach((l) -> {
            // log.debug("Notify address listener of address change {}", l.getClass());
            l.notifyConsistAddressThrottleFound(t);
        });
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
     * @param entry roster entry to set.
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
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        this.setContentPane(mainPanel);

        // center: address input
        addrSelector.setVariableSize(true);
        mainPanel.add(addrSelector.getCombinedJPanel(), BorderLayout.CENTER);
        addrSelector.getTextField().addActionListener(e -> {
            consistAddress = null;
            changeOfAddress();
        });

        // top : roster and consists selectors
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));

        rosterBox = new RosterEntrySelectorPanel();
        getRosterEntrySelector().setNonSelectedItem(Bundle.getMessage("NoLocoSelected"));
        getRosterEntrySelector().setToolTipText(Bundle.getMessage("SelectLocoFromRosterTT"));
        getRosterEntrySelector().addPropertyChangeListener("selectedRosterEntries", pce -> rosterItemSelected());
        getRosterEntrySelector().setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));
        topPanel.add(getRosterEntrySelector());

        conRosterBox = InstanceManager.getDefault(NceConsistRoster.class).fullRosterComboBox();
        if (InstanceManager.getDefault(NceConsistRoster.class).numEntries() > 0) {
            conRosterBox.insertItemAt(Bundle.getMessage("NoConsistSelected"), 0);  // empty entry
            conRosterBox.setSelectedIndex(0);
            conRosterBox.setToolTipText(Bundle.getMessage("SelectConsistFromRosterTT"));
            conRosterBox.addActionListener(e -> consistRosterSelected());
            topPanel.add(conRosterBox);
        }

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // bottom : buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));

        progButton = new JButton(Bundle.getMessage("ButtonProgram"));
        buttonPanel.add(progButton);
        progButton.setEnabled(false);
        progButton.addActionListener(e -> openProgrammer());

        dispatchButton = new JButton(Bundle.getMessage("ButtonDispatch"));
        buttonPanel.add(dispatchButton);
        dispatchButton.setEnabled(false);
        dispatchButton.addActionListener(e -> dispatchAddress());

        releaseButton = new JButton(Bundle.getMessage("ButtonRelease"));
        buttonPanel.add(releaseButton);
        releaseButton.setEnabled(false);
        releaseButton.addActionListener(e -> releaseAddress());

        setButton = new JButton(Bundle.getMessage("ButtonSet"));
        setButton.addActionListener(e -> {
            consistAddress = null;
            changeOfAddress();
        });
        buttonPanel.add(setButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    private void rosterItemSelected() {
        if (getRosterEntrySelector().getSelectedRosterEntries().length != 0) {
            setRosterEntry(getRosterEntrySelector().getSelectedRosterEntries()[0]);
            consistAddress = null;
        }
    }

    private void consistRosterSelected() {
        if (!(Objects.equals(conRosterBox.getSelectedItem(), Bundle.getMessage("NoConsistSelected")))) {
            String rosterEntryTitle = Objects.requireNonNull(conRosterBox.getSelectedItem()).toString();
            NceConsistRosterEntry nceConsistRosterEntry = InstanceManager.getDefault(NceConsistRoster.class)
                    .entryFromTitle(rosterEntryTitle);

            DccLocoAddress a = new DccLocoAddress(Integer.parseInt(nceConsistRosterEntry
                    .getLoco1DccAddress()), nceConsistRosterEntry.isLoco1LongAddress());
            addrSelector.setAddress(a);
            consistAddress = null;
            int cA = 0;
            try {
                cA = Integer.parseInt(nceConsistRosterEntry.getConsistNumber());
            } catch (NumberFormatException ignored) {

            }
            if (0 < cA && cA < 128) {
                consistAddress = new DccLocoAddress(cA, false);
            } else {
                log.warn("consist number missing {}", nceConsistRosterEntry.getLoco1DccAddress());
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
        }
        // send notification of new address
        listeners.forEach((l) -> {
            l.notifyAddressChosen(currentAddress);
        });
        log.debug("Requesting new slot for address {} rosterEntry {}",currentAddress,rosterEntry);
        boolean requestOK;
        if (rosterEntry == null) {
            requestOK = throttleManager.requestThrottle(currentAddress, this, true);
        }
        else {
            requestOK = throttleManager.requestThrottle(rosterEntry, this, true);
        }
        if (!requestOK) {
            JOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("AddressInUse"));
        }
    }

    private void changeOfConsistAddress() {
        if (consistAddress == null) {
            return; // no address
        }  // send notification of new address
        for (AddressListener l : listeners) {
            //log.debug("Notify address listener {} of address change ", l.getClass());
            l.notifyConsistAddressChosen(consistAddress.getNumber(), consistAddress.isLongAddress());
        }

        boolean requestOK
                = throttleManager.requestThrottle(consistAddress, this, true);
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
        String ptitle = java.text.MessageFormat.format(rbt.getString("FrameOpsProgrammerTitle"), rosterEntry.getId());
        // find the ops-mode programmer
        int address = Integer.parseInt(rosterEntry.getDccAddress());
        boolean longAddr = true;
        if (address < 100) {
            longAddr = false;
        }
        Programmer programmer = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(longAddr, address);
        // and created the frame
        JFrame p = new PaneOpsProgFrame(null, rosterEntry,
                ptitle, "programmers" + File.separator + ProgDefault.getDefaultProgFile() + ".xml",
                programmer);
        p.pack();
        p.setVisible(true);
    }

    /**
     * Dispatch the current address for use by other throttles
     */
    public void dispatchAddress() {
        if (throttle != null) {
            int usageCount  = throttleManager.getThrottleUsageCount(throttle.getLocoAddress()) - 1;

            if ( usageCount != 0 ) {
                JOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("CannotDisptach", usageCount));
                return;
            }
            throttleManager.dispatchThrottle(throttle, this);
            if (consistThrottle != null) {
                throttleManager.dispatchThrottle(consistThrottle, this);
                consistThrottle = null;
            }
            notifyThrottleDisposed();
        }
    }

    /**
     * Release the current address.
     */
    public void releaseAddress() {
        throttleManager.releaseThrottle(throttle, this);
        if (consistThrottle != null) {
            throttleManager.releaseThrottle(consistThrottle, this);
            consistThrottle = null;
        }
        notifyThrottleDisposed();
    }

    private void notifyListenersOfThrottleRelease() {
        if (listeners != null) {
            listeners.forEach((l) -> {
                // log.debug("Notify address listener {} of release", l.getClass());
                l.notifyAddressReleased(currentAddress);
            });
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
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1);
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
            log.debug("found {} locoaddress(es)", elementList.size() );
            currentAddress = (DccLocoAddress) (new jmri.configurexml.LocoAddressXml())
                    .getAddress(elementList.get(0));
            log.debug("Loaded address {} from xml",currentAddress);
            addrSelector.setAddress(currentAddress);
            consistAddress = null;
            // if there are two locoaddress, the second is the consist address
            if (elementList.size() > 1) {
                DccLocoAddress tmpAdd = ((DccLocoAddress) (new jmri.configurexml.LocoAddressXml())
                        .getAddress(elementList.get(1)));
                if (tmpAdd !=null && ! currentAddress.equals(tmpAdd)) {
                    log.debug("and consist with {}",tmpAdd);
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
            log.debug("Setting CurrentAddress to {}", currentAddress);
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
            log.debug("Setting Consist Address to {}", consistAddress);
        }
        this.consistAddress = consistAddress;
        changeOfConsistAddress();

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) {
            return;
        }
        if ("ThrottleConnected".compareTo(evt.getPropertyName()) == 0) {
            if (((Boolean) evt.getOldValue()) && (!((Boolean) evt.getNewValue()))) {
                log.debug("propertyChange: ThrottleConnected to false");
                notifyThrottleDisposed();
            }
        }

        if ("DispatchEnabled".compareTo(evt.getPropertyName()) == 0) {
            log.debug("propertyChange: Dispatch Button Enabled {}" , evt.getNewValue() );
            dispatchButton.setEnabled( (Boolean) evt.getNewValue() );
        }

        if ("ReleaseEnabled".compareTo(evt.getPropertyName()) == 0) {
            log.debug("propertyChange: release Button Enabled {}" , evt.getNewValue() );
            releaseButton.setEnabled( (Boolean) evt.getNewValue() );
        }
    }

    void applyPreferences() {
        // nothing to do, for now
    }

    private final static Logger log = LoggerFactory.getLogger(AddressPanel.class);

}

