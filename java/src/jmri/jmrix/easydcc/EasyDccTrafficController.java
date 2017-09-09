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
 * <P>
 * The connection to a EasyDccPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccTrafficController extends AbstractMRTrafficController
        implements EasyDccInterface {

    public EasyDccTrafficController() {
        super();
    }

    // The methods to implement the EasyDccInterface
    @Override
    public synchronized void addEasyDccListener(EasyDccListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeEasyDccListener(EasyDccListener l) {
        this.removeListener(l);
    }

    /**
     * Forward a EasyDccMessage to all registered EasyDccInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((EasyDccListener) client).message((EasyDccMessage) m);
    }

    /**
     * Forward a EasyDccReply to all registered EasyDccInterface listeners.
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
     * static function returning the EasyDccTrafficController instance to use.
     *
     * @return The registered EasyDccTrafficController instance for general use,
     *         if need be creating one.
     */
    static public EasyDccTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) {
                log.debug("creating a new EasyDccTrafficController object");
            }
            self = new EasyDccTrafficController();
        }
        return self;
    }

    static volatile protected EasyDccTrafficController self = null;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "temporary until mult-system; only set at startup")
    @Override
    @Deprecated
    protected void setInstance() {
        self = this;
    }

    @Override
    protected AbstractMRReply newReply() {
        return new EasyDccReply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        // note special case:  CV read / register read messages dont actually
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
