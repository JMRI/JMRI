//PowerPane.java
package jmri.jmrit.powerpanel;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import jmri.JmriException;
import jmri.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for power control
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2010
 * @version	$Revision$
 */
public class PowerPane extends jmri.util.swing.JmriPanel
        implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 7334101847422291784L;

    public String getHelpTarget() {
        return "package.jmri.jmrit.powerpanel.PowerPanelFrame";
    }

    public String getTitle() {
        return res.getString("TitlePowerPanel");
    }

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");
    JLabel onOffStatus = new JLabel(res.getString("LabelUnknown"));
    JButton onButton = new JButton(res.getString("ButtonOn"));
    JButton offButton = new JButton(res.getString("ButtonOff"));

    jmri.swing.PowerManagerMenu selectMenu;

    public List<JMenu> getMenus() {
        java.util.ArrayList<JMenu> list = new java.util.ArrayList<JMenu>();
        list.add(selectMenu);
        return list;
    }

    PowerManager listening = null;

    public PowerPane() {
        selectMenu = new jmri.swing.PowerManagerMenu() {
            /**
             *
             */
            private static final long serialVersionUID = -7173050098266625273L;

            protected void choiceChanged() {
                managerChanged();
            }
        };

        // add listeners to buttons
        onButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                onButtonPushed();
            }
        });
        offButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                offButtonPushed();
            }
        });

        // general GUI config
        setLayout(new jmri.util.javaworld.GridLayout2(2, 2));

        // install items in GUI
        add(new JLabel(res.getString("LabelLayoutPower")));
        add(onOffStatus);
        add(onButton);
        add(offButton);

        setStatus();
    }

    void setStatus() {
        // Check to see if the Power Manger has a current status
        if (mgrOK()) {
            try {
                if (listening.getPower() == PowerManager.ON) {
                    onOffStatus.setText(res.getString("StatusOn"));
                } else if (listening.getPower() == PowerManager.OFF) {
                    onOffStatus.setText(res.getString("StatusOff"));
                } else if (listening.getPower() == PowerManager.UNKNOWN) {
                    onOffStatus.setText(res.getString("StatusUnknown"));
                } else {
                    onOffStatus.setText(res.getString("StatusUnknown"));
                    log.error("Unexpected state value: +" + selectMenu.getManager().getPower());
                }
            } catch (JmriException ex) {
                onOffStatus.setText(res.getString("StatusUnknown"));
            }
        }
    }

    void managerChanged() {
        if (listening != null) {
            listening.removePropertyChangeListener(this);
        }
        listening = null;
        setStatus();
    }

    private boolean mgrOK() {
        if (listening == null) {
            listening = selectMenu.getManager();
	        log.debug("Manager = {}", listening);
            if (listening == null) {
                log.warn("No power manager instance found, panel not active");
                return false;
            } else {
                listening.addPropertyChangeListener(this);
            }
        }
        return true;
    }

    public void onButtonPushed() {
        if (mgrOK()) {
            try {
                selectMenu.getManager().setPower(PowerManager.ON);
            } catch (JmriException e) {
                log.error("Exception trying to turn power on " + e);
            }
        }
    }

    public void offButtonPushed() {
        if (mgrOK()) {
            try {
                selectMenu.getManager().setPower(PowerManager.OFF);
            } catch (JmriException e) {
                log.error("Exception trying to turn power off " + e);
            }
        }
    }

    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        log.debug("PropertyChange received ");
        try {
            if (listening.getPower() == PowerManager.ON) {
                onOffStatus.setText(res.getString("StatusOn"));
            } else if (listening.getPower() == PowerManager.OFF) {
                onOffStatus.setText(res.getString("StatusOff"));
            } else if (listening.getPower() == PowerManager.UNKNOWN) {
                onOffStatus.setText(res.getString("StatusUnknown"));
            } else {
                onOffStatus.setText(res.getString("StatusUnknown"));
                log.error("Unexpected state value: +" + listening.getPower());
            }
        } catch (JmriException ex) {
            onOffStatus.setText(res.getString("StatusUnknown"));
        }
    }

    public void dispose() {
        if (listening != null) {
            listening.removePropertyChangeListener(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PowerPane.class.getName());

}
