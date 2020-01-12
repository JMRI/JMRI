package jmri.jmrix.roco.z21;

/**
 * Z21 specific class to send heartbeat messages to
 * the Z21.  Heartbeat messages are only required if
 * no other messages are sent for a specific period
 * of time, so any outgoing message should restart
 * the timer.
 *
 * @author	Paul Bender Copyright (C) 2019 
 */
public class Z21HeartBeat implements Z21Listener {

    private javax.swing.Timer keepAliveTimer; // Timer used to periodically
    // send a message to both
    // ports to keep the ports
    // open
    private static final int keepAliveTimeoutValue = 30000; // Interval
    // to send a message
    // Must be < 60s.
    private Z21TrafficController tc;

    public Z21HeartBeat(Z21SystemConnectionMemo memo) {
        tc = memo.getTrafficController();
        tc.addz21Listener(this);
        keepAliveTimer();
    }

    /*
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if (keepAliveTimer == null) {
            keepAliveTimer = new javax.swing.Timer(keepAliveTimeoutValue, new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // If the timer times out, send a request for status
                    tc.sendz21Message(
                       jmri.jmrix.roco.z21.Z21Message.getSerialNumberRequestMessage(),
                       null);
                }
            });
        }
        keepAliveTimer.stop();
        keepAliveTimer.setInitialDelay(keepAliveTimeoutValue);
        keepAliveTimer.setRepeats(true);
        keepAliveTimer.start();
    }

    public void dispose(){
       if (keepAliveTimer != null) {
           keepAliveTimer.stop();
       }
       keepAliveTimer = null;
    }

    // Z21Listener methods.

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(Z21Reply msg){
        // this class doesn't care about incoming messages.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(Z21Message msg){
       if(keepAliveTimer!=null) { 
          // if we see any outgoing message, restart the timer
          keepAliveTimer.restart();
       }
    }

}
