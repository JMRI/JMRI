package jmri.jmrix.maple;

import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the OutputBits class
 *
 * @author Dave Duchamp 2009
 */
public class OutputBitsTest {

    @Test
    public void testConstructor1() {
        Assert.assertNotNull("check ctor", new OutputBits(new SerialTrafficControlScaffold()));
    }

    @Test
    public void testAccessors() {
        OutputBits.setNumOutputBits(75);
        OutputBits.setSendDelay(250);
        Assert.assertEquals("check numOutputBits", 75, OutputBits.getNumOutputBits());
        Assert.assertEquals("check sendDelay", 250, OutputBits.getSendDelay());
    }

    @Test
    public void testWriteOutputBits1() {
        OutputBits.setNumOutputBits(48);
        obit.setOutputBit(2, false);
        obit.setOutputBit(1, false);
        obit.setOutputBit(23, false);
        obit.setOutputBit(41, false);
        obit.setOutputBit(31, false);
        obit.setOutputBit(2, true);
        obit.setOutputBit(19, false);
        obit.setOutputBit(5, false);
        obit.setOutputBit(26, false);
        obit.setOutputBit(48, false);

        AbstractMRMessage m = obit.createOutPacket(1, 48);

        Assert.assertEquals("packet size", 62, m.getNumDataElements());
        Assert.assertEquals("node address 1", '0', m.getElement(1));
        Assert.assertEquals("node address 2", '0', m.getElement(2));
        Assert.assertEquals("packet type 1", 'W', m.getElement(3));
        Assert.assertEquals("packet type 2", 'C', m.getElement(4));
        Assert.assertEquals("TO val 1 f", '1', (m.getElement(10 + 1) & 0xff));
        Assert.assertEquals("TO val 2 t", '0', (m.getElement(10 + 2) & 0xff));
        Assert.assertEquals("TO val 3 unknown", '0', (m.getElement(10 + 3) & 0xff));
    }

    private OutputBits obit;

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        SerialTrafficControlScaffold tc = new SerialTrafficControlScaffold();
        obit = new OutputBits(tc);
    }

    @After
    public void tearDown() {
        obit = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
