package jmri.jmrix.ipocs;

import jmri.jmrix.ipocs.protocol.Message;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public interface IpocsClientListener {
  String getUserName();
  void clientConnected(IpocsClientHandler client);
  void clientDisconnected(IpocsClientHandler client);
  void onMessage(IpocsClientHandler client, Message msg);
}
