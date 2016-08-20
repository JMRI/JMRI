// StackMonFrame.java
package jmri.jmrix.lenz.swing.stackmon;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
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
 * This frame provides a method for searching the command station stack. Current
 * functionality is to search the stack and delete entries. Future capabilities
 * will include the ability to set the status of function buttons
 * <P>
 *
 * @author	Paul Bender Copyright (C) 2005-2010
 * @version	$Revision$
 */
public class StackMonFrame extends jmri.util.JmriJFrame implements XNetListener {

    /**
     *
     */
    private static final long serialVersionUID = 2129153656311593566L;
    JButton nextButton = new JButton("Next Entry");
    JButton previousButton = new JButton("Previous Entry");
    JButton deleteButton = new JButton("Delete Entry");
    JButton refreshButton = new JButton("Refresh");
    JLabel CurrentStatus = new JLabel(" ");

    JTextField adrTextField = new javax.swing.JTextField(4);

    StackMonDataModel stackModel = null;
    javax.swing.JTable stackTable = null;

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.stackmon.StackMonBundle");

    private boolean _getAll = false; // flag to know if get all or 
    // get next/previous was pressed

    protected XNetTrafficController tc = null;

    public StackMonFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super();
        // Configure GUI components
        stackModel = new StackMonDataModel(1, 4, memo);
        stackTable = new javax.swing.JTable(stackModel);

        // add listener object to retrieve the next entry
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getNextEntry();
            }
        });

        // Change the text on the nextButton according to the resource 
        // bundle
        nextButton.setText(rb.getString("NextButtonLabel"));

        // Set the nextButton to visible
        nextButton.setVisible(true);

        // add listener object to retrieve the previous entry
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getPreviousEntry();
            }
        });

        // Change the text on the previousButton according to the resource 
        // bundle
        previousButton.setText(rb.getString("PreviousButtonLabel"));

        // set the previous button to visible.
        previousButton.setVisible(true);

        // The previous function is not currently implemented on the 
        // command station, so we're going to disable the button for now
        previousButton.setEnabled(false);

        // Change the text on the deleteButton according to the resource 
        // bundle
        deleteButton.setText(rb.getString("DeleteButtonLabel"));

        // add listener object to remove the current entry
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteEntry();
            }
        });

        // Set the deleteButton to visible
        deleteButton.setVisible(true);

        // add listener object to retrieve the next entry
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getAllEntries();
            }
        });

        // Change the text on the nextButton according to the resource 
        // bundle
        refreshButton.setText(rb.getString("RefreshButtonLabel"));

        // Set the nextButton to visible
        refreshButton.setVisible(true);

        // Set the adrTextField to visible
        adrTextField.setVisible(true);

        // general GUI config
        setTitle(rb.getString("StackMonitorTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(refreshButton);
        getContentPane().add(pane1);
        //pane1.setMaximumSize(pane1.getSize());

        JPanel manualPanel = new JPanel();
        manualPanel.setLayout(new FlowLayout());
        manualPanel.add(nextButton);
        manualPanel.add(previousButton);
        manualPanel.add(deleteButton);

        //getContentPane().add(manualPanel);
        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(adrTextField);
        //getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(CurrentStatus);
	//getContentPane().add(pane3);

        // Set up the jtable in a Scroll Pane..
        JScrollPane stackPane = new JScrollPane(stackTable);
        stackPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        stackModel.initTable(stackTable, this);
        getContentPane().add(stackPane);

        addHelpMenu("package.jmri.jmrix.lenz.stackmon.StackMonFrame", true);

        pack();

        tc = memo.getXNetTrafficController();

        tc.addXNetListener(~0, this);
    }

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

    /*
     *  Request ALL entries
     */
    private void getAllEntries() {
        stackModel.clearData();
        _getAll = true;
        getNextEntry();
    }

    /*
     *  Request the next entry
     */
    private void getNextEntry() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
        }
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address, true);
        tc.sendXNetMessage(msg, this);
    }

    /*
     *  Request the next entry
     */
    private void getNextEntry(int address) {
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address, true);
        tc.sendXNetMessage(msg, this);
    }

    /*
     * Request the previous entry
     */
    private void getPreviousEntry() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
        }
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address, false);
        tc.sendXNetMessage(msg, this);
    }

    /* 
     * Remove the current entry
     */
    private void deleteEntry() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
            XNetMessage msg = XNetMessage.getDeleteAddressOnStackMsg(address);
            tc.sendXNetMessage(msg, this);
        }
    }

    /*
     * Request the status of the current address
     */
    @SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This is part of work in progress code to allow display of all information about the locomotives in the stack.")
    private void requestStatus() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
            XNetMessage msg = XNetMessage.getLocomotiveInfoRequestMsg(address);
            tc.sendXNetMessage(msg, this);
        }
    }

    /*
     * Request the momentary/continuous status of functions for the 
     * current address.
     */
    @SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "This is part of work in progress code to allow display of all information about the locomotives in the stack.")
    private void requestFunctionStatus() {
        int address = 0;
        if (!adrTextField.getText().equals("")) {
            address = Integer.parseInt(adrTextField.getText());
            XNetMessage msg = XNetMessage.getLocomotiveFunctionStatusMsg(address);
            tc.sendXNetMessage(msg, this);
        }
    }

    // The XNet Listener Interface
    // We need to be able to recieve information from the command station
    public void message(XNetReply r) {
        if (r.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
            int address = r.getThrottleMsgAddr();
            Integer intAddress = Integer.valueOf(address);
            switch (r.getElement(1)) {
                case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                    CurrentStatus.setText(rb.getString("SearchNormal"));
                    adrTextField.setText("" + address);
                    stackModel.updateData(intAddress, rb.getString("SearchNormal"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                    CurrentStatus.setText(rb.getString("SearchDH"));
                    adrTextField.setText("" + r.getThrottleMsgAddr());
                    stackModel.updateData(intAddress, rb.getString("SearchDH"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                    CurrentStatus.setText(rb.getString("SearchMUBase"));
                    adrTextField.setText("" + r.getThrottleMsgAddr());
                    stackModel.updateData(intAddress, rb.getString("SearchMUBase"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                    CurrentStatus.setText(rb.getString("SearchMU"));
                    adrTextField.setText("" + r.getThrottleMsgAddr());
                    stackModel.updateData(intAddress, rb.getString("SearchMU"));
                    // Request Address Status
                    // requestStatus();
                    // requestFunctionStatus();
                    if (_getAll) {
                        getNextEntry(address);
                    }
                    break;
                case XNetConstants.LOCO_SEARCH_NO_RESULT:
                    CurrentStatus.setText(rb.getString("SearchFail"));
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

    // We need to be able to receive information sent by the computer to 
    // the command station
    public void message(XNetMessage m) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    // Register for logging
    private final static Logger log = LoggerFactory.getLogger(StackMonFrame.class.getName());

}
