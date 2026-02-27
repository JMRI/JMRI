package jmri.jmrit.throttle;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;

import jmri.InstanceManager;
import jmri.Throttle;
import jmri.jmrit.consisttool.ConsistListCellRenderer;
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
    private static final ImageIcon STOP_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/stopOn24.png"));    
    final static int IMAGE_HEIGHT = 32;
    private static final RosterIconFactory ICN_FACT = new RosterIconFactory(IMAGE_HEIGHT);
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
            JLabel consistLabel = new JLabel();
            consistLabel.setOpaque(false);
            consistLabel.setHorizontalAlignment(JLabel.RIGHT);
            consistLabel.setVerticalAlignment(JLabel.CENTER);
            consistLabel.setIcon(ConsistListCellRenderer.getConsistIcon(tf.getAddressPanel().getConsistAddress(), ICN_FACT));
            consistLabel.setText(tf.getAddressPanel().getConsistAddress().toString());
            retPanel.add(consistLabel, BorderLayout.CENTER);
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
            locoID.setHorizontalAlignment(JLabel.RIGHT);
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
                JLayeredPane layerPane = new JLayeredPane();
                int cmpWidth = jtable.getWidth()/jtable.getColumnCount()/4 ;
                layerPane.setPreferredSize(new Dimension(cmpWidth, LINE_HEIGHT - 8));
                // speed
                JProgressBar speedBar = new javax.swing.JProgressBar();
                speedBar.setBounds(0,0,cmpWidth, LINE_HEIGHT - 8);
                speedBar.setMinimum(0);
                speedBar.setMaximum(1000);
                speedBar.setValue((int) (thr.getSpeedSetting() * 1000f));
                layerPane.add(speedBar, JLayeredPane.DEFAULT_LAYER);                
                // estop & stop icon
                if (thr.getSpeedSetting() == -1) {
                    JLabel estop = new JLabel();                    
                    estop.setHorizontalAlignment(JLabel.CENTER);
                    estop.setIcon(ESTOP_ICN);
                    estop.setBounds(0,0,cmpWidth, LINE_HEIGHT - 8);
                    layerPane.add(estop, JLayeredPane.PALETTE_LAYER);
                } else if (thr.getSpeedSetting() == 0) {
                    JLabel stop = new JLabel();                    
                    stop.setHorizontalAlignment(JLabel.CENTER);
                    stop.setIcon(STOP_ICN);
                    stop.setBounds(0,0,cmpWidth, LINE_HEIGHT - 8);
                    layerPane.add(stop, JLayeredPane.PALETTE_LAYER);
                }
                ctrlPanel.add(layerPane, BorderLayout.EAST);
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
            Color selBackground = javax.swing.UIManager.getDefaults().getColor("List.selectionBackground");
            if (selBackground == null) {
                selBackground = Color.ORANGE;
            }
            Color selForeground = javax.swing.UIManager.getDefaults().getColor("List.selectionForeground");
            if (selForeground == null) {
                selForeground = Color.BLACK;
            }
            retPanel.setBackground(selBackground);
            setForegroundAllComp( retPanel, selForeground );
            retPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        } else {
            retPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
        return retPanel;
    }
    
    private void setForegroundAllComp(JComponent cmp, Color color) {
        if (cmp != null) {
            cmp.setForeground(color);
            for (Component c : cmp.getComponents()) {
                if (c instanceof JComponent) {
                    setForegroundAllComp( (JComponent) c, color);
                }
            }
        }
    }
}
