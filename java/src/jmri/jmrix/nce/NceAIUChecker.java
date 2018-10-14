package jmri.jmrix.nce;

import javax.swing.JOptionPane;
import jmri.jmrix.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* 
 * Checks to see if AIU broadcasts are enabled and warns user to 
 * disable AIU broadcast for proper operation.  NCE command station
 * battery-backed memory location 0xDC15 contains the control for
 * AIU broadcasts, 0 = disabled, 1 = enabled.
 *  
 * @author Daniel Boudreau (C) 2007
 * 
 * 
 */
public class NceAIUChecker implements NceListener {

    private static final int MEM_AIU = 0xDC15;  // NCE CS AIU memory address 
    private static final int REPLY_LEN = 1;  // number of bytes read
    private boolean EXPECT_REPLY = false;   // flag 

    private NceTrafficController tc = null;

    public NceAIUChecker(NceTrafficController t) {
        super();
        this.tc = t;
    }

    public NceMessage nceAiuPoll() {

        if (tc.getCommandOptions() <= NceTrafficController.OPTION_1999) {
            return null;
        }

        // If USB, just return
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            return null;
        }

        // read one byte from NCE memory to determine if AIU broadcasts are enabled
        byte[] bl = NceBinaryCommand.accMemoryRead1(MEM_AIU);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_LEN);
        EXPECT_REPLY = true;
        return m;

    }

    @Override
    public void message(NceMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("unexpected message");
        }
    }

    @Override
    public void reply(NceReply r) {
        if (!EXPECT_REPLY && log.isDebugEnabled()) {
            log.debug("Unexpected reply in AIU broadcast checker");
            return;
        }
        EXPECT_REPLY = false;
        if (r.getNumDataElements() == REPLY_LEN) {

            // if broadcasts are enabled, put up warning
            byte AIUstatus = (byte) r.getElement(0);
            if (AIUstatus > 1) {
                log.warn("AIU check broadcast return value is out of range");
            }
            if (AIUstatus == 1) {
                log.warn("AIU broadcasts are enabled");
                ConnectionStatus.instance().setConnectionState(
                        tc.getUserName(),
                        tc.getPortName(),
                        ConnectionStatus.CONNECTION_DOWN);
                JOptionPane.showMessageDialog(null,
                        "JMRI has detected that AIU broadcasts are enabled. \n"
                        + "You must disable AIU broadcasts for proper operation of this program. \n"
                        + "For more information, see Setup Command Station in your NCE System Reference Manual.",
                        "Warning", JOptionPane.INFORMATION_MESSAGE);

            }

        } else {
            log.warn("wrong number of read bytes for revision check");
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(NceAIUChecker.class);

}

