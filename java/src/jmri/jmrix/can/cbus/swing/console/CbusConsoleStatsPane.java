package jmri.jmrix.can.cbus.swing.console;

import java.util.LinkedList;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;

/**
 * Panel for CBUS Console Stats
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleStatsPane extends javax.swing.JPanel {

    private JTextField sentCountField;
    private JTextField rcvdCountField;
    private JTextField eventsCountField;
    private JTextField dccCountField;
    private JTextField totalCountField;
    private JTextField framesLastSecondField;
    private JTextField meanFramesPerSecondField;
    private JTextField maxFramesPerSecondField;
    private JButton statsClearButton;

    private int sentTotal = 0;
    private int rcvdTotal = 0;
    private int eventTotal = 0;
    private int dccTotal = 0;
    private int total = 0;
    private int maxPerSecondCount = 0;
    private long startTime;

    private final LinkedList<Long> frameTimes;
    private transient TimerTask keepAliveTimer = null;
    private boolean disposed = false;

    public CbusConsoleStatsPane(CbusConsolePane mainPane){
        super();
        frameTimes = new LinkedList<>();
        initButtons();
        addToPanel();
        CbusConsoleStatsPane.this.setLayout(new jmri.util.swing.WrapLayout());
    }

    private void addToPanel() {

        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("StatisticsTitle")));
        add(sentCountField);
        add(rcvdCountField);
        add(totalCountField);
        add(eventsCountField);
        add(dccCountField);
        add(framesLastSecondField);
        add(meanFramesPerSecondField);
        add(maxFramesPerSecondField);

        add(statsClearButton);
        statsClearButton.addActionListener(this::statsClearButtonActionPerformed);
        startUpdateTimer();

    }

    protected void incremenetTotal(){
        total++;
        long currentTime = System.currentTimeMillis();
        frameTimes.add(currentTime);
        countFramesInLastSecond(currentTime); // update Max value
    }

    protected void incremenetReceived(){
        rcvdTotal++;
    }

    protected void incremenetSent(){
        sentTotal++;
    }

    protected void incrementEvents() {
        eventTotal++;
    }

    protected void incrementDcc() {
        dccTotal++;
    }

    private void initButtons() {
        sentCountField = new JTextField("0", 7);
        rcvdCountField = new JTextField("0", 7);
        eventsCountField = new JTextField("0", 7);
        dccCountField = new JTextField("0", 7);
        totalCountField = new JTextField("0", 7);
        framesLastSecondField = new JTextField("0", 7);
        meanFramesPerSecondField = new JTextField("0", 7);
        maxFramesPerSecondField = new JTextField("0", 7);
        statsClearButton = new JButton();
        initButtonBorderToolTips();
    }

    private void initButtonBorderToolTips(){

        sentCountField.setToolTipText(Bundle.getMessage("TooltipSent"));
        sentCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SentTitle")));

        rcvdCountField.setToolTipText(Bundle.getMessage("TooltipReceived"));
        rcvdCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("ReceivedTitle")));

        eventsCountField.setToolTipText(Bundle.getMessage("eventsCountFieldTip"));
        eventsCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CbusEvents")));

        dccCountField.setToolTipText(Bundle.getMessage("dccCountFieldTip"));
        dccCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("dccCountField")));

        totalCountField.setToolTipText(Bundle.getMessage("totalCountFieldTip"));
        totalCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("totalCountField")));

        framesLastSecondField.setToolTipText(Bundle.getMessage("FramesPerSecondTip"));
        framesLastSecondField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("FramesPerSecond")));

        meanFramesPerSecondField.setToolTipText(Bundle.getMessage("AverageFramesPerSecondTip"));
        meanFramesPerSecondField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("AverageFramesPerSecond")));

        maxFramesPerSecondField.setToolTipText(Bundle.getMessage("MaxFramesPerSecondTip"));
        maxFramesPerSecondField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("MaxFramesPerSecond")));

        statsClearButton.setText(Bundle.getMessage("ButtonReset"));
        statsClearButton.setVisible(true);
    }

    private void statsClearButtonActionPerformed(java.awt.event.ActionEvent e) {
        frameTimes.clear();
        startTime = System.currentTimeMillis();
        sentTotal = 0;
        rcvdTotal = 0;
        eventTotal = 0;
        dccTotal = 0;
        total = 0;
        maxPerSecondCount = 0;
    }

    /**
     * Set up the GUI Update Timer, and start it.
     */
    private void startUpdateTimer() {
        disposed = false;
        startTime = System.currentTimeMillis();
        keepAliveTimer = new TimerTask(){
            @Override
            public void run () {
                if ( disposed ) {
                    return;
                }
                long currentTime = System.currentTimeMillis();
                float secsDuration = (currentTime-startTime)/1000f;
                framesLastSecondField.setText(Integer.toString(
                    countFramesInLastSecond(currentTime)));
                maxFramesPerSecondField.setText(Integer.toString(maxPerSecondCount));
                float average = total / secsDuration;
                meanFramesPerSecondField.setText(String.format("%.01f", average));
                totalCountField.setText(Integer.toString(total));
                rcvdCountField.setText(Integer.toString(rcvdTotal));
                sentCountField.setText(Integer.toString(sentTotal));
                eventsCountField.setText(Integer.toString(eventTotal));
                dccCountField.setText(Integer.toString(dccTotal));
                statsClearButton.setToolTipText(Bundle.getMessage("ResetButtonLastRestTip",
                    String.format("%.01f", secsDuration)));
                jmri.util.TimerUtil.scheduleOnGUIThread(keepAliveTimer, 500);
            }
        };
        jmri.util.TimerUtil.scheduleOnGUIThread(keepAliveTimer, 500);
    }

    private int countFramesInLastSecond(long currentTime) {
        while (!frameTimes.isEmpty() && currentTime - frameTimes.peek() > 1000) {
            frameTimes.remove(); // Remove Frames older than 1 second
        }
        maxPerSecondCount = Math.max(maxPerSecondCount, frameTimes.size());
        return frameTimes.size();
    }

    public void dispose() {
        disposed = true; // cancels GUI updates
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel();
            keepAliveTimer = null;
        }
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusConsoleStatsPane.class);

}
