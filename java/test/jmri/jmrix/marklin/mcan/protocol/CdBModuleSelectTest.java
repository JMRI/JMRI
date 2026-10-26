package jmri.jmrix.marklin.mcan.protocol;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.CdBClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBModuleSelect}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBModuleSelectTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static final int CAN_CLASS = 0x1A;
    private static final int MODULE_ID = 5;

    private static CdBClassifiedFrame decode(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CDB_CONFIG, cf.protocol(), "Frame not classified as CDB_CONFIG");
        return CdBProtocolDecoder.decode(cf);
    }

    // --- buildSelect ---

    @Test
    public void testBuildSelectProduces13Bytes() {
        assertEquals(13, CdBModuleSelect.buildSelect(CAN_CLASS, MODULE_ID).length);
    }

    @Test
    public void testBuildSelectIsWriteFrame() {
        CdBClassifiedFrame cdf = decode(CdBModuleSelect.buildSelect(CAN_CLASS, MODULE_ID));
        assertEquals(CdBFrameType.WRITE, cdf.frameType());
    }

    @Test
    public void testBuildSelectHasDlc4() {
        int[] frame = CdBModuleSelect.buildSelect(CAN_CLASS, MODULE_ID);
        assertEquals(4, frame[4] & 0x0F);
    }

    @Test
    public void testBuildSelectHasSelectRegister() {
        int[] frame = CdBModuleSelect.buildSelect(CAN_CLASS, MODULE_ID);
        assertEquals(CdBModuleSelect.REG_SELECT, frame[6] & 0xFF);
    }

    @Test
    public void testBuildSelectEncodesCorrctClassAndModule() {
        CdBClassifiedFrame cdf = decode(CdBModuleSelect.buildSelect(CAN_CLASS, MODULE_ID));
        assertEquals(CAN_CLASS, cdf.canClass());
        assertEquals(MODULE_ID, cdf.moduleId());
    }

    // --- buildFactorySelect ---

    @Test
    public void testBuildFactorySelectProduces13Bytes() {
        assertEquals(13, CdBModuleSelect.buildFactorySelect(CAN_CLASS, 0xAB, 0xCD).length);
    }

    @Test
    public void testBuildFactorySelectIsWriteFrameToModuleZero() {
        CdBClassifiedFrame cdf = decode(CdBModuleSelect.buildFactorySelect(CAN_CLASS, 0xAB, 0xCD));
        assertEquals(CdBFrameType.WRITE, cdf.frameType());
        assertEquals(0, cdf.moduleId());
    }

    @Test
    public void testBuildFactorySelectHasDlc6() {
        int[] frame = CdBModuleSelect.buildFactorySelect(CAN_CLASS, 0xAB, 0xCD);
        assertEquals(6, frame[4] & 0x0F);
    }

    @Test
    public void testBuildFactorySelectEncodesChallengeBytesAtPositions7and8() {
        int[] frame = CdBModuleSelect.buildFactorySelect(CAN_CLASS, 0xAB, 0xCD);
        assertEquals(0xAB, frame[9]  & 0xFF);
        assertEquals(0xCD, frame[10] & 0xFF);
    }

    @Test
    public void testBuildFactorySelectHasSelectRegisterAtPosition6() {
        int[] frame = CdBModuleSelect.buildFactorySelect(CAN_CLASS, 0x11, 0x22);
        assertEquals(CdBModuleSelect.REG_SELECT, frame[6] & 0xFF);
    }

    // --- buildDeselect ---

    @Test
    public void testBuildDeselectProduces13Bytes() {
        assertEquals(13, CdBModuleSelect.buildDeselect(CAN_CLASS, MODULE_ID).length);
    }

    @Test
    public void testBuildDeselectIsWriteFrame() {
        CdBClassifiedFrame cdf = decode(CdBModuleSelect.buildDeselect(CAN_CLASS, MODULE_ID));
        assertEquals(CdBFrameType.WRITE, cdf.frameType());
    }

    @Test
    public void testBuildDeselectHasDlc2() {
        int[] frame = CdBModuleSelect.buildDeselect(CAN_CLASS, MODULE_ID);
        assertEquals(2, frame[4] & 0x0F);
    }

    @Test
    public void testBuildDeselectHasDeselectRegister() {
        int[] frame = CdBModuleSelect.buildDeselect(CAN_CLASS, MODULE_ID);
        assertEquals(CdBModuleSelect.REG_DESELECT, frame[6] & 0xFF);
    }

    // --- buildFactoryReset ---

    @Test
    public void testBuildFactoryResetProduces13Bytes() {
        assertEquals(13, CdBModuleSelect.buildFactoryReset(CAN_CLASS, MODULE_ID).length);
    }

    @Test
    public void testBuildFactoryResetIsWriteFrame() {
        CdBClassifiedFrame cdf = decode(CdBModuleSelect.buildFactoryReset(CAN_CLASS, MODULE_ID));
        assertEquals(CdBFrameType.WRITE, cdf.frameType());
    }

    @Test
    public void testBuildFactoryResetHasDlc2() {
        int[] frame = CdBModuleSelect.buildFactoryReset(CAN_CLASS, MODULE_ID);
        assertEquals(2, frame[4] & 0x0F);
    }

    @Test
    public void testBuildFactoryResetHasFactoryResetRegister() {
        int[] frame = CdBModuleSelect.buildFactoryReset(CAN_CLASS, MODULE_ID);
        assertEquals(CdBModuleSelect.REG_FACTORY_RESET, frame[6] & 0xFF);
    }
}
