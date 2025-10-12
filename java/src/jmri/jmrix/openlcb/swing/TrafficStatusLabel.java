package jmri.jmrix.openlcb.swing;

import javax.swing.JLabel;
import javax.swing.Timer;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.ThreadingUtil;

/**
 *
 * @author Bob Jacobsen
 */
public class TrafficStatusLabel extends JLabel implements CanListener {
    
    private static final int INTERVAL = 200;
    CanSystemConnectionMemo memo;
    Timer timer;
    boolean active;
    
    public TrafficStatusLabel(CanSystemConnectionMemo memo) {
        super("Active");
        this.setEnabled(false);
        this.memo = memo;
        this.active = false;
        
        // set up traffic listener
        memo.getTrafficController().addCanConsoleListener(this);
        
        // start the process
        displayActive();
    }
    
    void traffic() {
        timer.stop();
        active = true;
        displayActive();
    }
    
    void displayActive() {
        if (active != isEnabled()) setEnabled(active); // reduce redisplays 
        timer = ThreadingUtil.runOnLayoutDelayed(() -> { active = false; displayActive(); }, INTERVAL);
    }

    @Override
    public synchronized void message(CanMessage l) {
        traffic();
    }

    @Override
    public synchronized void reply(CanReply l) { 
        traffic();
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrafficStatusLabel.class);
}
