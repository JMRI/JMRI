package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusOpCodesTest {

    @Test
    public void testCTor() {
        CbusOpCodes t = new CbusOpCodes();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testDecode() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 ); // request e stop
        Assert.assertTrue(CbusOpCodes.decode(m).contains("Request Emergency Stop All"));
        Assert.assertTrue(CbusOpCodes.decodeopc(m).contains("RESTP"));

        m.setElement(0, 0x18);
        Assert.assertEquals("0x18 no current opc definition","Reserved opcode",CbusOpCodes.decode(m));
        m.setElement(0, CbusConstants.CBUS_DKEEP);
        m.setElement(1, 0x04);
        Assert.assertEquals("CBUS_DKEEP","Keep Alive Session: 4",CbusOpCodes.decode(m));

        m.setElement(0, CbusConstants.CBUS_RLOC);
        m.setElement(1, 0x00);
        m.setElement(2, 0x2c);
        Assert.assertEquals("CBUS_RLOC","Request Session Addr: 44 S",CbusOpCodes.decode(m));

        m.setElement(0, CbusConstants.CBUS_ACON);
        m.setElement(3, 0xd4);
        m.setElement(4, 0xac);
     //   Assert.assertEquals("CBUS_ACON","N: 44 E:5432",CbusOpCodes.decode(m));
     
        m.setElement(0, CbusConstants.CBUS_ERR);
        m.setElement(1, 0xcc);
        m.setElement(2, 0x8f);
        m.setElement(3, 0x01);
        Assert.assertEquals("CBUS_ERR 1","Command Station Error Loco stack full for address 3215 L",CbusOpCodes.decode(m));
        m.setElement(3, 0x02);
        Assert.assertEquals("CBUS_ERR 2","Command Station Error Loco address 3215 L taken",CbusOpCodes.decode(m));
        m.setElement(3, 0x03);
        Assert.assertEquals("CBUS_ERR 3","Command Station Error Session 204 not present on Command Station",CbusOpCodes.decode(m));
        m.setElement(3, 0x04);
        Assert.assertEquals("CBUS_ERR 4","Command Station Error Consist empty for consist 204",CbusOpCodes.decode(m));
        m.setElement(3, 0x05);
        Assert.assertEquals("CBUS_ERR 5","Command Station Error Loco not found for session 204",CbusOpCodes.decode(m));
        m.setElement(3, 0x06);
        Assert.assertEquals("CBUS_ERR 6","Command Station Error CAN bus error ",CbusOpCodes.decode(m));
        m.setElement(3, 0x07);
        Assert.assertEquals("CBUS_ERR 7","Command Station Error Invalid request for address 3215 L",CbusOpCodes.decode(m));
        m.setElement(3, 0x08);
        Assert.assertEquals("CBUS_ERR 8","Command Station Error Throttle cancelled for session 204",CbusOpCodes.decode(m));
        m.setElement(3, 0x09);
        Assert.assertEquals("CBUS_ERR 9","Command Station Error ",CbusOpCodes.decode(m));
    }
    
    @Test
    public void testDecodeCMDERR() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_CMDERR,12,34,01 },0x12  );
        Assert.assertEquals("CBUS_CMDERR 1","Node Config Error NN:3106 ERROR : Command Not Supported.",CbusOpCodes.decode(m));
        m.setElement(3, 0xaa);
        Assert.assertEquals("CBUS_CMDERR aa","Node Config Error NN:3106 ",CbusOpCodes.decode(m));
    }    

    @Test
    public void testDecodeextend() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_CMDERR,12,34,01 },0x12  );
        Assert.assertEquals("CBUS_CMDERR false1","Node Config Error NN:3106 ERROR : Command Not Supported.",CbusOpCodes.decode(m,false,0x12));
        Assert.assertEquals("CBUS_CMDERR true","Bootloader Message Type: 18",CbusOpCodes.decode(m,true,0x12));
    }

    @Test
    public void testdecodeopc() {
        CanMessage m = new CanMessage( new int[]{0x18 },0x12  );
        Assert.assertEquals("decodeopc 1","Reserved opcode",CbusOpCodes.decodeopc(m));
        Assert.assertEquals("decodeopc 2","Reserved opcode",CbusOpCodes.decodeopc(m,false,0x12));
        Assert.assertEquals("decodeopc 3","Bootloader Message Type: 18",CbusOpCodes.decodeopc(m,true,0x12));
    }

    @Test
    public void testisEventNotRequest() {
        Assert.assertEquals("EventNotRequest false",false,CbusOpCodes.isEventNotRequest(CbusConstants.CBUS_CMDERR));
        Assert.assertEquals("EventNotRequest true",true,CbusOpCodes.isEventNotRequest(CbusConstants.CBUS_ACON));
    }
    
    @Test
    public void testisDCC() {
        Assert.assertEquals("isDCC false",false,CbusOpCodes.isDcc(CbusConstants.CBUS_ACON));
        Assert.assertEquals("isDCC true",true,CbusOpCodes.isDcc(CbusConstants.CBUS_DKEEP));
    }    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusOpCodesTest.class);

}
