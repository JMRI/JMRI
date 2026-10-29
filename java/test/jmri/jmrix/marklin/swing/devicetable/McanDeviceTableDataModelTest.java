package jmri.jmrix.marklin.swing.devicetable;

import jmri.jmrix.marklin.*;
import jmri.jmrix.marklin.mcan.protocol.NativeDeviceInfo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link McanDeviceTableDataModel}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class McanDeviceTableDataModelTest {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;
    private McanDeviceManager manager;
    private McanDeviceTableDataModel model;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
        manager = memo.getMcanDeviceManager();
        model = new McanDeviceTableDataModel(manager);
    }

    @AfterEach
    void tearDown() {
        tc.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    @Test
    void testDataModelColumnsAndRows() {
        assertEquals(McanDeviceTableDataModel.NUM_COLUMNS, model.getColumnCount());
        assertEquals("System Name", model.getColumnName(McanDeviceTableDataModel.COLUMN_SYS_NAME));

        McanDeviceIdentity.Native identity = McanDeviceIdentity.forNative(0x00112233L, NativeDeviceInfo.TYPE_MS2);
        McanDevice device = manager.provideDevice(identity);

        JUnitUtil.waitFor(() -> model.getRowCount() == 1, "Model row count updated");

        assertEquals(1, model.getRowCount());
        assertEquals("M:dev:uid-00112233", model.getValueAt(0, McanDeviceTableDataModel.COLUMN_SYS_NAME));
        assertEquals(McanDeviceState.DISCOVERED, model.getValueAt(0, McanDeviceTableDataModel.COLUMN_STATE));
        assertSame(device, model.getDeviceAt(0));
    }
}
