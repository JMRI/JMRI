// SprogVersionQuery.java

package jmri.jmrix.sprog.update;

import org.apache.log4j.Logger;
import java.util.Vector;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;

/**
 * Get the firmware version of the attached SPROG
 * 
 * @author			Andrew Crosland   Copyright (C) 2012
 * @version			$Revision:  $
 */
public class SprogVersionQuery implements SprogListener {
    
    String replyString;
    static SprogTrafficController tc;
    static SprogVersion ver;
    
    // enum for version query states
    enum QueryState {IDLE,
                     CRSENT,     // awaiting reply to " "
                     QUERYSENT,  // awaiting reply to "?"
                     DONE}       // Version has been found
    static QueryState state = QueryState.IDLE;

    static final protected int LONG_TIMEOUT = 2000;
    static javax.swing.Timer timer = null;
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // Ignore FindBugs warnings as there can only be one instance at present
    public SprogVersionQuery() {
        if (log.isDebugEnabled()) { log.debug("setting instance: " + this); }
        self = this;
        tc = SprogTrafficController.instance();
        state = QueryState.IDLE;
    }

    protected static final Vector<SprogVersionListener> versionListeners = new Vector<SprogVersionListener>();

    protected synchronized void addSprogVersionListener(SprogVersionListener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!versionListeners.contains(l)) {
            versionListeners.addElement(l);
        }
    }

    public synchronized void removeSprogVersionListener(SprogVersionListener l) {
        if (versionListeners.contains(l)) {
            versionListeners.removeElement(l);
        }
    }

    @SuppressWarnings("unchecked")
    private static synchronized Vector<SprogVersionListener> getCopyOfListeners() {
        return (Vector<SprogVersionListener>) versionListeners.clone();
    }

    /**
     * static function returning the SprogVersionQuery instance to use.
     * 
     * @return The registered SprogVersionQuery instance for general use,
     *         if need be creating one.
     */
    static public SprogVersionQuery instance() {
        if (self == null) {
            if (log.isDebugEnabled()) { log.debug("creating a new SprogVersionQuery object"); }
            self = new SprogVersionQuery();
        }
        return self;
    }
    static volatile protected SprogVersionQuery self = null;

    static synchronized public void requestVersion(SprogVersionListener l) {
        SprogMessage m;
        if (log.isDebugEnabled()) { log.debug("SprogVersion requested by "+l.toString()); }
        if (state == QueryState.DONE) {
            // Reply immediately
            l.notifyVersion(ver);
            return;
        }
        // Remember this listener
        SprogVersionQuery.instance().addSprogVersionListener(l);
        if (state == QueryState.IDLE) {
            // Kick things off with a blank message
            m = new SprogMessage(1);
            m.setOpCode(' ');
            tc.sendSprogMessage(m, SprogVersionQuery.instance());
            state = QueryState.CRSENT;
            startLongTimer();
        }
    }

    /**
     * Notify all registered listeners of the SPROG version
     *
     * @param v
     */
    protected static synchronized void notifyVersion(SprogVersion v) {
        ver = v;
        for (SprogVersionListener listener : getCopyOfListeners()) {
            try {
                listener.notifyVersion(ver);
                versionListeners.remove(listener);
            } catch (Exception e) {
                log.warn("notify: During dispatch to " + listener + "\nException " + e);
            }
        }

    }

    /**
     * SprogListener notify Message not used
     * @param m
     */
    public void notifyMessage(SprogMessage m) {}   // Ignore

    /**
     * SprogListener notify Reply listens to replies and looks for version reply
     * @param m
     */
    synchronized public void notifyReply(SprogReply m) {
        SprogMessage msg;
        SprogVersion v;
        replyString = m.toString();
            switch(state) {
                case IDLE: {
                    if (log.isDebugEnabled()) { log.debug("reply in IDLE state"); }
                    break;
                }
                
                case CRSENT: {
                    stopTimer();
                    if (log.isDebugEnabled()) { log.debug("reply in CRSENT state"+replyString); }
                    if ((replyString.indexOf("P>")) >= 0) {
                        msg = new SprogMessage(1);
                        msg.setOpCode('?');
                        tc.sendSprogMessage(msg, this);
                        state = QueryState.QUERYSENT;
                        // Start a timeout in case ther is other traffic on the interface
                        startLongTimer();
                    }
                    break;
                }

                case QUERYSENT: {
                    if (log.isDebugEnabled()) { log.debug("reply in QUERYSENT state"+replyString); }
                    // see if reply is from a SPROG
                    String[] splits = replyString.split("\n");
                    splits = splits[1].split(" ");
                    int index = 1;
                    if (log.isDebugEnabled()) { log.debug("Elements in version reply: " + splits.length); }
                    if (log.isDebugEnabled()) { log.debug("First element: <"+splits[0]+">"); }
                    if (splits[0].contains("SPROG")) {
                        if (log.isDebugEnabled()) { log.debug("Found a SPROG "+splits[index]); }
                        if (splits[index].equals("3")) {
                            index += 2;
                            v = new SprogVersion(new SprogType(SprogType.SPROG3), splits[index]);
                        } else if (splits[index].equals("IV")) {
                            index += 2;
                            v = new SprogVersion(new SprogType(SprogType.SPROGIV), splits[index]);
                        } else if (splits[index].equals("5")) {
                            index += 2;
                            v = new SprogVersion(new SprogType(SprogType.SPROG5), splits[index]);
                        } else if (splits[index].equals("Nano")) {
                            index += 2;
                            v = new SprogVersion(new SprogType(SprogType.NANO), splits[index]);
                        } else if (splits[index].equals("Sniffer")) {
                            index += 2;
                            v = new SprogVersion(new SprogType(SprogType.SNIFFER), splits[index]);
                        } else if (splits[index].equals("II")) {
                            index++;
                            if (splits[index].equals("USB")) {
                                index += 2;
                                v = new SprogVersion(new SprogType(SprogType.SPROGIIUSB), splits[index]);
                            } else {
                                index++;
                                v = new SprogVersion(new SprogType(SprogType.SPROGII), splits[index]);
                            }
                        } else if (splits[index].equals("Ver")) {
                            index += 1;
                            v = new SprogVersion(new SprogType(SprogType.SPROGV4), splits[index]);
                        } else {
                            if (log.isDebugEnabled()) { log.debug("Unrecognised SPROG"+splits[index]); }
                            v = new SprogVersion(new SprogType(SprogType.NOT_RECOGNISED));
                        }
                    } else {
                        // Wait for timeout
                        break;
                    }

                    if ((v.sprogType.sprogType == SprogType.SPROGII) && (v.getMajorVersion() >= 3)) {
                        // Correct for SPROG IIv3 which is different hardware
                        v = new SprogVersion(new SprogType(SprogType.SPROGIIv3), v.sprogVersion);
                    }
                    if (log.isDebugEnabled()) { log.debug("Found: " + v.toString()); }
                    stopTimer();
                    notifyVersion(v);
                    state = QueryState.DONE;
                    break;
                }

                case DONE: break;
                
                default: {
                    log.error("Unknown case");
                }
            }
    }

    /**
     * Internal routine to handle a timeout
     */
    synchronized static protected void timeout() {
        SprogVersion v;
        switch(state) {
            case CRSENT: 
                if (log.isDebugEnabled()) { log.debug("Timeout no SPROG prompt"); }
                state = QueryState.IDLE;
                v = new SprogVersion(new SprogType(SprogType.TIMEOUT));
                notifyVersion(v);
                break;
            case QUERYSENT:
                if (log.isDebugEnabled()) { log.debug("Timeout no SPROG found"); }
                state = QueryState.IDLE;
                v = new SprogVersion(new SprogType(SprogType.NOT_A_SPROG));
                notifyVersion(v);
                break;
            case DONE:
            case IDLE:
                log.error("Timeout in unexpected state: "+state);
                break;
        }

        // *** check state and set ver to timeout, no prompt or not a sprog

    }

    /**
     * Internal routine to restart timer with a long delay
     */
    static protected void startLongTimer() {
        restartTimer(LONG_TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well
     */
    protected void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Internal routine to handle timer starts & restarts
     */
    static protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    timeout();
                }
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }
    
    static Logger log = Logger.getLogger(SprogVersionQuery.class.getName());
}
