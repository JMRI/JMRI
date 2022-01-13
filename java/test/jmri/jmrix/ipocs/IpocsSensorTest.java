package jmri.jmrix.ipocs;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.enums.RqInputState;
import jmri.jmrix.ipocs.protocol.packets.InputStatusPacket;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class IpocsSensorTest extends jmri.implementation.AbstractSensorTestBase {

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

    @Override
    public int numListeners() {
        return 0;
    }

    @Disabled("Test requires further development")
    @Override
    public void checkActiveMsgSent() {
    }

    @Disabled("Test requires further development")
    @Override
    public void checkInactiveMsgSent() {
    }

    @Disabled("Test requires further development")
    @Override
    public void checkStatusRequestMsgSent() {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        IpocsPortController portController = mock(IpocsPortController.class);
        t  = new IpocsSensor(portController, "ES33", "Li91");
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        t = null;
        JUnitUtil.tearDown();
    }

}
