// SRCPTrafficController.java

package jmri.jmrix.srcp;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

import jmri.jmrix.srcp.parser.SRCPClientParser;
import jmri.jmrix.srcp.parser.ParseException;
import jmri.jmrix.srcp.parser.SimpleNode;
import jmri.jmrix.srcp.parser.SRCPClientVisitor;


/**
 * Converts Stream-based I/O to/from SRCP messages.  The "SRCPInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SRCPPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 * 
 * @author Bob Jacobsen  Copyright (C) 2001
 * @version $Revision$
 */
public class SRCPTrafficController extends AbstractMRTrafficController
	implements SRCPInterface {

    protected SRCPSystemConnectionMemo _memo = null;

    public SRCPTrafficController() {
        super();
    }

    // The methods to implement the SRCPInterface

    public synchronized void addSRCPListener(SRCPListener l) {
        this.addListener(l);
    }

    public synchronized void removeSRCPListener(SRCPListener l) {
        this.removeListener(l);
    }

    /*
     * Set the system connection memo associated with the traffic
     * controller
     */
    void setSystemConnectionMemo(SRCPSystemConnectionMemo memo)
    {
        _memo=memo;
    }
    
    /*
     * Get the system connection memo associated with the traffic
     * controller
     */
    SRCPSystemConnectionMemo  getSystemConnectionMemo(){ return _memo;}


    static int HANDSHAKEMODE=0;
    static int RUNMODE=1;
    private int mode = HANDSHAKEMODE;

    /*
     * We are going to override the receiveLoop() function so that we can
     * handle messages received by the system using the SRCP parser.
     */
    @Override
    public void receiveLoop(){
	if(log.isDebugEnabled()) log.debug("SRCP receiveLoop starts");
	SRCPClientParser parser = new SRCPClientParser(istream);
        while(true){
              try {
                  SimpleNode e;
                  if(mode==HANDSHAKEMODE)
			e=parser.handshakeresponse();
		  else
			e=parser.inforesponse();
		  
		  SRCPReply msg=new SRCPReply(e);

                  // forward the message to the registered recipients,
                  // which includes the communications monitor
                  // return a notification via the Swing event queue to ensure proper thread
                  Runnable r = newRcvNotifier(msg, mLastSender, this);
                  try {
                     javax.swing.SwingUtilities.invokeAndWait(r);
                  } catch (Exception ex) {
                     log.error("Unexpected exception in invokeAndWait:" +ex);
                     ex.printStackTrace();
                  }
                  if (log.isDebugEnabled()) log.debug("dispatch thread invoked");
		  
                  if (e.toString().equals("GO")) mode=RUNMODE;

                  SRCPClientVisitor v = new SRCPClientVisitor();
                  e.jjtAccept(v,_memo);

if (!msg.isUnsolicited()) {
            // effect on transmit:
            switch (mCurrentState) {
            case WAITMSGREPLYSTATE: {
                // check to see if the response was an error message we want
                // to automatically handle by re-queueing the last sent
                // message, otherwise go on to the next message
                if(msg.isRetransmittableErrorMsg()){
                  if(log.isDebugEnabled())
                        log.debug("Automatic Recovery from Error Message: +msg.toString()");
                   synchronized (xmtRunnable) {
                       mCurrentState = AUTORETRYSTATE;
                       replyInDispatch = false;
                       xmtRunnable.notify();
                   }
                } else {
                   // update state, and notify to continue
                   synchronized (xmtRunnable) {
                       mCurrentState = NOTIFIEDSTATE;
                       replyInDispatch = false;
                       xmtRunnable.notify();
                   }
                }
                break;
            }

            case WAITREPLYINPROGMODESTATE: {
                // entering programming mode
                mCurrentMode = PROGRAMINGMODE;
                replyInDispatch = false;

                // check to see if we need to delay to allow decoders to become
                // responsive
                int warmUpDelay = enterProgModeDelayTime();
                if (warmUpDelay != 0) {
                    try {
                        synchronized (xmtRunnable) {
                            xmtRunnable.wait(warmUpDelay);
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
                }
                // update state, and notify to continue
                synchronized (xmtRunnable) {
                    mCurrentState = OKSENDMSGSTATE;
                    xmtRunnable.notify();
                }
                break;
            }
            case WAITREPLYINNORMMODESTATE: {
                // entering normal mode
                mCurrentMode = NORMALMODE;
                replyInDispatch = false;
                // update state, and notify to continue
                synchronized (xmtRunnable) {
                    mCurrentState = OKSENDMSGSTATE;
                    xmtRunnable.notify();
                }
                break;
            }
            default: {
                replyInDispatch = false;
                if (allowUnexpectedReply == true) {
                    if (log.isDebugEnabled())
                        log.debug("Allowed unexpected reply received in state: "
                                  + mCurrentState   + " was " + msg.toString());
                   synchronized (xmtRunnable) {
                       // The transmit thread sometimes gets stuck
                       // when unexpected replies are received.  Notify
                       // it to clear the block without a timeout.
                       // (do not change the current state)
                       //if(mCurrentState!=IDLESTATE)
                          xmtRunnable.notify();
                   }
                } else {
                    log.error("reply complete in unexpected state: "
                              + mCurrentState + " was " + msg.toString());
                }
            }
            }
            // Unsolicited message
        } else {
            if(log.isDebugEnabled()) log.debug("Unsolicited Message Received "
                                               + msg.toString());

            replyInDispatch = false;
        }
 



              } catch (ParseException pe){
              /*     if(log.isDebugEnabled())
                   {
		      log.debug("Parse Exception");
                      pe.printStackTrace();
                   }
                   outstream.writeBytes("425 ERROR not supported\n");
              } catch (java.io.IOException e) {*/
                rcvException = true;
                reportReceiveLoopException(pe);
                break;
              } catch (Exception e1) {
                log.error("Exception in receive loop: "+e1);
                e1.printStackTrace();
            }
        }
    }


    /**
     * Forward a SRCPMessage to all registered SRCPInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SRCPListener)client).message((SRCPMessage)m);
    }

    /**
     * Forward a SRCPReply to all registered SRCPInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SRCPListener)client).reply((SRCPReply)m);
    }

    public void setSensorManager(jmri.SensorManager m) { }
    protected AbstractMRMessage pollMessage() {
		return null;
    }
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
        sendMessage(m, reply);
    }

    protected AbstractMRMessage enterProgMode() {
        return SRCPMessage.getProgMode();
    }
    protected AbstractMRMessage enterNormalMode() {
        return SRCPMessage.getExitProgMode();
    }

    /**
     * static function returning the SRCPTrafficController instance to use.
     * 
     * @return The registered SRCPTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SRCPTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new SRCP TrafficController object");
            self = new SRCPTrafficController();
        }
        return self;
    }

    static volatile protected SRCPTrafficController self = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                        justification="temporary until mult-system; only set at startup")
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { return new SRCPReply(); }

    protected boolean endOfMessage(AbstractMRReply msg) {
        int index = msg.getNumDataElements()-1;
        if (msg.getElement(index) == 0x0D) return true;
        if (msg.getElement(index) == 0x0A) return true;
        else return false;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPTrafficController.class.getName());
}


/* @(#)SRCPTrafficController.java */

