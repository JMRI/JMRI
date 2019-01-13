package jmri.jmrix.lenz;

/**
 * XNet specific class to send heartbeat messages to
 * the XNet.  Heartbeat messages are only required if
 * no other messages are sent for a specific period
 * of time, so any outgoing message should restart
 * the timer.
 *
 * @author	Paul Bender Copyright (C) 2019 
 */
public class XNetHeartBeat implements XNetListener {

    private javax.swing.Timer keepAliveTimer; // Timer used to periodically
    // send a message to both
    // ports to keep the ports
    // open
    private static final int keepAliveTimeoutValue = 30000; // Interval
    // to send a message
    // Must be < 60s.
    private XNetTrafficController tc;
    private XNetSystemConnectionMemo memo;

    public XNetHeartBeat(XNetSystemConnectionMemo memo) {
        this.memo = memo;
        tc = memo.getXNetTrafficController();
        tc.addXNetListener(~0,this);
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
                    // If the timer times out, and we are not currently 
                    // programming, send a request for status
                    jmri.jmrix.lenz.XNetProgrammer p = null;
                    if(memo.provides(jmri.GlobalProgrammerManager.class)){
                        p = (jmri.jmrix.lenz.XNetProgrammer) (memo.getProgrammerManager().getGlobalProgrammer());
                    }
                    if (p == null || !(p.programmerBusy())) {
                       tc.sendXNetMessage(
                        jmri.jmrix.lenz.XNetMessage.getCSStatusRequestMessage(),
                        null);
                    }
                }
            });
        }
        keepAliveTimer.stop();
        keepAliveTimer.setInitialDelay(keepAliveTimeoutValue);
        keepAliveTimer.setRepeats(true);
        keepAliveTimer.start();
    }

    public void dispose(){
       keepAliveTimer.stop();
       keepAliveTimer = null;
    }

    // XNetListener methods.

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(XNetReply msg){
        // this class doesn't care about incoming messages.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(XNetMessage msg){
       // if we see any outgoing message, restart the timer
       keepAliveTimer.restart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(XNetMessage msg){
        // this class doesn't care about timeouts.
    }

}
