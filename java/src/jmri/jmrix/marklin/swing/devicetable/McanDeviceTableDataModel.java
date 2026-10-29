package jmri.jmrix.marklin.swing.devicetable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import jmri.Manager;
import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.McanDeviceIdentity;
import jmri.jmrix.marklin.McanDeviceManager;
import jmri.jmrix.marklin.McanDeviceService;
import jmri.jmrix.marklin.WeichenChefMagnetGeneration;
import jmri.jmrix.marklin.WeichenChefMagnetService;
import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;
import jmri.util.ThreadingUtil;

/**
 * Table data model for displaying and monitoring MCAN bus devices in Swing UI.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanDeviceTableDataModel extends AbstractTableModel implements PropertyChangeListener, Manager.ManagerDataListener<McanDevice> {

    public static final int COLUMN_SYS_NAME = 0;
    public static final int COLUMN_USER_NAME = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_ID = 3;
    public static final int COLUMN_STATE = 4;
    public static final int COLUMN_INFO = 5;
    public static final int COLUMN_FIRST_SEEN = 6;
    public static final int COLUMN_LAST_SEEN = 7;
    public static final int NUM_COLUMNS = 8;

    private final McanDeviceManager manager;
    private final List<McanDevice> devices = new ArrayList<>();

    public McanDeviceTableDataModel(McanDeviceManager manager) {
        this.manager = manager;
        if (manager != null) {
            manager.addDataListener(this);
            updateDeviceList();
        }
    }

    private void updateDeviceList() {
        ThreadingUtil.runOnGUI(() -> {
            for (McanDevice d : devices) {
                d.removePropertyChangeListener(this);
            }
            devices.clear();
            if (manager != null) {
                for (McanDevice d : manager.getNamedBeanSet()) {
                    devices.add(d);
                    d.addPropertyChangeListener(this);
                }
            }
            fireTableDataChanged();
        });
    }

    @Override
    public void contentsChanged(Manager.ManagerDataEvent<McanDevice> e) {
        updateDeviceList();
    }

    @Override
    public void intervalAdded(Manager.ManagerDataEvent<McanDevice> e) {
        updateDeviceList();
    }

    @Override
    public void intervalRemoved(Manager.ManagerDataEvent<McanDevice> e) {
        updateDeviceList();
    }

    @Override
    public int getRowCount() {
        return devices.size();
    }

    @Override
    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COLUMN_SYS_NAME:
                return "System Name";
            case COLUMN_USER_NAME:
                return "User Name";
            case COLUMN_TYPE:
                return "Device Type";
            case COLUMN_ID:
                return "ID";
            case COLUMN_STATE:
                return "State";
            case COLUMN_INFO:
                return "Info";
            case COLUMN_FIRST_SEEN:
                return "First Found";
            case COLUMN_LAST_SEEN:
                return "Last Seen";
            default:
                return "";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= devices.size()) {
            return null;
        }
        McanDevice device = devices.get(rowIndex);
        switch (columnIndex) {
            case COLUMN_SYS_NAME:
                return device.getSystemName();
            case COLUMN_USER_NAME:
                return device.getUserName();
            case COLUMN_TYPE:
                return getDeviceTypeName(device);
            case COLUMN_ID:
                return getDeviceId(device);
            case COLUMN_STATE:
                return device.getDeviceState();
            case COLUMN_INFO:
                return getInfoText(device);
            case COLUMN_FIRST_SEEN:
                return device.getFirstSeen() != null ? device.getFirstSeen().toString() : "";
            case COLUMN_LAST_SEEN:
                return device.getLastSeen() != null ? device.getLastSeen().toString() : "";
            default:
                return null;
        }
    }

    private String getDeviceTypeName(McanDevice device) {
        McanDeviceIdentity id = device.getIdentity();
        if (id.isCdB()) {
            McanDeviceIdentity.CdB cdb = (McanDeviceIdentity.CdB) id;
            CdBDeviceClass cls = CdBDeviceClass.forClassId(cdb.getCanClass()).orElse(null);
            return cls != null ? "CdB " + cls.getDisplayName() : String.format("CdB Class 0x%02X", cdb.getCanClass());
        }
        return id.getDisplayName();
    }

    private String getDeviceId(McanDevice device) {
        McanDeviceIdentity id = device.getIdentity();
        if (id.isCdB()) {
            return "Module " + ((McanDeviceIdentity.CdB) id).getModuleId();
        }
        if (id.isNative()) {
            return String.format("UID 0x%08X", ((McanDeviceIdentity.Native) id).getUid());
        }
        return "";
    }

    private String getInfoText(McanDevice device) {
        McanDeviceIdentity id = device.getIdentity();
        if (id.isCdB()) {
            McanDeviceIdentity.CdB cdb = (McanDeviceIdentity.CdB) id;
            if (cdb.getCanClass() == 0x27) {
                long fp = device.getHwFingerprint();
                if (fp != -1L) {
                    return String.format("HW-ID: %08X", fp);
                }
                return "";
            }
            if (cdb.getCanClass() == WeichenChefMagnetService.CDB_CLASS_ID) {
                McanDeviceService svc = device.getDeviceService();
                if (svc instanceof WeichenChefMagnetService) {
                    WeichenChefMagnetGeneration gen = ((WeichenChefMagnetService) svc).getGeneration();
                    return "Gen: " + gen.name();
                }
                return "Gen: unknown";
            }
            return "";
        }
        int sw = device.getSoftwareVersion();
        return sw >= 0 ? String.format("v%d.%d", (sw >> 8) & 0xFF, sw & 0xFF) : "";
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        ThreadingUtil.runOnGUI(() -> {
            int row = devices.indexOf(evt.getSource());
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        });
    }

    public McanDevice getDeviceAt(int row) {
        if (row >= 0 && row < devices.size()) {
            return devices.get(row);
        }
        return null;
    }
}
