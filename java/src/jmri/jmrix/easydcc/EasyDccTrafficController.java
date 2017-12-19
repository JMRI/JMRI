package jmri.jmrix.easydcc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from EasyDcc messages. The "EasyDccInterface"
 * side sends/receives message objects.
 * <p>
 * The connection to a EasyDccPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 * <p>
 * Migrated for multiple connections, multi char connection prefix and Simulator.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccTrafficController extends AbstractMRTrafficController
        implements EasyDccInterface {

    /**
     * Ctor
     *
     * @param adaptermemo the associated SystemConnectionMemo
     */
    public EasyDccTrafficController(EasyDccSystemConnectionMemo adaptermemo) {
        super();
        mMemo = adaptermemo;
        log.debug("creating a new EasyDccTrafficController object");
    }

    // Methods to implement the EasyDccInterface

    @Override
    public synchronized void addEasyDccListener(EasyDccListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeEasyDccListener(EasyDccListener l) {
        this.removeListener(l);
    }

    /**
     * Forward an EasyDccMessage to all registered EasyDccInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((EasyDccListener) client).message((EasyDccMessage) m);
    }

    /**
     * Forward an EasyDccReply to all registered EasyDccInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((EasyDccListener) client).reply((EasyDccReply) m);
    }

    public void setSensorManager(jmri.SensorManager m) {
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
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendEasyDccMessage(EasyDccMessage m, EasyDccListener reply) {
        if (m == null) {
            log.debug("empty message");
            return;
        }
        log.debug("EasyDccTrafficController sendMessage() {}", m.toString());
        sendMessage(m, reply);
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        return EasyDccMessage.getProgMode();
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        return EasyDccMessage.getExitProgMode();
    }

    /**
     * Static function returning the EasyDccTrafficController instance to use.
     *
     * @return The registered EasyDccTrafficController instance for general use,
     *         if need be creating one.
     * @deprecated JMRI Since 4.9.5 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public EasyDccTrafficController instance() {
        log.warn("deprecated instance() call for EasyDccTrafficController");
        return null;
    }

    /**
     * @deprecated JMRI Since 4.9.5 instance() shouldn't be used
     */
    @Deprecated
    static volatile protected EasyDccTrafficController self = null;

    /**
     * Reference to the system connection memo.
     */
    EasyDccSystemConnectionMemo mMemo = null;

    /**
     * Get access to the system connection memo associated with this traffic
     * controller.
     *
     * @return associated systemConnectionMemo object
     */
    public EasyDccSystemConnectionMemo getSystemConnectionMemo() {
        return mMemo;
    }

    /**
     * Set the system connection memo associated with this traffic controller.
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(EasyDccSystemConnectionMemo m) {
        mMemo = m;
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "temporary until multi-system; only set at startup")
    @Override
    @Deprecated
    protected void setInstance() {
        // this is called from AbstractMRTrafficController, so suppress this
        // error.
    }

    @Override
    protected AbstractMRReply newReply() {
        return new EasyDccReply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        // note special case:  CV read / register read messages don't actually
        // end until a P is received!
        if ((msg.getElement(0) == 'C' && msg.getElement(1) == 'V') || (msg.getElement(0) == 'V')) {
            // require the P
            if ((msg.getNumDataElements() > 4) && msg.getElement(msg.getNumDataElements() - 2) != 'P') {
                return false;
            }
        }
        // detect that the reply buffer ends with "\n"
        int index = msg.getNumDataElements() - 1;
        if (msg.getElement(index) != 0x0d) {
            return false;
        } else {
            return true;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccTrafficController.class);

}
