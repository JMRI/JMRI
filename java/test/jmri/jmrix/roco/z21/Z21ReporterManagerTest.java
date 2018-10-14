package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * The class being tested only has one reporter, hence some tests pulled down.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21ReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "ZR" + i;
    }

   @Before
    @Override
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        Z21InterfaceScaffold tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        l = new Z21ReporterManager(memo);
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

    @Override
    protected int maxN() { return 1; }
    
    @Override
    protected String getNameToTest1() {
        return "1";
    }

}
