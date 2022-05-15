package jmri.jmrix.bachrus.kpfserialdriver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Steve Young Copyright(c) 2022
 */
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
    }

}
