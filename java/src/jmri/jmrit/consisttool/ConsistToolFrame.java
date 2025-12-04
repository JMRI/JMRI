package jmri.jmrit.consisttool;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

import jmri.Consist;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.ConsistManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.util.JmriJFrame;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.JDOMException;

/**
 * Frame object for manipulating consists.
 *
 * @author Paul Bender Copyright (C) 2003-2008
 */
public class ConsistToolFrame extends JmriJFrame implements ConsistListener, ConsistListListener {

    // GUI member declarations
    JLabel textAdrLabel = new JLabel();
    DccLocoAddressSelector adrSelector = new DccLocoAddressSelector();
    ConsistComboBox consistComboBox = new ConsistComboBox();
    JRadioButton isAdvancedConsist = new JRadioButton(Bundle.getMessage("AdvancedConsistButtonText"));
    JRadioButton isCSConsist = new JRadioButton(Bundle.getMessage("CommandStationConsistButtonText"));
    JButton deleteButton = new JButton();
    JButton throttleButton = new JButton();
    JButton reverseButton = new JButton();
    JButton restoreButton = new JButton();
    JLabel textLocoLabel = new JLabel();
    DccLocoAddressSelector locoSelector = new DccLocoAddressSelector();
    RosterEntryComboBox locoRosterBox;
    JButton addLocoButton = new JButton();
    JButton resetLocoButton = new JButton();
    JCheckBox locoDirectionNormal = new JCheckBox(Bundle.getMessage("DirectionNormalText"));
    ConsistDataModel consistModel = new ConsistDataModel();
    JTable consistTable = new JTable(consistModel);
    ConsistManager consistManager = null;
    JLabel _status = new JLabel(Bundle.getMessage("DefaultStatusText"));
    private int _Consist_Type = Consist.ADVANCED_CONSIST;
    private ConsistFile consistFile = null;

    public ConsistToolFrame() {
        super();
        init();
    }

    private void init() {
        consistManager = InstanceManager.getDefault(jmri.ConsistManager.class);

        consistFile = new ConsistFile();
        try {
            consistFile.readFile();
        } catch (IOException | JDOMException e) {
            log.warn("error reading consist file: {}", e.getMessage());
        }

        // register to be notified if the consist list changes.
        consistManager.addConsistListListener(this);

        // request an update from the layout.
        consistManager.requestUpdateFromLayout();

        // configure items for GUI
        textAdrLabel.setText(Bundle.getMessage("AddressLabelText"));
        textAdrLabel.setVisible(true);

        adrSelector.setVisible(true);
        adrSelector.setToolTipText(Bundle.getMessage("AddressSelectorToolTip"));
        textAdrLabel.setLabelFor(adrSelector);

        initializeConsistBox();

        consistComboBox.addActionListener((ActionEvent e) -> consistSelected());

        if (consistManager.isAdvancedConsistPossible()) {
            isAdvancedConsist.setSelected(true);
            isAdvancedConsist.setVisible(true);
            isAdvancedConsist.setEnabled(false);
            isAdvancedConsist.addActionListener((ActionEvent e) -> {
                isAdvancedConsist.setSelected(true);
                isCSConsist.setSelected(false);
                _Consist_Type = Consist.ADVANCED_CONSIST;
                adrSelector.setEnabled(true);
            });
            isCSConsist.setSelected(false);
        } else {
            isAdvancedConsist.setSelected(false);
            isAdvancedConsist.setVisible(false);
            isCSConsist.setSelected(true);
            _Consist_Type = Consist.CS_CONSIST;
            adrSelector.setEnabled((consistManager.csConsistNeedsSeperateAddress()));
        }

        isCSConsist.setVisible(true);
        isCSConsist.setEnabled(false);
        isCSConsist.addActionListener((ActionEvent e) -> {
            isAdvancedConsist.setSelected(false);
            isCSConsist.setSelected(true);
            _Consist_Type = Consist.CS_CONSIST;
            adrSelector.setEnabled((consistManager.csConsistNeedsSeperateAddress()));
        });

        if (consistManager.isCommandStationConsistPossible()) {
            isAdvancedConsist.setEnabled(true);
            isCSConsist.setEnabled(true);
        }

        // link the protocol selectors if required by ConsistManager
        if (consistManager.isSingleFormConsistRequired()) {
            locoSelector.followAnotherSelector(adrSelector);
        }
        
        deleteButton.setText(Bundle.getMessage("ButtonDelete"));
        deleteButton.setVisible(true);
        deleteButton.setToolTipText(Bundle.getMessage("DeleteButtonToolTip"));
        deleteButton.addActionListener(this::deleteButtonActionPerformed);

        throttleButton.setText(Bundle.getMessage("ThrottleButtonText"));
        throttleButton.setVisible(true);
        throttleButton.setToolTipText(Bundle.getMessage("ThrottleButtonToolTip"));
        throttleButton.addActionListener(this::throttleButtonActionPerformed);

        reverseButton.setText(Bundle.getMessage("ReverseButtonText"));
        reverseButton.setVisible(true);
        reverseButton.setToolTipText(Bundle.getMessage("ReverseButtonToolTip"));
        reverseButton.addActionListener(this::reverseButtonActionPerformed);

        restoreButton.setText(Bundle.getMessage("RestoreButtonText"));
        restoreButton.setVisible(true);
        restoreButton.setToolTipText(Bundle.getMessage("RestoreButtonToolTip"));
        restoreButton.addActionListener(this::restoreButtonActionPerformed);

        // Set up the controls for the First Locomotive in the consist.
        textLocoLabel.setText(Bundle.getMessage("LocoLabelText"));
        textLocoLabel.setVisible(true);

        locoSelector.setToolTipText(Bundle.getMessage("LocoSelectorToolTip"));
        locoSelector.setVisible(true);
        textLocoLabel.setLabelFor(locoSelector);

        locoSelector.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ( !consistManager.isSingleFormConsistRequired()) {
                    // if combo boxes are not locked together, 
                    // and if user start typing, set the selected index of the locoRosterbox to nothing
                    // to get the user to make a decision
                    locoRosterBox.setSelectedIndex(0);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // only handling key presses
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // only handling key presses
            }
        });

        locoRosterBox = new GlobalRosterEntryComboBox();
        locoRosterBox.setNonSelectedItem("");
        locoRosterBox.setSelectedIndex(0);

        locoRosterBox.addPropertyChangeListener("selectedRosterEntries", (PropertyChangeEvent pce) -> locoSelected());

        locoRosterBox.setVisible(true);

        locoDirectionNormal.setToolTipText(Bundle.getMessage("DirectionNormalToolTip"));

        locoDirectionNormal.setSelected(true);
        locoDirectionNormal.setVisible(true);
        locoDirectionNormal.setEnabled(false);

        addLocoButton.setText(Bundle.getMessage("ButtonAddText"));
        addLocoButton.setVisible(true);
        addLocoButton.setToolTipText(Bundle.getMessage("AddButtonToolTip"));
        addLocoButton.addActionListener(this::addLocoButtonActionPerformed);

        resetLocoButton.setText(Bundle.getMessage("ButtonReset"));
        resetLocoButton.setVisible(true);
        resetLocoButton.setToolTipText(Bundle.getMessage("ResetButtonToolTip"));
        resetLocoButton.addActionListener(this::resetLocoButtonActionPerformed);

        // general GUI config
        setTitle(Bundle.getMessage("ConsistToolTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // add a "File" menu
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);

        // Add a save item
        fileMenu.add(new AbstractAction(Bundle.getMessage("ScanConsists")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                scanRoster();
                initializeConsistBox();
                consistModel.fireTableDataChanged();
                resetLocoButtonActionPerformed(e);
            }
        });

        // install items in GUI
        // The address and related buttons are installed in a single pane
        JPanel addressPanel = new JPanel();
        addressPanel.setLayout(new FlowLayout());

        addressPanel.add(textAdrLabel);
        addressPanel.add(adrSelector.getCombinedJPanel());
        addressPanel.add(consistComboBox);
        addressPanel.add(isAdvancedConsist);
        addressPanel.add(isCSConsist);

        getContentPane().add(addressPanel);

        // The address and related buttons for each Locomotive
        // are installed in a single pane
        // New Locomotive
        JPanel locoPanel = new JPanel();
        locoPanel.setLayout(new FlowLayout());

        locoPanel.add(textLocoLabel);

        locoPanel.add(locoSelector.getCombinedJPanel());

        locoPanel.add(locoRosterBox);
        locoPanel.add(locoDirectionNormal);

        locoPanel.add(addLocoButton);
        locoPanel.add(resetLocoButton);

        getContentPane().add(locoPanel);

        // setup the consist table
        consistTable.setRowHeight(InstanceManager.getDefault(GuiLafPreferencesManager.class).getFontSize()*2 + 4);
        // Set up the jtable in a Scroll Pane..
        JScrollPane consistPane = new JScrollPane(consistTable);
        consistPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        consistModel.initTable(consistTable);
        getContentPane().add(consistPane);

        // Set up the Control Button panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        controlPanel.add(deleteButton);
        controlPanel.add(throttleButton);
        controlPanel.add(reverseButton);
        controlPanel.add(restoreButton);

        getContentPane().add(controlPanel);

        // add the status line directly to the bottom of the ContentPane.
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout());
        statusPanel.add(_status);
        getContentPane().add(statusPanel);

        addHelpMenu("package.jmri.jmrit.consisttool.ConsistToolFrame", true);
        pack();

    }

    private void initializeConsistBox() {
        ArrayList<LocoAddress> existingConsists = consistManager.getConsistList();
        if (!existingConsists.isEmpty()) {
            java.util.Collections.sort(existingConsists, new jmri.util.LocoAddressComparator()); // sort the consist list.
            if (adrSelector.getAddress() != null) {
                if (consistModel.getConsist() != null) {
                    consistModel.getConsist().removeConsistListener(this);
                    setDefaultStatus();
                }
                consistModel.setConsist(adrSelector.getAddress());
                consistModel.getConsist().addConsistListener(this);
                adrSelector.setEnabled(false);
            } else {
                if (consistModel.getConsist() != null) {
                    consistModel.getConsist().removeConsistListener(this);
                    setDefaultStatus();
                }
                consistModel.setConsist((Consist) null);
                adrSelector.setEnabled(true);
            }
        } else {
            if (consistModel.getConsist() != null) {
                consistModel.getConsist().removeConsistListener(this);
                setDefaultStatus();
            }
            consistModel.setConsist((Consist) null);
            adrSelector.setEnabled(true);
        }
    }

    public void deleteButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            reportNoConsistSeletected();
            return;
        }
        DccLocoAddress address = adrSelector.getAddress();
        consistManager.getConsist(address);
        // confirm delete
        if (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("DeleteWarningDialog", address),
                Bundle.getMessage("QuestionTitle"), JmriJOptionPane.YES_NO_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE) != JmriJOptionPane.YES_OPTION ) {
            return; // do not delete
        }
        try {
            adrSelector.reset();
            consistManager.delConsist(address);
        } catch (Exception ex) {
            log.error("Error delting consist {}", address, ex);
        }        
        adrSelector.setEnabled(true);
        initializeConsistBox();
        resetLocoButtonActionPerformed(e);
        canAdd();
    }

    public void throttleButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            reportNoConsistSeletected();
            return;
        }
        // make sure any new locomotives are added to the consist.
        addLocoButtonActionPerformed(e);
        // Create a throttle object with the
        ThrottleFrame tf
                = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();        

        // Notify the throttle of the selected consist address
        tf.getAddressPanel().setConsistAddress(adrSelector.getAddress());
        tf.toFront();
    }

    public void reverseButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            reportNoConsistSeletected();
            return;
        }
        // make sure any new locomotives are added to the consist.
        addLocoButtonActionPerformed(e);

        /*
         * get the array list of the locomotives in the consist
         */
        DccLocoAddress address = adrSelector.getAddress();
        Consist tempConsist = consistManager.getConsist(address);
        tempConsist.reverse();
        consistManager.notifyConsistListChanged();
    }

    public void restoreButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            reportNoConsistSeletected();
            return;
        }
        // make sure any new locomotives are added to the consist.
        addLocoButtonActionPerformed(e);

        /*
         * get the array list of the locomotives in the consist
         */
        DccLocoAddress address = adrSelector.getAddress();
        Consist tempConsist = consistManager.getConsist(address);
        tempConsist.restore();
        consistManager.notifyConsistListChanged();
    }

    public void consistSelected() {
        log.debug("Consist Selected");
        if (consistComboBox.getSelectedIndex() == -1 && adrSelector.getAddress() != null) {
            log.debug("No Consist Selected");
            adrSelector.setEnabled(false);
            recallConsist();
        } else if (consistComboBox.getSelectedIndex() == -1
                || consistComboBox.getSelectedItem().equals("") 
                || consistComboBox.getSelectedItem().equals(Bundle.getMessage("NoConsistSelected"))) {
            log.debug("Null Consist Selected");
            adrSelector.reset();
            adrSelector.setEnabled(true);
            recallConsist();
        } else if (((DccLocoAddress) consistComboBox.getSelectedItem()) != adrSelector.getAddress()) {
            log.debug("Consist {} consistComboBox", consistComboBox.getSelectedItem());
            adrSelector.setEnabled(false);
            adrSelector.setAddress((DccLocoAddress) consistComboBox.getSelectedItem());
            recallConsist();
        }
    }

    // Recall the consist
    private void recallConsist() {
        if (adrSelector.getAddress() == null) {
            // Clear any consist information that was present
            locoSelector.reset();
            locoRosterBox.setSelectedIndex(0);
            if (consistModel.getConsist() != null) {
                consistModel.getConsist().removeConsistListener(this);
                setDefaultStatus();
            }
            consistModel.setConsist((Consist) null);

            canAdd();

            return;
        }
        DccLocoAddress address = adrSelector.getAddress();
        if (consistModel.getConsist() != null) {
            consistModel.getConsist().removeConsistListener(this);
            _status.setText(Bundle.getMessage("DefaultStatusText"));
            setDefaultStatus();
        }
        Consist selectedConsist = consistManager.getConsist(address);
        consistModel.setConsist(selectedConsist);
        selectedConsist.addConsistListener(this);

        // reset the editable locomotive information.
        locoSelector.reset();
        locoRosterBox.setSelectedIndex(0);
        locoDirectionNormal.setSelected(true);

        // if there aren't any locomotives in the consist, don't let
        // the user change the direction
        locoDirectionNormal.setEnabled(consistModel.getRowCount()!=0);

        log.debug("Recall Consist {}", address);

        // What type of consist is this?
        if (selectedConsist.getConsistType() == Consist.ADVANCED_CONSIST) {
            log.debug("Consist type is Advanced Consist ");
            isAdvancedConsist.setSelected(true);
            isCSConsist.setSelected(false);
            _Consist_Type = Consist.ADVANCED_CONSIST;
        } else {
            // This must be a CS Consist.
            log.debug("Consist type is Command Station Consist ");
            isAdvancedConsist.setSelected(false);
            isCSConsist.setSelected(true);
            _Consist_Type = Consist.CS_CONSIST;
        }

        canAdd();
    }

    public void resetLocoButtonActionPerformed(ActionEvent e) {
        locoSelector.reset();
        locoRosterBox.setSelectedIndex(0);
        locoDirectionNormal.setSelected(true);
        // if there aren't any locomotives in the consist, don't let
        // the user change the direction
        locoDirectionNormal.setEnabled(consistModel.getRowCount() != 0);
    }

    // Check to see if a consist address is selected, and if it
    // is, dissable the "add button" if the maximum consist size is reached
    public void canAdd() {
        // If a consist address is selected, dissable the "add button"
        // if the maximum size is reached
        if (adrSelector.getAddress() != null) {
            DccLocoAddress address = adrSelector.getAddress();
            if (consistModel.getRowCount() == consistManager.getConsist(address).sizeLimit()) {
                locoSelector.setEnabled(false);
                locoRosterBox.setEnabled(false);
                addLocoButton.setEnabled(false);
                resetLocoButton.setEnabled(false);
                locoDirectionNormal.setEnabled(false);
            } else {
                enableGuiControls();
            }
        } else {
            enableGuiControls();
        }
    }

    private void enableGuiControls(){
        locoSelector.setEnabled(true);
        locoRosterBox.setEnabled(true);
        addLocoButton.setEnabled(true);
        resetLocoButton.setEnabled(true);
        locoDirectionNormal.setEnabled(false);
        // if there aren't any locomotives in the consist, don't let
        // the user change the direction
        locoDirectionNormal.setEnabled(consistModel.getRowCount() != 0);
    }

    public void addLocoButtonActionPerformed(ActionEvent e) {
        if (locoSelector.getAddress() == null) {
            return;
        }
        if (_Consist_Type == Consist.ADVANCED_CONSIST && adrSelector.getAddress() == null) {
            reportNoConsistSeletected();
            return;
        } else if (_Consist_Type == Consist.ADVANCED_CONSIST
                && adrSelector.getAddress().isLongAddress()) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("RequiresShortConsistError"));
            return;
        } else if (_Consist_Type == Consist.CS_CONSIST && adrSelector.getAddress() == null) {
            if (consistManager.csConsistNeedsSeperateAddress()) {
                reportNoConsistSeletected();
                return;
            } else {
                // We need to set an identifier so we can recall the
                // consist.  We're going to use the lead locomotive number
                // for this.
                adrSelector.setAddress(locoSelector.getAddress());
            }
        }
        DccLocoAddress address = adrSelector.getAddress();
        /*
         * Make sure the marked consist type matches the consist type stored for
         * this consist
         */
        if (_Consist_Type != consistManager.getConsist(address).getConsistType()) {
            if (log.isDebugEnabled()) {
                if (_Consist_Type == Consist.ADVANCED_CONSIST) {
                    log.debug("Setting Consist Type to Advanced Consist");
                } else if (_Consist_Type == Consist.CS_CONSIST) {
                    log.debug("Setting Consist Type to Command Station Assisted Consist");
                }
            }
            consistManager.getConsist(address).setConsistType(_Consist_Type);
        }

        DccLocoAddress locoaddress = locoSelector.getAddress();

        // Make sure the Address in question is allowed for this type of
        // consist, and add it to the consist if it is
        if (!consistManager.getConsist(address).isAddressAllowed(locoaddress)) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("AddressNotAllowedError"));
            return;
        }
        if (consistManager.getConsist(address).contains(locoaddress)) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("AddressAlreadyInConsistError"));
            return;
        } 
            
        Consist tempConsist = consistManager.getConsist(address);
        tempConsist.add(locoaddress, locoDirectionNormal.isSelected());
        
        // Try to get a roster entry
        RosterEntry re = null;
        if (locoRosterBox.getSelectedRosterEntries().length == 1) {
            re = locoRosterBox.getSelectedRosterEntries()[0];
        } else {
            List<RosterEntry> res = Roster.getDefault().matchingList(null, null, "" + locoaddress.getNumber(), null, null, null, null);
            if (!res.isEmpty()) {
                re = res.get(0);
            }
        }
                        
        if (re != null) {    
            tempConsist.setRosterId(locoaddress, re.titleString());
        }        
            
        if (consistComboBox.getSelectedItem() != adrSelector.getAddress()) {
            initializeConsistBox();
            consistComboBox.setSelectedItem(adrSelector.getAddress());
        }
        consistManager.notifyConsistListChanged();
        consistModel.fireTableDataChanged();
        resetLocoButtonActionPerformed(e);        
    }

    public void locoSelected() {
        if (locoRosterBox.getSelectedRosterEntries().length == 1) {
            locoSelector.setAddress(locoRosterBox.getSelectedRosterEntries()[0].getDccLocoAddress());
        }
    }

    /**
     * we're registering as a listener for Consist events, so we need to
     * implement the interface.
     * {@inheritDoc}
     */
    @Override
    public void consistReply(LocoAddress locoaddress, int status) {
        log.debug("Consist Reply received for Locomotive {} with status {}", locoaddress, status);
        _status.setText(consistManager.decodeErrorCode(status));
        // For some status codes, we want to trigger specific actions
        //if((status & jmri.ConsistListener.CONSIST_FULL)!=0) {
        // canAdd();
        //} else {
        canAdd();
        //}
        consistModel.fireTableDataChanged();
        try {
            consistFile.writeFile(consistManager.getConsistList());
        } catch (IOException e) {
            log.warn("error writing consist file: {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        // de-register to be notified if the consist list changes.
        consistManager.removeConsistListListener(this);
    }

    // ConsistListListener interface
    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyConsistListChanged() {
        // Save consist file
        try {
            consistFile.writeFile(consistManager.getConsistList());
        } catch (IOException e) {
            log.warn("error writing consist file: {}", e.getMessage());
        }  
        // update the consist list.
        initializeConsistBox();
    }

    /**
     * private method to scan the roster for consists
     */
    private void scanRoster(){
       List<RosterEntry> roster = Roster.getDefault().getAllEntries();
       for(RosterEntry entry:roster){
            DccLocoAddress address = entry.getDccLocoAddress();
            CvTableModel  cvTable = new CvTableModel(_status, null);  // will hold CV objects
            entry.readFile();  // read, but don't yet process

            entry.loadCvModel(null, cvTable);
            CvValue cv19Value = cvTable.getCvByNumber("19");
            if(cv19Value!=null && (cv19Value.getValue() & 0x7F)!=0){
                boolean direction = ((cv19Value.getValue()&0x80)==0);
                DccLocoAddress consistAddress = new DccLocoAddress((cv19Value.getValue()&0x7f),false);
                /*
                 * Make sure the marked consist type is an advanced consist.
                 * this consist
                  */
                Consist consist = consistManager.getConsist(consistAddress);
                if (Consist.ADVANCED_CONSIST != consist.getConsistType()) {
                    consist.setConsistType(Consist.ADVANCED_CONSIST);
                }

                if (!consist.contains(address)) {
                   consist.add(address, direction );
                   consist.setRosterId(address, entry.titleString());
                }
            }
       }
    }

    private void reportNoConsistSeletected(){
        JmriJOptionPane.showMessageDialog(this,
                Bundle.getMessage("NoConsistSelectedError"));

    }

    public void setDefaultStatus() {
        _status.setText(Bundle.getMessage("DefaultStatusText"));
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConsistToolFrame.class);

}
