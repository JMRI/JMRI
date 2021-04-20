package jmri.jmrix.ipocs;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.enums.RqInputState;
import jmri.jmrix.ipocs.protocol.packets.InputStatusPacket;

public class IpocsSensorTest {

  @Test
  public void constructorTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    assertNotNull(new IpocsSensor(portController, "ES33", "Li91"));
  }

  @Test
  public void requestUpdateFromLayout() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsSensor sensor  = new IpocsSensor(portController, "ES33", "Li91");
    sensor.requestUpdateFromLayout();
  }

  @Test
  public void clientConnectedTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsSensor sensor  = new IpocsSensor(portController, "ES33", "Li91");
    sensor.clientConnected(mock(IpocsClientHandler.class));
  }
  
  @Test
  public void clientDisconnectedTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsSensor sensor  = new IpocsSensor(portController, "ES33", "Li91");
    sensor.clientDisconnected(mock(IpocsClientHandler.class));
  }
    
  @Test
  public void onMessageTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsSensor sensor  = new IpocsSensor(portController, "ES33", "Li91");
    IpocsClientHandler client = mock(IpocsClientHandler.class);
    Message msg = new Message();
    msg.setObjectName("Li92");
    sensor.onMessage(client, msg);
    msg.setObjectName("Li91");
    sensor.onMessage(client, msg);
    InputStatusPacket pkt = new InputStatusPacket();
    msg.getPackets().add(pkt);
    pkt.setState(RqInputState.Off);
    sensor.onMessage(client, msg);
    pkt.setState(RqInputState.On);
    sensor.onMessage(client, msg);
    pkt.setState(RqInputState.Undefined);
    sensor.onMessage(client, msg);
  }

}
