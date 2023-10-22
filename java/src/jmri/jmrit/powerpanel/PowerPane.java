package jmri.jmrit.powerpanel;

import java.awt.Color;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.swing.PowerManagerMenu;

/**
 * Pane for power control
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2010
 */
public class PowerPane extends jmri.util.swing.JmriPanel {

    final jmri.swing.PowerManagerMenu selectMenu;
    final JPanel contentP;
    final List<SinglePowerPane> mgrList = new java.util.ArrayList<>();

    /**
     * Constructor for PowerPane.
     */
    public PowerPane() {
        this(null);
    }

    public PowerPane(PowerManager initialMgr){
        contentP = new JPanel();
        selectMenu = new PowerManagerMenuImpl(initialMgr);
        contentP.setLayout(new BoxLayout(contentP, BoxLayout.Y_AXIS));
        add(contentP);
        PowerPane.this.managerChanged();
    }

    /**
     * Add Connection menu to choose which to turn on/off.
     * @return List of menu items (all active connections)
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> list = new java.util.ArrayList<>();
        list.add(selectMenu);
        return list;
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrit.powerpanel.PowerPanelFrame";
    }

    @Override
    public String getTitle() {
        var mgr = selectMenu.getManager();
        return Bundle.getMessage("TitlePowerPanel") + " : " +
           ( mgr== null ? Bundle.getMessage("AllConnections") : mgr.getUserName());
    }

    /**
     * Reset listener and update status.
     */
    void managerChanged() {
        log.debug("manager changed to {}", selectMenu.getManager() );

        mgrList.forEach( SinglePowerPane::dispose);
        mgrList.clear();
        contentP.removeAll();
        var mgr = selectMenu.getManager();
        if ( mgr != null ){
            mgrList.add(new SinglePowerPane(mgr));
        } else {
            List<PowerManager> managers = InstanceManager.getList(PowerManager.class);
            for (PowerManager pm : managers) {
                mgrList.add(new SinglePowerPane(pm));
            }
        }
        for ( SinglePowerPane spp: mgrList){
            contentP.add(spp);
        }

        JFrame f = (JFrame)javax.swing.SwingUtilities.windowForComponent(this);
        if ( f != null ) {
            f.pack();
            f.setTitle(this.getTitle());
        }
    }

    @Override
    public void dispose() {
        mgrList.forEach( SinglePowerPane::dispose);
    }

    private static final NamedIcon onIcon = new NamedIcon("resources/icons/throttles/power_green.png", "resources/icons/throttles/power_green.png") ;
    private static final NamedIcon offIcon = new NamedIcon("resources/icons/throttles/power_red.png", "resources/icons/throttles/power_red.png") ;
    private static final NamedIcon unknownIcon = new NamedIcon("resources/icons/throttles/power_yellow.png", "resources/icons/throttles/power_yellow.png") ;

    class SinglePowerPane extends javax.swing.JPanel implements java.beans.PropertyChangeListener {

        private final PowerManager powerMgr;

        // GUI member declarations
        private final JLabel onOffStatus = new JLabel(Bundle.getMessage("LabelUnknown"));
        private final JLabel connLabel = new JLabel(Bundle.getMessage("LabelLayoutPower"));
        private final JButton onButton = new JButton(Bundle.getMessage("ButtonOn"));
        private final JButton offButton = new JButton(Bundle.getMessage("ButtonOff"));
        private final JButton idleButton = new JButton(Bundle.getMessage("ButtonIdle"));

        SinglePowerPane(@Nonnull PowerManager powerManager){
        
            super();
            powerMgr = powerManager;

            // add listeners to buttons
            onButton.addActionListener(e -> onButtonPushed());
            offButton.addActionListener(e -> offButtonPushed());
            idleButton.addActionListener(e -> idleButtonPushed());
            idleButton.setToolTipText(Bundle.getMessage("ToolTipIdleButton"));

            // general GUI config
            setLayout(new java.awt.GridLayout((powerMgr.implementsIdle() ? 3 : 2), 2, 3, 5)); // r, c, hgap , vgap
            var border = BorderFactory.createLineBorder(Color.BLACK, 1);
            setBorder(BorderFactory.createTitledBorder(border,
                PowerManagerMenu.getManagerNameIncludeIfDefault(powerMgr)));

            // set minimum size ( for all 6 cells in the layout ) to prevent twitching
            onOffStatus.setPreferredSize(new java.awt.Dimension(getLabelMinimumWidth(onOffStatus), onButton.getPreferredSize().height+10 ));

            // install items in GUI
            add(connLabel);
            add(onButton);
            add(onOffStatus); // on row 2
            add(offButton);
            
            if ( powerMgr.implementsIdle()) {
                add(new JLabel("")); // on row 3
                add(idleButton);
            }
            powerMgr.addPropertyChangeListener(SinglePowerPane.this);
            setStatus();
        }

        /**
         * Respond to Power On button pressed.
         */
        private void onButtonPushed() {
            if (mgrOK()) {
                try {
                    powerMgr.setPower(PowerManager.ON);
                } catch (JmriException e) {
                    couldNotSetPower("Exception trying to turn power on", e);
                }
            }
        }

        /**
         * Respond to Power Off button pressed.
         */
        private void offButtonPushed() {
            if (mgrOK()) {
                try {
                    powerMgr.setPower(PowerManager.OFF);
                } catch (JmriException e) {
                    couldNotSetPower("Exception trying to turn power off", e);
                }
            }
        }

        /**
         * Respond to Power Idle button pressed.
         */
        private void idleButtonPushed() {
            if ( mgrOK() && powerMgr.implementsIdle() ) {
                try {
                    powerMgr.setPower(PowerManager.IDLE);
                } catch (JmriException e) {
                    couldNotSetPower("Exception trying to set power to idle", e);
                }
            }
        }

        private void couldNotSetPower( String action, JmriException e){
            log.error("PowerPane {}", action, e);
            jmri.util.swing.JmriJOptionPane.showMessageDialog(this,
                powerMgr.getUserName() + System.lineSeparator() +
                action + System.lineSeparator() + e.getMessage(),
                action,
                jmri.util.swing.JmriJOptionPane.ERROR_MESSAGE);
        }

        /**
         * Get Minimum width for the current power status JLabel.
         * @return minimum width
         */
        private int getLabelMinimumWidth (JLabel label){
            String[] bundleStrings = {"StatusIdle", "StatusOn", "StatusOff", "StatusUnknown"};
            int a= 10;
            for ( String bs : bundleStrings ) {
                java.awt.FontMetrics fm = label.getFontMetrics(label.getFont());
                int wi = fm.stringWidth(Bundle.getMessage(bs))
                    + onIcon.getIconWidth() + 5;
                a = Math.max(a, wi);
            }
            return a;
        }

        /**
         * Display status changes from PowerManager in PowerPane.
         */
        private void setStatus() {
            // Check to see if the Power Manager has a current status
            if (mgrOK()) {
                switch (powerMgr.getPower()) {
                    case PowerManager.ON:
                        onOffStatus.setText(Bundle.getMessage("StatusOn"));
                        onOffStatus.setIcon(onIcon);
                        break;
                    case PowerManager.OFF:
                        onOffStatus.setText(Bundle.getMessage("StatusOff"));
                        onOffStatus.setIcon(offIcon);
                        break;
                    case PowerManager.IDLE:
                        onOffStatus.setText(Bundle.getMessage("StatusIdle"));
                        onOffStatus.setIcon(unknownIcon);
                        break;
                    case PowerManager.UNKNOWN:
                        onOffStatus.setText(Bundle.getMessage("StatusUnknown"));
                        onOffStatus.setIcon(unknownIcon);
                        break;
                    default:
                        onOffStatus.setText(Bundle.getMessage("StatusUnknown"));
                        onOffStatus.setIcon(unknownIcon);
                        log.error("Unexpected state value: {}", selectMenu.getManager());
                        break;
                }
            }
        }

        /**
         * Check for presence of PowerManager.
         * @return True if one is available, false if not
         */
        private boolean mgrOK() {
            return InstanceManager.getList(PowerManager.class).contains(powerMgr);
        }
    
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent ev) {
            setStatus();
        }

        void dispose() {
            powerMgr.removePropertyChangeListener(this);
        }
        
    }

    class PowerManagerMenuImpl extends PowerManagerMenu {

        PowerManagerMenuImpl(PowerManager mgr) {
            super(true, mgr);
        }

        @Override
        protected void choiceChanged() {
            managerChanged();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PowerPane.class);

}
