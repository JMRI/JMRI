package jmri.jmrix.ipocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;

import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.packets.InputStatusPacket;
import jmri.jmrix.ipocs.protocol.packets.Packet;
import jmri.jmrix.ipocs.protocol.packets.RequestStatusPacket;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsSensor extends AbstractSensor implements IpocsClientListener {
  private final static Logger log = LoggerFactory.getLogger(IpocsSensor.class);
  private final IpocsPortController portController;

  public IpocsSensor(IpocsPortController portController, String systemName, String userName) {
    super(systemName, userName);
    this.portController = portController;
    this.portController.addListener(this);
  }

  @Override
  public void requestUpdateFromLayout() {
    Message order = new Message();
    order.setObjectName(getUserName());
    RequestStatusPacket packet = new RequestStatusPacket();
    order.getPackets().add(packet);
    portController.send(order);
  }

  @Override
  public void clientConnected(IpocsClientHandler client) {
  }

  @Override
  public void clientDisconnected(IpocsClientHandler client) {
    setOwnState(Sensor.UNKNOWN);
  }

  @Override
  public void onMessage(IpocsClientHandler client, Message msg) {
    if (!msg.getObjectName().equals(super.getUserName())) {
      return;
    }
    // We have a status, let's interpret it.
    for (Packet packet : msg.getPackets()) {
      switch (packet.getId()) {
        case InputStatusPacket.IDENT:
          switch (((InputStatusPacket) packet).getState()) {
            case On:
              setOwnState(Sensor.ON);
              break;
            case Off:
              setOwnState(Sensor.OFF);
              break;
            case Undefined:
              setOwnState(Sensor.UNKNOWN);
              break;
            default:
              log.error("Unknown sensor state {}", ((InputStatusPacket)packet).getState().toString());
              break;
          }
          break;
        default:
          break;
      }
    }
  }
}
