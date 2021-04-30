package jmri.jmrix.sproggen5;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogConnectionTypeList.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogGen5ConnectionTypeListTest {

   @Test
   public void ConstructorTest(){
       SprogGen5ConnectionTypeList ct = new SprogGen5ConnectionTypeList();
       Assert.assertNotNull(ct);
   }

   @Test
   public void ManfacturerString(){
       SprogGen5ConnectionTypeList ct = new SprogGen5ConnectionTypeList();
       Assert.assertEquals("Manufacturers",new String[]{"SPROG DCC Generation 5"}, ct.getManufacturers());
   }

   @Test
   public void ProtocolClassList(){
       SprogGen5ConnectionTypeList ct = new SprogGen5ConnectionTypeList();
       Assert.assertEquals("Protocol Class List", new String[]{
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.CanisbConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Sprog3PlusConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3PlusConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3v2ConnectionConfig",
            "jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3ConnectionConfig"},
            ct.getAvailableProtocolClasses());
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
