// BDL16Frame.java

package jmri.jmrix.loconet.bdl16;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * Frame displaying and programming a BDL16 configuration.
 * <P>The read and write require a sequence of operations, which
 * we handle with a state variable.
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public class BDL16Frame extends JFrame implements LocoNetListener {

    public BDL16Frame() {
        super("BDL16 programmer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
            pane0.add(new JLabel("Unit address: "));
            pane0.add(addrField);
            pane0.add(readAllButton);
            pane0.add(writeAllButton);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.setLayout(new GridLayout(10, 1));
            pane1.add(commonrail);
            pane1.add(forceoccupied);
            pane1.add(section16qualpower);
            pane1.add(nomaster);
            pane1.add(noterminate);
            pane1.add(delayhalfsecond);
            pane1.add(highthreshold);
            pane1.add(drivefromswitch);
            pane1.add(decodefromloconet);
            pane1.add(setdefault);
        getContentPane().add(pane1);

        getContentPane().add(status);

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

        // add status
        getContentPane().add(status);

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        // listen for BDL16 traffic
        LnTrafficController.instance().addLocoNetListener(~0, this);

        // and prep for display
        pack();
        addrField.setText("1");
    }

    boolean read = false;
    int state = 0;

    void readAll() {
        // Start the first operation
        read = true;
        state = 1;
        nextRequest();
    }

    void nextRequest() {
        if (read) {
            // read op
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            l.setElement(1, 0x62);
            l.setElement(2, Integer.parseInt(addrField.getText())-1);
            l.setElement(3, 0x71);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2);
            LnTrafficController.instance().sendLocoNetMessage(l);
        } else {
            //write op
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xD0);
            l.setElement(1, 0x72);
            l.setElement(2, Integer.parseInt(addrField.getText())-1);
            l.setElement(3, 0x71);
            int loc = (state-1)/8;
            int bit = (state-1)-loc*8;
            l.setElement(4, loc*16+bit*2+(opsw[state]?1:0));
            LnTrafficController.instance().sendLocoNetMessage(l);
        }
    }

    void writeAll() {
        // copy over the display
        opsw[ 1] = commonrail.isSelected();
        opsw[ 9] = forceoccupied.isSelected();
        opsw[10] = section16qualpower.isSelected();
        opsw[11] = nomaster.isSelected();
        opsw[12] = noterminate.isSelected();
        opsw[13] = delayhalfsecond.isSelected();
        opsw[19] = highthreshold.isSelected();
        opsw[25] = drivefromswitch.isSelected();
        opsw[26] = decodefromloconet.isSelected();
        opsw[40] = setdefault.isSelected();

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
    boolean[] opsw = new boolean[64];

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
        state = nextState();
        if (state == 0) {
            // done
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            return;
        } else {
            // create next
            nextRequest();
            return;
        }
    }

    void updateDisplay() {
        commonrail.setSelected(opsw[ 1]);
        forceoccupied.setSelected(opsw[ 9]);
        section16qualpower.setSelected(opsw[10]);
        nomaster.setSelected(opsw[11]);
        noterminate.setSelected(opsw[12]);
        delayhalfsecond.setSelected(opsw[13]);
        highthreshold.setSelected(opsw[19]);
        drivefromswitch.setSelected(opsw[25]);
        decodefromloconet.setSelected(opsw[26]);
        setdefault.setSelected(opsw[40]);
    }

    int nextState() {
        switch (state) {
            case  1: return 9;
            case  9: return 10;
            case 10: return 11;
            case 11: return 12;
            case 12: return 13;
            case 13: return 19;
            case 19: return 25;
            case 25: return 26;
            case 26: return 40;
            case 40: return  0;   // done!
            default:
                log.error("unexpected state "+state);
                return 0;
        }
    }

    JTextField addrField = new JTextField(4);


    JCheckBox commonrail            = new JCheckBox("common rail wiring");  // opsw 01
    JCheckBox forceoccupied         = new JCheckBox("not occupied with off");  // opsw 09
    JCheckBox section16qualpower    = new JCheckBox("section 16 used to sense power");  // opsw 10
    JCheckBox nomaster              = new JCheckBox("do not allow BDL16 to be LocoNet master");  // opsw 11
    JCheckBox noterminate           = new JCheckBox("do not allow BDL16 to terminate LocoNet");  // opsw 12
    JCheckBox delayhalfsecond       = new JCheckBox("delay 1/2 second at power up");  // opsw 13
    JCheckBox highthreshold         = new JCheckBox("high threshold sense (10kohms)");  // opsw 19
    JCheckBox drivefromswitch       = new JCheckBox("drive LEDs from switch commands");  // opsw 25
    JCheckBox decodefromloconet     = new JCheckBox("decode switch commands from LocoNet");  // opsw 26
    JCheckBox setdefault            = new JCheckBox("restore factory default, including address");  // opsw 40

    JLabel status = new JLabel("             ");

    JToggleButton readAllButton = new JToggleButton("Read from PM4");
    JToggleButton writeAllButton = new JToggleButton("Write to PM4");

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BDL16Frame.class.getName());

}
