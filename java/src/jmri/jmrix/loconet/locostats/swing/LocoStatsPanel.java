package jmri.jmrix.loconet.locostats.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locostats.LocoBufferIIStatus;
import jmri.jmrix.loconet.locostats.LocoNetInterfaceStatsListener;
import jmri.jmrix.loconet.locostats.LocoStatsFunc;
import jmri.jmrix.loconet.locostats.PR2Status;
import jmri.jmrix.loconet.locostats.PR3MS100ModeStatus;
import jmri.jmrix.loconet.locostats.RawStatus;
import jmri.jmrix.loconet.swing.LnPanel;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel displaying LocoNet interface status information.
 * <p>
 * The LocoBuffer family from RR-CirKits and the PRn family from Digitrax use
 * different formats for the status message. This class detects this from the
 * reply contents, and displays different panes depending on which message was
 * received. If the format is not recognised, a raw display format is used.
 * <p>
 * Moved from loconet.locobuffer.LocoBufferStatsFrame
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Alex Shepherd Copyright (C) 2003
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 * @since 2.1.5
 */
public class LocoStatsPanel extends LnPanel implements LocoNetInterfaceStatsListener {

    JPanel lb2Panel;
    JPanel rawPanel;
    JPanel pr2Panel;
    JPanel ms100Panel;
    boolean updateRequestPending = false;
    
    LocoStatsFunc stats;

    /**
     * Provides a path to the help html for this tool
     * 
     * @return path
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.locostats.LocoStatsFrame"; // NOI18N
    }

    /**
     * Provides the string for the title of the window
     * 
     * This is pulled from the properties file for the LocoNet menu entry for
     * this tool, to ensure consistency between the menu and the window title.
     * 
     * @return an internationalized string containing the tool's LocoNet menu name
     */
    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemLocoStats"));
    }

    /**
     * Constructor
     */
    public LocoStatsPanel() {
        super();
    }

    /**
     * GUI initializations
     */
    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add GUI items
        rawPanel = new JPanel();
        rawPanel.setLayout(new BoxLayout(rawPanel, BoxLayout.X_AXIS));
        rawPanel.add(new JLabel(Bundle.getMessage("LabelRawData")));
        rawPanel.add(r1);
        rawPanel.add(r2);
        rawPanel.add(r3);
        rawPanel.add(r4);
        rawPanel.add(r5);
        rawPanel.add(r6);
        rawPanel.add(r7);
        rawPanel.add(r8);
        
        lb2Panel = new JPanel();
        lb2Panel.setLayout(new BoxLayout(lb2Panel, BoxLayout.X_AXIS));
        lb2Panel.add(new JLabel(Bundle.getMessage("LabelVersion")));
        lb2Panel.add(version);
        lb2Panel.add(new JLabel(Bundle.getMessage("LabelBreaks")));
        breaks.setPreferredSize(version.getPreferredSize());
        lb2Panel.add(breaks);
        lb2Panel.add(new JLabel(Bundle.getMessage("LabelErrors")));
        errors.setPreferredSize(version.getPreferredSize());
        lb2Panel.add(errors);

        pr2Panel = new JPanel();
        pr2Panel.setLayout(new BoxLayout(pr2Panel, BoxLayout.X_AXIS));
        pr2Panel.add(new JLabel(Bundle.getMessage("LabelSerialNumber")));
        pr2Panel.add(serial);
        pr2Panel.add(new JLabel(Bundle.getMessage("LabelPR2Status")));
        pr2Panel.add(status);
        pr2Panel.add(new JLabel(Bundle.getMessage("LabelCurrent")));
        pr2Panel.add(current);
        pr2Panel.add(new JLabel(Bundle.getMessage("LabelHardwareVersion")));
        pr2Panel.add(hardware);
        pr2Panel.add(new JLabel(Bundle.getMessage("LabelSoftwareVersion")));
        pr2Panel.add(software);

        ms100Panel = new JPanel();
        ms100Panel.setLayout(new BoxLayout(ms100Panel, BoxLayout.X_AXIS));
        ms100Panel.add(new JLabel(Bundle.getMessage("LabelGoodCnt")));
        ms100Panel.add(goodMsgCnt);
        ms100Panel.add(new JLabel(Bundle.getMessage("LabelBadCnt")));
        ms100Panel.add(badMsgCnt);
        ms100Panel.add(new JLabel(Bundle.getMessage("LabelMS100Status")));
        ms100Panel.add(ms100status);

        add(rawPanel);
        add(lb2Panel);
        add(pr2Panel);
        add(ms100Panel);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        add(updateButton);
        add(panel);

        // install "update" button handler
        updateButton.addActionListener((ActionEvent a) -> {
            requestUpdate();
        });

        // and prep for display
        lb2Panel.setVisible(false);
        rawPanel.setVisible(true);
        pr2Panel.setVisible(false);
        ms100Panel.setVisible(false);
        revalidate();

        // will connect when memo is available
    }

    /**
     * Configure LocoNet connection
     * 
     * @param memo  specifies which LocoNet connection is used by this tool
     */
    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        
        stats = new LocoStatsFunc(memo);
        stats.addLocoNetInterfaceStatsListener(this);

        // request interface data from interface device
        requestUpdate();
    }
    
    /**
     * Destructor
     */
    @Override
    public void dispose() {
        // unregister listener
        stats.removeLocoNetInterfaceStatsListener(this);
    }
    
    /**
     * Send LocoNet request for interface status.
     * 
     * Performs the send from the "Layout" thread, to avoid GUI-related 
     * threading problems.
     */
    public void requestUpdate() {
        // Invoke the LocoNet request send on the layout thread, not the GUI thread!
        // Note - there is no guarantee that the LocoNet message will be sent 
        // before execution returns to this method (but it might).
        ThreadingUtil.runOnLayoutEventually(()->{  stats.sendLocoNetInterfaceStatusQueryMessage(); });
        updateRequestPending = true;
        log.debug("Sending a ping");
    }

    JTextField r1 = new JTextField(5);
    JTextField r2 = new JTextField(5);
    JTextField r3 = new JTextField(5);
    JTextField r4 = new JTextField(5);
    JTextField r5 = new JTextField(5);
    JTextField r6 = new JTextField(5);
    JTextField r7 = new JTextField(5);
    JTextField r8 = new JTextField(5);

    JTextField serial = new JTextField(6);
    JTextField status = new JTextField(5);
    JTextField current = new JTextField(4);
    JTextField hardware = new JTextField(2);
    JTextField software = new JTextField(3);

    JTextField goodMsgCnt = new JTextField(5);
    JTextField badMsgCnt = new JTextField(5);
    JTextField ms100status = new JTextField(6);

    JTextField version = new JTextField(8);
    JTextField breaks = new JTextField(6);
    JTextField errors = new JTextField(6);


    JButton updateButton = new JButton(Bundle.getMessage("ButtonTextUpdate"));

    /**
     * Nested class to create one of these tools using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemLocoStats"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    LocoStatsPanel.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

    /**
     * Listener for LocoNet Interface Status changes
     * 
     * @param o a LocoNetStatus object
     */
    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "GUI elements are created such that cast to JmriJFrame this is accurate")
    public void notifyChangedInterfaceStatus(Object o) {
        log.debug("Update is being handled:" +o.toString());  // NOI18N
        if (!updateRequestPending) {
            return;
        }
        
        if (o.getClass() == LocoBufferIIStatus.class) {
            LocoBufferIIStatus s = (LocoBufferIIStatus) o;
            version.setText((Integer.toHexString(s.version)));
            breaks.setText((Integer.toString(s.breaks)));
            errors.setText((Integer.toString(s.errors)));
            lb2Panel.setVisible(true);
            rawPanel.setVisible(false);
            ms100Panel.setVisible(false);
            pr2Panel.setVisible(false);
            ((JmriJFrame) getRootPane().getParent()).setPreferredSize(null);
            ((JmriJFrame) getRootPane().getParent()).pack();
        } else if (o.getClass() == PR2Status.class) {
            PR2Status s = (PR2Status) o;
            serial.setText(Integer.toString(s.serial));
            status.setText(Integer.toString(s.status));
            current.setText(Integer.toString(s.current));
            hardware.setText(Integer.toString(s.hardware));
            software.setText(Integer.toString(s.software));
            lb2Panel.setVisible(false);
            rawPanel.setVisible(false);
            ms100Panel.setVisible(true);
            pr2Panel.setVisible(true);
            ((JmriJFrame) getRootPane().getParent()).setPreferredSize(null);
            ((JmriJFrame) getRootPane().getParent()).pack();
        } else if (o.getClass() == PR3MS100ModeStatus.class) {
            PR3MS100ModeStatus s = (PR3MS100ModeStatus) o;
            goodMsgCnt.setText(Integer.toString(s.goodMsgCnt));
            badMsgCnt.setText(Integer.toString(s.badMsgCnt));
            ms100status.setText(Integer.toString(s.ms100status));
            lb2Panel.setVisible(false);
            rawPanel.setVisible(false);
            ms100Panel.setVisible(true);
            pr2Panel.setVisible(true);
            ((JmriJFrame) getRootPane().getParent()).setPreferredSize(null);
            ((JmriJFrame) getRootPane().getParent()).pack();
        } else if (o.getClass() == RawStatus.class) {
            RawStatus s = (RawStatus)o;
            r1.setText(Integer.toString(s.raw[0]));
            r2.setText(Integer.toString(s.raw[1]));
            r3.setText(Integer.toString(s.raw[2]));
            r4.setText(Integer.toString(s.raw[3]));
            r5.setText(Integer.toString(s.raw[4]));
            r6.setText(Integer.toString(s.raw[5]));
            r7.setText(Integer.toString(s.raw[6]));
            r8.setText(Integer.toString(s.raw[7]));
            lb2Panel.setVisible(false);
            rawPanel.setVisible(true);
            ms100Panel.setVisible(false);
            pr2Panel.setVisible(false);
            ((JmriJFrame) getRootPane().getParent()).setPreferredSize(null);
            ((JmriJFrame) getRootPane().getParent()).pack();
            }
        }

    private final static Logger log = LoggerFactory.getLogger(LocoStatsPanel.class);

}
