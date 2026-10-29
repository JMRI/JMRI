package jmri.jmrix.dccpp.swing.messages;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;

import jmri.jmrix.dccpp.*;
import jmri.util.ThreadingUtil;

/** Logs DCC-EX MESSAGE() broadcasts and opens the Messages window when one arrives. */
public class MessagesWatcher implements DCCppListener {

    private final DCCppSystemConnectionMemo _memo;

    public MessagesWatcher(DCCppSystemConnectionMemo memo) {
        _memo = memo;
        memo.getDCCppTrafficController().addDCCppListener(DCCppInterface.CS_INFO, this);
    }

    /** {@inheritDoc} */
    @Override
    public void message(DCCppReply msg) {
        if (!msg.isMessageReply()) {
            return;
        }
        String text = msg.getMessageString();
        log.info("DCC-EX message: {}", text);
        if (!GraphicsEnvironment.isHeadless() && MessagesFrame.isAutoOpen(_memo)) {
            ThreadingUtil.runOnGUI(() -> {
                MessagesFrame frame = MessagesFrame.findFrame(_memo);
                if (frame == null) {
                    MessagesFrame.showFrame(_memo, text);
                } else if ((frame.getExtendedState() & Frame.ICONIFIED) != 0) {
                    frame.setExtendedState(Frame.NORMAL);
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void message(DCCppMessage msg) {
    }

    /** {@inheritDoc} */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessagesWatcher.class);

}
