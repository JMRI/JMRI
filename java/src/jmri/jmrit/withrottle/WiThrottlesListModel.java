package jmri.jmrit.withrottle;

/**
 *
 * WiThrottle
 *
 * @author Brett Hoffman Copyright (C) 2009
 * @author Created by Brett Hoffman on:
 * @author 11/11/09.
 * @version $Revision$
 */
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiThrottlesListModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 473377515756069668L;

    ArrayList<DeviceServer> deviceList;
    //DeviceServer[] deviceList;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    WiThrottlesListModel(ArrayList<DeviceServer> deviceList) {

        this.deviceList = deviceList;

    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return deviceList.size();
    }

    public String getColumnName(int col) {
        String title;
        switch (col) {
            case 0: {
                title = rb.getString("LabelDeviceName");
                break;
            }
            case 1: {
                title = rb.getString("LabelAddress");
                break;
            }
            default: {
                title = "";
            }
        }
        return title;
    }

    public String getValueAt(int row, int col) {
        if (deviceList.size() < 1) {
            return null;
        }
        // some error checking
        if (row >= deviceList.size()) {
            log.debug("row is greater than device list size");
            return null;
        }
        if (col == 0) {
            return deviceList.get(row).getName();
        } else {
            return deviceList.get(row).getCurrentAddressString();
        }
    }

    public void updateDeviceList(ArrayList<DeviceServer> deviceList) {
        this.deviceList = deviceList;
        this.fireTableDataChanged();
    }

    private final static Logger log = LoggerFactory.getLogger(WiThrottlesListModel.class.getName());
}
