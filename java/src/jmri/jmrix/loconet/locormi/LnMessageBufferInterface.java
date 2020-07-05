package jmri.jmrix.loconet.locormi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import jmri.jmrix.loconet.LocoNetMessage;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Bob Jacobsen, Alex Shepherd Copyright (c) 2002
 */
@API(status = EXPERIMENTAL)
public interface LnMessageBufferInterface extends Remote {

    public void enable(int mask) throws RemoteException;

    public void disable(int mask) throws RemoteException;

    public void clear() throws RemoteException;

    public Object[] getMessages(long timeout) throws RemoteException;

    public void sendLocoNetMessage(LocoNetMessage m) throws RemoteException;

}
