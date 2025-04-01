package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.ProgrammingTool;
import jmri.jmrix.loconet.Lnsv1DevicesManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.lnsvf1.Lnsv1Device;
import jmri.util.swing.JmriJOptionPane;

import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Table model for the programmed LNSV1 Modules table.
 * See Svf1 Programing tool
 *
 * @author Egbert Broerse Copyright (C) 2020, 2025
 */
public class Lnsv1ProgTableModel extends AbstractTableModel implements PropertyChangeListener, ProgrammingTool {

    public static final int COUNT_COLUMN = 0;
    public static final int MODADDR_COLUMN = 1;
    public static final int MODADDRSPLIT_COLUMN = 2;
    public static final int VERSION_COLUMN = 3;
    public static final int CV_COLUMN = 4;
    public static final int VALUE_COLUMN = 5;
    public static final int ROSTERENTRY_COLUMN = 6;
    public static final int ROSTERSV1MODE_COLUMN = 7;
    public static final int ROSTERNAME_COLUMN = 8;
    public static final int OPENPRGMRBUTTON_COLUMN = 9;
    static public final int NUMCOLUMNS = 10;
    private final Lnsv1ProgPane parent;
    private final transient LocoNetSystemConnectionMemo memo;
    protected Roster _roster;
    protected Lnsv1DevicesManager lnsv1dm;

    Lnsv1ProgTableModel(Lnsv1ProgPane parent, @Nonnull LocoNetSystemConnectionMemo memo) {
        this.parent = parent;
        this.memo = memo;
        lnsv1dm = memo.getLnsv1DevicesManager();
        _roster = Roster.getDefault();
        lnsv1dm.addPropertyChangeListener(this);
    }

    public void initTable(javax.swing.JTable lnsv1ModulesTable) {
       TableColumnModel assignmentColumnModel = lnsv1ModulesTable.getColumnModel();
       TableColumn idColumn = assignmentColumnModel.getColumn(0);
       idColumn.setMaxWidth(3);
    }

    @Override
    public String getColumnName(int c) {
        switch (c) {
            case MODADDR_COLUMN:
                return Bundle.getMessage("HeadingDccAddress");
            case MODADDRSPLIT_COLUMN:
                return Bundle.getMessage("HeadingAddressSplit");
            case VERSION_COLUMN:
                return Bundle.getMessage("HeadingVersion");
            case CV_COLUMN:
                return Bundle.getMessage("HeadingCvLastRead");
            case VALUE_COLUMN:
                return Bundle.getMessage("HeadingValue");
            case ROSTERENTRY_COLUMN:
                return Bundle.getMessage("HeadingDeviceId");
            case ROSTERNAME_COLUMN:
                return Bundle.getMessage("HeadingDeviceModel");
            case ROSTERSV1MODE_COLUMN:
                return Bundle.getMessage("HeadingIsSv1");
            case OPENPRGMRBUTTON_COLUMN:
                return Bundle.getMessage("ButtonProgram");
            case COUNT_COLUMN:
            default:
                return "#";
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        switch (c) {
            case MODADDR_COLUMN:
            case COUNT_COLUMN:
            case VERSION_COLUMN:
            case CV_COLUMN:
            case VALUE_COLUMN:
                return Integer.class;
            case OPENPRGMRBUTTON_COLUMN:
                return javax.swing.JButton.class;
            case ROSTERSV1MODE_COLUMN:
                return Boolean.class;
            case MODADDRSPLIT_COLUMN:
            case ROSTERNAME_COLUMN:
            case ROSTERENTRY_COLUMN:
            default:
                return String.class;
       }
   }

    @Override
    public boolean isCellEditable(int r, int c) {
       return (c == OPENPRGMRBUTTON_COLUMN);
   }

    @Override
    public int getColumnCount() {
      return NUMCOLUMNS;
   }

    @Override
    public int getRowCount() {
        if (lnsv1dm == null) {
            return 0;
        } else {
            return lnsv1dm.getDeviceCount();
        }
    }

    @Override
    public Object getValueAt(int r, int c) {
        Lnsv1Device dev = memo.getLnsv1DevicesManager().getDeviceList().getDevice(r);
        try {
            switch (c) {
                case MODADDR_COLUMN:
                    assert dev != null;
                    return dev.getDestAddr();
                case MODADDRSPLIT_COLUMN:
                    assert dev != null;
                    return dev.getDestAddrLow() + "/" + dev.getDestAddrHigh();
                case VERSION_COLUMN:
                    assert dev != null;
                    return dev.getSwVersion();
                case CV_COLUMN:
                    assert dev != null;
                    return dev.getCvNum();
                case VALUE_COLUMN:
                    assert dev != null;
                    return dev.getCvValue();
                case ROSTERENTRY_COLUMN:
                    assert dev != null;
                    return dev.getRosterEntry().getId();
                case ROSTERSV1MODE_COLUMN:
                    boolean isLnsv1 = false;
                    if (dev != null && dev.getDecoderFile() != null) {
                        isLnsv1 = dev.getDecoderFile().isProgrammingMode("LOCONETSV1MODE");
                        // can't access LnProgrammerManager.LOCONETSV1MODE constant
                    }
                    return isLnsv1;
                case ROSTERNAME_COLUMN:
                    assert dev != null;
                    if (dev.getRosterEntry() != null) {
                        return dev.getRosterEntry().getDecoderModel();
                    } else {
                        return "";
                    }
                case OPENPRGMRBUTTON_COLUMN:
                    if (dev != null && !dev.getRosterName().isEmpty()) {
                        if (dev.getDecoderFile().isProgrammingMode("LOCONETSV1MODE")) {
                            return Bundle.getMessage("ButtonProgram");
                        } else {
                            return Bundle.getMessage("ButtonWrongMode");
                        }
                    }
                    return Bundle.getMessage("ButtonNoMatchInRoster");
                default: // column 0
                    return r + 1;
            }
        } catch (NullPointerException npe) {
            log.warn("No match for Module {}, c{}", r, c);
            return "";
        }
    }

    @Override
    public void setValueAt(Object value, int r, int c) {
        if (getRowCount() < r + 1) {
            // prevent update of a row that does not (yet) exist
            return;
        }
        if (c == OPENPRGMRBUTTON_COLUMN) {
            if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonProgram")) == 0) {
                openProgrammer(r);
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonWrongMode")) == 0) {
                infoNotForLnsv1(getValueAt(r, 1).toString()); // TODO once we check for LNSV1 progMode this can be removed
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonNoMatchInRoster")) == 0){
                // no match, info add roster entry
                infoNoMatch(getValueAt(r, 1).toString());
            }
        } else {
            // no change, so do not fire a property change event
            return;
        }
        if (getRowCount() >= 1) {
            this.fireTableRowsUpdated(r, r);
        }
    }

    private void openProgrammer(int r) {
        Lnsv1Device dev = memo.getLnsv1DevicesManager().getDeviceList().getDevice(r);

        Lnsv1DevicesManager.ProgrammingResult result = lnsv1dm.prepareForSymbolicProgrammer(dev, this);
        switch (result) {
            case SUCCESS_PROGRAMMER_OPENED:
                return;
            case FAIL_NO_SUCH_DEVICE:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_SUCH_DEVICE"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_APPROPRIATE_PROGRAMMER:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_APPROPRIATE_PROGRAMMER"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_MATCHING_ROSTER_ENTRY:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_MATCHING_ROSTER_ENTRY"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_DESTINATION_ADDRESS_IS_ZERO:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_DESTINATION_ADDRESS_IS_ZERO"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS", dev.getDestAddr()),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_ADDRESSED_PROGRAMMER:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_ADDRESSED_PROGRAMMER"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_LNSV1_PROGRAMMER:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_LNSV1_PROGRAMMER"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            default:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_UNKNOWN"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openPaneOpsProgFrame(RosterEntry re, String name,
                                     String programmerFile, Programmer p) {
        // would be better if this was a new task on the GUI thread...
        log.debug("attempting to open programmer, re={}, name={}, programmerFile={}, programmer={}",
                re, name, programmerFile, p);

        DecoderFile decoderFile = InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle(re.getDecoderModel());

        PaneOpsProgFrame progFrame =
                new PaneOpsProgFrame(decoderFile, re, name, programmerFile, p);

        progFrame.pack();
        progFrame.setVisible(true);
    }

    /**
     * Show dialog to instruct to add a roster entry supporting LNSV1 prog mode.
     */
    private void infoNoMatch(String address) {
        //log.debug("address = {}", address);
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonOK")};
        JmriJOptionPane.showOptionDialog(parent,
                Bundle.getMessage("DialogInfoNoRosterMatch", address), // not displaying addr?
                Bundle.getMessage("TitleOpenRosterEntry"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.INFORMATION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[0]);
    }

    /**
     * Show dialog to inform that address matched decoder doesn't support LNSV1 mode.
     */
    private void infoNotForLnsv1(String address) {
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonOK")};
        JmriJOptionPane.showOptionDialog(parent,
                Bundle.getMessage("DialogInfoMatchNotX", address, "LNSV1"),
                Bundle.getMessage("TitleOpenRosterEntry"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.INFORMATION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[0]);
    }

    /*
     * Process the "property change" events from Lnsv1DevicesManager.
     *
     * @param evt event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // these messages can arrive without a complete
        // GUI, in which case we just ignore them
        //String eventName = evt.getPropertyName();
        /* always use fireTableDataChanged() because it does not always
            resize columns to "preferred" widths!
            This may slow things down, but that is a small price to pay!
        */
        fireTableDataChanged();
    }

    public void dispose() {
        if ((memo != null) && (memo.getLnsv1DevicesManager() != null)) {
            memo.getLnsv1DevicesManager().removePropertyChangeListener(this);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Lnsv1ProgTableModel.class);

}
