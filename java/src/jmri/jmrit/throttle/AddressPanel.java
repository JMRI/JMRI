package jmri.jmrit.throttle;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Programmer;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.consisttool.ConsistComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.symbolicprog.ProgDefault;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.nce.consist.NceConsistRoster;
import jmri.jmrix.nce.consist.NceConsistRosterEntry;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.WrapLayout;

import org.jdom2.Element;

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
    private final ConsistManager consistManager; 

    private DccThrottle throttle;
    private DccThrottle consistThrottle;

    private final DccLocoAddressSelector addrSelector = new DccLocoAddressSelector();
    private DccLocoAddress currentAddress;
    private DccLocoAddress consistAddress;
    private DccLocoAddress requestedAddress;
    private ArrayList<AddressListener> listeners;

    private JPanel mainPanel;

    private JButton releaseButton;
    private JButton dispatchButton;
    private JButton progButton;
    private JButton setButton;
    private RosterEntrySelectorPanel rosterBox;
    @SuppressWarnings("rawtypes") // TBD: once JMRI consists vs NCE consists resolved, can be removed
    private JComboBox conRosterBox;
    private boolean isUpdatingUI = false;

    private RosterEntry rosterEntry;

    /**
     * Constructor
     * @param throttleManager the throttle manager
     */
    public AddressPanel(ThrottleManager throttleManager) {
        this.throttleManager = throttleManager;
        consistManager = InstanceManager.getNullableDefault(jmri.ConsistManager.class);
        initGUI();
        applyPreferences();
    }

    public void destroy() { // Handle disposing of the throttle
        if (conRosterBox != null && conRosterBox instanceof ConsistComboBox) {
            ((ConsistComboBox)conRosterBox).dispose();
        }
        if ( requestedAddress != null ) {
            throttleManager.cancelThrottleRequest(requestedAddress, this);
            requestedAddress = null;
        }
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
            throttleManager.releaseThrottle(throttle, this);
            notifyListenersOfThrottleRelease();
            throttle = null;
        }
        if (consistThrottle != null) {
            consistThrottle.removePropertyChangeListener(this);
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
            listeners = new ArrayList<>();
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
        if (isUpdatingUI) {
            return;
        }
        if (getRosterEntrySelector().getSelectedRosterEntries().length != 0) {
            setRosterEntry(getRosterEntrySelector().getSelectedRosterEntries()[0]);
            consistAddress = null;
        }
    }

    /**
     * Get notification that a throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        log.debug("Throttle found :  {} ", t.getLocoAddress());
        // is this a consist address?
        if (consistAddress == null && consistManager != null && consistManager.isEnabled() && consistManager.getConsistList().contains(t.getLocoAddress())) { 
            // we found a consist with this address, this is a consist
            consistAddress = (DccLocoAddress) t.getLocoAddress();
        }
        if (consistAddress != null && t.getLocoAddress().equals(consistAddress)) {
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
                JmriJOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("AddressInUse"));
                requestedAddress = null;
            }
            return;
        }

        requestedAddress = null;
        currentAddress = (DccLocoAddress) t.getLocoAddress();
        // can we find a roster entry?
        if ((rosterEntry == null)
                && (InstanceManager.getDefault(ThrottlesPreferences.class).isUsingExThrottle())
                && (InstanceManager.getDefault(ThrottlesPreferences.class).isEnablingRosterSearch())
                && currentAddress != null) {
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null, "" + currentAddress.getNumber(), null, null, null, null);
            if (!l.isEmpty()) {
                rosterEntry = l.get(0);
            }
        }
        
        if (consistAddress != null) {
            // if we get there, it means we got the throttle for the head locomotive of a consist
            // only update the function panel
            log.debug("Advanced consist throttle, got the throttle for the head locomotive functions control");
            (new ArrayList<AddressListener>(listeners)).forEach((l) -> {
                if (l instanceof FunctionPanel) {
                    l.notifyAddressThrottleFound(t);
                }
            });
            return;
        }
        
        if (throttle != null) {
            log.debug("notifyThrottleFound() throttle non null, called for loc {}",t.getLocoAddress());
            return;
        }  
        
        throttle = t;        
        throttle.addPropertyChangeListener(this);

        // update GUI
        updateGUIOnThrottleFound(true);
        
        // send notification of new address
        // work on a copy because some new listeners may be added while notifying the existing ones        
        (new ArrayList<AddressListener>(listeners)).forEach((l) -> {
            // log.debug("Notify address listener of address change {}", l.getClass());
            l.notifyAddressThrottleFound(t);
        });
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        JmriJOptionPane.showMessageDialog(null, reason, Bundle.getMessage("FailedSetupRequestTitle"), JmriJOptionPane.WARNING_MESSAGE);
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
                        if ( JmriJOptionPane.YES_OPTION == JmriJOptionPane.showConfirmDialog(
                                this, Bundle.getMessage("StealQuestionText",address.toString()),
                                Bundle.getMessage("StealRequestTitle"), JmriJOptionPane.YES_NO_OPTION)) {
                            throttleManager.responseThrottleDecision(address, this, DecisionType.STEAL );
                        } else {
                            throttleManager.cancelThrottleRequest(address, this);
                            requestedAddress = null;
                        }
                    });
                    break;
                case SHARE:
                    if (InstanceManager.getDefault(ThrottlesPreferences.class).isSilentShare() ){
                        throttleManager.responseThrottleDecision(address, this, DecisionType.SHARE );
                        return;
                    }
                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        if ( JmriJOptionPane.YES_OPTION == JmriJOptionPane.showConfirmDialog(
                                this, Bundle.getMessage("ShareQuestionText",address.toString()),
                                Bundle.getMessage("ShareRequestTitle"), JmriJOptionPane.YES_NO_OPTION)) {
                            throttleManager.responseThrottleDecision(address, this, DecisionType.SHARE );
                        } else {
                            throttleManager.cancelThrottleRequest(address, this);
                            requestedAddress = null;
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
                    String[] options = new String[] {Bundle.getMessage("StealButton"), Bundle.getMessage("ShareButton"), Bundle.getMessage("ButtonCancel")};
                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        int response = JmriJOptionPane.showOptionDialog(AddressPanel.this,
                                Bundle.getMessage("StealShareQuestionText",address.toString()), Bundle.getMessage("StealShareRequestTitle"),
                                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.QUESTION_MESSAGE, null, options, options[1]);
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
                                requestedAddress = null;
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
        if (consistThrottle != null) {
            log.debug("notifyConsistThrottleFound() consistThrottle non null, called for loc {}",t.getLocoAddress());
            return;
        }        
        requestedAddress = null;
        consistThrottle = t;
        currentAddress = (DccLocoAddress) t.getLocoAddress();
        consistThrottle.addPropertyChangeListener(this);
        
        Consist consist = getConsistEntry();
        if (consist != null && consist.getConsistType() == Consist.CS_CONSIST) {
            // CS Consist, consist has the head locomotive id
            // can we find a roster entry?
            if ((rosterEntry == null)
                    && (InstanceManager.getDefault(ThrottlesPreferences.class).isUsingExThrottle())
                    && (InstanceManager.getDefault(ThrottlesPreferences.class).isEnablingRosterSearch())
                    && currentAddress != null) {
                List<RosterEntry> l = Roster.getDefault().matchingList(null, null, "" + currentAddress.getNumber(), null, null, null, null);
                if (!l.isEmpty()) {
                    rosterEntry = l.get(0);
                }
            }
        }
        
        updateGUIOnThrottleFound(true);
                
        // send notification of new address
        // work on a clone because some new listeners may be added while notifying the existing ones
        (new ArrayList<AddressListener>(listeners)).forEach((l) -> {
            l.notifyConsistAddressThrottleFound(t);
        });
        
        if (consist != null && consist.getConsistType() == Consist.ADVANCED_CONSIST) {      
            // request a throttle for head locomotive for functions
            DccLocoAddress headLocoAddress = consist.getConsistList().get(0);
            // only if consist address is not head locomotive address
            if (! headLocoAddress.equals(currentAddress)) {
                log.debug("Advanced consist throttle, requesting secondary throttle for head locomotive function control.");
                changeOfAddress(headLocoAddress);
            }
        }
    }
    
    private void updateGUIOnThrottleFound(boolean throttleActive) {
        // update GUI
        isUpdatingUI = true;
        //addrSelector.setAddress(currentAddress);
        setButton.setEnabled(!throttleActive);
        addrSelector.setEnabled(!throttleActive);
        releaseButton.setEnabled(throttleActive);
        if (throttleActive && rosterEntry != null) {
            getRosterEntrySelector().setSelectedRosterEntry(rosterEntry);
        } else {
            getRosterEntrySelector().getRosterEntryComboBox().setSelectedItem(Bundle.getMessage("NoLocoSelected"));
        }
        getRosterEntrySelector().setEnabled(!throttleActive);
        if (conRosterBox != null) {
            if (throttleActive && consistThrottle != null) {
                conRosterBox.setSelectedItem(consistThrottle.getLocoAddress());
            } else {
                conRosterBox.setSelectedItem(Bundle.getMessage("NoConsistSelected"));
            }
            conRosterBox.setEnabled(!throttleActive);
        }     
        if (throttleManager.hasDispatchFunction()) {
            dispatchButton.setEnabled(throttleActive);
        }  
        // enable program button if programmer available
        // for ops-mode programming
        if ((rosterEntry != null) && (ProgDefault.getDefaultProgFile() != null)
                && (InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class) != null)
                && (InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).isAddressedModePossible())) {
            progButton.setEnabled(true);
        } else {
            progButton.setEnabled(false);
        }
        isUpdatingUI = false;
    }

    /**
     * Receive notification that an address has been release or dispatched.
     */
    public void notifyThrottleDisposed() {
        log.debug("notifyThrottleDisposed");
        notifyListenersOfThrottleRelease();
        updateGUIOnThrottleFound(false);
        rosterEntry = null;
        if (consistThrottle != null) {
            consistThrottle.removePropertyChangeListener(this);
        }
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
        }
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
     * Get the selected Consist if there's one for this throttle.
     *
     * @return Consist or null
     */    
    public Consist getConsistEntry() {
        if (consistManager == null || consistAddress == null || !consistManager.isEnabled()) {
            return null;
        }
        if (consistManager.getConsistList().contains(consistAddress)) {
            return consistManager.getConsist(consistAddress);
        }
        return null;
    }

    /**
     * Set the RosterEntry for this throttle and initiate a throttle request
     * @param entry roster entry to set.
     */
    public void setRosterEntry(RosterEntry entry) {
        isUpdatingUI = true;
        getRosterEntrySelector().setSelectedRosterEntry(entry);
        addrSelector.setAddress(entry.getDccLocoAddress());
        isUpdatingUI = false;
        rosterEntry = entry;
        changeOfAddress(addrSelector.getAddress());
    }

    /**
     * Create, initialize and place the GUI objects.
     */
    @SuppressWarnings("unchecked") //for the onRosterBox.insertItemAt(), to be a removed once NCE consists clarified
    private void initGUI() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        this.setContentPane(mainPanel);

        // center: address input
        addrSelector.setVariableSize(true);
        mainPanel.add(addrSelector.getCombinedJPanel(), BorderLayout.CENTER);
        addrSelector.getTextField().addActionListener(e -> {
            if (isUpdatingUI) {
                return;
            }
            consistAddress = null;
            changeOfAddress(addrSelector.getAddress());
        });

        // top : roster and consists selectors
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));

        rosterBox = new RosterEntrySelectorPanel();
        getRosterEntrySelector().setNonSelectedItem(Bundle.getMessage("NoLocoSelected"));
        getRosterEntrySelector().setToolTipText(Bundle.getMessage("SelectLocoFromRosterTT"));
        getRosterEntrySelector().addPropertyChangeListener("selectedRosterEntries", pce -> selectRosterEntry());
        getRosterEntrySelector().setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));
        topPanel.add(getRosterEntrySelector());

        if (InstanceManager.getDefault(NceConsistRoster.class).numEntries() > 0) { // NCE consists
            // NCE implementation of consists is specific, TODO: refactor to use generic JMRI consists
            conRosterBox = InstanceManager.getDefault(NceConsistRoster.class).fullRosterComboBox();
            conRosterBox.insertItemAt(Bundle.getMessage("NoConsistSelected"), 0);  // empty entry
            conRosterBox.setSelectedIndex(0);
            conRosterBox.setToolTipText(Bundle.getMessage("SelectConsistFromRosterTT"));
            conRosterBox.addActionListener(e -> nceConsistRosterSelected());
            topPanel.add(conRosterBox);
        } else {                      
            if ((consistManager != null) && (consistManager.isEnabled())) {  // JMRI consists
                JPanel consistPanel = new JPanel();
                JButton consistToolButton = new JButton(new jmri.jmrit.consisttool.ConsistToolAction());
                consistPanel.add(consistToolButton);
                conRosterBox = new ConsistComboBox();
                conRosterBox.addActionListener(e -> jmriConsistRosterSelected());
                consistPanel.add(conRosterBox);
                topPanel.add(consistPanel);
            }
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
            changeOfAddress(addrSelector.getAddress());
        });
        buttonPanel.add(setButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    private void jmriConsistRosterSelected() {
        if (isUpdatingUI) {
            return;
        }
        if ((conRosterBox.getSelectedIndex() != 0) && (conRosterBox.getSelectedItem() instanceof DccLocoAddress)) {
            consistAddress = (DccLocoAddress) conRosterBox.getSelectedItem() ;            
            changeOfConsistAddress();
        }
    }

    private void nceConsistRosterSelected() {
        if (isUpdatingUI) {
            return;
        }
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
                JmriJOptionPane.showMessageDialog(mainPanel,
                        Bundle.getMessage("ConsistNumberHasNotBeenAssigned"),
                        Bundle.getMessage("NeedsConsistNumber"),
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JmriJOptionPane.showConfirmDialog(mainPanel,
                    Bundle.getMessage("SendFunctionToLead"), Bundle.getMessage("NCEconsistThrottle"),
                    JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
                addrSelector.setAddress(consistAddress);
                consistAddress = null;
            }
            changeOfAddress(addrSelector.getAddress());
        }
    }

    /**
     * The user has selected a new address. Notify all listeners.
     */
    private void changeOfAddress(DccLocoAddress a) {
        currentAddress = a;
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
            requestedAddress = currentAddress;
            requestOK = throttleManager.requestThrottle(currentAddress, this, true);
        }
        else {
            requestedAddress = rosterEntry.getDccLocoAddress();
            requestOK = throttleManager.requestThrottle(rosterEntry, this, true);
        }
        if (!requestOK) {
            requestedAddress = null;
            JmriJOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("AddressInUse"));
        }
    }

    private void changeOfConsistAddress() {
        if (consistAddress == null) {
            return; // no address
        }  
        addrSelector.setAddress(consistAddress);
        // send notification of new address
        listeners.forEach((l) -> {
            l.notifyAddressChosen(currentAddress);
        });
        log.debug("Requesting new slot for consist address {}",consistAddress);        
        requestedAddress = consistAddress;
        boolean requestOK = throttleManager.requestThrottle(consistAddress, this, true);
        if (!requestOK) {
            requestedAddress = null;
            JmriJOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("AddressInUse"));
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
                JmriJOptionPane.showMessageDialog(mainPanel, Bundle.getMessage("CannotDispatch", usageCount));
                return;
            }
            notifyThrottleDisposed();
            throttleManager.dispatchThrottle(throttle, this);
            throttle = null;
        }
    }

    /**
     * Release the current address.
     */
    public void releaseAddress() {
        notifyThrottleDisposed();
        if (throttle != null) {
            throttleManager.releaseThrottle(throttle, this);
            throttle = null;
        }
        if (consistThrottle != null) {
            throttleManager.releaseThrottle(consistThrottle, this);
            consistThrottle = null;
        }
    }

    private void notifyListenersOfThrottleRelease() {
        if (listeners != null) {
            listeners.forEach((l) -> {
                // log.debug("Notify address listener {} of release", l.getClass());
                if (consistAddress != null) {
                    l.notifyConsistAddressReleased(consistAddress);
                }
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
            changeOfAddress(addrSelector.getAddress());
        }

        List<Element> elementList = e.getChildren("locoaddress");
        if ((!elementList.isEmpty()) && (getThrottle() == null)) {
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
            changeOfAddress(addrSelector.getAddress());
        }
    }

    /**
     * @return the RosterEntrySelectorPanel
     */
    public RosterEntrySelectorPanel getRosterEntrySelector() {
        return rosterBox;
    }

    /**
     * @return the curently assigned motor throttle for regular locomotives or consist
     */
    public DccThrottle getThrottle() {
        if (consistThrottle != null) {
            return consistThrottle;
        }
        return throttle;
    }
    
    /**
     * @return the curently assigned function throttle for regular locomotives or consist
     */
    public DccThrottle getFunctionThrottle() {        
        if (throttle != null) {
            return throttle;
        }
        return consistThrottle;
    }
        
    
    /**
     * @return the currently used decoder address
     */    
    public DccLocoAddress getCurrentAddress() {
        return currentAddress;
    }

    /**
     * set the currently used decoder address and initiate a throttle request
     * if a consist address is already set, this address will be used only for functions
     * 
     * @param currentAddress the address to use
     * 
     */ 
    public void setCurrentAddress(DccLocoAddress currentAddress) {
        if (log.isDebugEnabled()) {
            log.debug("Setting CurrentAddress to {}", currentAddress);
        }
        addrSelector.setAddress(currentAddress);
        changeOfAddress(addrSelector.getAddress());
    }
    
    /**
     * set the currently used decoder address and initiate a throttle request (same as setCurrentAddress)
     * if a consist address is already set, this address will be used only for functions
     * 
     * @param number the address
     * @param isLong long/short (true/false) address
     * 
     */ 
    public void setAddress(int number, boolean isLong) {
        setCurrentAddress(new DccLocoAddress(number, isLong));
    }

    /**
     * @return the current consist address if any
     */
    @CheckForNull
    public DccLocoAddress getConsistAddress() {
        return consistAddress;
    }

    /**
     * set the currently used consist address and initiate a throttle request
     * 
     * @param consistAddress the consist address to use
     */ 
    public void setConsistAddress(DccLocoAddress consistAddress) {
        log.debug("Setting Consist Address to {}", consistAddress);
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
                throttle = null;
                consistThrottle = null;
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddressPanel.class);

}

