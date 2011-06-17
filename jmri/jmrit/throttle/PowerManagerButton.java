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

public abstract class PowerManagerButton extends JButton implements PropertyChangeListener {
	static final ResourceBundle rb = ThrottleBundle.bundle();
	 
    private PowerPane powerControl  = new PowerPane();
    private PowerManager powerMgr = null;
    
    protected NamedIcon powerXIcon;
    protected NamedIcon powerOffIcon;
    protected NamedIcon powerOnIcon;

    public PowerManagerButton() {
    	powerMgr = InstanceManager.powerManagerInstance();
        if (powerMgr == null)
            log.info("No power manager instance found, panel not active");
        else 
        	powerMgr.addPropertyChangeListener(this);
        loadIcons();
        addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (powerMgr.getPower() == PowerManager.ON)
						powerControl.offButtonPushed();
					else if (powerMgr.getPower() == PowerManager.OFF)
						powerControl.onButtonPushed();
					else if (powerMgr.getPower() == PowerManager.UNKNOWN)
						powerControl.offButtonPushed();
				} catch (JmriException ex) {
					setIcon(powerXIcon);
				}
			}
		});
        setPowerIcons();
    }

    public void dispose()
    {
        if (powerMgr!=null) powerMgr.removePropertyChangeListener(this);
    }
    
    abstract void loadIcons();
    
	public void propertyChange(PropertyChangeEvent evt) {
		setPowerIcons();
	}
	
    private void setPowerIcons() {
    	if (powerMgr==null) return;
        try {
            if (powerMgr.getPower()==PowerManager.ON) {
                setIcon(powerOnIcon);
                setToolTipText(rb.getString("LayoutPowerOn"));
            }
            else if (powerMgr.getPower()==PowerManager.OFF) {
                setIcon(powerOffIcon);
                setToolTipText(rb.getString("LayoutPowerOff"));
            }
            else if (powerMgr.getPower()==PowerManager.UNKNOWN) {
                setIcon(powerXIcon);
                setToolTipText(rb.getString("LayoutPowerUnknown"));
            }
            else {
                setIcon(powerXIcon);
                setToolTipText(rb.getString("LayoutPowerUnknown"));
                log.error("Unexpected state value: +"+powerMgr.getPower());
            }
        } catch (JmriException ex) {
            setIcon(powerXIcon);
            setToolTipText(rb.getString("LayoutPowerUnknown"));
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PowerManagerButton.class.getName());
}
