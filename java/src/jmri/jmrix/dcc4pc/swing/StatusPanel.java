// StatusPanel.java
package jmri.jmrix.dcc4pc.swing;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import jmri.JmriException;
import jmri.jmrix.dcc4pc.Dcc4PcListener;
import jmri.jmrix.dcc4pc.Dcc4PcMessage;
import jmri.jmrix.dcc4pc.Dcc4PcReply;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcTrafficController;

/**
 * Panel to show DCC4PC status
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 17977 $
 */
public class StatusPanel extends jmri.jmrix.dcc4pc.swing.Dcc4PcPanel implements Dcc4PcListener, Dcc4PcPanelInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1884301741261020275L;
    String appString = "Info        : ";
    String proString = "Description : ";
    String hrdString = "Serial No   : ";
    JLabel infoText = new JLabel(appString + "<unknown>");
    JLabel infoDescription = new JLabel(proString + "<unknown>");
    JLabel serialNo = new JLabel(hrdString + "<unknown>");

    JButton sendButton;

    public StatusPanel() {
        super();
    }

    public void initComponents(Dcc4PcSystemConnectionMemo memo) {
        super.initComponents(memo);
        //memo.getTrafficController().addEcosListener(this);
        tc = memo.getDcc4PcTrafficController();
        // Create GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(infoText);
        add(infoDescription);
        add(serialNo);

        // ask to be notified
        Dcc4PcMessage m = new jmri.jmrix.dcc4pc.Dcc4PcMessage(new byte[]{(byte) 0x00});
        nextPacket = 0x00;
        tc.sendDcc4PcMessage(m, this);

        sendButton = new JButton("Update");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Request status update from DCC4PC");

        add(sendButton);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
    }

    public void initComponents() throws Exception {
    }

    void reset() {
        infoText.setText(appString + "<unknown>");
        infoDescription.setText(proString + "<unknown>");
        serialNo.setText(hrdString + "<unknown>");
    }

    // to free resources when no longer used
    public void dispose() {
        tc.removeDcc4PcListener(this);
        tc = null;
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        reset();
        Dcc4PcMessage m = new jmri.jmrix.dcc4pc.Dcc4PcMessage(new byte[]{(byte) 0x00});
        nextPacket = 0x00;
        tc.sendDcc4PcMessage(m, this);

    }

    @SuppressWarnings("unused")
    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use EcosPowerManager after dispose");
        }
    }

    Dcc4PcTrafficController tc;

    public void notifyReply(Dcc4PcReply r) {
    }

    public void notifyMessage(Dcc4PcMessage m) {
    }

    // to listen for status changes from Ecos system
    public void reply(Dcc4PcReply r) {
        // power message?
        switch (nextPacket) {
            case 0x00: {
                nextPacket = -1;
                int i = 0;
                StringBuffer buf = new StringBuffer();
                while (i < 4) {
                    buf.append((char) r.getElement(i));
                    i++;
                }
                //skip supported speeds for now
                String str = buf.toString();
                i = i + 2;
                str = str + " ver ";
                str = str + r.getElement(i) + ".";
                i++;
                str = str + r.getElement(i) + " Max Bus Speed : ";
                i++;
                str = str + r.getElement(i);
                infoText.setText(appString + str);
                i++;
                jmri.jmrix.dcc4pc.Dcc4PcMessage m = new jmri.jmrix.dcc4pc.Dcc4PcMessage(new byte[]{(byte) 0x01});
                nextPacket = 0x01;
                tc.sendDcc4PcMessage(m, this);
                break;
            }
            case 0x01: {

                infoDescription.setText(proString + r.toString());
                jmri.jmrix.dcc4pc.Dcc4PcMessage m = new jmri.jmrix.dcc4pc.Dcc4PcMessage(new byte[]{(byte) 0x02});
                nextPacket = 0x02;
                tc.sendDcc4PcMessage(m, this);
                break;
            }
            case 0x02: {
                nextPacket = -1;
                serialNo.setText(hrdString + r.toString());
                break;
            }
            default:
                break;
        }
    }

    int nextPacket = -1;

    public void message(Dcc4PcMessage m) {
        byte[] theByteArray = m.getFormattedMessage();
        if (theByteArray[0] == 0x00) {
            nextPacket = 0x00;
        } else if (theByteArray[0] == 0x01) {
            nextPacket = 0x01;
        } else if (theByteArray[0] == 0x02) {
            nextPacket = 0x02;
        }
    }

    public void handleTimeout(Dcc4PcMessage m) {
    }

    public void processingData() {
        //We should be increasing our timeout
    }

}


/* @(#)StatusPane.java */
