package jmri.jmrix.ipocs;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class IpocsTurnoutManagerTest {

  @Test
  public void constructorTest() {
    final IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    assertNotNull(new IpocsTurnoutManager(memo));
  }
  
  @Test
  public void createNewTurnoutTest() {
    final IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    final IpocsPortController portController = mock(IpocsPortController.class);
    when(memo.getPortController()).thenReturn(portController);
    final IpocsTurnoutManager manager = new IpocsTurnoutManager(memo);
    assertNotNull(manager.createNewTurnout("AT33", "Li91"));
  }

}
