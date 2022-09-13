package jmri.jmrit.powerpanel;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for power control
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2010
 */
public class PowerPane extends jmri.util.swing.JmriPanel
        implements java.beans.PropertyChangeListener {

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrit.powerpanel.PowerPanelFrame";
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("TitlePowerPanel");
    }

    // GUI member declarations
    JLabel onOffStatus = new JLabel(Bundle.getMessage("LabelUnknown"));
    JButton onButton = new JButton(Bundle.getMessage("ButtonOn"));
    JButton offButton = new JButton(Bundle.getMessage("ButtonOff"));
    JButton idleButton = new JButton(Bundle.getMessage("ButtonIdle"));

    NamedIcon onIcon = new NamedIcon("resources/icons/throttles/power_green.png", "resources/icons/throttles/power_green.png") ;
    NamedIcon offIcon = new NamedIcon("resources/icons/throttles/power_red.png", "resources/icons/throttles/power_red.png") ;
    NamedIcon unknownIcon = new NamedIcon("resources/icons/throttles/power_yellow.png", "resources/icons/throttles/power_yellow.png") ;

    jmri.swing.PowerManagerMenu selectMenu;

    /**
     * Add Connection menu to choose which to turn on/off.
     * @return List of menu items (all active connections)
     */
    @Override
    public List<JMenu> getMenus() {
        java.util.ArrayList<JMenu> list = new java.util.ArrayList<JMenu>();
        list.add(selectMenu);
        return list;
    }

    PowerManager listening = null;

    /**
     * Constructor for PowerPane.
     */
    public PowerPane() {
        init();
    }

    private void init() {
        selectMenu = new jmri.swing.PowerManagerMenu() {
            @Override
            protected void choiceChanged() {
                managerChanged();
            }
        };

        // add listeners to buttons
        onButton.addActionListener(e -> onButtonPushed());
        offButton.addActionListener(e -> offButtonPushed());
        idleButton.addActionListener(e -> idleButtonPushed());

        if ((selectMenu != null) && (selectMenu.getManager() != null)) {
            idleButton.setVisible(selectMenu.getManager().implementsIdle());
        } else {
            // assume IDLE not supported if no manager or selectMenu
            idleButton.setVisible(false);
        }
        idleButton.setToolTipText(Bundle.getMessage("ToolTipIdleButton"));

        // general GUI config
        setLayout(new jmri.util.javaworld.GridLayout2(3, 2, 6, 0)); // r, c, hgap , vgap

        // install items in GUI
        add(new JLabel(Bundle.getMessage("LabelLayoutPower")));
        add(onButton);
        add(onOffStatus); // on row 2
        add(offButton);
        add(new JLabel("")); // on row 3
        add(idleButton);

        setStatus();
    }

    /**
     * Display status changes from PowerManager in PowerPane.
     */
    void setStatus() {
        // Check to see if the Power Manager has a current status
        if (mgrOK()) {
            switch (listening.getPower()) {
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
                    log.error("Unexpected state value: {}", selectMenu.getManager().getPower());
                    break;
            }
        }
    }

    /**
     * Reset listener and update status.
     */
    void managerChanged() {
        if (listening != null) {
            listening.removePropertyChangeListener(this);
        }
        listening = null;
        setStatus();
    }

    /**
     * Check for presence of PowerManager.
     * @return True if one is available, false if not
     */
    private boolean mgrOK() {
        if (listening == null) {
            listening = selectMenu.getManager();
            log.debug("Manager = {}", listening);
            if (listening == null) {
                log.debug("No power manager instance found, panel not active");
                return false;
            } else {
                listening.addPropertyChangeListener(this);
            }
            idleButton.setVisible(listening.implementsIdle());
        }
        return true;
    }

    /**
     * Respond to Power On button pressed.
     */
    public void onButtonPushed() {
        if (mgrOK()) {
            try {
                selectMenu.getManager().setPower(PowerManager.ON);
            } catch (JmriException e) {
                log.error("Exception trying to turn power on", e);
            }
        }
    }

    /**
     * Respond to Power Off button pressed.
     */
    public void offButtonPushed() {
        if (mgrOK()) {
            try {
                selectMenu.getManager().setPower(PowerManager.OFF);
            } catch (JmriException e) {
                log.error("Exception trying to turn power off", e);
            }
        }
    }

    /**
     * Respond to Power Idle button pressed.
     */
    public void idleButtonPushed() {
        if (mgrOK()) {
            if (!listening.implementsIdle()) {
                return;
            }
            try {
                selectMenu.getManager().setPower(PowerManager.IDLE);
            } catch (JmriException e) {
                log.error("Exception trying to set power to idle", e);
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        log.debug("PropertyChange received ");
        switch (listening.getPower()) {
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
                log.error("Unexpected state value: {}", listening.getPower());
                break;
        }
    }

    @Override
    public void dispose() {
        if (listening != null) {
            listening.removePropertyChangeListener(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PowerPane.class);

}
