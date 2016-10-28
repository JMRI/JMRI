package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PowerManagerButton extends JButton implements PropertyChangeListener {

    private final PropertyChangeListener listener;
    private Boolean fullText = true;
    private NamedIcon powerUnknownIcon;
    private NamedIcon powerOffIcon;
    private NamedIcon powerOnIcon;
    private final static Logger log = LoggerFactory.getLogger(PowerManagerButton.class);

    public PowerManagerButton() {
        this(true);
    }

    public PowerManagerButton(Boolean fullText) {
        this.fullText = fullText;
        this.listener = (PropertyChangeEvent evt) -> {
            this.setPowerIcons();
        };
        PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        } else {
            powerMgr.addPropertyChangeListener(this.listener);
        }
        super.addActionListener((ActionEvent e) -> {
            this.setPower();
        });
        this.initComponentsImpl();
    }

    public void dispose() {
        PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.setPowerIcons();
    }

    protected void setPowerIcons() {
        PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr == null) {
            return;
        }
        try {
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
        } catch (JmriException ex) {
            setIcon(getPowerUnknownIcon());
            setToolTipText(Bundle.getMessage("LayoutPowerUnknown"));
            if (getFullText()) {
                setText(Bundle.getMessage("PowerStateUnknown"));
            }
        }
    }

    private void setPower() {
        PowerManager powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr != null) {
            try {
                switch (powerMgr.getPower()) {
                    case PowerManager.ON:
                        powerMgr.setPower(PowerManager.OFF);
                        break;
                    case PowerManager.OFF:
                        powerMgr.setPower(PowerManager.ON);
                        break;
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
     * @return true if text should be shown
     */
    protected Boolean getFullText() {
        return fullText;
    }

    /**
     * @param fullText true if text should be shown
     */
    protected void setFullText(Boolean fullText) {
        this.fullText = fullText;
        this.setPowerIcons();
    }
}
