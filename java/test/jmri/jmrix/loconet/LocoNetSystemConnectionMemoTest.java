package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the LocoNetSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LocoNetSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
       memo = new LocoNetSystemConnectionMemo();
       memo.setLnTrafficController(lnis);
       lnis.setSystemConnectionMemo(memo);
       memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);
       memo.configureManagers();
       scm = memo;
    }

    @Override
    @After
    public void tearDown(){
       memo.dispose();
       JUnitUtil.tearDown();
    }

}
