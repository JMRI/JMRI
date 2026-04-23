package jmri.jmrix.openlcb.swing;

import java.util.TimerTask;
import javax.swing.JLabel;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.TimerUtil;

/**
 * A JLabel that displays whether a LCC/OpenLCB connection
 * is currently active or not by making the word "Active"
 * dark (enabled) or light (disabled).
 *
 * @author Bob Jacobsen
 */
public class TrafficStatusLabel extends JLabel implements CanListener {
    
    private static final int INTERVAL = 200;
    final CanSystemConnectionMemo memo;
    
    TimerTask timertask;

    boolean active;
    
    public TrafficStatusLabel(CanSystemConnectionMemo memo) {
        super("Active");
        this.setEnabled(false);
        this.memo = memo;
        
        // set up traffic listener
        memo.getTrafficController().addCanConsoleListener(this);
        
        // start the process
        this.active = false;
        setEnabled(false);
    }
    
    TimerTask getNewTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                active = false;
                displayActive(); 
            }
        };
    }
    
    void traffic() {
        if (timertask != null) {
            timertask.cancel();
            timertask = null;
        }
        active = true;
        displayActive();
        if (active) {
            timertask = TimerUtil.scheduleOnGUIThread(getNewTimerTask(), INTERVAL); // return value kept for cancel
        }
        
    }

    void displayActive() {
        if (active != isEnabled()) {
            setEnabled(active); // `if` reduces redisplays 
        }
    }
    
    @Override
    public synchronized void message(CanMessage l) {
        traffic();
    }

    @Override
    public synchronized void reply(CanReply l) { 
        traffic();
    }
    
    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrafficStatusLabel.class);
}
