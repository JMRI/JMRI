package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Class for formatting the Z21 RMBus Feedback Reply
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RMBusFeedbackReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x0080;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not supported");
        }
               int groupIndex = m.getElement(4) & 0xff;
               int offset = (groupIndex * 10) + 1;
               String[] moduleStatus = new String[10];
        for(int i=0;i<10;i++){
               moduleStatus[i]= Bundle.getMessage("RMModuleFeedbackStatus",offset+i,
                   getState(1,(m.getElement(i+5)&0x01)==0x01),
                   getState(2,(m.getElement(i+5)&0x02)==0x02),
                   getState(3,(m.getElement(i+5)&0x04)==0x04),
                   getState(4,(m.getElement(i+5)&0x08)==0x08),
                   getState(5,(m.getElement(i+5)&0x10)==0x10),
                   getState(6,(m.getElement(i+5)&0x20)==0x20),
                   getState(7,(m.getElement(i+5)&0x40)==0x40),
                   getState(8,(m.getElement(i+5)&0x80)==0x80));
        }
        return Bundle.getMessage("RMBusFeedbackStatus",groupIndex,
               moduleStatus[0],moduleStatus[1],moduleStatus[2],
               moduleStatus[3],moduleStatus[4],moduleStatus[5],
               moduleStatus[6],moduleStatus[7],moduleStatus[8],
               moduleStatus[9]);
    }

    String getState(int contact,boolean on){

        return Bundle.getMessage("RMModuleContactStatus",contact,
                on ? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff"));
    }

}
