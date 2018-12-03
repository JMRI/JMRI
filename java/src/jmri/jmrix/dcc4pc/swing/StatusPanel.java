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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel to show DCC4PC status
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class StatusPanel extends jmri.jmrix.dcc4pc.swing.Dcc4PcPanel implements Dcc4PcListener, Dcc4PcPanelInterface {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(Dcc4PcSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getDcc4PcTrafficController();
        // Create GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(infoText);
        add(infoDescription);
        add(serialNo);

        // ask to be notified
        Dcc4PcMessage m = new jmri.jmrix.dcc4pc.Dcc4PcMessage(new byte[]{(byte) 0x00});
        nextPacket = 0x00;
        if(tc!=null){
            tc.sendDcc4PcMessage(m, this);
        } else {
            log.error("no Traffic Controller Found");
        }

        sendButton = new JButton("Update");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Request status update from DCC4PC");

        add(sendButton);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
    }

    void reset() {
        infoText.setText(appString + "<unknown>");
        infoDescription.setText(proString + "<unknown>");
        serialNo.setText(hrdString + "<unknown>");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if(tc!=null){
            tc.removeDcc4PcListener(this);
            tc = null;
        }
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        if(tc!=null){
            reset();
            Dcc4PcMessage m = new jmri.jmrix.dcc4pc.Dcc4PcMessage(new byte[]{(byte) 0x00});
            nextPacket = 0x00;
            tc.sendDcc4PcMessage(m, this);
        }
    }

    Dcc4PcTrafficController tc;

    public void notifyReply(Dcc4PcReply r) {
    }

    public void notifyMessage(Dcc4PcMessage m) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleTimeout(Dcc4PcMessage m) {
    }
    
    private final static Logger log = LoggerFactory.getLogger(StatusPanel.class);
}
