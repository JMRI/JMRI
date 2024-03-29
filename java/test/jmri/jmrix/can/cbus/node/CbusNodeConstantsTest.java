package jmri.jmrix.can.cbus.node;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeConstantsTest {

    // no testCtor as class only supplies static methods
    
    @Test
    public void testGetBusType() {
        Assert.assertEquals("GetBusType","CAN",CbusNodeConstants.getBusType(1));
        Assert.assertEquals("GetBusType unknown","Unknown",CbusNodeConstants.getBusType(999));
    }
    
    @Test
    public void testGetManu() {
        Assert.assertTrue("getManu 0",CbusNodeConstants.getManu(0).isEmpty());
        Assert.assertEquals("getManu","MERG",CbusNodeConstants.getManu(165));
        Assert.assertEquals("getManu 999","Manufacturer 999",CbusNodeConstants.getManu(999));
    }

    @Test
    public void testgetModuleTypeFromConstants() {
        Assert.assertEquals("getModuleType 165 29","CANPAN",CbusNodeConstants.getModuleType(165,29));
        Assert.assertEquals("getModuleType 70 4","CANGC4",CbusNodeConstants.getModuleType(70,4));
        Assert.assertEquals("getModuleType 80 2","DUALCAB",CbusNodeConstants.getModuleType(80,2));
        Assert.assertTrue("getModuleType unknown",CbusNodeConstants.getModuleType(999,999).isEmpty() );
    }
    
    @Test
    public void testgetModuleTypeExtra() {
        Assert.assertEquals("getModuleTypeExtra 165 31","Control panel 64 Inputs / 64 Outputs",CbusNodeConstants.getModuleTypeExtra(165,31));
        Assert.assertEquals("getModuleTypeExtra 70 4","8 channel RFID reader.",CbusNodeConstants.getModuleTypeExtra(70,4));
        Assert.assertEquals("getModuleTypeExtra 80 2","Dual cab based on cancab.",CbusNodeConstants.getModuleTypeExtra(80,2));
        Assert.assertTrue("getModuleTypeExtra unknown",CbusNodeConstants.getModuleTypeExtra(999,999).isEmpty() );
    }
    
    @Test
    public void testgetSupportLink() {
        Assert.assertTrue("getModuleSupportLink 165 31",CbusNodeConstants.getModuleSupportLink(165,31).contains("https://"));
        Assert.assertTrue("getModuleSupportLink 165 999",CbusNodeConstants.getModuleSupportLink(165,999).isEmpty() );
        Assert.assertTrue("getModuleSupportLink 70 4",CbusNodeConstants.getModuleSupportLink(70,4).contains("http"));
        Assert.assertTrue("getModuleSupportLink unknown",CbusNodeConstants.getModuleSupportLink(999,999).isEmpty() );
    }
    
    @Test
    public void testgetReservedModule() {
        Assert.assertTrue("getReservedModule 260",CbusNodeConstants.getReservedModule(260).isEmpty() );
        Assert.assertEquals("getModuleTypeExtra 70 4","Reserved, used by all CABS",CbusNodeConstants.getReservedModule(65535));
        Assert.assertTrue("getReservedModule 99",CbusNodeConstants.getReservedModule(99).isEmpty() );
        Assert.assertEquals("getReservedModule 100",Bundle.getMessage("NdNumReserveFixed"),CbusNodeConstants.getReservedModule(100));
        Assert.assertEquals("getReservedModule 125",Bundle.getMessage("NdNumReserveFixed"),CbusNodeConstants.getReservedModule(100));        
        
        
    }    
    
    @Test
    public void testBackupDisplayPhrase() {
        Assert.assertEquals("Backup Enum Text outstanding",
            Bundle.getMessage("BackupOutstanding"),
            CbusNodeConstants.displayPhrase(CbusNodeConstants.BackupType.OUTSTANDING));
        
        Assert.assertEquals("Backup Enum Text complete error",
            Bundle.getMessage("BackupCompleteError"),
            CbusNodeConstants.displayPhrase(CbusNodeConstants.BackupType.COMPLETEDWITHERROR));
    }
    
    @Test
    public void testBackupLookupByName() {
        Assert.assertEquals("Backup Enum Text lookup outstanding",
            CbusNodeConstants.BackupType.OUTSTANDING,
            CbusNodeConstants.lookupByName("OUTSTANDING"));
        
        Assert.assertEquals("Backup Enum Text lookup complete error",
            CbusNodeConstants.BackupType.COMPLETEDWITHERROR,
            CbusNodeConstants.lookupByName("COMPLETEDWITHERROR"));
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeConstantsTest.class);

}
