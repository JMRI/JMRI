package jmri.jmrix.ipocs;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.log4j.Level;
import org.junit.Test;

import jmri.Light;
import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.enums.RqOutputState;
import jmri.jmrix.ipocs.protocol.packets.OutputStatusPacket;

public class IpocsLightTest {
  @Test
  public void constructorTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    assertNotNull(new IpocsLight(portController, "ee33", "Li91"));
  }

  @Test
  public void doNewStateTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsLight light  = new IpocsLight(portController, "ee33", "Li91");
    light.doNewState(-1, Light.ON);
    //assertEquals(Light.ON, light.getState());
    light.doNewState(-1, Light.OFF);
    //assertEquals(Light.OFF, light.getState());
    light.doNewState(-1, Light.UNKNOWN);
    jmri.util.JUnitAppender.suppressMessage(Level.DEBUG, "Unknown light order state");
    //assertEquals(Light.OFF, light.getState());
  }

  @Test
  public void setStateTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsLight light  = new IpocsLight(portController, "ee33", "Li91");
    light.setState(Light.ON);
    light.setState(Light.OFF);
    light.setState(Light.UNKNOWN);
    assertThrows(IllegalArgumentException.class, () -> light.setState(Light.INTERMEDIATE));
  }

  @Test
  public void clientConnectedTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsLight light  = new IpocsLight(portController, "ee33", "Li91");
    light.clientConnected(mock(IpocsClientHandler.class));
  }
  
  @Test
  public void clientDisconnectedTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsLight light  = new IpocsLight(portController, "ee33", "Li91");
    light.clientDisconnected(mock(IpocsClientHandler.class));
  }
    
  @Test
  public void onMessageTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsLight light  = new IpocsLight(portController, "ee33", "Li91");
    IpocsClientHandler client = mock(IpocsClientHandler.class);
    Message msg = new Message();
    msg.setObjectName("Li92");
    light.onMessage(client, msg);
    msg.setObjectName("Li91");
    light.onMessage(client, msg);
    OutputStatusPacket pkt = new OutputStatusPacket();
    msg.getPackets().add(pkt);
    pkt.setState(RqOutputState.Off);
    light.onMessage(client, msg);
    pkt.setState(RqOutputState.On);
    light.onMessage(client, msg);
  }
}
