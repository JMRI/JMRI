package jmri.jmrit.etcs.dmi.swing;

import java.beans.PropertyChangeListener;

import jmri.util.TimerUtil;

import org.apiguardian.api.API;

/**
 * Class to provide a Timer to ensure all flashing DMI Icons flash in unison.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class DmiFlashTimer {

    private final DmiPanel panel;

    private static final String PROP_CHANGE_SLOW_FLASH = "SlowFlash";
    private static final String PROP_CHANGE_FAST_FLASH = "FastFlash";

    private boolean fastFlashOn = false;
    private boolean slowFlashOn = false;
    private boolean disposed = false;

    private java.util.TimerTask flashTimer;

    protected DmiFlashTimer(DmiPanel mainPanel) {
        panel = mainPanel;
    }

    protected void addFlashListener( PropertyChangeListener pcl, boolean fast ) {
        log.debug("add pcl {}", pcl);
        panel.addPropertyChangeListener(
            ( fast ? PROP_CHANGE_FAST_FLASH : PROP_CHANGE_SLOW_FLASH), pcl);
        ensureRunning();
    }

    protected void removeFlashListener ( PropertyChangeListener pcl, boolean fast ) {
        
        log.debug("remove pcl {} num listeners {}", pcl, 
                    panel.getPropertyChangeListeners(PROP_CHANGE_FAST_FLASH).length +
            panel.getPropertyChangeListeners(PROP_CHANGE_SLOW_FLASH).length
                    
                    );
        
        panel.removePropertyChangeListener(( fast ? PROP_CHANGE_FAST_FLASH : PROP_CHANGE_SLOW_FLASH), pcl);
        
        
        log.debug("remove pcl {} num listeners {}", pcl, 
                    panel.getPropertyChangeListeners(PROP_CHANGE_FAST_FLASH).length +
            panel.getPropertyChangeListeners(PROP_CHANGE_SLOW_FLASH).length
                    
                    );
        
        if ( panel.getPropertyChangeListeners(PROP_CHANGE_FAST_FLASH).length +
            panel.getPropertyChangeListeners(PROP_CHANGE_SLOW_FLASH).length == 0 ) {
            dispose();
        }
        
    }

    private void ensureRunning(){
        log.debug("ensureRunning");
        disposed = false;
        if (flashTimer==null) {
            flashTimer = new java.util.TimerTask(){
                @Override
                public void run() {
                    if ( !disposed ) {
                        triggerFastFlash();
                        TimerUtil.scheduleOnGUIThread(flashTimer, 250);
                    }
                }
            };
            TimerUtil.scheduleOnGUIThread(flashTimer, 250);
        }
    }

    private void triggerFastFlash(){
        fastFlashOn = !fastFlashOn;
        log.debug("fast flash {}", fastFlashOn );
        panel.firePropertyChange(PROP_CHANGE_FAST_FLASH, !fastFlashOn, fastFlashOn);
        if (!fastFlashOn) {
            triggerSlowFlash();
        }
    }

    private void triggerSlowFlash(){
        slowFlashOn = !slowFlashOn;
        log.debug("slow flash {}", slowFlashOn );
        panel.firePropertyChange(PROP_CHANGE_SLOW_FLASH, !slowFlashOn, slowFlashOn);
    }

    protected void dispose(){
        log.debug("dispose");
        disposed = true;
        if ( flashTimer != null ) {
            flashTimer.cancel();
            flashTimer = null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiFlashTimer.class);

}
