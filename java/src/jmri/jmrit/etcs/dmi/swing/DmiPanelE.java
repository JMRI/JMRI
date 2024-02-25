package jmri.jmrit.etcs.dmi.swing;

import jmri.jmrit.etcs.CabMessage;
import jmri.jmrit.etcs.ResourceUtil;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * Class for ERTMS DMI Panel E, the Driver Messages area.
 * @author Steve Young Copyright (C) 2024
 */
public class DmiPanelE extends JPanel {

    private final JButton e10upArrow;
    private final JButton e11downArrow;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private final JLabel labele1;
    private final DmiPanel mainPane;
    private final JPanel messagePanel;
    private final JButton messageButton;
    private int msgScroll = 0;
    private final List<DmiCabMessage> messageList = new ArrayList<>();
    private final JLabel[] messageLabels;
    private final JLabel[] timeLabels;

    private final Font timeFont = new Font(DmiPanel.FONT_NAME, Font.PLAIN, 12);
    private final Font messageFont = new Font(DmiPanel.FONT_NAME, Font.PLAIN, 16);

    private CabMessage cabMessageBeingConfirmed;

    private final JLabel msglabel1 = new JLabel();
    private final JLabel msglabel2 = new JLabel();
    private final JLabel msglabel3 = new JLabel();
    private final JLabel msglabel4 = new JLabel();
    private final JLabel msglabel5 = new JLabel();

    private final JLabel timeLabel1 = new JLabel();
    private final JLabel timeLabel2 = new JLabel();
    private final JLabel timeLabel3 = new JLabel();
    private final JLabel timeLabel4 = new JLabel();
    private final JLabel timeLabel5 = new JLabel();

    public DmiPanelE(@Nonnull DmiPanel mainPanel){
        super();

        mainPane = mainPanel;
        messageButton = new JButton();

        setLayout(null); // Set the layout manager to null

        setBackground(DmiPanel.BACKGROUND_COLOUR);
        setBounds(0, 365, 334, 100);

        JPanel e1 = new JPanel();
        JPanel e2 = new JPanel();
        JPanel e3 = new JPanel();
        JPanel e4 = new JPanel();
        messagePanel = initMsgPanel();

        e10upArrow = new JButton();
        e11downArrow = new JButton();

        e10upArrow.setFocusable(false);
        e11downArrow.setFocusable(false);

        e1.setBounds(0, 0, 54, 25);
        e1.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        e1.setBackground(DmiPanel.BACKGROUND_COLOUR);

        e2.setBounds(0, 25, 54, 25);
        e2.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        e2.setBackground(DmiPanel.BACKGROUND_COLOUR);

        e3.setBounds(0, 50, 54, 25);
        e3.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        e3.setBackground(DmiPanel.BACKGROUND_COLOUR);

        e4.setBounds(0, 75, 54, 25);
        e4.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        e4.setBackground(DmiPanel.BACKGROUND_COLOUR);

        messagePanel.setBounds(0, 0, 234, 100);
        messagePanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        messagePanel.setBackground(DmiPanel.BACKGROUND_COLOUR);

        messageButton.setBounds(54, 0, 234, 100);
        messageButton.setLayout(null);
        messageButton.setContentAreaFilled(false); // Make the button transparent
        messageButton.setBorderPainted(false); // Remove button border
        messageButton.setName("messageAcknowledgeButton");
        messageButton.add(messagePanel);
        messageButton.addActionListener(this::acknowledgeButtonPressed);

        e10upArrow.setBounds(234+54, 0, 46, 50);
        e10upArrow.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        e10upArrow.setBackground(DmiPanel.BACKGROUND_COLOUR);

        e11downArrow.setBounds(234+54, 50, 46, 50);
        e11downArrow.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        e11downArrow.setBackground(DmiPanel.BACKGROUND_COLOUR);

        e10upArrow.setIcon(ResourceUtil.getImageIcon("NA_13.bmp"));
        e11downArrow.setIcon(ResourceUtil.getImageIcon("NA_14.bmp"));
        e10upArrow.setDisabledIcon(ResourceUtil.getImageIcon("NA_15.bmp"));
        e11downArrow.setDisabledIcon(ResourceUtil.getImageIcon("NA_16.bmp"));
        e10upArrow.setName("e10upArrow");
        e11downArrow.setName("e11downArrow");
        e10upArrow.addActionListener( (ActionEvent e) -> { msgScroll--; updateMsgPanel(); });
        e11downArrow.addActionListener( (ActionEvent e) -> { msgScroll++; updateMsgPanel(); });

        add(e1);
        add(e2);
        add(e3);
        add(e4);
        add(messageButton);

        add(e10upArrow);
        add(e11downArrow);

        labele1 = new JLabel();
        e1.add(labele1);

        timeLabels = new JLabel[]{timeLabel1,timeLabel2,timeLabel3,timeLabel4,timeLabel5};
        messageLabels = new JLabel[]{msglabel1,msglabel2,msglabel3,msglabel4,msglabel5};

        e10upArrow.setEnabled(false);
        e11downArrow.setEnabled(false);
    }

    private void acknowledgeButtonPressed(ActionEvent e){
        setMessageButtonEnabled(false);
        cabMessageBeingConfirmed.setConfirmed();
        mainPane.firePropertyChange(DmiPanel.PROP_CHANGE_CABMESSAGE_ACK, cabMessageBeingConfirmed.getMessageId());
        updateMsgPanel();
    }

    private JPanel initMsgPanel() {
        JPanel p = new JPanel();
        p.setLayout(null);

        timeLabel1.setBounds(5, 0, 35, 20);
        timeLabel2.setBounds(5, 20, 35, 20);
        timeLabel3.setBounds(5, 40, 35, 20);
        timeLabel4.setBounds(5, 60, 35, 20);
        timeLabel5.setBounds(5,80, 35, 20);

        msglabel1.setBounds(50,  0, 204, 20);
        msglabel2.setBounds(50, 20, 204, 20);
        msglabel3.setBounds(50, 40, 204, 20);
        msglabel4.setBounds(50, 60, 204, 20);
        msglabel5.setBounds(50, 80, 204, 20);

        timeLabel1.setBackground(DmiPanel.BACKGROUND_COLOUR);
        timeLabel2.setBackground(DmiPanel.BACKGROUND_COLOUR);
        timeLabel3.setBackground(DmiPanel.BACKGROUND_COLOUR);
        timeLabel4.setBackground(DmiPanel.BACKGROUND_COLOUR);
        timeLabel5.setBackground(DmiPanel.BACKGROUND_COLOUR);

        msglabel1.setBackground(DmiPanel.BACKGROUND_COLOUR);
        msglabel2.setBackground(DmiPanel.BACKGROUND_COLOUR);
        msglabel3.setBackground(DmiPanel.BACKGROUND_COLOUR);
        msglabel4.setBackground(DmiPanel.BACKGROUND_COLOUR);
        msglabel5.setBackground(DmiPanel.BACKGROUND_COLOUR);

        timeLabel1.setForeground(Color.WHITE);
        timeLabel2.setForeground(Color.WHITE);
        timeLabel3.setForeground(Color.WHITE);
        timeLabel4.setForeground(Color.WHITE);
        timeLabel5.setForeground(Color.WHITE);

        msglabel1.setForeground(Color.WHITE);
        msglabel2.setForeground(Color.WHITE);
        msglabel3.setForeground(Color.WHITE);
        msglabel4.setForeground(Color.WHITE);
        msglabel5.setForeground(Color.WHITE);

        timeLabel1.setFont(timeFont);
        timeLabel2.setFont(timeFont);
        timeLabel3.setFont(timeFont);
        timeLabel4.setFont(timeFont);
        timeLabel5.setFont(timeFont);

        msglabel1.setFont(messageFont);
        msglabel2.setFont(messageFont);
        msglabel3.setFont(messageFont);
        msglabel4.setFont(messageFont);
        msglabel5.setFont(messageFont);

        msglabel1.setName("msglabel1");
        msglabel5.setName("msglabel5");
        timeLabel1.setName("timeLabel1");

        p.add(timeLabel1);
        p.add(timeLabel2);
        p.add(timeLabel3);
        p.add(timeLabel4);
        p.add(timeLabel5);

        p.add(msglabel1);
        p.add(msglabel2);
        p.add(msglabel3);
        p.add(msglabel4);
        p.add(msglabel5);

        return p;
    }

    protected void addMessage(@Nonnull CabMessage msg){
        // replace existing message with same ID
        removeMessage(msg.getMessageId());
        messageList.add(new DmiCabMessage(msg, messageFont));
        msgScroll = 0;
        updateMsgPanel();
    }

    protected void removeMessage(String messageId){
        Iterator<DmiCabMessage> iterator = messageList.iterator();
        while (iterator.hasNext()) {
            DmiCabMessage obj = iterator.next();
            if (obj.getMessageId().equals(messageId)) {
                iterator.remove(); // Remove the object from the list
            }
        }
        updateMsgPanel();
    }

    private void updateMsgPanel(){
        // log.info("starting update");
        Comparator<CabMessage> customComparator = Comparator
            .comparing(CabMessage::getAckRequired, Comparator.reverseOrder()) // Sort by boolean value (true first)
            .thenComparingInt(CabMessage::getGroup) // Then sort by integer value (low to high)
            .thenComparing(CabMessage::getSentTime, Comparator.reverseOrder()); // Then sort by time (newest first)

        // Sort the list using the custom comparator
        Collections.sort(messageList, customComparator);

        // reset previous message display
        for (int i = 0; i < 5; i++){
            messageLabels[i].setText("");
            timeLabels[i].setText("");
        }

        if ( !messageList.isEmpty()) {
            DmiCabMessage msg = messageList.get(0); 
            if ( msg.getAckRequired() ) {
                setAckReqdMessage(msg);
                return;
            }
        }

        displayMessages();
    }

    private void displayMessages() {
        setMessageButtonEnabled(false);

        List<String> tempTimes = new ArrayList<>();
        List<String> tempMessages = new ArrayList<>();
        for ( DmiCabMessage msg : messageList) {
            log.debug("CabM: {}", msg);
            String[] msgText = msg.getMessageArray();
            for (int i = 0; i < msgText.length; i++){
                tempTimes.add( i==0 ? dateFormat.format(msg.getSentTime().getTime()): "");
                tempMessages.add(msgText[i]);
            }
        }

        msgScroll = Math.min(msgScroll, Math.max(tempMessages.size()-5, 0));
        for (int i = 0; i < 5; i++){
            int position = i + msgScroll;
            if ( position < tempMessages.size() ) {
                timeLabels[i].setText(tempTimes.get(position));
                messageLabels[i].setText(tempMessages.get(position));
            }
        }
        e10upArrow.setEnabled(msgScroll > 0);
        e11downArrow.setEnabled(msgScroll < tempMessages.size()-5);
    }

    private void setAckReqdMessage(DmiCabMessage msg){
        log.debug("ack reqd, display single message");
        setMessageButtonEnabled(true);

        timeLabels[0].setText(dateFormat.format(msg.getSentTime().getTime()));
        cabMessageBeingConfirmed = msg;

        String[] msgText = msg.getMessageArray();
        log.debug("formatted msg has {} lines", msgText.length);

        for (int i = 0; i < 5; i++){
            if ( i < msgText.length ){
                log.debug("msgText {} {}", i, msgText[i]);
                messageLabels[i].setText(msgText[i]);
            }
        }
        e10upArrow.setEnabled(false);
        e11downArrow.setEnabled(false);
    }

    private void setMessageButtonEnabled(boolean newVal) {
        messageButton.setEnabled(newVal);
        messagePanel.setBorder(BorderFactory.createLineBorder(
            newVal ? DmiPanel.YELLOW : DmiPanel.BACKGROUND_COLOUR, 2));
    }

    // 1 ok, 0 conn  lost, -1 not visible
    protected void setSafeRadioConnection(int newVal) {
        switch (newVal) {
            case 1:
                labele1.setIcon(ResourceUtil.getImageIcon("ST_03.bmp"));
                labele1.setToolTipText(Bundle.getMessage("RadioConnectionOK"));
                break;
            case 0:
                labele1.setIcon(ResourceUtil.getImageIcon("ST_04.bmp"));
                labele1.setToolTipText(Bundle.getMessage("RadioConnectionLost"));
                break;
            default:
                labele1.setIcon(null);
                labele1.setToolTipText(null);
                break;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiPanelE.class);

}
