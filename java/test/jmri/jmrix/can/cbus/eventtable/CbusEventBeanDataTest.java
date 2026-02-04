package jmri.jmrix.can.cbus.eventtable;

import java.util.HashSet;

import jmri.Light;
import jmri.NamedBean;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusLightManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventBeanDataTest {

    @Test
    public void testCTor() {
        CbusEventBeanData t = new CbusEventBeanData(new HashSet<>(0),new HashSet<>(0));
        assertNotNull(t);
    }

    @Test
    public void testToString(){

        CbusLightManager lm = new CbusLightManager(memo);
        HashSet<NamedBean> hsA = new HashSet<>();
        HashSet<NamedBean> hsB = new HashSet<>();

        Light lightA = lm.provideLight("+17");
        lightA.setUserName("MyLightA");

        Light lightB = lm.provideLight("+18");
        lightB.setUserName("MyLightB");

        hsA.add(lightA);
        hsB.add(lightB);

        CbusEventBeanData t = new CbusEventBeanData(hsA,hsB);
        assertEquals("Light On: MyLightA Light Off: MyLightB",t.toString());

        assertEquals( 1, t.getActionA().size());
        assertEquals( 1, t.getActionB().size());

        assertTrue( t.getActionA().contains(lightA));
        assertTrue( t.getActionB().contains(lightB));


        t = new CbusEventBeanData(new HashSet<>(0),new HashSet<>(0));
        assertTrue(t.toString().isEmpty());

        lm.dispose();

    }

    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventBeanDataTest.class);

}
