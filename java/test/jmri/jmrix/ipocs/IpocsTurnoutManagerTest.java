package jmri.jmrix.ipocs;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class IpocsTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

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

    @Override
    public String getSystemName(int i) {
        return "PT" + i;
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
    public void testSetAndGetOutputInterval() {}

    @Disabled("Test requires further development")
    @Test
    @Override
    public void testRegisterDuplicateSystemName() {}

    @Test
    @Override
    public void testAutoSystemNames() {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        final IpocsPortController portController = mock(IpocsPortController.class);
        final IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
        when(memo.getPortController()).thenReturn(portController);
        when(memo.getSystemPrefix()).thenReturn("P");
        l = new IpocsTurnoutManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

}
