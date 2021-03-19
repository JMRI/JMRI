package jmri.jmrix.ipocs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.packets.ConnectionRequestPacket;
import jmri.jmrix.ipocs.protocol.packets.ConnectionResponsePacket;
import jmri.jmrix.ipocs.protocol.packets.Packet;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsClientHandler implements CompletionHandler<Integer, ByteBuffer> {

  private final static Logger log = LoggerFactory.getLogger(IpocsClientHandler.class);
  private final AsynchronousSocketChannel client;
  private String unitId;
  private final List<IpocsClientListener> clientListeners = new ArrayList<IpocsClientListener>();

  public String getUnitId() {
      return unitId;
  }

  public IpocsClientHandler(final AsynchronousSocketChannel client) {
    this.client = client;
    ByteBuffer recvBuffer = ByteBuffer.allocate(256);
    client.read(recvBuffer, recvBuffer, this);
  }

  @Override
  public void completed(final Integer bytesRead, final ByteBuffer recvBuffer) {
    // connection closed by the server
    if (bytesRead == -1) {
      try {
        client.close();
        for (IpocsClientListener listener : clientListeners) {
          listener.clientDisconnected(this);
        }
      } catch (final IOException ex) {
        log.error("Unable to close client: {}", ex.getMessage());
      }
      return;
    }
    int currPos = recvBuffer.position();
    recvBuffer.rewind();

    Message msg = null;
    while (recvBuffer.position() < currPos
       && (msg = Message.parse(recvBuffer, currPos - recvBuffer.position())) != null) {
      for (Packet pkt : msg.getPackets()) {
        switch (pkt.getId()) {
          case ConnectionRequestPacket.IDENT:
            unitId = msg.getObjectName();
            // TODO Check site data version
            for (IpocsClientListener listener : clientListeners) {
              listener.clientConnected(this);
            }
            Message response = new Message();
            response.setObjectName(msg.getObjectName());
            ConnectionResponsePacket respPkt = new ConnectionResponsePacket();
            respPkt.setProtocolVersion(((ConnectionRequestPacket)pkt).getProtocolVersion());
            response.getPackets().add(respPkt);
            client.write(response.serialize());
            break;
          default:
            for (IpocsClientListener listener : clientListeners) {
              listener.onMessage(this, msg);
            }
            break;
        }
      }
    }
    ByteBuffer newRecvBuffer = ByteBuffer.allocate(256);
    if (recvBuffer.position() < currPos) {
      int position = recvBuffer.position();
      newRecvBuffer.put(recvBuffer);
      newRecvBuffer.position(currPos - position);
    }
    client.read(newRecvBuffer, newRecvBuffer, this);
  }

  @Override
  public void failed(final Throwable exc, final ByteBuffer attachment) {
    try {
      client.close();
    } catch (IOException ex) {
      log.error("Error closing connection", ex);
    }
    for (IpocsClientListener listener : clientListeners) {
      listener.clientDisconnected(this);
    }
  }

  public void addClientListener(IpocsClientListener clientListener) {
    clientListeners.add(clientListener);
  }

  public void removeClientListener(IpocsClientListener clientListener) {
    clientListeners.remove(clientListener);
  }

  public void send(Message msg) {
    client.write(msg.serialize());
  }
}
