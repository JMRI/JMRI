package jmri.jmrix.roco.z21.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for Z21ComponentFactory class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class Z21ComponentFactoryTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;

    @Test
    public void memoConstructorZ21ComponentFactoryTest(){
        assertNotNull( new Z21ComponentFactory(memo), "Z21ComponentFactory constructor");
    }

    @Test
    public void getMenu(){
        Z21ComponentFactory zcf = new Z21ComponentFactory(memo);
        assertNotNull( zcf.getMenu(), "Component Factory getMenu method");
    }

    @Test
    public void getMenuDisabled(){
        memo.setDisabled(true);
        Z21ComponentFactory zcf = new Z21ComponentFactory(memo);
        assertNull( zcf.getMenu(), "Disabled Component Factory getMenu method");
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
