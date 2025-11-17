package jmri.jmrix.roco.z21.swing.configtool;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for Z21ConfigAction class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class Z21ConfigActionTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;

    @Test
    public void constructorZ21ConfigActionTest(){
        assertNotNull( new Z21ConfigAction("Z21",memo), "Z21ConfigAction constructor");
    }

    @Test
    public void memoConstructorZ21ConfigActionTest(){
        assertNotNull( new Z21ConfigAction(memo), "Z21ConfigAction constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown(){
        memo=null;
        tc.terminateThreads();
        tc=null;
        JUnitUtil.tearDown();
    }

}
