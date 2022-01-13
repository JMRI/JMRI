package jmri.jmrix.ipocs;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IpocsSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

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

    @Override
    public String getSystemName(int i) {
        return "PS" + i;
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
        final IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
        final IpocsPortController portController = mock(IpocsPortController.class);
        when(memo.getPortController()).thenReturn(portController);
        when(memo.getSystemPrefix()).thenReturn("P");
        l = new IpocsSensorManager(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
