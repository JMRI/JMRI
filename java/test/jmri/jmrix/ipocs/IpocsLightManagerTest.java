package jmri.jmrix.ipocs;

import jmri.util.JUnitUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.*;

public class IpocsLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    @Override
    protected int getNumToTest1() {
        return 10;
    }

    @Override
    protected int getNumToTest2() {
        return 56517;
    }

    @Override
    public String getSystemName(int i) {
        return "PL" + i;
    }

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

    @Disabled("Test requires further development")
    @Test
    @Override
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Disabled("Test requires further development")
    @Test
    @Override
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    @Disabled("Test requires further development")
    @Test
    @Override
    public void testRegisterDuplicateSystemName() {}
  
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
        IpocsPortController portController = mock(IpocsPortController.class);
        when(memo.getPortController()).thenReturn(portController);
        when(memo.getSystemPrefix()).thenReturn("P");
        l = new IpocsLightManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
        JUnitUtil.tearDown();
    }

}
