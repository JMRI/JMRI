package jmri.jmrix.loconet.locogen;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for sending LocoNet messages to exercise the system.
 * <p>
 * When sending a sequence of operations:
 * <ul>
 *   <li>Send the next message
 *   <li>Wait until you hear the echo, then start a timer
 *   <li>When the timer trips, repeat if buttons still down.
 * </ul>
 * @see jmri.jmrix.can.swing.send.CanSendPane
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2010
 */
public class LocoGenPanel extends jmri.jmrix.loconet.swing.LnPanel
        implements LocoNetListener, ThrottleListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    javax.swing.JButton addThrottles = new javax.swing.JButton("+Throttles");
    javax.swing.JButton delThrottles = new javax.swing.JButton("-Throttles");
    javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    javax.swing.JSpinner numberOfThrottles = new javax.swing.JSpinner(new SpinnerNumberModel(50,1,400,1));
    javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
    javax.swing.JSpinner firstThrottleAddress = new javax.swing.JSpinner(new SpinnerNumberModel(300,1,9999,1));


    public LocoGenPanel() {
        super();
    }

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[] = new JTextField[MAXSEQUENCE];
    JCheckBox mUseField[] = new JCheckBox[MAXSEQUENCE];
    JTextField mDelayField[] = new JTextField[MAXSEQUENCE];
    JToggleButton mRunButton = new JToggleButton(Bundle.getMessage("ButtonGo"));

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.locogen.LocoGenFrame"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemSendPacket"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // handle single-packet part
        add(new JLabel(Bundle.getMessage("LabelSendOne")));
        {
            JPanel pane1 = new JPanel();
            pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

            jLabel1.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("PacketLabel")));
            jLabel1.setVisible(true);

            sendButton.setText(Bundle.getMessage("ButtonSend"));
            sendButton.setVisible(true);
            sendButton.setToolTipText(Bundle.getMessage("TooltipSendPacket"));

            packetTextField.setToolTipText(Bundle.getMessage("EnterHexToolTip"));

            pane1.add(jLabel1);
            pane1.add(packetTextField);
            pane1.add(sendButton);

            jLabel2.setText("DCC Start #");
            jLabel2.setVisible(true);
            pane1.add(jLabel2);
            pane1.add(firstThrottleAddress);
            jLabel3.setText("# Throttles");
            jLabel3.setVisible(true);
            pane1.add(jLabel3);
            pane1.add(numberOfThrottles);
            pane1.add(addThrottles);
            pane1.add(delThrottles);
            pane1.add(Box.createVerticalGlue());

            addThrottles.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addThrottlesActionPerformed(e);
                }
            });
            delThrottles.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    delThrottlesActionPerformed(e);
                }
            });

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

            add(pane1);
        }

        add(new JSeparator());

        // Configure the sequence
        add(new JLabel(Bundle.getMessage("SendSeqTitle")));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout(MAXSEQUENCE + 2, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel(Bundle.getMessage("ButtonSend")));
        pane2.add(new JLabel(Bundle.getMessage("PacketLabel")));
        pane2.add(new JLabel(Bundle.getMessage("WaitLabel")));
        for (int i = 0; i < MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i + 1)));
            mUseField[i] = new JCheckBox();
            mPacketField[i] = new JTextField(10);
            mDelayField[i] = new JTextField(10);
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            pane2.add(mDelayField[i]);
        }
        pane2.add(mRunButton); // starts a new row in layout
        add(pane2);

        mRunButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runButtonActionPerformed(e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        memo.getLnTrafficController().addLocoNetListener(~0, this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        memo.getLnTrafficController().sendLocoNetMessage(createPacket(packetTextField.getText()));
    }

    private ArrayList<DccThrottle> throttles = new ArrayList<>();
    private Thread abrty;
    private int throttleAddr = 300;
    private int ac = 0;
    public void addThrottlesActionPerformed(java.awt.event.ActionEvent e) {
        int count=0;
        try {
            throttleAddr = (int) firstThrottleAddress.getValue();
        }
        catch (Exception e3) {
            log.error("Bother leaving it at[{}]",throttleAddr);
        }
        while (count < (int) numberOfThrottles.getValue()) {
            log.debug("requesting throttle address={}",  throttleAddr);
            boolean ok;
            ok = InstanceManager.throttleManagerInstance().requestThrottle(throttleAddr, this);
            if (!ok) {
                log.warn("Throttle for locomotive address {} could not be setup.", throttleAddr);
            }
            throttleAddr+=1;
            // try{Thread.sleep(20);} catch (Exception e2) {log.info("Ahh");}
            count++;
        }
        firstThrottleAddress.setValue(throttleAddr);
        //try{Thread.sleep(2000);} catch (Exception e2) {log.info("Ahh2");}
        log.info("Start 300 Current[{}] Size[{}] nonnull[{}] ergo Throttles Good[{}",throttleAddr, throttles.size(),ac,throttleAddr-300 );
    }

    public void delThrottlesActionPerformed(java.awt.event.ActionEvent e) {
        int count = (int) numberOfThrottles.getValue();
        int startingDCC = (int) firstThrottleAddress.getValue();
        Iterator<DccThrottle> throttleList = throttles.iterator();
        while (throttleList.hasNext()) {
            DccThrottle item = throttleList.next();
            if (item != null && item.getLocoAddress().getNumber() > (startingDCC -1) ) {
                if ( count < 1) {
                    break;
                }
                item.setSpeedSetting(0.0f);
                InstanceManager.throttleManagerInstance().releaseThrottle(item, null);
                throttleList.remove();
                count--;
            }
        }
    }

    // Throttle feedback method - Initiates running AutoEngineer with the new throttle
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        if (t == null) {
            log.error("Null Throttle returned");
            return;
        }
        for( DccThrottle item : throttles) {
            if (item == null ) {
                item=t;
                return;
            }
        }
        ac = 0;
        for( DccThrottle item : throttles) {
            if (item != null ) {
                ac++;
            }
        }
        throttles.add(t);
    }

    @Override
    public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
        log.error("Throttle request failed for {} because {}", address, reason);
    }

    @Override
    public void notifyStealThrottleRequired(jmri.LocoAddress address) {
        // this is an automatically stealing impelementation.
        log.warn("Stealing");
        //InstanceManager.getDefault(ThrottleManager.class).stealThrottleRequest(address, this, true);
        //
        InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
    }

    // control sequence operation
    int mNextSequenceElement = 0;
    LocoNetMessage mNextEcho = null;
    javax.swing.Timer timer = null;

    /**
     * Internal routine to handle timer starts {@literal &} restarts
     * <p>
     * @param delay in mSec
     */
    protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendNextItem();
                }
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Run button pressed down, start the sequence operation
     *<p>
     * @param e - a {@link java.awt.event.ActionEvent} to be triggered
     */
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (!mRunButton.isSelected()) {
            return;
        }
        // make sure at least one is checked
        boolean ok = false;
        for (int i = 0; i < MAXSEQUENCE; i++) {
            if (mUseField[i].isSelected()) {
                ok = true;
            }
        }
        if (!ok) {
            mRunButton.setSelected(false);
            return;
        }
        // start the operation
        mNextSequenceElement = 0;
        sendNextItem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(LocoNetMessage m) {
        log.debug("message"); // NOI18N
        // are we running?
        if (!mRunButton.isSelected()) {
            return;
        }
        // yes, is this what we're looking for
        if (!(mNextEcho.equals(m))) {
            return;
        }
        // yes, we got it, do the next
        startSequenceDelay();
    }

    /**
     * Echo has been heard, start delay for next packet
     */
    void startSequenceDelay() {
        log.debug("startSequenceDelay"); // NOI18N
        // at the start, mNextSequenceElement contains index we're
        // working on
        int delay = Integer.parseInt(mDelayField[mNextSequenceElement].getText());
        // increment to next line at completion
        mNextSequenceElement++;
        // start timer
        restartTimer(delay);
    }

    /**
     * Send next item; may be used for the first item or when a delay has
     * elapsed.
     */
    void sendNextItem() {
        log.debug("sendNextItem"); // NOI18N
        // check if still running
        if (!mRunButton.isSelected()) {
            return;
        }
        // have we run off the end?
        if (mNextSequenceElement >= MAXSEQUENCE) {
            // past the end, go back
            mNextSequenceElement = 0;
        }
        // is this one enabled?
        if (mUseField[mNextSequenceElement].isSelected()) {
            // make the packet
            LocoNetMessage m = createPacket(mPacketField[mNextSequenceElement].getText());
            // send it
            mNextEcho = m;
            memo.getLnTrafficController().sendLocoNetMessage(m);
        } else {
            // ask for the next one
            mNextSequenceElement++;
            sendNextItem();
        }
    }

    /**
     * Create a well-formed LocoNet packet from a String
     * <p>
     * Well-formed generally means a space-separated string of hex values of
     * two characters each, as defined in
     * {@link jmri.util.StringUtil#bytesFromHexString(String s)} .
     * <p>
     * @param s - a string containing raw hex data of good form
     * @return The packet, with contents filled-in
     */
    LocoNetMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length == 0) {
            return null;  // no such thing as a zero-length message
        }
        LocoNetMessage m = new LocoNetMessage(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        return m;
    }

    /**
     * When the window closes, stop any sequences running
     */
    @Override
    public void dispose() {
        mRunButton.setSelected(false);
        super.dispose();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoGenPanel.class);

}
