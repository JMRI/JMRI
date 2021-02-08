package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Egbert Broerse Copyright (C) 2021
 */
public class LncvMessageContentsTest {

    @Test
    public void testCTorIllegalArgument() {
        final LocoNetMessage lm = new LocoNetMessage(3); // LncvMessage length should be 15
        Assert.assertThrows(IllegalArgumentException.class, () -> new LncvMessageContents(lm));
        final LocoNetMessage lm2 = new LocoNetMessage(new int[] {0xD0, 0x01, 0x20, 0x08, 0x20, 0x26});
        Assert.assertThrows(IllegalArgumentException.class, () -> new LncvMessageContents(lm2));
        
        LocoNetMessage l = new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x01, 0x00, 0x69, 0x03, 0x00, 0x4D});
        LncvMessageContents lncvm = new LncvMessageContents(l);
        Assert.assertTrue("check supported command", lncvm.isSupportedLncvCommand());
        Assert.assertTrue("check isSupportedLncvReadReply", lncvm.isSupportedLncvReadReply());
        Assert.assertEquals("check mod num not in reply", -1, lncvm.getLncvModuleNum());
        Assert.assertEquals("check (art) prod ID", 5033, lncvm.getLncvArticleNum());
        Assert.assertThrows(IllegalArgumentException.class, () ->new LncvMessageContents(new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x47, 0x01, 0x10, 0x3D, 0x01, 0x0D, 0x01, 0x10, 0x0B, 0x00, 0x00, 0x00, 0x75})));
        Assert.assertThrows(IllegalArgumentException.class, () ->new LncvMessageContents(new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x47, 0x02, 0x00, 0x3D, 0x01, 0x0D, 0x01, 0x10, 0x0B, 0x00, 0x00, 0x00, 0x75})));
     }
     
    @Test
    public void testIsLnMessageASpecificLncvCommand() {
        LocoNetMessage l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x47, 0x02, 0x10, 0x3D, 0x01, 0x0D, 0x01, 0x10, 0x0B, 0x00, 0x00, 0x00, 0x75}); // 16 bits
        Assert.assertFalse(LncvMessageContents.isLnMessageASpecificLncvCommand(l, LncvMessageContents.LncvCommand.LNCV_READ_REPLY));

        l = new LocoNetMessage(new int[]{0xED, 0x0F, 0x01, 0x05, 0x00, 0x21, 0x27, 0x29, 0x13, 0x00, 0x00, 0x13, 0x29, 0x00, 0x1F});
        LncvMessageContents lncvm = new LncvMessageContents(l);
        Assert.assertFalse("check isSupportedLncvReadReply", lncvm.isSupportedLncvReadReply());
    }

    @Test
    public void testGetModuleNum() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xED, 0x0F, 0x01, 0x05, 0x00, 0x21, 0x01, 0x29, 0x13, 0x00, 0x00, 0x01, 0x00, 0x00, 0x02});
        for (int i = 0; i < 07f; ++i) {
            l.setElement(11, i);
            Assert.assertEquals(""+i+" as LSByte of destAddr", i, new LncvMessageContents(l).getLncvModuleNum());
        }
        l.setElement(11,0);
        l.setElement(6,0x51); // hibit
        Assert.assertEquals("128 as LSByte of destAddr", 128, new LncvMessageContents(l).getLncvModuleNum());
        l.setElement(11,0);
        l.setElement(6,0x01); // hibit
        for (int i = 0; i < 0x7f; ++i) {
            l.setElement(12, i);
            Assert.assertEquals(""+(i<<8)+" as MSByte of destAddr", i<<8, new LncvMessageContents(l).getLncvModuleNum());
        }
        l.setElement(11,0);
        l.setElement(12,0);
        l.setElement(6,0x61); // hibits
        Assert.assertEquals("32768 as MSByte of destAddr", 32768, new LncvMessageContents(l).getLncvModuleNum());
    }

    @Test
    public void testGetProdId() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xED, 0x0F, 0x01, 0x05, 0x00, 0x21, 0x20, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1F});
        for (int i = 0; i < 0x7f; ++i) {
            l.setElement(7, i);
            Assert.assertEquals(""+i+" as LSByte of ProductId", i, new LncvMessageContents(l).getLncvArticleNum());
        }
        l.setElement(7,0);
        l.setElement(6,0x1); // hibits
        Assert.assertEquals("128 as LSByte of ProductId", 128, new LncvMessageContents(l).getLncvArticleNum());
        l.setElement(6,0x20);
        for (int i = 0; i < 0x7f; ++i) {
            l.setElement(8, i);
            Assert.assertEquals(""+(i<<8)+" as MSByte of ProductId", i<<8, new LncvMessageContents(l).getLncvArticleNum());
        }
        l.setElement(7,0);
        l.setElement(8,0);
        l.setElement(6,0x12); // hibits
        Assert.assertEquals("32768 as MSByte of ProductId", 32768, new LncvMessageContents(l).getLncvArticleNum());
    }
    
    @Test
    public void testExtractMessageType() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE5, 0x10, 0x01, 0x47, 0x02, 0x10, 0x3D, 0x01, 0x0D, 0x01, 0x10, 0x0B, 0x00, 0x00, 0x00, 0x75});
        Assert.assertNull("check extract of cmd not lncv", LncvMessageContents.extractMessageType(l));

        l = new LocoNetMessage(new int[]{0xED, 0x0F, 0x01, 0x05, 0x00, 0x21, 0x27, 0x29, 0x13, 0x00, 0x00, 0x13, 0x29, 0x00, 0x1F});
        Assert.assertEquals("check extract of cmd read is ok", LncvMessageContents.LncvCommand.LNCV_READ, LncvMessageContents.extractMessageType(l));

        l = new LocoNetMessage(new int[]{0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x01, 0x29, 0x13, 0x08, 0x00, 0x62, 0x00, 0x00, 0x5C});
        Assert.assertEquals("check extract of cmd readreply is ok", LncvMessageContents.LncvCommand.LNCV_READ_REPLY, LncvMessageContents.extractMessageType(l));

        l = new LocoNetMessage(new int[]{0xE5, 0x0F, 0x01, 0x05, 0x00, 0x21, 0x01, 0x29, 0x13, 0x00, 0x00, 0x01, 0x00, 0x40, 0x4A});
        Assert.assertEquals("check extract of cmd stopmodprog is ok", LncvMessageContents.LncvCommand.LNCV_PROG_END, LncvMessageContents.extractMessageType(l));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LncvMessageContentsTest.class);

}
