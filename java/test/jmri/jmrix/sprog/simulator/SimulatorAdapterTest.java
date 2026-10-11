package jmri.jmrix.sprog.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for sprog SimulatorAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimulatorAdapterTest {

   @Test
   public void testSprogSimulatorAdapterConstructor(){
       SimulatorAdapter a = new SimulatorAdapter();
       Assertions.assertNotNull(a);

       // clean up
       a.getSystemConnectionMemo().getSprogTrafficController().dispose();
   }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
