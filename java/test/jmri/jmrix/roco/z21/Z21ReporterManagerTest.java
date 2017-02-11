package jmri.jmrix.roco.z21;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for Z21ReporterManager class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21ReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "ZR" + i;
    }

   @Before
    @Override
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        Z21InterfaceScaffold tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        l = new Z21ReporterManager(memo);
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

    @Override
    protected int getNumToTest1() {
        return 1;
    }

    @Override
    protected int getNumToTest2() {
        return 1;
    }

    @Override
    protected int getNumToTest3() {
        return 1;
    }

    @Override
    protected int getNumToTest4() {
        return 1;
    }

    @Override
    protected int getNumToTest5() {
        return 1;
    }


}
