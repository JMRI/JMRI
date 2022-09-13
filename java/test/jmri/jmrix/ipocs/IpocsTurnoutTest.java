package jmri.jmrix.ipocs;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jmri.implementation.AbstractTurnoutTestBase;
import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.enums.RqPointsState;
import jmri.jmrix.ipocs.protocol.packets.ControllerStatusPacket;
import jmri.jmrix.ipocs.protocol.packets.PointsStatusPacket;
import jmri.util.JUnitUtil;

public class IpocsTurnoutTest extends AbstractTurnoutTestBase {

  @Mock
  IpocsPortController portController;

  @Override
  public int numListeners() {
    return 0;
  }

  @Override
  public void checkThrownMsgSent() throws InterruptedException {
  }

  @Override
  public void checkClosedMsgSent() throws InterruptedException {
  }

  @Test
  public void testOnMessage() {
    MockitoAnnotations.openMocks(this);
    t = new IpocsTurnout(portController, "MT2", "Vx2");

    final IpocsClientHandler client = mock(IpocsClientHandler.class);
    final Message msg = new Message();

    msg.setObjectName("WRONG");
    ((IpocsTurnout) t).onMessage(client, msg);

    // Test a packet that's unknown
    msg.setObjectName(t.getUserName());
    msg.getPackets().add(new ControllerStatusPacket());
    ((IpocsTurnout) t).onMessage(client, msg);

    // Test a known packet
    msg.getPackets().clear();
    final PointsStatusPacket pkt = new PointsStatusPacket();
    pkt.setState(RqPointsState.Left);
    msg.getPackets().add(pkt);
    ((IpocsTurnout) t).onMessage(client, msg);

    // Test a known packet
    pkt.setState(RqPointsState.Right);
    ((IpocsTurnout) t).onMessage(client, msg);

    // Test a known packet
    pkt.setState(RqPointsState.Moving);
    ((IpocsTurnout) t).onMessage(client, msg);

    // Test a known packet
    pkt.setState(RqPointsState.OutOfControl);
    ((IpocsTurnout) t).onMessage(client, msg);
  }

  @Test
  public void testForwardCommandChangeToLayout() {
    MockitoAnnotations.openMocks(this);
    t = new IpocsTurnout(portController, "MT2", "Vx2");

    ((IpocsTurnout) t).forwardCommandChangeToLayout(IpocsTurnout.UNKNOWN);
  }

  @Test
  public void testTurnoutPushbuttonLockout() {
    MockitoAnnotations.openMocks(this);
    t = new IpocsTurnout(portController, "MT2", "Vx2");

    ((IpocsTurnout) t).turnoutPushbuttonLockout(false);
  }

  @Test
  public void testClientConnected() {
    MockitoAnnotations.openMocks(this);
    t = new IpocsTurnout(portController, "MT2", "Vx2");

    final IpocsClientHandler client = mock(IpocsClientHandler.class);
    ((IpocsTurnout) t).clientConnected(client);
  }

  @Test
  public void testClientDisconnected() {
    MockitoAnnotations.openMocks(this);
    t = new IpocsTurnout(portController, "MT2", "Vx2");

    final IpocsClientHandler client = mock(IpocsClientHandler.class);
    ((IpocsTurnout)t).clientDisconnected(client);
  }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        MockitoAnnotations.openMocks(this);
        // when(portController..send())
        t = new IpocsTurnout(portController, "PT2", "Vx2");
        // t.client
    }

}
