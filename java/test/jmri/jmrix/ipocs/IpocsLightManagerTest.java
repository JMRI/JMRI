package jmri.jmrix.ipocs;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class IpocsLightManagerTest {
  @Test
  public void constructorTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    assertNotNull(new IpocsLightManager(memo));
  }

  @Test
  public void validSystemNameConfigTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    assertFalse(new IpocsLightManager(memo).validSystemNameConfig(""));
  }
  
  @Test
  public void createNewLightTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController portController = mock(IpocsPortController.class);
    when(memo.getPortController()).thenReturn(portController);
    IpocsLightManager manager = new IpocsLightManager(memo);
    assertNotNull(manager.createNewLight("AL33", "Li91"));
  }
}
