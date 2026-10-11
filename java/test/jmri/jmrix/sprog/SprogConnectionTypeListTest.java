package jmri.jmrix.sprog;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SprogConnectionTypeList.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogConnectionTypeListTest {

   @Test
   public void testSprogConnectionTypeListConstructor(){
       SprogConnectionTypeList ct = new SprogConnectionTypeList();
       assertNotNull(ct);
   }

   @Test
   public void testManfacturerString(){
       SprogConnectionTypeList ct = new SprogConnectionTypeList();
       assertArrayEquals(new String[]{"SPROG DCC"}, ct.getManufacturers(), "Manufacturers");
   }

   @Test
   public void testProtocolClassList(){
       SprogConnectionTypeList ct = new SprogConnectionTypeList();
       assertArrayEquals( new String[]{
            "jmri.jmrix.sprog.sprog.ConnectionConfig",
            "jmri.jmrix.sprog.sprogCS.ConnectionConfig",
            "jmri.jmrix.sprog.sprognano.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogone.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprogonecs.ConnectionConfig",
            "jmri.jmrix.sprog.pi.pisprognano.ConnectionConfig",
            "jmri.jmrix.sprog.simulator.ConnectionConfig",
            "jmri.jmrix.sprog.SprogCSStreamConnectionConfig"},
            ct.getAvailableProtocolClasses(),
            "Protocol Class List");
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
