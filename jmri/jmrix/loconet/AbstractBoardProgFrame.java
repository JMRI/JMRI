// AbstractBoardProgFrame.java

package jmri.jmrix.loconet;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Display and modify an Digitrax board configuration.
 * <P>
 * The read and write require a sequence of operations, which
 * we handle with a state variable.
 * <P>
 * Programming of the board is done via configuration messages, so
 * the board should not be put into programming mode via the
 * built-in pushbutton while this tool is in use.
 * <P>
 * Throughout, the terminology is "closed" == true, "thrown" == false.
 * Variables are named for their closed state.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author  Bob Jacobsen   Copyright (C) 2004
 * @version $Revision: 1.1 $
 */
abstract public class AbstractBoardProgFrame extends JFrame implements LocoNetListener {

    protected AbstractBoardProgFrame(String title) {
        super(title);

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        // listen for message traffic
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No LocoNet connection available, can't function");

        // and prep for display
        addrField.setText("1");
    }

    /**
     * Provide read, write buttons and address
     */
    protected JPanel provideAddressing(String type) {
        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
            pane0.add(new JLabel("Unit address: "));
            pane0.add(addrField);
            pane0.add(readAllButton);
            pane0.add(writeAllButton);
            readAllButton.setText("Read from "+type);
            writeAllButton.setText("Write to "+type);
        // install read all, write all button handlers
        readAllButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	readAll();
                }
            }
        );
        writeAllButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeAll();
                }
            }
        );
        return pane0;
    }
    JToggleButton readAllButton  = new JToggleButton();
    JToggleButton writeAllButton = new JToggleButton();
    JTextField addrField = new JTextField(4);
    /**
     * provide the status line for the GUI
     */
    protected JComponent provideStatusLine() {
        return status;
    }
    JLabel status = new JLabel();
    protected void setStatus(String msg) {
        status.setText(msg);
    }
    
    /**
     * Handle layout details during construction.
     * <P>
     * @param c component to put on a single line
     */
    protected void appendLine(JComponent c) {
        c.setAlignmentX(0.f);
        getContentPane().add(c);
    }

    boolean read = false;
    int state = 0;
    
    void readAll() {
        // check the address
        try {
            setAddress(256);
        } catch (Exception e) {
            log.debug("readAll aborted due to invalid address");
        }
        // Start the first operation
        read = true;
        state = 1;
        nextRequest();
    }

    int address = 0;
    int typeWord;
    
    /**
     * Configure the type word in the LocoNet messages.
     * <P>
     * Known values:
     *<UL>
     *<LI>0x70 - PM4
     *<LI>0x71 - BDL16
     *<LI>0x72 - SE8
     *</ul>
     */
    protected void setTypeWord(int type) {
        typeWord = type;
    }
    void nextRequest() {
        if (read) {
            // read op
            status.setText("Reading opsw "+state);
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            int element = 0x62;
            if ( (address&0x80) != 0 ) element|= 1;
            l.setElement(1, element);
            l.setElement(2, address&0x7F);
            l.setElement(3, typeWord);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2);
            LnTrafficController.instance().sendLocoNetMessage(l);
        } else {
            //write op
            status.setText("Writing opsw "+state);
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            int element = 0x72;
            if ( (address&0x80) != 0 ) element|= 1;
            l.setElement(1, element);
            l.setElement(2, address&0x7F);
            l.setElement(3, typeWord);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2+(opsw[state]?1:0));
            LnTrafficController.instance().sendLocoNetMessage(l);
        }
    }

    /**
      * Turn the textfield containing the address into 
      * a valid integer address, handling user-input
      * errors as needed
      */
    void setAddress(int maxValid) throws Exception {
        try {
            address = (Integer.parseInt(addrField.getText())-1);
        } catch (Exception e) {
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText("Input Error");
            JOptionPane.showMessageDialog(this,"Invalid Address",
                    "Error",JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing Rate Field: "+e);
            throw e;
        }
        // parsed OK, check range
        if (address > (maxValid-1) || address < 0) {
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText("Input Error");
            JOptionPane.showMessageDialog(this,
                "Address out of range, must be 1 to "+maxValid,
                "Error",JOptionPane.ERROR_MESSAGE);
            log.error("Invalid address value");
            throw new jmri.JmriException("Address out of range: "+address);
        }
        return;  // OK
    }

    /**
     * Copy from the GUI to the opsw array. 
     * <p>
     * Used before write operations start
     */
    abstract protected void copyToOpsw();
    
    abstract protected void updateDisplay();
    
    /**
     * Specify which opsw (and which sequence) need to be read/written
     */
    abstract protected int nextState(int state);
    
    void writeAll() {
        // check the address
        try {
            setAddress(256);
        } catch (Exception e) {
            log.debug("writeAll aborted due to invalid address"+e);
        }
        
        // copy over the display
        copyToOpsw();
        
        // Start the first operation
        read = false;
        state = 1;
        nextRequest();
    }

    /**
     * True is "closed", false is "thrown". This matches how we
     * do the check boxes also, where we use the terminology for the
     * "closed" option.
     */
    protected boolean[] opsw = new boolean[64];

    public void message(LocoNetMessage m) {
        if (log.isDebugEnabled()) log.debug("get message "+m);
        // are we reading? If not, ignore
        if (state == 0) return;
        // check for right type, unit
        if (m.getOpCode() != 0xb4 || m.getElement(1) != 0x00)  return;

        // LACK with 0 in opcode; assume its to us.  Note that there
        // should be a 0x50 in the opcode, not zero, but this is what we
        // see...

        boolean value = false;
        if ( (m.getElement(2)&0x20) != 0) value = true;

        // record this bit
        opsw[state] = value;

        // show what we've got so far
        if (read) updateDisplay();

        // and continue through next state, if any
        state = nextState(state);
        if (state == 0) {
            // done
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText("Done");
            return;
        } else {
            // create next
            nextRequest();
            return;
        }
    }

    // Destroy the window when the close box is clicked, as there is no
    // way to get it to show again
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // Drop loconet connection
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().removeLocoNetListener(~0, this);
        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractBoardProgFrame.class.getName());

}
