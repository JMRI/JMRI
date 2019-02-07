package jmri.jmrix.loconet.clockmon;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.swing.LnPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane displaying a LocoNet clock monitor.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <p>
 * The original module has been converted to a clock monitor by removing all
 * active items (Dave Duchamp 2007-2008).
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2010
 */
public class ClockMonPane extends LnPanel implements SlotListener {

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.clockmon.ClockMonFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemClockMon"));
    }

    public ClockMonPane() {
        super();
    }

    @Override
    public void initComponents(final LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add GUI items
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        panel1.add(new JLabel(Bundle.getMessage("ClockDayLabel")));
        panel1.add(days);
        days.setPreferredSize(spacer.getPreferredSize());
        panel1.add(new JLabel(Bundle.getMessage("ClockTimeLabel")));
        panel1.add(hours);
        hours.setPreferredSize(spacer.getPreferredSize());
        panel1.add(new JLabel(":"));
        panel1.add(minutes);
        minutes.setPreferredSize(spacer.getPreferredSize());
        panel1.add(new JLabel("."));
        panel1.add(frac_mins);
        add(panel1);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.add(new JLabel(Bundle.getMessage("ClockRateLabel")));
        panel2.add(rate);
        add(panel2);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout());
        panel3.add(readButton);
        add(panel3);
        // Load GUI element contents with current slot contents
        notifyChangedSlot(memo.getSlotManager().slot(LnConstants.FC_SLOT));

        // install "read" button handler
        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                memo.getSlotManager().sendReadSlot(LnConstants.FC_SLOT);
            }
        }
        );
        // listen for updated slot contents
        if (memo.getSlotManager() != null) {
            memo.getSlotManager().addSlotListener(this);
        } else {
            log.error("No LocoNet connection available, can't function"); // NOI18N
        }
    }

    /**
     * Handle changed slot contents, due to clock changes.
     *
     */
    @Override
    public void notifyChangedSlot(LocoNetSlot s) {
        if (s.getSlot() != LnConstants.FC_SLOT) {
            return; // only watch clock slot
        }
        if (log.isDebugEnabled()) {
            log.debug("slot update " + s); // NOI18N
        }

        // update GUI from the new slot contents
        days.setText("" + s.getFcDays());
        hours.setText("" + s.getFcHours());
        minutes.setText("" + s.getFcMinutes());
        rate.setText("" + s.getFcRate());
        frac_mins.setText("" + s.getFcFracMins());
    }

    @Override
    public void dispose() {
        // Drop LocoNet connection
        if (memo.getSlotManager() != null) {
            memo.getSlotManager().removeSlotListener(this);
        }

        // take apart the JFrame
        super.dispose();
    }

    JTextField days = new JTextField("00"); // NOI18N
    JTextField hours = new JTextField("00"); // NOI18N
    JTextField minutes = new JTextField("00"); // NOI18N
    JTextField frac_mins = new JTextField("00"); // NOI18N

    JTextField rate = new JTextField(4);

    JButton readButton = new JButton(Bundle.getMessage("ButtonRead"));
    final static JTextField spacer = new JTextField("123");

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemClockMon"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    ClockMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ClockMonPane.class);

}
