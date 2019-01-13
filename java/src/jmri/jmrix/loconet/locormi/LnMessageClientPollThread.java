package jmri.jmrix.loconet.locormi;

import jmri.jmrix.loconet.LocoNetMessage;

import javax.annotation.Nonnull;

/**
 * @author Alex Shepherd Copyright (c) 2002
 */
class LnMessageClientPollThread extends Thread {

    LnMessageClient parent = null;

    LnMessageClientPollThread(@Nonnull LnMessageClient lnParent) {
        parent = lnParent;
        this.setDaemon(true);
        this.setName("LnMessageClientPollThread " + lnParent);
        this.start();
    }

    @Override
    public void run() {
        try {
            Object[] lnMessages = null;
            while (!Thread.interrupted()) {
                if (parent.lnMessageBuffer == null) {
                    // no work to do
                    return;
                }

                lnMessages = parent.lnMessageBuffer.getMessages(parent.pollTimeout);

                if (lnMessages != null) {

                    log.debug("Received Message Array Size: {}", lnMessages.length);
                    for (int lnMessageIndex = 0; lnMessageIndex < lnMessages.length; lnMessageIndex++) {
                        LocoNetMessage message = (LocoNetMessage) lnMessages[lnMessageIndex];
                        parent.message(message);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Exception: ", ex);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnMessageClientPollThread.class);

}
