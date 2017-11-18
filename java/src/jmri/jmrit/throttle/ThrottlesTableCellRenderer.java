package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;
import jmri.InstanceManager;
import jmri.Throttle;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.util.FileUtil;

public class ThrottlesTableCellRenderer implements TableCellRenderer {

    private static final ImageIcon fwdIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/up-green.png"));
    private static final ImageIcon bckIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/down-green.png"));
    private static final ImageIcon estopIcon = new ImageIcon(FileUtil.findURL("resources/icons/throttles/estop24.png"));
    private static final RosterIconFactory iconFactory = new RosterIconFactory(32);
    final static int height = 42;

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean bln, boolean bln1, int i, int i1) {
        JPanel retPanel = new JPanel();
        retPanel.setLayout(new BorderLayout());

        if (value == null) {
            return retPanel;
        }

        ThrottleFrame tf = (ThrottleFrame) value;
        ImageIcon icon = null;
        String text;
        if (tf.getRosterEntry() != null) {
            icon = iconFactory.getIcon(tf.getAddressPanel().getRosterEntry());
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

        if (tf.getAddressPanel().getThrottle() != null) {
            JPanel ctrlPanel = new JPanel();
            ctrlPanel.setLayout(new BorderLayout());
            Throttle thr = tf.getAddressPanel().getThrottle();
            JLabel dir = new JLabel();
            if (InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isUsingExThrottle()
                    && InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isUsingFunctionIcon()) {
                if (thr.getIsForward()) {
                    dir.setIcon(fwdIcon);
                } else {
                    dir.setIcon(bckIcon);
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
            if (InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isUsingExThrottle()
                    && InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences().isUsingFunctionIcon()) {
                if (thr.getSpeedSetting() == -1) {
                    JLabel estop = new JLabel();
                    estop.setPreferredSize(new Dimension(64, height - 8));
                    estop.setHorizontalAlignment(JLabel.CENTER);
                    estop.setIcon(estopIcon);
                    ctrlPanel.add(estop, BorderLayout.CENTER);
                } else {
                    JProgressBar speedBar = new javax.swing.JProgressBar();
                    speedBar.setPreferredSize(new Dimension(64, height - 8));
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
            retPanel.add(ctrlPanel, BorderLayout.EAST);
        }

        retPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return retPanel;
    }
}
