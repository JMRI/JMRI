package jmri.jmrix.nce.usbinterface;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for configuring an NCE USB interface.
 *
 * @author ken cameron Copyright (C) 2013
 */
public class UsbInterfacePanel extends jmri.jmrix.nce.swing.NcePanel implements jmri.jmrix.nce.NceListener {

    private int replyLen = 0;    // expected byte length
    private int waiting = 0;     // to catch responses not
    // intended for this module
    private int minCabNum = -1;  // either the USB or serial size depending on what we connect to
    private int maxCabNum = -1;  // either the USB or serial size depending on what we connect to
    private int minCabSetNum = -1;
    private int maxCabSetNum = -1;
    private static final int CAB_MIN_USB = 2;   // USB cabs start at 2
    private static final int CAB_MIN_PRO = 2;   // Serial cabs start at 2
    private static final int CAB_MAX_USB_128 = 4;   // There are up to 4 cabs on 1.28
    private static final int CAB_MAX_USB_165 = 10;   // There are up to 10 cabs on 1.65
    private static final int CAB_MAX_PRO = 63;   // There are up to 63 cabs
    private static final int CAB_MAX_SB3 = 5;   // There are up to 5 cabs

    private static final int REPLY_1 = 1;   // reply length of 1 byte
    private static final int REPLY_2 = 2;   // reply length of 2 byte
    private static final int REPLY_4 = 4;   // reply length of 4 byte

    Thread nceCabUpdateThread;
    private boolean setRequested = false;
    private int setCabId = -1;

    private NceTrafficController tc = null;

    JTextField newCabId = new JTextField(5);
    JLabel oldCabId = new JLabel("     ");
    JButton setButton = new JButton(Bundle.getMessage("ButtonSet"));

    JLabel space1 = new JLabel(" ");
    JLabel space2 = new JLabel("  ");
    JLabel space3 = new JLabel("   ");
    JLabel space4 = new JLabel("    ");
    JLabel space5 = new JLabel("     ");

    JLabel statusText = new JLabel();

    public UsbInterfacePanel() {
        super();
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.nce.usbinterface.UsbInterfacePanel";
    }

    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append(Bundle.getMessage("TitleUsbInterface"));
        return x.toString();
    }

    @Override
    public void initComponents(NceSystemConnectionMemo m) {
        this.memo = m;
        this.tc = m.getNceTrafficController();

        minCabNum = CAB_MIN_PRO;
        maxCabNum = CAB_MAX_PRO;
        minCabSetNum = CAB_MIN_PRO + 1;
        maxCabSetNum = CAB_MAX_PRO;
        if ((tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE)
                && (tc.getCmdGroups() & NceTrafficController.CMDS_MEM) != 0) {
            minCabNum = CAB_MIN_USB;
            maxCabNum = CAB_MAX_USB_165;
        } else if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERPRO) {
            minCabNum = CAB_MIN_PRO;
            maxCabNum = CAB_MAX_PRO;
        } else if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3) {
            minCabNum = CAB_MIN_PRO;
            maxCabNum = CAB_MAX_SB3;
        } else if (tc.getCommandOptions() >= NceTrafficController.OPTION_1_65) {
            maxCabSetNum = CAB_MAX_USB_165;
        } else {
            maxCabSetNum = CAB_MAX_USB_128;
        }
        // general GUI config

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setPreferredSize(new Dimension(400, 75));

        addItem(p1, new JLabel(Bundle.getMessage("LabelSetCabId")), 1, 2);
        newCabId.setText(" ");
        addItem(p1, newCabId, 2, 2);
        addItem(p1, setButton, 3, 2);
        add(p1);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        addItem(p2, new JLabel(Bundle.getMessage("LabelStatus")), 1, 1);
        statusText.setText(" ");
        addItem(p2, statusText, 2, 1);
        add(p2);

        JPanel p3 = new JPanel();
        add(p3);

        addButtonAction(setButton);
    }

    // validate value as legal cab id for the system
    // needed since there are gaps in the USB based command stations
    public boolean validateCabId(int id) {
        if ((id < minCabNum) || (id > maxCabNum)) {
            // rough range check
            return false;
        }
        if ((tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERCAB)
                && (tc.getCmdGroups() & NceTrafficController.CMDS_MEM) != 0) {
            // is a 1.65 or better firmware, has gaps, for PowerCab only
            if ((id  == 6) || (id == 7))
                return false;
        }
        return true;
    }

    // button actions
    public void buttonActionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == setButton) {
            changeCabId();
        } else {
            log.error("unknown action performed: " + src);
        }
    }

    private void changeCabId() {
        int i = -1;
        try {
            i = Integer.parseInt(newCabId.getText().trim());
            if (validateCabId(i)) {
                processMemory(true, i);
            } else {
                statusText.setText(MessageFormat.format(Bundle.getMessage("StatusInvalidCabIdEntered"), i));
            }
        } catch (RuntimeException e) {
            // presume it failed to convert.
            log.debug("failed to convert {}", i);
        }
    }

    private void processMemory(boolean doSet, int cabId) {
        if (doSet) {
            setRequested = true;
            setCabId = cabId;
        }
        // Set up a separate thread to access CS memory
        if (nceCabUpdateThread != null && nceCabUpdateThread.isAlive()) {
            return; // thread is already running
        }
        nceCabUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
                    if (setRequested) {
                        cabSetIdUsb();
                    }
                }
            }
        });
        nceCabUpdateThread.setName(Bundle.getMessage("ThreadTitle"));
        nceCabUpdateThread.setPriority(Thread.MIN_PRIORITY);
        nceCabUpdateThread.start();
    }

    private boolean firstTime = true; // wait for panel to display

    // Thread to set cab id, allows the use of sleep or wait, for NCE-USB connection
    private void cabSetIdUsb() {

        if (firstTime) {
            try {
                Thread.sleep(1000); // wait for panel to display
            } catch (InterruptedException e) {
                log.error("Thread interrupted.", e);
            }
        }

        firstTime = false;
        recChar = -1;
        setRequested = false;
        if (validateCabId(setCabId)) {
            statusText.setText(MessageFormat.format(Bundle.getMessage("StatusSetIdStart"), setCabId));
            writeUsbCabId(setCabId);
            if (!waitNce()) {
                return;
            }
            if (recChar != '!') {
                statusText.setText(MessageFormat.format(Bundle.getMessage("StatusUsbErrorCode"), recChars[0]));
            } else {
                statusText.setText(MessageFormat.format(Bundle.getMessage("StatusSetIdFinished"), setCabId));
            }
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    //nothing to see here, move along
                }
            }
        } else {
            statusText.setText(MessageFormat.format(Bundle.getMessage("StatusInvalidCabId"), setCabId, minCabSetNum, maxCabSetNum));
        }
        this.setVisible(true);
        this.repaint();
    }

    @Override
    public void message(NceMessage m) {
    }  // ignore replies

    // response from read
    int recChar = 0;
    int[] recChars = new int[16];

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification = "Thread wait from main transfer loop")
    @Override
    public void reply(NceReply r) {
        if (log.isDebugEnabled()) {
            log.debug("Receive character");
        }
        if (waiting <= 0) {
            log.error("unexpected response. Len: " + r.getNumDataElements() + " code: " + r.getElement(0));
            return;
        }
        waiting--;
        if (r.getNumDataElements() != replyLen) {
            statusText.setText(Bundle.getMessage("StatusError"));
            return;
        }
        // Read one byte
        if (replyLen == REPLY_1) {
            // Looking for proper response
            recChar = r.getElement(0);
        }
        // Read two byte
        if (replyLen == REPLY_2) {
            // Looking for proper response
            for (int i = 0; i < REPLY_2; i++) {
                recChars[i] = r.getElement(i);
            }
        }
        // Read four byte
        if (replyLen == REPLY_4) {
            // Looking for proper response
            for (int i = 0; i < REPLY_4; i++) {
                recChars[i] = r.getElement(i);
            }
        }
        // wake up thread
        synchronized (this) {
            notify();
        }
    }

    // puts the thread to sleep while we wait for the read CS memory to complete
    private boolean waitNce() {
        int count = 100;
        if (log.isDebugEnabled()) {
            log.debug("Going to sleep");
        }
        while (waiting > 0) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    //nothing to see here, move along
                }
            }
            count--;
            if (count < 0) {
                statusText.setText(Bundle.getMessage("StatusReplyTimeout"));
                return false;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("awake!");
        }
        return true;
    }

    // USB set Cab Id in USB
    private void writeUsbCabId(int value) {
        replyLen = REPLY_1;   // Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.usbSetCabId(value);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_1);
        tc.sendNceMessage(m, this);
    }

    /**
     * Add item to a panel.
     *
     * @param p Panel Id
     * @param c Component Id
     * @param x Column
     * @param y Row
     */
    protected void addItem(JPanel p, JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        p.add(c, gc);
    }

    private void addButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonActionPerformed(e);
            }
        });
    }

    private final static Logger log = LoggerFactory.getLogger(UsbInterfacePanel.class);

}
