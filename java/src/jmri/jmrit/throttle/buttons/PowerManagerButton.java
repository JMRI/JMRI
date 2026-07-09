package jmri.jmrit.throttle.buttons;

import java.beans.PropertyChangeListener;

import javax.annotation.CheckForNull;
import javax.swing.JButton;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;

/**
 * 
 * A button handling layout power
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
 */

public abstract class PowerManagerButton extends JButton {

    private final transient PropertyChangeListener listener;
    private final PowerManager powerMgr;
    private boolean fullText = true;
    private NamedIcon powerUnknownIcon;
    private NamedIcon powerOffIcon;
    private NamedIcon powerOnIcon;
    private NamedIcon powerIdleIcon;

    public PowerManagerButton() {
        this(true);
    }

    public PowerManagerButton(Boolean fullText) {
        this(fullText, InstanceManager.getNullableDefault(PowerManager.class));
    }

    PowerManagerButton(boolean fullText, @CheckForNull PowerManager powerMgr) {
        this.fullText = fullText;
        this.powerMgr = powerMgr;
        this.listener = evt -> this.setPowerIcons();

        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        } else {
            powerMgr.addPropertyChangeListener(this.listener);
        }
        super.addActionListener( e -> this.setPower());

        this.initComponentsImpl();
    }

    public void dispose() {
        if (powerMgr != null) {
            powerMgr.removePropertyChangeListener(this.listener);
        }
    }

    private void initComponentsImpl() {
        this.initComponents();
        this.loadIcons();
        this.setPowerIcons();
    }

    /**
     * Initialize any components within the button or aspects of the button
     * itself. The default implementation does nothing.
     */
    protected void initComponents() {
        // empty implementation to be overloaded if needed
    }

    /**
     * Must be overridden to provide icons for power state indicators.
     */
    abstract void loadIcons();

    protected void setPowerIcons() {
        if (powerMgr == null) {
            return;
        }
        switch (powerMgr.getPower()) {
            case PowerManager.ON:
                setIcon(getPowerOnIcon());
                setToolTipText(Bundle.getMessage("LayoutPowerOn"));
                if (getFullText()) {
                    setText(Bundle.getMessage("PowerStateOn"));
                }
                break;
            case PowerManager.OFF:
                setIcon(getPowerOffIcon());
                setToolTipText(Bundle.getMessage("LayoutPowerOff"));
                if (getFullText()) {
                    setText(Bundle.getMessage("PowerStateOff"));
                }
                break;
            case PowerManager.IDLE:
                setIcon(getPowerIdleIcon());
                setToolTipText(Bundle.getMessage("LayoutPowerIdle"));
                if (getFullText()) {
                    setText(Bundle.getMessage("PowerStateIdle"));
                }
                break;
            case PowerManager.UNKNOWN:
                setIcon(getPowerUnknownIcon());
                setToolTipText(Bundle.getMessage("LayoutPowerUnknown"));
                if (getFullText()) {
                    setText(Bundle.getMessage("PowerStateUnknown"));
                }
                break;
            default:
                setIcon(getPowerUnknownIcon());
                setToolTipText(Bundle.getMessage("LayoutPowerUnknown"));
                log.error("Unexpected state value: {}", powerMgr.getPower());
                if (getFullText()) {
                    setText(Bundle.getMessage("PowerStateUnknown"));
                }
                break;
        }
    }

    private void setPower() {
        if (powerMgr != null) {
            try {
                switch (powerMgr.getPower()) {
                    case PowerManager.OFF:
                        powerMgr.setPower(PowerManager.ON);
                        break;
                    case PowerManager.ON:
                    case PowerManager.IDLE:
                    case PowerManager.UNKNOWN:
                    default:
                        powerMgr.setPower(PowerManager.OFF);
                        break;
                }
            } catch (JmriException ex) {
                setIcon(getPowerUnknownIcon());
                setToolTipText(Bundle.getMessage("LayoutPowerUnknown"));
                if (getFullText()) {
                    setText(Bundle.getMessage("PowerStateUnknown"));
                }
                log.error("Could not set Power to {}, {}",powerMgr,ex.getMessage(),ex);
            }
        }
    }

    /**
     * @return the icon that represents an unknown power state
     */
    public NamedIcon getPowerUnknownIcon() {
        return powerUnknownIcon;
    }

    /**
     * @param powerUnknownIcon the icon that represents an unknown power state
     */
    public void setPowerUnknownIcon(NamedIcon powerUnknownIcon) {
        this.powerUnknownIcon = powerUnknownIcon;
        this.setPowerIcons();
    }

    /**
     * @return the icon that represents a power off state
     */
    public NamedIcon getPowerOffIcon() {
        return powerOffIcon;
    }

    /**
     * @param powerOffIcon the icon that represents a power off state
     */
    public void setPowerOffIcon(NamedIcon powerOffIcon) {
        this.powerOffIcon = powerOffIcon;
        this.setPowerIcons();
    }

    /**
     * @return the icon that represents a power on state
     */
    public NamedIcon getPowerOnIcon() {
        return powerOnIcon;
    }

    /**
     * @param powerOnIcon the icon that represents a power on state
     */
    public void setPowerOnIcon(NamedIcon powerOnIcon) {
        this.powerOnIcon = powerOnIcon;
        this.setPowerIcons();
    }

    /**
     * @return the icon that represents a power on state
     */
    public NamedIcon getPowerIdleIcon() {
        return powerIdleIcon;
    }

    /**
     * @param icon the icon that represents a power Idle state.
     */
    public void setPowerIdleIcon(NamedIcon icon) {
        this.powerIdleIcon = icon;
        this.setPowerIcons();
    }

    /**
     * @return true if text should be shown
     */
    private boolean getFullText() {
        return fullText;
    }

    /**
     * @param fullText true if text should be shown
     */
    protected void setFullText(boolean fullText) {
        this.fullText = fullText;
        this.setPowerIcons();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PowerManagerButton.class);

}
