// AlmBrowserFrame.java
package jmri.jmrix.loconet.almbrowser;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.StringUtil;

/**
 * User interface for browsing ALM contents
 * <P>
 * This GUI works in the throttle editor space, so that values presented in the
 * GUI are 1 more than the values in the ALM messages. This includes both the
 * address and data values.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 * @deprecated 2.13.5, Does not work with the multi-connection correctly,
 * believe not to work correctly before hand and that the feature is not used.
 */
@Deprecated
public class AlmBrowserFrame extends jmri.util.JmriJFrame implements LocoNetListener {

    /**
     *
     */
    private static final long serialVersionUID = -7808858367861671176L;

    public AlmBrowserFrame() {
        super("Configuration Browser");
    }

    // internal members to hold widgets
    JButton readButton = new JButton("Read");
    JButton writeButton = new JButton("Write");

    JTextField almNumber = new JTextField("1");
    JTextField itemSize = new JTextField("64");
    JTextField itemNumber = new JTextField("1");
    JTextField elementNumber = new JTextField("1");

    JLabel blkl = new JLabel("    ");
    JLabel blkh = new JLabel("    ");

    JTextField values[] = new JTextField[4];

    public void initComponents() {

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // buttons
        {
            JPanel pane = new JPanel();
            pane.setLayout(new FlowLayout());
            pane.add(readButton);
            pane.add(writeButton);

            getContentPane().add(pane);
        }

        // address
        {
            JPanel pane = new JPanel();
            pane.setLayout(new FlowLayout());
            pane.add(new JLabel("ALM:"));
            pane.add(almNumber);
            pane.add(new JLabel("Address:"));
            pane.add(itemNumber);
            pane.add(new JLabel("*"));
            pane.add(itemSize);
            pane.add(new JLabel("+"));
            pane.add(elementNumber);
            itemNumber.setToolTipText("Index of this item, starting with 0");
            itemSize.setToolTipText("Number of entries per item");
            elementNumber.setToolTipText("Index of entry in item, starting with zero");

            getContentPane().add(pane);
        }

        {
            JPanel pane = new JPanel();
            pane.setLayout(new FlowLayout());
            pane.add(new JLabel("BLKL:"));
            pane.add(blkl);
            pane.add(new JLabel("BLKH:"));
            pane.add(blkh);

            getContentPane().add(pane);
        }

        // results
        {
            JPanel pane = new JPanel();
            pane.setLayout(new FlowLayout());
            values[0] = new JTextField(4);
            pane.add(values[0]);
            values[1] = new JTextField(4);
            pane.add(values[1]);
            values[2] = new JTextField(4);
            pane.add(values[2]);
            values[3] = new JTextField(4);
            pane.add(values[3]);

            getContentPane().add(pane);
        }

        readButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                readButtonActionPerformed(e);
            }
        });
        writeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                writeButtonActionPerformed(e);
            }
        });

        // pack to cause display
        pack();
    }

    public void readButtonActionPerformed(java.awt.event.ActionEvent e) {
        // format and send request
        reading = true;
        LocoNetMessage l = new LocoNetMessage(16);
        l.setElement(0, 0xEE);
        l.setElement(1, 0x10);
        l.setElement(2, Integer.parseInt(almNumber.getText()));
        l.setElement(3, 2);    // read
        l.setElement(4, block() & 0x7F);    // blockl
        l.setElement(5, block() / 128);    // blockh
        l.setElement(6, 0x03);
        l.setElement(7, 0x02);
        l.setElement(8, 0x08);
        l.setElement(9, 0x7F);
        l.setElement(10, 0x00);
        l.setElement(11, 0x00);
        l.setElement(12, 0x00);
        l.setElement(13, 0x00);
        l.setElement(14, 0x00);
        l.setElement(15, 0x00);
        LnTrafficController.instance().sendLocoNetMessage(l);

        blkl.setText("0x" + StringUtil.twoHexFromInt(block() & 0x7F));
        blkh.setText("0x" + StringUtil.twoHexFromInt(block() / 128));
    }

    int block() {
        int element = Integer.parseInt(elementNumber.getText()) - 1;
        int size = Integer.parseInt(itemSize.getText());
        int item = Integer.parseInt(itemNumber.getText()) - 1;
        return size * item + element;
    }

    public void writeButtonActionPerformed(java.awt.event.ActionEvent e) {
        int arg1 = Integer.parseInt(values[0].getText()) - 1;
        int arg2 = Integer.parseInt(values[1].getText()) - 1;
        int arg3 = Integer.parseInt(values[2].getText()) - 1;
        int arg4 = Integer.parseInt(values[3].getText()) - 1;

        // format message and send
        LocoNetMessage l = new LocoNetMessage(16);
        l.setElement(0, 0xEE);
        l.setElement(1, 0x10);
        l.setElement(2, Integer.parseInt(almNumber.getText()));
        l.setElement(3, 3);    // write
        l.setElement(4, block() & 0x7F);  // blockl
        l.setElement(5, block() / 128);  // blockh
        l.setElement(6, 0x03);
        l.setElement(7, arg1 & 0x7F);
        l.setElement(8, arg1 / 128);
        l.setElement(9, arg2 & 0x7F);
        l.setElement(10, arg2 / 128);
        l.setElement(11, arg3 & 0x7F);
        l.setElement(12, arg3 / 128);
        l.setElement(13, arg4 & 0x7F);
        l.setElement(14, arg4 / 128);
        l.setElement(15, 0x00);
        LnTrafficController.instance().sendLocoNetMessage(l);

        blkl.setText("0x" + StringUtil.twoHexFromInt(block() & 0x7F));
        blkh.setText("0x" + StringUtil.twoHexFromInt(block() / 128));

        return;
    }

    boolean reading;

    /**
     * Process the incoming message to look for the response to a read request
     *
     * @param msg
     */
    public void message(LocoNetMessage msg) {
        if (!reading) {
            return;
        }
        if (msg.getOpCode() != 0xE6) {
            return;
        }
        if (msg.getElement(2) != Integer.parseInt(almNumber.getText())) {
            return;
        }
        if (msg.getElement(3) != 2) {
            return;  // check for read
        }
        if (block() != msg.getElement(5) * 128 + msg.getElement(4)) {
            return;
        }
        // here OK, update
        values[0].setText("" + (msg.getElement(7) + msg.getElement(8) * 128 + 1));
        values[1].setText("" + (msg.getElement(9) + msg.getElement(10) * 128 + 1));
        values[2].setText("" + (msg.getElement(11) + msg.getElement(12) * 128 + 1));
        values[3].setText("" + (msg.getElement(13) + msg.getElement(14) * 128 + 1));
    }

    public void dispose() {
        // disconnect from LnTrafficController
        tc.removeLocoNetListener(~0, this);
        tc = null;
        super.dispose();
    }

    // connect to the LnTrafficController
    public void connect(LnTrafficController t) {
        tc = t;
        tc.addLocoNetListener(~0, this);
    }

    // private data
    private LnTrafficController tc = null;

}
