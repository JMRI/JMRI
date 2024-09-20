package jmri.jmrix.sproggen5;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SprogConnectionTypeList.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogGen5ConnectionTypeListTest {

   @Test
   public void testSprogGen5ConnectionTypeListConstructor(){
       SprogGen5ConnectionTypeList ct = new SprogGen5ConnectionTypeList();
       Assertions.assertNotNull(ct);
   }

   @Test
   public void testManfacturerString(){
       SprogGen5ConnectionTypeList ct = new SprogGen5ConnectionTypeList();
       Assertions.assertArrayEquals(new String[]{"SPROG DCC Generation 5"}, ct.getManufacturers(), "Manufacturers");
   }

   @Test
   public void testProtocolClassList(){
       SprogGen5ConnectionTypeList ct = new SprogGen5ConnectionTypeList();
       Assertions.assertArrayEquals( new String[]{
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.CanisbConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Sprog3PlusConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3PlusConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3v2ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3ConnectionConfig"},
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
