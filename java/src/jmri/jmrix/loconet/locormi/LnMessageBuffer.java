package jmri.jmrix.loconet.locormi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * Handle an RMI connection for a single remote client.
 *
 * @author Alex Shepherd Copyright (c) 2002
 */
public class LnMessageBuffer extends UnicastRemoteObject implements LnMessageBufferInterface, LocoNetListener {

    LinkedList<LocoNetMessage> messageList = null;
    LnTrafficController tc;

    public LnMessageBuffer(LnTrafficController _tc) throws RemoteException {
        super();
        tc = _tc;
    }

    @Override
    public void enable(int mask) throws RemoteException {
        if (messageList == null) {
            messageList = new LinkedList<LocoNetMessage>();
        }
        tc.addLocoNetListener(mask, this);
    }

    @Override
    public void disable(int mask) throws RemoteException {
        tc.removeLocoNetListener(mask, this);
    }

    @Override
    public void clear() throws RemoteException {
        messageList.clear();
    }

    @Override
    public void message(LocoNetMessage msg) {
        synchronized (messageList) {
            messageList.add(msg);
            messageList.notify();
        }
    }

    @Override
    public Object[] getMessages(long timeout) {
        Object[] messagesArray = null;

        synchronized (messageList) {
            if (messageList.size() == 0) {
                try {
                    messageList.wait(timeout);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // retain if needed later
                }
            }

            if (messageList.size() > 0) {
                messagesArray = messageList.toArray();
                messageList.clear();
            }
        }

        return messagesArray;
    }

    @Override
    public void sendLocoNetMessage(LocoNetMessage m) {
        tc.sendLocoNetMessage(m);
    }

}
