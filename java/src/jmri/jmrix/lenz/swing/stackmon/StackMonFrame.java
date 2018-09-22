package jmri.jmrix.lenz.swing.stackmon;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This frame provides a method for searching the command station stack.
 * <p>
 * Current functionality is to search the stack and delete entries.
 * Future capabilities may include the ability to set the status of function buttons.
 *
 * @author Paul Bender Copyright (C) 2005-2010
 */
public class StackMonFrame extends jmri.util.JmriJFrame implements XNetListener {

    // buttons currently (4.8) not displayed
    JButton nextButton = new JButton(Bundle.getMessage("NextButtonLabel"));
    JButton previousButton = new JButton(Bundle.getMessage("PreviousButtonLabel"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton refreshButton = new JButton(Bundle.getMessage("RefreshButtonLabel"));
    JLabel currentStatus = new JLabel(" ");

    JTextField adrTextField = new javax.swing.JTextField(4);

    StackMonDataModel stackModel = null;
    javax.swing.JTable stackTable = null;

    // flag to know if Get All or Get Next/Previous was pressed
    private boolean _getAll = false;

    protected XNetTrafficController tc = null;

    public StackMonFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super();
        // Configure GUI components
        stackModel = new StackMonDataModel(1, 4, memo);
        stackTable = new javax.swing.JTable(stackModel);

        // Add listener object to retrieve the next entry
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getNextEntry();
            }
        });

        // Set the Next button to visible
        nextButton.setVisible(true);
        // add listener object to retrieve the previous entry
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getPreviousEntry();
            }
        });

        // Set the Previous button to visible.
        previousButton.setVisible(true);
        // The previous function is not currently implemented on the 
        // command station, so we're going to disable the button for now
        previousButton.setEnabled(false);

        // Set the Delete button to visible
        deleteButton.setVisible(true);
        // add listener object to remove the current entry
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteEntry();
            }
        });

        // Set the nextButton to visible
        refreshButton.setVisible(true);
        // add listener object to retrieve the next entry
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getAllEntries();
            }
        });

        // Set the adrTextField to visible
        adrTextField.setVisible(true);

        // general GUI config
        setTitle(Bundle.getMessage("MenuItemCSDatabaseManager"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(refreshButton);
        getContentPane().add(pane1);
        //pane1.setMaximumSize(pane1.getSize());

        JPanel manualPanel = new JPanel();
        manualPanel.setLayout(new FlowLayout());
        manualPanel.add(previousButton);
        manualPanel.add(nextButton);
        manualPanel.add(deleteButton);

        //getContentPane().add(manualPanel); // not working?
        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(adrTextField);
        //getContentPane().add(pane2); // not working?

        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(currentStatus);
        //getContentPane().add(pane3); // not working?

        // Set up the JTable in a Scroll Pane
        JScrollPane stackPane = new JScrollPane(stackTable);
        stackPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        stackModel.initTable(stackTable, this);
        getContentPane().add(stackPane);

        addHelpMenu("package.jmri.jmrix.lenz.stackmon.StackMonFrame", true);

        pack();

        tc = memo.getXNetTrafficController();

        tc.addXNetListener(~0, this);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
    }

    /**
     * Request ALL entries.
     */
    private void getAllEntries() {
        stackModel.clearData();
        _getAll = true;
        getNextEntry();
    }

    /**
     * Request the next entry.
     */
    private void getNextEntry() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
        }
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address, true);
        tc.sendXNetMessage(msg, this);
    }

    /**
     * Request the next entry by ID.
     */
    private void getNextEntry(int address) {
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address, true);
        tc.sendXNetMessage(msg, this);
    }

    /**
     * Request the previous entry.
     */
    private void getPreviousEntry() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
        }
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address, false);
        tc.sendXNetMessage(msg, this);
    }

    /**
     * Remove the current entry.
     */
    private void deleteEntry() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
            XNetMessage msg = XNetMessage.getDeleteAddressOnStackMsg(address);
            tc.sendXNetMessage(msg, this);
        }
    }

    /**
     * Request the status of the current address.
     */
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", 
        justification = "This is part of work in progress code to allow display of all information about the locomotives in the stack.")
    private void requestStatus() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
            XNetMessage msg = XNetMessage.getLocomotiveInfoRequestMsg(address);
            tc.sendXNetMessage(msg, this);
        }
    }

    /**
     * Request the momentary/continuous status of functions for the 
     * current address.
     */
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", 
            justification = "This is part of work in progress code to allow display of all information about the locomotives in the stack.")
    private void requestFunctionStatus() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
            XNetMessage msg = XNetMessage.getLocomotiveFunctionStatusMsg(address);
            tc.sendXNetMessage(msg, this);
        }
    }

    // The XNet Listener Interface

    /**
     * Receive information from the command station.
     */
    @Override
    public void message(XNetReply r) {
        if (r.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
            int address = r.getThrottleMsgAddr();
            Integer intAddress = Integer.valueOf(address);
            switch (r.getElement(1)) {
                case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                    currentStatus.setText(Bundle.getMessage("SearchNormal"));
                    adrTextField.setText("" + address);
                    stackModel.updateData(intAddress, Bundle.getMessage("SearchNormal"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                    currentStatus.setText(Bundle.getMessage("SearchDH"));
                    adrTextField.setText("" + r.getThrottleMsgAddr());
                    stackModel.updateData(intAddress, Bundle.getMessage("SearchDH"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                    currentStatus.setText(Bundle.getMessage("SearchMUBase"));
                    adrTextField.setText("" + r.getThrottleMsgAddr());
                    stackModel.updateData(intAddress, Bundle.getMessage("SearchMUBase"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                    currentStatus.setText(Bundle.getMessage("SearchMU"));
                    adrTextField.setText("" + r.getThrottleMsgAddr());
                    stackModel.updateData(intAddress, Bundle.getMessage("SearchMU"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_NO_RESULT:
                    currentStatus.setText(Bundle.getMessage("SearchFail"));
                    adrTextField.setText("" + r.getThrottleMsgAddr());
                    if (_getAll) {
                        _getAll = false;  //finished getting all entries
                    }
                    break;
                default:
                    if (log.isDebugEnabled()) {
                        log.debug("not search result");
                    }
            }
        }

    }

    /**
     * Receive information sent by the computer to the command station.
     */
    @Override
    public void message(XNetMessage m) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    // Register for logging
    private final static Logger log = LoggerFactory.getLogger(StackMonFrame.class);

}
