// ClockMonPane.java

package jmri.jmrix.loconet.clockmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.swing.LnPanel;

import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.*;

/**
 * Pane displaying a LocoNet clock monitor.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * The original module has been converted to a clock monitor by removing all active 
 * items (Dave Duchamp 2007-2008).
 *
 * @author			Bob Jacobsen   Copyright (C) 2003, 2004, 2010
 * @version			$Revision$
 */
public class ClockMonPane extends LnPanel implements SlotListener {

    public String getHelpTarget() { return "package.jmri.jmrix.loconet.clockmon.ClockMonFrame"; }
    public String getTitle() { 
        return getTitle(jmri.jmrix.loconet.LocoNetBundle.bundle().getString("MenuItemClockMon")); 
    }

    public ClockMonPane() {
        super();
    }
    
    public void initComponents(final LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add GUI items
		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout());
        panel1.add(new JLabel(" Day:"));
        panel1.add(days);
        panel1.add(new JLabel(" Time:"));
        panel1.add(hours);
        panel1.add(new JLabel(":"));
        panel1.add(minutes);
        panel1.add(new JLabel("."));
        panel1.add(frac_mins);
        add(panel1);

        JPanel panel2 = new JPanel();
		panel2.setLayout(new FlowLayout());
        panel2.add(new JLabel(" Rate:"));
        panel2.add(rate);
        add(panel2);

        JPanel panel3 = new JPanel();
		panel3.setLayout(new FlowLayout());
		panel3.add(readButton);
        add(panel3);
        // Load GUI element contents with current slot contents
        notifyChangedSlot(memo.getSlotManager().slot(LnConstants.FC_SLOT));

        // install "read" button handler
        readButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	memo.getSlotManager().sendReadSlot(LnConstants.FC_SLOT);
                }
            }
        );
        // listen for updated slot contents
        if (memo.getSlotManager()!=null)
            memo.getSlotManager().addSlotListener(this);
        else
            log.error("No LocoNet connection available, can't function");

    }

    /**
     * Handle changed slot contents, due to clock changes.
     * @param s
     */
    public void notifyChangedSlot(LocoNetSlot s) {
        if (s.getSlot()!= LnConstants.FC_SLOT ) return; // only watch clock slot
        if (log.isDebugEnabled()) log.debug("slot update "+s);

        // update GUI from the new slot contents
        days.setText(""+s.getFcDays());
        hours.setText(""+s.getFcHours());
        minutes.setText(""+s.getFcMinutes());
        rate.setText(""+s.getFcRate());
        frac_mins.setText( ""+s.getFcFracMins());
    }

    public void dispose() {
        // Drop loconet connection
        if (memo.getSlotManager()!=null)
            memo.getSlotManager().removeSlotListener(this);

        // take apart the JFrame
        super.dispose();
    }

    JTextField days = new JTextField("00");
    JTextField hours = new JTextField("00");
    JTextField minutes = new JTextField("00");
    JTextField frac_mins = new JTextField("00");

    JTextField rate = new JTextField(4);

    JButton readButton = new JButton("Read");

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {
        public Default() {
            super(LocoNetBundle.bundle().getString("MenuItemClockMon"), 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                ClockMonPane.class.getName(), 
                jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }
    
    static Logger log = LoggerFactory.getLogger(ClockMonPane.class.getName());

}
