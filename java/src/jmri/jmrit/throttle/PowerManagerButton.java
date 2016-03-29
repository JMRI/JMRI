package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.powerpanel.PowerPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PowerManagerButton extends JButton implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -6973591266016989688L;
    static final ResourceBundle pprb = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");
    private PowerPane powerControl = new PowerPane();
    private PowerManager powerMgr = null;
    private Boolean fullText = false;
    protected NamedIcon powerXIcon;
    protected NamedIcon powerOffIcon;
    protected NamedIcon powerOnIcon;

    public PowerManagerButton() {
        this(true);
    }

    public PowerManagerButton(Boolean fullText) {
        this.fullText = fullText;
        powerMgr = InstanceManager.powerManagerInstance();
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        } else {
            powerMgr.addPropertyChangeListener(this);
        }
        loadIcons();
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (powerMgr.getPower() == PowerManager.ON) {
                        powerControl.offButtonPushed();
                    } else if (powerMgr.getPower() == PowerManager.OFF) {
                        powerControl.onButtonPushed();
                    } else if (powerMgr.getPower() == PowerManager.UNKNOWN) {
                        powerControl.offButtonPushed();
                    }
                } catch (JmriException ex) {
                    setIcon(powerXIcon);
                }
            }
        });
        setPowerIcons();
    }

    public void dispose() {
        if (powerMgr != null) {
            powerMgr.removePropertyChangeListener(this);
        }
    }

    abstract void loadIcons();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setPowerIcons();
    }

    protected void setPowerIcons() {
        if (powerMgr == null) {
            return;
        }
        try {
            if (powerMgr.getPower() == PowerManager.ON) {
                setIcon(powerOnIcon);
                setToolTipText(Bundle.getMessage("LayoutPowerOn"));
                if (fullText) {
                    setText(pprb.getString("StatusOn"));
                }
            } else if (powerMgr.getPower() == PowerManager.OFF) {
                setIcon(powerOffIcon);
                setToolTipText(Bundle.getMessage("LayoutPowerOff"));
                if (fullText) {
                    setText(pprb.getString("StatusOff"));
                }
            } else if (powerMgr.getPower() == PowerManager.UNKNOWN) {
                setIcon(powerXIcon);
                setToolTipText(Bundle.getMessage("LayoutPowerUnknown"));
                if (fullText) {
                    setText(pprb.getString("StatusUnknown"));
                }
            } else {
                setIcon(powerXIcon);
                setToolTipText(Bundle.getMessage("LayoutPowerUnknown"));
                log.error("Unexpected state value: +" + powerMgr.getPower());
                if (fullText) {
                    setText(pprb.getString("StatusUnknown"));
                }
            }
        } catch (JmriException ex) {
            setIcon(powerXIcon);
            setToolTipText(Bundle.getMessage("LayoutPowerUnknown"));
            if (fullText) {
                setText(pprb.getString("StatusUnknown"));
            }
        }
    }
    private final static Logger log = LoggerFactory.getLogger(PowerManagerButton.class.getName());
}
