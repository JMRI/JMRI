package jmri.jmrix.ipocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Light;
import jmri.implementation.AbstractLight;
import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.enums.RqOutputCommand;
import jmri.jmrix.ipocs.protocol.packets.OutputStatusPacket;
import jmri.jmrix.ipocs.protocol.packets.Packet;
import jmri.jmrix.ipocs.protocol.packets.SetOutputPacket;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsLight extends AbstractLight implements IpocsClientListener {
  private final static Logger log = LoggerFactory.getLogger(IpocsLight.class);
  private final IpocsPortController portController;

  public IpocsLight(IpocsPortController portController, String systemName, String userName) {
    super(systemName, userName);
    this.portController = portController;
    this.portController.addListener(this);
  }

  @Override
  protected void doNewState(int oldState, int newState) {
    Message order = new Message();
    order.setObjectName(getUserName());
    SetOutputPacket packet = new SetOutputPacket();
    switch (newState) {
      case ON:
        packet.setCommand(RqOutputCommand.On);
        break;
      case OFF:
        packet.setCommand(RqOutputCommand.Off);
        break;
      case UNKNOWN:
        // ignore, but not an error as normal during AbstractTurnout testCreate()
        return;
      default:
        log.error("Unknown light order state");
        return;
    }
    order.getPackets().add(packet);
    portController.send(order);
  }

  @Override
  public void setState(int newState) {
      log.debug("setState {} was {}", newState, mState);
      
      //int oldState = mState;
      if (newState != ON && newState != OFF && newState != UNKNOWN) {
          throw new IllegalArgumentException("cannot set state value " + newState);
      }
      
      // do the state change in the hardware
      doNewState(mState, newState); // old state, new state
      // change value and tell listeners
      notifyStateChange(mState, newState);
  }

  @Override
  public void clientConnected(IpocsClientHandler client) {
  }

  @Override
  public void clientDisconnected(IpocsClientHandler client) {
    setState(Light.UNKNOWN);
  }

  @Override
  public void onMessage(IpocsClientHandler client, Message msg) {
    if (!msg.getObjectName().equals(super.getUserName())) {
      return;
    }
    // We have a status, let's interpret it.
    for (Packet packet : msg.getPackets()) {
      switch (packet.getId()) {
        case OutputStatusPacket.IDENT:
          switch (((OutputStatusPacket) packet).getState()) {
            case On:
              setState(Light.ON);
              break;
            case Off:
              setState(Light.OFF);
              break;
            default:
              log.error("Unknown light state {}", ((OutputStatusPacket) packet).getState().toString());
              break;
          }
          break;
        default:
          break;
      }
    }
  }
}
