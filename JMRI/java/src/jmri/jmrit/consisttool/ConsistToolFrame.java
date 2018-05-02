package jmri.jmrit.consisttool;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame object for manipulating consists.
 *
 * @author Paul Bender Copyright (C) 2003-2008
 */
public class ConsistToolFrame extends jmri.util.JmriJFrame implements jmri.ConsistListener, jmri.ConsistListListener {

    // GUI member declarations
    JLabel textAdrLabel = new JLabel();
    DccLocoAddressSelector adrSelector = new DccLocoAddressSelector();
    JComboBox<Object> consistAdrBox = new JComboBox<>();
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
    ConsistDataModel consistModel = new ConsistDataModel(1, 4);
    JTable consistTable = new JTable(consistModel);
    ConsistManager consistManager = null;
    JLabel _status = new JLabel(Bundle.getMessage("DefaultStatusText"));
    private int _Consist_Type = Consist.ADVANCED_CONSIST;
    private ConsistFile consistFile = null;

    public ConsistToolFrame() {
        super();

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

        initializeConsistBox();

        consistAdrBox.addActionListener((ActionEvent e) -> {
            consistSelected();
        });

        consistAdrBox.setToolTipText(Bundle.getMessage("ConsistAddressBoxToolTip"));

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
        isCSConsist.setVisible(true);
        isCSConsist.setEnabled(false);
        isCSConsist.addActionListener((ActionEvent e) -> {
            isAdvancedConsist.setSelected(false);
            isCSConsist.setSelected(true);
            _Consist_Type = Consist.CS_CONSIST;
            if (consistManager.csConsistNeedsSeperateAddress()) {
                adrSelector.setEnabled(false);
            } else {
                adrSelector.setEnabled(true);
            }
        });

        if (consistManager.isCommandStationConsistPossible()) {
            isAdvancedConsist.setEnabled(true);
            isCSConsist.setEnabled(true);
        }

        deleteButton.setText(Bundle.getMessage("ButtonDelete"));
        deleteButton.setVisible(true);
        deleteButton.setToolTipText(Bundle.getMessage("DeleteButtonToolTip"));
        deleteButton.addActionListener((ActionEvent e) -> {
            deleteButtonActionPerformed(e);
        });

        throttleButton.setText(Bundle.getMessage("ThrottleButtonText"));
        throttleButton.setVisible(true);
        throttleButton.setToolTipText(Bundle.getMessage("ThrottleButtonToolTip"));
        throttleButton.addActionListener((ActionEvent e) -> {
            throttleButtonActionPerformed(e);
        });

        reverseButton.setText(Bundle.getMessage("ReverseButtonText"));
        reverseButton.setVisible(true);
        reverseButton.setToolTipText(Bundle.getMessage("ReverseButtonToolTip"));
        reverseButton.addActionListener((ActionEvent e) -> {
            reverseButtonActionPerformed(e);
        });

        restoreButton.setText(Bundle.getMessage("RestoreButtonText"));
        restoreButton.setVisible(true);
        restoreButton.setToolTipText(Bundle.getMessage("RestoreButtonToolTip"));
        restoreButton.addActionListener((ActionEvent e) -> {
            restoreButtonActionPerformed(e);
        });

        // Set up the controls for the First Locomotive in the consist.
        textLocoLabel.setText(Bundle.getMessage("LocoLabelText"));
        textLocoLabel.setVisible(true);

        locoSelector.setToolTipText(Bundle.getMessage("LocoSelectorToolTip"));
        locoSelector.setVisible(true);

        locoSelector.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // if we start typing, set the selected index of the locoRosterbox to nothing.
                locoRosterBox.setSelectedIndex(0);
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        locoRosterBox = new GlobalRosterEntryComboBox();
        locoRosterBox.setNonSelectedItem("");
        locoRosterBox.setSelectedIndex(0);

        locoRosterBox.addPropertyChangeListener("selectedRosterEntries", (PropertyChangeEvent pce) -> {
            locoSelected();
        });

        locoRosterBox.setVisible(true);

        locoDirectionNormal.setToolTipText(Bundle.getMessage("DirectionNormalToolTip"));

        locoDirectionNormal.setSelected(true);
        locoDirectionNormal.setVisible(true);
        locoDirectionNormal.setEnabled(false);

        addLocoButton.setText(Bundle.getMessage("AddButtonText"));
        addLocoButton.setVisible(true);
        addLocoButton.setToolTipText(Bundle.getMessage("AddButtonToolTip"));
        addLocoButton.addActionListener((ActionEvent e) -> {
            addLocoButtonActionPerformed(e);
        });

        resetLocoButton.setText(Bundle.getMessage("ButtonReset"));
        resetLocoButton.setVisible(true);
        resetLocoButton.setToolTipText(Bundle.getMessage("ResetButtonToolTip"));
        resetLocoButton.addActionListener((ActionEvent e) -> {
            resetLocoButtonActionPerformed(e);
        });

        // general GUI config
        setTitle(Bundle.getMessage("ConsistToolTitle"));
        //getContentPane().setLayout(new GridLayout(4,1));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        // The address and related buttons are installed in a single pane
        JPanel addressPanel = new JPanel();
        addressPanel.setLayout(new FlowLayout());

        addressPanel.add(textAdrLabel);
        addressPanel.add(adrSelector.getCombinedJPanel());
        addressPanel.add(consistAdrBox);
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

        // Set up the jtable in a Scroll Pane..
        JScrollPane consistPane = new JScrollPane(consistTable);
        consistPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
            consistAdrBox.removeAllItems();
            existingConsists.forEach((consist) -> consistAdrBox.addItem(consist));
            consistAdrBox.setEnabled(true);
            consistAdrBox.insertItemAt("", 0);
            consistAdrBox.setSelectedItem(adrSelector.getAddress());
            if (adrSelector.getAddress() != null) {
                if (consistModel.getConsist() != null) {
                    consistModel.getConsist().removeConsistListener(this);
                    _status.setText(Bundle.getMessage("DefaultStatusText"));
                }
                consistModel.setConsist(adrSelector.getAddress());
                consistModel.getConsist().addConsistListener(this);
                adrSelector.setEnabled(false);
            } else {
                if (consistModel.getConsist() != null) {
                    consistModel.getConsist().removeConsistListener(this);
                    _status.setText(Bundle.getMessage("DefaultStatusText"));
                }
                consistModel.setConsist((Consist) null);
                adrSelector.setEnabled(true);
            }
        } else {
            consistAdrBox.setEnabled(false);
            consistAdrBox.removeAllItems();
            consistAdrBox.insertItemAt("", 0);
            consistAdrBox.setSelectedIndex(0);
            if (consistModel.getConsist() != null) {
                consistModel.getConsist().removeConsistListener(this);
                _status.setText(Bundle.getMessage("DefaultStatusText"));
            }
            consistModel.setConsist((Consist) null);
            adrSelector.setEnabled(true);
        }
    }

    public void deleteButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NoConsistSelectedError"));
            return;
        }
        DccLocoAddress address = adrSelector.getAddress();
        consistManager.getConsist(address);
        // confirm delete
        if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("DeleteWarningDialog", address),
                Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
            return; // do not delete
        }
        /*
         * get the list of locomotives to delete
         */
 /*ArrayList<DccLocoAddress> addressList = tempConsist.getConsistList();
            addressList.forEach((locoaddress)->{
                if (log.isDebugEnabled()) {
                    log.debug("Deleting Locomotive: " + locoaddress.toString());
                }
                try {
                    tempConsist.remove(locoaddress);
                } catch (Exception ex) {
                    log.error("Error removing address "
                            + locoaddress.toString()
                            + " from consist "
                            + address.toString());
                }
            });*/
        try {
            consistManager.delConsist(address);
        } catch (Exception ex) {
            log.error("Error delting consist {}", address, ex);
        }
        adrSelector.reset();
        adrSelector.setEnabled(true);
        initializeConsistBox();
        try {
            consistFile.writeFile(consistManager.getConsistList());
        } catch (IOException ex) {
            log.warn("error writing consist file: {}", ex.getMessage());
        }
        resetLocoButtonActionPerformed(e);
        canAdd();
    }

    public void throttleButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NoConsistSelectedError"));
            return;
        }
        // make sure any new locomotives are added to the consist.
        addLocoButtonActionPerformed(e);
        // Create a throttle object with the
        ThrottleFrame tf
                = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
        DccLocoAddress address = adrSelector.getAddress();

        /*
         * get the lead locomotive from the list of locomotives so we can
         * register function button bindings in the throttle.
         */
        Consist tempConsist = consistManager.getConsist(address);
        ArrayList<DccLocoAddress> addressList = tempConsist.getConsistList();
        DccLocoAddress locoaddress = addressList.get(0);
        if (address != locoaddress) {
            log.debug("Consist Address {}, Lead Locomoitve  {}", address, locoaddress);
            // the consist address and the lead locomotive address differ,
            // register so the function buttons trigger the lead locomotive
            tf.getAddressPanel().setCurrentAddress(locoaddress);
        }
        // Notify the throttle of the selected consist address
        tf.getAddressPanel().setConsistAddress(address);
        tf.toFront();
    }

    public void reverseButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NoConsistSelectedError"));
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
    }

    public void restoreButtonActionPerformed(ActionEvent e) {
        if (adrSelector.getAddress() == null) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NoConsistSelectedError"));
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
    }

    public void consistSelected() {
        log.debug("Consist Selected");
        if (consistAdrBox.getSelectedIndex() == -1 && !(adrSelector.getAddress() == null)) {
            log.debug("No Consist Selected");
            adrSelector.setEnabled(false);
            recallConsist();
        } else if (consistAdrBox.getSelectedIndex() == -1
                || consistAdrBox.getSelectedItem().equals("")) {
            log.debug("Null Consist Selected");
            adrSelector.reset();
            adrSelector.setEnabled(true);
            recallConsist();
        } else if (((DccLocoAddress) consistAdrBox.getSelectedItem()) != adrSelector.getAddress()) {
            log.debug("Consist {} Selected", consistAdrBox.getSelectedItem());
            adrSelector.setEnabled(false);
            adrSelector.setAddress((DccLocoAddress) consistAdrBox.getSelectedItem());
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
                _status.setText(Bundle.getMessage("DefaultStatusText"));
            }
            consistModel.setConsist((Consist) null);

            canAdd();

            return;
        }
        DccLocoAddress address = adrSelector.getAddress();
        if (consistModel.getConsist() != null) {
            consistModel.getConsist().removeConsistListener(this);
            _status.setText(Bundle.getMessage("DefaultStatusText"));
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
        if (consistModel.getRowCount() == 0) {
            locoDirectionNormal.setEnabled(false);
        } else {
            locoDirectionNormal.setEnabled(true);
        }

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
        if (consistModel.getRowCount() == 0) {
            locoDirectionNormal.setEnabled(false);
        } else {
            locoDirectionNormal.setEnabled(true);
        }
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
                locoSelector.setEnabled(true);
                locoRosterBox.setEnabled(true);
                addLocoButton.setEnabled(true);
                resetLocoButton.setEnabled(true);
                locoDirectionNormal.setEnabled(false);
                // if there aren't any locomotives in the consist,
                // don't let the user change the direction
                if (consistModel.getRowCount() == 0) {
                    locoDirectionNormal.setEnabled(false);
                } else {
                    locoDirectionNormal.setEnabled(true);
                }
            }
        } else {
            locoSelector.setEnabled(true);
            locoRosterBox.setEnabled(true);
            addLocoButton.setEnabled(true);
            resetLocoButton.setEnabled(true);
            locoDirectionNormal.setEnabled(false);
            // if there aren't any locomotives in the consist, don't let
            // the user change the direction
            if (consistModel.getRowCount() == 0) {
                locoDirectionNormal.setEnabled(false);
            } else {
                locoDirectionNormal.setEnabled(true);
            }
        }
    }

    public void addLocoButtonActionPerformed(ActionEvent e) {
        if (locoSelector.getAddress() == null) {
            return;
        }
        if (_Consist_Type == Consist.ADVANCED_CONSIST && adrSelector.getAddress() == null) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NoConsistSelectedError"));
            return;
        } else if (_Consist_Type == Consist.ADVANCED_CONSIST
                && adrSelector.getAddress().isLongAddress()) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("RequiresShortConsistError"));
            return;
        } else if (_Consist_Type == Consist.CS_CONSIST && adrSelector.getAddress() == null) {
            if (consistManager.csConsistNeedsSeperateAddress()) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("NoConsistSelectedError"));
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
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("AddressNotAllowedError"));
        } else {
            if (consistManager.getConsist(address).contains(locoaddress)) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("AddressAlreadyInConsistError"));
            } else {
                Consist tempConsist = consistManager.getConsist(address);
                tempConsist.add(locoaddress, locoDirectionNormal.isSelected());
                if (locoRosterBox.getSelectedRosterEntries().length == 1) {
                    tempConsist.setRosterId(locoaddress, locoRosterBox.getSelectedRosterEntries()[0].titleString());
                }

            }
            if (consistAdrBox.getSelectedItem() != adrSelector.getAddress()) {
                initializeConsistBox();
            }
            consistModel.fireTableDataChanged();
            resetLocoButtonActionPerformed(e);
        }
    }

    public void locoSelected() {
        if (locoRosterBox.getSelectedRosterEntries().length == 1) {
            locoSelector.setAddress(locoRosterBox.getSelectedRosterEntries()[0].getDccLocoAddress());
        }
    }

    /*
     * we're registering as a listener for Consist events, so we need to
     * implement the interface
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

    // want to read the consist file after the consists have been loaded
    // from the command station.  The _readConsistFile flag tells the
    // notifyConsistListChanged routine to do this.  notifyConsistListChanged
    // sets the value to false after the file is read.
    private boolean _readConsistFile = true;

    // ConsistListListener interface
    @Override
    public void notifyConsistListChanged() {
        if (_readConsistFile) {
            // read the consist file after the consist manager has
            // finished loading consists on startup.
            try {
                consistFile.readFile();
            } catch (IOException | JDOMException e) {
                log.warn("error reading consist file: {}", e.getMessage());
            }
            _readConsistFile = false;
        }
        // update the consist list.
        initializeConsistBox();
    }

    private final static Logger log = LoggerFactory.getLogger(ConsistToolFrame.class);

}
