package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.powerpanel.PowerPane;

public abstract class PowerManagerButton extends JButton implements PropertyChangeListener {
	
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
                setToolTipText("Layout Power On.  Click light to turn off, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.OFF) {
                setIcon(powerOffIcon);
                setToolTipText("Layout Power Off.  Click light to turn on, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.UNKNOWN) {
                setIcon(powerXIcon);
                setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
            }
            else {
                setIcon(powerXIcon);
                setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
                log.error("Unexpected state value: +"+powerMgr.getPower());
            }
        } catch (JmriException ex) {
            setIcon(powerXIcon);
            setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
        }
    }

	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PowerManagerButton.class.getName());
}
