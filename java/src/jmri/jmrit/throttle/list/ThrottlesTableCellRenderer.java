package jmri.jmrit.throttle.list;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;

import jmri.InstanceManager;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.FileUtil;

/**
 * A TableCellRender to graphicaly display an active throttles in a summary table
 * (see ThrottlesListPanel)
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * @author Lionel Jeanson - 2011-2026
 * 
 */

public class ThrottlesTableCellRenderer implements TableCellRenderer {

    private static final ImageIcon FWD_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirFwdOn.png"));
    private static final ImageIcon BCK_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/dirBckOn.png"));
    private static final ImageIcon ESTOP_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/estop24.png"));
    private static final ImageIcon STOP_ICN = new ImageIcon(FileUtil.findURL("resources/icons/throttles/stopOn24.png"));    
    static final int LINE_HEIGHT = 42;    
    
    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean bln, boolean bln1, int i, int i1) {
        JPanel retPanel = new JPanel();
        retPanel.setLayout(new BorderLayout());

        if (value == null) {
            return retPanel;
        }

        ThrottleControllerUI tf = (ThrottleControllerUI) value;

        retPanel.add(tf.getLabel(), BorderLayout.CENTER);
        
        if (tf.getThrottle() != null) {
            final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
            JPanel ctrlPanel = new JPanel();
            ctrlPanel.setLayout(new BorderLayout());
            // direction
            JLabel dir = new JLabel();
            if (preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()) {
                if (tf.getThrottle().getIsForward()) {
                    dir.setIcon(FWD_ICN);
                } else {
                    dir.setIcon(BCK_ICN);
                }
            } else {
                if (tf.getThrottle().getIsForward()) {
                    dir.setText(Bundle.getMessage("ButtonForward"));
                } else {
                    dir.setText(Bundle.getMessage("ButtonReverse"));
                }
            }
            dir.setVerticalAlignment(JLabel.CENTER);
            ctrlPanel.add(dir, BorderLayout.WEST);
            // speed
            if (preferences.isUsingExThrottle() && preferences.isUsingLargeSpeedSlider()) {
                JLayeredPane layerPane = new JLayeredPane();
                int cmpWidth = jtable.getWidth()/jtable.getColumnCount()/4 ;
                layerPane.setPreferredSize(new Dimension(cmpWidth, LINE_HEIGHT - 8));
                // speed
                JProgressBar speedBar = new javax.swing.JProgressBar();
                speedBar.setBounds(0,0,cmpWidth, LINE_HEIGHT - 8);
                speedBar.setMinimum(0);
                speedBar.setMaximum(1000);
                speedBar.setValue((int) (tf.getThrottle().getSpeedSetting() * 1000f));
                layerPane.add(speedBar, JLayeredPane.DEFAULT_LAYER);                
                // estop & stop icon
                if (tf.getThrottle().getSpeedSetting() == -1) {
                    JLabel estop = new JLabel();                    
                    estop.setHorizontalAlignment(JLabel.CENTER);
                    estop.setIcon(ESTOP_ICN);
                    estop.setBounds(0,0,cmpWidth, LINE_HEIGHT - 8);
                    layerPane.add(estop, JLayeredPane.PALETTE_LAYER);
                } else if (tf.getThrottle().getSpeedSetting() == 0) {
                    JLabel stop = new JLabel();                    
                    stop.setHorizontalAlignment(JLabel.CENTER);
                    stop.setIcon(STOP_ICN);
                    stop.setBounds(0,0,cmpWidth, LINE_HEIGHT - 8);
                    layerPane.add(stop, JLayeredPane.PALETTE_LAYER);
                }
                ctrlPanel.add(layerPane, BorderLayout.EAST);
            } else {
                JLabel speedLabel = new JLabel("");
                if (tf.getThrottle().getSpeedSetting() == -1) {
                    speedLabel.setText(" " + Bundle.getMessage("ButtonEStop") + " ");
                } else {
                    speedLabel.setText(" " + (int) (tf.getThrottle().getSpeedSetting() * 100f) + "% ");
                }
                ctrlPanel.add(speedLabel, BorderLayout.CENTER);
            }
            ctrlPanel.setOpaque(false);
            retPanel.add(ctrlPanel, BorderLayout.EAST);
        }
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

//    private static final Logger log = LoggerFactory.getLogger(ThrottlesTableCellRenderer.class);

}
