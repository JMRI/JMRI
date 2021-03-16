package jmri.jmrix.ecos.networkdriver;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016, 2021
 **/
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase  {

    private EcosSystemConnectionMemo memo;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();

        memo = new EcosSystemConnectionMemo(); // takes care of cleaning up the EcosPreferenceManager
        cc = new ConnectionConfig();
        ((ConnectionConfig)cc).setInstance(); // create an adapter assumed to exist in tests
    }

    @AfterEach
    @Override
    public void tearDown(){
        cc = null;
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
