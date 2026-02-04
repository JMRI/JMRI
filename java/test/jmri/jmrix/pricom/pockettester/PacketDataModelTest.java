package jmri.jmrix.pricom.pockettester;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the PacketDataModel class
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class PacketDataModelTest {

    @Test
    public void testCreate() {
        Assertions.assertNotNull( new PacketDataModel() );
    }

    @Test
    public void testGetPrefix() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("version", "PRICOM D", f.getPrefix(TestConstants.version));
        Assert.assertEquals("speed012A", "ADR= 012", f.getPrefix(TestConstants.speed012A));
    }

    @Test
    public void testGetKey() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("version", null, f.getKey(TestConstants.version));
        Assert.assertEquals("speed012A", "ADR= 012 CMD=Speed    ", f.getKey(TestConstants.speed012A));
    }

    @Test
    public void testGetType() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("speed012A", "CMD=Speed    ", f.getType(TestConstants.speed012A));
        Assert.assertEquals("acc0222A", "CMD=Accessry ", f.getType(TestConstants.acc0222A));
    }

    @Test
    public void testInsert() {
        PacketDataModel f = new PacketDataModel();
        Assert.assertEquals("no rows", 0, f.getRowCount());
        f.asciiFormattedMessage(TestConstants.speed012A);
        Assert.assertEquals("one row", 1, f.getRowCount());
        f.asciiFormattedMessage(TestConstants.speed0123A);
        Assert.assertEquals("two rows", 2, f.getRowCount());
        f.asciiFormattedMessage(TestConstants.speed012A);
        Assert.assertEquals("Still two rows", 2, f.getRowCount());
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
