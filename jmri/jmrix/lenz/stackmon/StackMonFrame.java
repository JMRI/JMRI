// StackMonFrame.java

package jmri.jmrix.lenz.stackmon;

import jmri.jmrix.lenz.*;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;



/**
 * This frame provides a method for searching the command station stack.
 * Current functionality is to search the stack and delete entries.  
 * Future capabilities will include the ability to set the status of 
 * function buttons
 * <P>
 *
 * @author	Paul Bender   Copyright (C) 2005
 * @version	$Revision: 1.1 $
 */
public class StackMonFrame extends jmri.util.JmriJFrame implements XNetListener {

    JButton nextButton  = new JButton("Next Entry");
    JButton previousButton  = new JButton("Previous Entry");
    JButton deleteButton  = new JButton("Delete Entry");
    JLabel CurrentStatus = new JLabel(" ");

    JTextField adrTextField = new javax.swing.JTextField(4);

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.stackmon.StackMonBundle");

    public StackMonFrame() {

	// Configure GUI components

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

	// Set the adrTextField to visible
	adrTextField.setVisible(true);

        // general GUI config
        setTitle(rb.getString("StackMonitorTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(nextButton);
        pane1.add(previousButton);
        pane1.add(deleteButton);

        getContentPane().add(pane1);
        //pane1.setMaximumSize(pane1.getSize());

	JPanel pane2 = new JPanel();
	pane2.setLayout(new FlowLayout());
	pane2.add(adrTextField);
	getContentPane().add(pane2);

	JPanel pane3 = new JPanel();
	pane3.setLayout(new FlowLayout());
	pane3.add(CurrentStatus);
	getContentPane().add(pane3);

        pack();

	XNetTrafficController.instance().addXNetListener(~0,this);
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

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        super.dispose();
    }

    /*
     *  Request the next entry
     */
    private void getNextEntry() {
	int address=0;
	if(!adrTextField.getText().equals(""))
	   address=Integer.parseInt(adrTextField.getText());
	XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address,true);
	XNetTrafficController.instance().sendXNetMessage(msg,this);
    }

    /*
     * Request the previous entry
     */
    private void getPreviousEntry(){	
	int address=0;
	if(!adrTextField.getText().equals(""))
	   address=Integer.parseInt(adrTextField.getText());
	XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(address,false);
	XNetTrafficController.instance().sendXNetMessage(msg,this);
    }

    /* 
     * Remove the current entry
     */
    private void deleteEntry() {
	int address=0;
	if(!adrTextField.getText().equals(""))
	   address=Integer.parseInt(adrTextField.getText());
	XNetMessage msg = XNetMessage.getDeleteAddressOnStackMsg(address);
	XNetTrafficController.instance().sendXNetMessage(msg,this);
    }


    // The XNet Listener Interface
    
    // We need to be able to recieve information from the command station
    public void message(XNetReply r) {
       if(r.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
	   switch(r.getElement(1)) {
		case XNetConstants.LOCO_SEARCH_RESPONSE_N:
			CurrentStatus.setText(rb.getString("SearchNormal"));
			adrTextField.setText("" + r.getThrottleMsgAddr());
			break;
		case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
			CurrentStatus.setText(rb.getString("SearchDH"));
			adrTextField.setText("" + r.getThrottleMsgAddr());
			break;
		case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
			CurrentStatus.setText(rb.getString("SearchMUBase"));
			adrTextField.setText("" + r.getThrottleMsgAddr());
			break;
		case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
			CurrentStatus.setText(rb.getString("SearchMU"));
			adrTextField.setText("" + r.getThrottleMsgAddr());
			break;
		case XNetConstants.LOCO_SEARCH_NO_RESULT:
			CurrentStatus.setText(rb.getString("SearchFail"));
			adrTextField.setText("" + r.getThrottleMsgAddr());
			break;
		default:
			if(log.isDebugEnabled()) log.debug("not search result");
		}
	}

    }

    // We need to be able to receive information sent by the computer to 
    // the command station
    public void message(XNetMessage m) {
    }

    // Register for logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StackMonFrame.class.getName());

}

