package jmri.jmrix.ipocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.enums.RqPointsCommand;
import jmri.jmrix.ipocs.protocol.packets.Packet;
import jmri.jmrix.ipocs.protocol.packets.PointsStatusPacket;
import jmri.jmrix.ipocs.protocol.packets.ThrowPointsPacket;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsTurnout extends AbstractTurnout implements IpocsClientListener {
  private final static Logger log = LoggerFactory.getLogger(IpocsTurnout.class);
  private IpocsPortController portController;

  IpocsTurnout(IpocsPortController portController, String systemName, String userName) {
    super(systemName, userName);
    this.portController = portController;
    this.portController.addListener(this);
    super._validFeedbackNames = new String[]{"MONITORING"};
    super._validFeedbackModes = new int[]{MONITORING};
    super._validFeedbackTypes = MONITORING;
    super._activeFeedbackType = MONITORING;
    Message msg = portController.getLastStatus(userName);
    if (msg != null) {
      onMessage(null, msg);
    }
  }

  @Override
  protected void forwardCommandChangeToLayout(int s) {
    Message order = new Message();
    order.setObjectName(getUserName());
    ThrowPointsPacket packet = new ThrowPointsPacket();
    switch (s) {
      case THROWN:
        packet.setCommand(RqPointsCommand.Left);
        break;
      case CLOSED:
        packet.setCommand(RqPointsCommand.Right);
        break;
      default:
        log.error("Unknown turnout order state");
        return;
    }
    order.getPackets().add(packet);
    portController.send(order);
  }

  @Override
  protected void turnoutPushbuttonLockout(boolean locked) {
    // Not supported
  }

  @Override
  public void clientConnected(IpocsClientHandler client) {
    // Nothing to do when a client connects.
  }

  @Override
  public void clientDisconnected(IpocsClientHandler client) {
    newKnownState(Turnout.UNKNOWN);
  }

  @Override
  public void onMessage(IpocsClientHandler client, Message msg) {
    if (!msg.getObjectName().equals(super.getUserName())) {
      return;
    }
    // We have a status, let's interpret it.
    for (Packet packet : msg.getPackets()) {
      switch (packet.getId()) {
        case PointsStatusPacket.IDENT:
          switch (((PointsStatusPacket) packet).getState()) {
            case Left:
              newKnownState(Turnout.THROWN);
              break;
            case Right:
              newKnownState(Turnout.CLOSED);
              break;
            case Moving:
              newKnownState(Turnout.INCONSISTENT);
              break;
            case OutOfControl:
              newKnownState(Turnout.UNKNOWN);
              break;
            default:
              log.error("Unknown turnout state {}", ((PointsStatusPacket) packet).getState().toString());
              break;
          }
          break;
        default:
          break;
      }
    }
  }
}
