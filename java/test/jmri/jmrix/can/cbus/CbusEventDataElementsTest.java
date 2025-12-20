package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventDataElementsTest {

    @Test
    public void testCTor() {
        Assertions.assertNotNull(t);
    }

    @Test
    public void testSetGet() {

        assertEquals(0,t.getNumElements());

        t.setNumElements(1);
        assertEquals(1,t.getNumElements());

        t.setData(1, 79);
        assertEquals(79,t.getData(1));

    }

    @Test
    public void testMessageRequest() {

        // Long Request
        assertEquals("[581] 92 00 02 00 03",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.REQUEST).getToString());

        t.setNumElements(3); // does nothing as request event

        // Short Request
        assertEquals("[581] 9A 00 00 00 05",
            t.getCanMessage(1,0,5,CbusEventDataElements.EvState.REQUEST).getToString());

    }

    @Test
    public void testMessageOn() {

        // Long On
        assertEquals("[581] 90 00 02 00 03",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());

        t.setNumElements(1);
        t.setData(1, 0xaa);
        // Long On Data1
        assertEquals("[581] B0 00 02 00 03 AA",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());

        t.setNumElements(2);
        t.setData(2, 0xbb);
        // Long On Data2
        assertEquals("[581] D0 00 02 00 03 AA BB",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());

        t.setNumElements(3);
        t.setData(3, 0xcc);
        // Long On Data3
        assertEquals("[581] F0 00 02 00 03 AA BB CC",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.ON).getToString());

    }

    @Test
    public void testMessageOff() {

        // Long Of
        assertEquals("[581] 91 00 02 00 03",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());

        t.setNumElements(1);
        t.setData(1, 0xaa);
        // Long Off Data1
        assertEquals("[581] B1 00 02 00 03 AA",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());

        t.setNumElements(2);
        t.setData(2, 0xbb);
        // Long Off Data2
        assertEquals("[581] D1 00 02 00 03 AA BB",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());

        t.setNumElements(3);
        t.setData(3, 0xcc);
        // Long Off Data3
        assertEquals("[581] F1 00 02 00 03 AA BB CC",
            t.getCanMessage(1,2,3,CbusEventDataElements.EvState.OFF).getToString());

    }

    @Test
    public void testGetJmriString(){
        assertEquals("+0",CbusEvent.getJmriString(0, 0));
        assertEquals("+N123E456",CbusEvent.getJmriString(123, 456));
    }

    @Test
    public void testGetNumElements(){

        assertEquals( 0, CbusEvent.getNumEventDataElements(new CanMessage(
            new int[]{CbusConstants.CBUS_TON}, 0)) );

        assertEquals( 0, CbusEvent.getNumEventDataElements(new CanMessage(
            new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01}, 0)) );

        assertEquals( 1, CbusEvent.getNumEventDataElements(new CanMessage(
            new int[]{CbusConstants.CBUS_ASON1, 0x00, 0x00, 0x00, 0x01, 0x01}, 0)) );

        assertEquals( 2, CbusEvent.getNumEventDataElements(new CanMessage(
            new int[]{CbusConstants.CBUS_ASON2, 0x00, 0x00, 0x00, 0x01, 0x01, 0x02}, 0)) );

        assertEquals( 2, CbusEvent.getNumEventDataElements(new CanMessage(
            new int[]{CbusConstants.CBUS_ARSOF2, 0x00, 0x00, 0x00, 0x01, 0x01, 0x02}, 0)) );

        assertEquals( 3, CbusEvent.getNumEventDataElements(new CanMessage(
            new int[]{CbusConstants.CBUS_ASON3, 0x00, 0x00, 0x00, 0x01, 0x01, 0x02, 0x03}, 0)) );

    }

    @Test
    public void testSetDataFromCanFrame(){

        t.setDataFromFrame(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x01}, 0));
        assertEquals( 0, t.getNumElements() );

        t.setDataFromFrame(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF1, 0x00, 0x00, 0x00, 0x01, 0xff}, 0));
        assertEquals( 1, t.getNumElements() );
        assertEquals( 255, t.getData(1) );

        t.setDataFromFrame(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF2, 0x00, 0x00, 0x00, 0x01, 0xda, 0xaf}, 0));
        assertEquals( 2, t.getNumElements() );
        assertEquals( 0xda, t.getData(1) );
        assertEquals( 0xaf, t.getData(2) );

        t.setDataFromFrame(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF3, 0x00, 0x00, 0x00, 0x01, 0xda, 0xaf, 0xaa}, 0));
        assertEquals( 3, t.getNumElements() );
        assertEquals( 0xda, t.getData(1) );
        assertEquals( 0xaf, t.getData(2) );
        assertEquals( 0xaa, t.getData(3) );

    }

    private CbusEventDataElements t;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new CbusEventDataElements();
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventDataElementsTest.class);

}
