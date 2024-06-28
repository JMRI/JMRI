package jmri.jmrit.etcs.dmi.swing;

import java.awt.*;

import javax.annotation.CheckForNull;
import javax.swing.*;

import jmri.jmrit.etcs.ResourceUtil;
import jmri.util.swing.JmriMouseAdapter;
import jmri.util.swing.JmriMouseEvent;

/**
 * Class to demonstrate features of ERTMS DMI Panel F, the Menu area.
 * @author Steve Young Copyright (C) 2024
 */
public class DmiPanelF extends JPanel {

    private final JButton f1;
    private final JButton f2;
    private final JButton f3;
    private final JButton f4;
    private final JButton f5;

    private final DmiPanel main;

    public DmiPanelF(@CheckForNull DmiPanel mainPane){

        super();
        setLayout(null); // Set the layout manager to null

        setBackground(DmiPanel.BACKGROUND_COLOUR);
        setBounds(580, 15, 60, 450);

        main = mainPane;

        f1 = new JButton(Bundle.getMessage("ButtonMain"));
        f2 = new JButton(Bundle.getMessage("ButtonOverRide"));
        f3 = new JButton(Bundle.getMessage("ButtonDataView"));
        f4 = new JButton("Spec");
        f5 = new JButton();

        f1.setBounds(0, 0, 60, 50);
        f2.setBounds(0, 50, 60, 50);
        f3.setBounds(0, 100, 60, 50);
        f4.setBounds(0, 150, 60, 50);
        f5.setBounds(0, 200, 60, 50);

        f1.setBackground(DmiPanel.BACKGROUND_COLOUR);
        f2.setBackground(DmiPanel.BACKGROUND_COLOUR);
        f3.setBackground(DmiPanel.BACKGROUND_COLOUR);
        f4.setBackground(DmiPanel.BACKGROUND_COLOUR);
        f5.setBackground(DmiPanel.BACKGROUND_COLOUR);

        f1.setForeground(DmiPanel.GREY);
        f2.setForeground(DmiPanel.GREY);
        f3.setForeground(DmiPanel.GREY);
        f4.setForeground(DmiPanel.GREY);
        

        f1.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
        f2.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
        f3.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
        f4.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
        f5.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));

        f1.setFocusable(false);
        f2.setFocusable(false);
        f3.setFocusable(false);
        f4.setFocusable(false);
        f5.setFocusable(false);

        f1.setHorizontalAlignment(SwingConstants.CENTER);
        f2.setHorizontalAlignment(SwingConstants.CENTER);
        f3.setHorizontalAlignment(SwingConstants.CENTER);
        f4.setHorizontalAlignment(SwingConstants.CENTER);
        f5.setHorizontalAlignment(SwingConstants.CENTER);

        f1.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 17));
        f2.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 17));
        f3.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 17));
        f4.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 17));
        f5.setIcon(ResourceUtil.getImageIcon( "SE_04.bmp"));

        add(f1);
        add(f2);
        add(f3);
        add(f4);
        add(f5);

        f5.addMouseListener(jmri.util.swing.JmriMouseListener.adapt(new PopupListener()));
    }

    /**
     * Listener to trigger display of table header column menu.
     */
    private class PopupListener extends JmriMouseAdapter {

        private final jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        private JCheckBoxMenuItem jcmi;

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        private void showPopup(JmriMouseEvent e){

            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem mi = new JMenuItem(Bundle.getMessage("RunDemo"));
            mi.addActionListener(this::runDemo);
            popupMenu.add(mi);

            jcmi = new JCheckBoxMenuItem(Bundle.getMessage("CentreSpeedText"));
            jcmi.setToolTipText(Bundle.getMessage("GlobalPreference"));
            jcmi.setSelected(p.getSimplePreferenceState(DmiPanel.PROPERTY_CENTRE_TEXT));
            jcmi.addActionListener(this::setCentreSpeedText);
            popupMenu.add(jcmi);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        private void runDemo(java.awt.event.ActionEvent e){
            log.debug("running demo from {}", e.getActionCommand());
            new DmiDemo(main).runDemo();
        }

        private void setCentreSpeedText(java.awt.event.ActionEvent e) {
            log.debug("set centre speed pref from {}", e.getActionCommand());
            p.setSimplePreferenceState(DmiPanel.PROPERTY_CENTRE_TEXT, jcmi.isSelected());
            main.repaint();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiPanelF.class);

}
