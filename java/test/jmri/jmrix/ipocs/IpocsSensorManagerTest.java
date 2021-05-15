package jmri.jmrix.ipocs;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class IpocsSensorManagerTest {
  @Test
  public void constructorTest() {
    final IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    assertNotNull(new IpocsSensorManager(memo));
  }
  
  @Test
  public void createNewSensorTest() {
    final IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    final IpocsPortController portController = mock(IpocsPortController.class);
    when(memo.getPortController()).thenReturn(portController);
    final IpocsSensorManager manager = new IpocsSensorManager(memo);
    assertNotNull(manager.createNewSensor("AS33", "Li91"));
  }

}
