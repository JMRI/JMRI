package jmri.jmrix.ipocs;

import jmri.jmrix.ipocs.protocol.Message;

public interface IpocsClientListener {
  public String getUserName();
  public void clientConnected(IpocsClientHandler client);
  public void clientDisconnected(IpocsClientHandler client);
  public void onMessage(IpocsClientHandler client, Message msg);
}
