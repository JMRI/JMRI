package jmri.jmrix.ecos;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from ECOS messages. The "EcosInterface" side
 * sends/receives message objects.
 * <p>
 * The connection to a EcosPortController is via a pair of *Streams, which then
 * carry sequences of characters for transmission. Note that this processing is
 * handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EcosTrafficController extends AbstractMRTrafficController implements EcosInterface {

    /**
     * Create a new EcosTrafficController instance.
     */
    public EcosTrafficController() {
        super();
        log.debug("creating a new EcosTrafficController object");
        // set as command station too
        this.setAllowUnexpectedReply(true);
        this.setSynchronizeRx(false);
    }

    public void setAdapterMemo(EcosSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    EcosSystemConnectionMemo adaptermemo;

    // The methods to implement the EcosInterface
    @Override
    public synchronized void addEcosListener(EcosListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeEcosListener(EcosListener l) {
        this.removeListener(l);
    }

    @Override
    protected int enterProgModeDelayTime() {
        // we should to wait at least a second after enabling the programming track
        return 1000;
    }

    /**
     * Forward an EcosMessage to all registered EcosInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((EcosListener) client).message((EcosMessage) m);
    }

    /**
     * Forward a EcosReply to all registered EcosInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((EcosListener) client).reply((EcosReply) r);
    }

    @Override
    protected AbstractMRMessage pollMessage() {
        return null;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a pre-formatted message to the actual interface.
     */
    @Override
    public void sendEcosMessage(EcosMessage m, EcosListener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        super.forwardToPort(m, reply);
    }

    protected boolean unsolicitedSensorMessageSeen = false;

    /**
     *  ECoS doesn't support this function.
     */
    @Override
    protected AbstractMRMessage enterProgMode() {
        return EcosMessage.getProgMode();
    }

    //Ecos doesn't support this function!
    @Override
    protected AbstractMRMessage enterNormalMode() {
        return EcosMessage.getExitProgMode();
    }

    @SuppressFBWarnings(value = "MS_PKGPROTECT")
    // SpotBugs wants this package protected, but we're removing it when multi-connection
    // migration is complete
    final static protected EcosTrafficController self = null;

    @Override
    protected AbstractMRReply newReply() {
        EcosReply reply = new EcosReply();
        return reply;
    }

    /**
     * @return for now, receive always OK
     */
    @Override
    protected boolean canReceive() {
        return true;
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        // detect that the reply buffer ends with "COMMAND: " (note ending
        // space)
        int num = msg.getNumDataElements();
        // ptr is offset of last element in EcosReply
        int ptr = num - 1;

        if ((num >= 2)
                && // check NL at end of buffer
                (msg.getElement(ptr) == 0x0A)
                && (msg.getElement(ptr - 1) == 0x0D)
                && (msg.getElement(ptr - 2) == '>')) {

            // this might be end of element, check for "<END "
            return ((EcosReply) msg).containsEnd();
        }

        // otherwise, it's not the end
        return false;
    }

    // Override the finalize method for this class
    public boolean sendWaitMessage(EcosMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) {
            log.debug("Send a message and wait for the response");
        }
        if (ostream == null) {
            return false;
        }
        m.setTimeout(500);
        m.setRetries(10);
        synchronized (this) {
            forwardToPort(m, reply);
            // wait for reply
            try {
                if (xmtRunnable != null) {
                    synchronized (xmtRunnable) {
                        xmtRunnable.wait(m.getTimeout());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error("transmit interrupted");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void terminate() {
        if (log.isDebugEnabled()) {
            log.debug("Cleanup Starts");
        }
        if (ostream == null) {
            return;    // no connection established
        }
        EcosPreferences p = adaptermemo.getPreferenceManager();
        if (p.getAdhocLocoFromEcos() == 0x01) {
            return; //Just a double check that we can delete locos
        }        //AbstractMRMessage modeMsg=enterNormalMode();
        AbstractMRMessage modeMsg;
        List<String> en;
        String ecosObject;

        modeMsg = new EcosMessage("release(10, view)");
        modeMsg.setTimeout(50);
        modeMsg.setRetries(10);
        synchronized (this) {
            forwardToPort(modeMsg, null);
            // wait for reply
            try {
                if (xmtRunnable != null) {
                    synchronized (xmtRunnable) {
                        xmtRunnable.wait(modeMsg.getTimeout());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.error("transmit interrupted");
            }
        }

        EcosTurnoutManager objEcosTurnManager = adaptermemo.getTurnoutManager();
        en = objEcosTurnManager.getEcosObjectList();
        for (int i = 0; i < en.size(); i++) {
            ecosObject = en.get(i);
            modeMsg = new EcosMessage("release(" + ecosObject + ", view)");
            modeMsg.setTimeout(50);
            modeMsg.setRetries(10);
            synchronized (this) {
                forwardToPort(modeMsg, null);
                // wait for reply
                try {
                    if (xmtRunnable != null) {
                        synchronized (xmtRunnable) {
                            xmtRunnable.wait(modeMsg.getTimeout());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                    log.error("transmit interrupted");
                }
            }
        }

        EcosLocoAddressManager objEcosLocoManager = adaptermemo.getLocoAddressManager();
        en = objEcosLocoManager.getEcosObjectList();
        for (int i = 0; i < en.size(); i++) {
            ecosObject = en.get(i);
            //we only delete locos if they were a temp entry.
            if (objEcosLocoManager.getByEcosObject(ecosObject).getEcosTempEntry()) {
                /*The ecos can be funny in not providing control on the first request, plus we have no way to determine if we have
                 therefore we send the request twice and hope we have control, failure not to have control isn't a problem as the loco
                 will simply be left on the ecos.*/
                for (int x = 0; x < 4; x++) {
                    switch (x) {
                        case 3:
                            modeMsg = new EcosMessage("delete(" + ecosObject + ")");
                            break;
                        case 2:
                            modeMsg = new EcosMessage("set(" + ecosObject + ", stop)");
                            break;
                        default:
                            modeMsg = new EcosMessage("request(" + ecosObject + ", control)");
                            break;
                    }
                    modeMsg.setTimeout(50);
                    modeMsg.setRetries(10);
                    synchronized (this) {
                        forwardToPort(modeMsg, null);
                        // wait for reply
                        try {
                            if (xmtRunnable != null) {
                                synchronized (xmtRunnable) {
                                    xmtRunnable.wait(modeMsg.getTimeout());
                                }
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            log.error("transmit interrupted");
                        }
                    }
                }
            }

        }
    }

    private final static Logger log = LoggerFactory.getLogger(EcosTrafficController.class);

}
