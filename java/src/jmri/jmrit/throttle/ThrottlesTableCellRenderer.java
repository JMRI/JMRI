package jmri.jmrit.throttle;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;

import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Throttle;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.util.FileUtil;

/**
 * A TableCellRender to graphicaly display an active throttles in a summary table
 * (see ThrottlesListPanel)
 * 
 * @author Lionel Jeanson - 2011
 * 
 */

public class ThrottlesTableCellRenderer implements TableCellRenderer {

    private static final ImageIcon FWD_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirFwdOn.png"));
    private static final ImageIcon BCK_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirBckOn.png"));
    private static final ImageIcon ESTOP_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/estop24.png"));
    private static final RosterIconFactory ICN_FACT = new RosterIconFactory(32);
    final static int LINE_HEIGHT = 42;

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean bln, boolean bln1, int i, int i1) {
        JPanel retPanel = new JPanel();
        retPanel.setLayout(new BorderLayout());

        if (value == null) {
            return retPanel;
        }

        ThrottleFrame tf = (ThrottleFrame) value;
        // loco icon
        if ((tf.getAddressPanel().getConsistAddress() != null) && (tf.getAddressPanel().getThrottle() != null)) {
            // consists
            JPanel consistPanel = new JPanel();
            consistPanel.setLayout(new FlowLayout());
            consistPanel.setOpaque(false);
            Consist consist =  InstanceManager.getDefault(jmri.ConsistManager.class).getConsist(tf.getAddressPanel().getConsistAddress());
            String consistName = "";
            for (DccLocoAddress loco : consist.getConsistList()) {
                String reName = consist.getRosterId(loco);
                JLabel label;
                if (reName != null) {
                    consistName = " ["+reName+"]" + consistName ;
                    label = new JLabel();
                    ImageIcon icon;
                    Boolean dir = consist.getLocoDirection(loco);
                    if (dir) {
                        icon = ICN_FACT.getIcon(reName);
                    } else {
                        icon = ICN_FACT.getReversedIcon(reName);
                    }
                    if (icon != null) {
                        icon.setImageObserver(jtable);
                        label.setIcon(icon);
                    } else {
                        label.setName(reName);
                    }
                } else {
                     label = new JLabel("["+loco.toString()+"]");
                     consistName = " ["+loco.toString()+"]" + consistName ;
                }
                consistPanel.add(label,0); //always add last at first, the consist is facing right
            }
            consistPanel.add(new JLabel(consistName));
            retPanel.add(consistPanel, BorderLayout.CENTER);
        } else { // regular locomotive
            ImageIcon icon = null;
            String text;
            if (tf.getRosterEntry() != null) {
                icon = ICN_FACT.getIcon(tf.getAddressPanel().getRosterEntry());
                text = tf.getAddressPanel().getRosterEntry().getId();
            } else if ((tf.getAddressPanel().getCurrentAddress() != null) && (tf.getAddressPanel().getThrottle() != null)) {
                switch (tf.getAddressPanel().getCurrentAddress().getNumber()) {
                    case 0:
                        text = Bundle.getMessage("ThrottleDCControl") + " - " + tf.getAddressPanel().getCurrentAddress();
                        break;
                    case 3:
                        text = Bundle.getMessage("ThrottleDCCControl") + " - " + tf.getAddressPanel().getCurrentAddress();
                        break;
                    default:
                        text = Bundle.getMessage("ThrottleAddress") + " " + tf.getAddressPanel().getCurrentAddress();
                        break;
                }
            } else {
                text = Bundle.getMessage("ThrottleNotAssigned");
            }
            if (icon != null) {
                icon.setImageObserver(jtable);
            }
            JLabel locoID = new JLabel();
            locoID.setHorizontalAlignment(JLabel.CENTER);
            locoID.setVerticalAlignment(JLabel.CENTER);
            locoID.setIcon(icon);
            locoID.setText(text);        
            retPanel.add(locoID, BorderLayout.CENTER);
        }
        
        if (tf.getAddressPanel().getThrottle() != null) {
            final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
            JPanel ctrlPanel = new JPanel();
            ctrlPanel.setLayout(new BorderLayout());
            Throttle thr = tf.getAddressPanel().getThrottle();
            // direction
            JLabel dir = new JLabel();
            if (preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
                if (thr.getIsForward()) {
                    dir.setIcon(FWD_ICN);
                } else {
                    dir.setIcon(BCK_ICN);
                }
            } else {
                if (thr.getIsForward()) {
                    dir.setText(Bundle.getMessage("ButtonForward"));
                } else {
                    dir.setText(Bundle.getMessage("ButtonReverse"));
                }
            }
            dir.setVerticalAlignment(JLabel.CENTER);
            ctrlPanel.add(dir, BorderLayout.WEST);
            // speed
            if (preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
                if (thr.getSpeedSetting() == -1) {
                    JLabel estop = new JLabel();
                    estop.setPreferredSize(new Dimension(64, LINE_HEIGHT - 8));
                    estop.setHorizontalAlignment(JLabel.CENTER);
                    estop.setIcon(ESTOP_ICN);
                    ctrlPanel.add(estop, BorderLayout.CENTER);
                } else {
                    JProgressBar speedBar = new javax.swing.JProgressBar();
                    speedBar.setPreferredSize(new Dimension(64, LINE_HEIGHT - 8));
                    speedBar.setMinimum(0);
                    speedBar.setMaximum(100);
                    speedBar.setValue((int) (thr.getSpeedSetting() * 100f));
                    ctrlPanel.add(speedBar, BorderLayout.CENTER);
                }
            } else {
                JLabel speedLabel = new JLabel("");
                if (thr.getSpeedSetting() == -1) {
                    speedLabel.setText(" " + Bundle.getMessage("ButtonEStop") + " ");
                } else {
                    speedLabel.setText(" " + (int) (thr.getSpeedSetting() * 100f) + "% ");
                }
                ctrlPanel.add(speedLabel, BorderLayout.CENTER);
            }
            ctrlPanel.setOpaque(false);
            retPanel.add(ctrlPanel, BorderLayout.EAST);
        }
        // visibility -> selected
        if (tf.isVisible()) {
            retPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("List.selectionBackground"));
            retPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        } else {
            retPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
        return retPanel;
    }
}
