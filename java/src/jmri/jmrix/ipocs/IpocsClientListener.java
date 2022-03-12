package jmri.jmrix.ipocs;

import jmri.jmrix.ipocs.protocol.Message;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public interface IpocsClientListener {
  public String getUserName();
  public void clientConnected(IpocsClientHandler client);
  public void clientDisconnected(IpocsClientHandler client);
  public void onMessage(IpocsClientHandler client, Message msg);
}
