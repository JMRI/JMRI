package jmri.jmrix.dccpp.swing.messages;

import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.DCCppSystemConnectionAction;

/** Action to open the DCC-EX Messages window. */
public class MessagesAction extends DCCppSystemConnectionAction {

    public MessagesAction(String name, DCCppSystemConnectionMemo memo) {
        super(name, memo);
    }

    public MessagesAction(DCCppSystemConnectionMemo memo) {
        super(Bundle.getMessage("MessagesFrameTitle"), memo);
    }

    public MessagesAction() {
        this(InstanceManager.getNullableDefault(DCCppSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo == null) {
            log.error("connection memo was null!");
            return;
        }
        MessagesFrame.showFrame(memo, null);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessagesAction.class);
}
