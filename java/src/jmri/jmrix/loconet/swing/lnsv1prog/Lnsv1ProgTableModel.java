package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.ProgrammingTool;
import jmri.jmrix.loconet.LnSv1DevicesManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.lnsvf1.LnSv1Device;
import jmri.util.swing.JmriJOptionPane;

import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Table model for the programmed LNSV1 Modules table.
 * See Svf1 Programing tool
 *
 * @author Egbert Broerse Copyright (C) 2020, 2025
 */
public class Lnsv1ProgTableModel extends AbstractTableModel implements PropertyChangeListener, ProgrammingTool {

    public static final int COUNT_COLUMN = 0;
    public static final int MODADDRL_COLUMN = 1;
    public static final int MODADDRH_COLUMN = 2;
    public static final int VERSION_COLUMN = 3;
    public static final int CV_COLUMN = 4;
    public static final int VALUE_COLUMN = 5;
    public static final int DEVICENAMECOLUMN = 6;
    public static final int ROSTERENTRYCOLUMN = 7;
    public static final int OPENPRGMRBUTTONCOLUMN = 8;
    static public final int NUMCOLUMNS = 9;
    private final Lnsv1ProgPane parent;
    private final transient LocoNetSystemConnectionMemo memo;
    protected Roster _roster;
    protected LnSv1DevicesManager lnsv1dm;

    Lnsv1ProgTableModel(Lnsv1ProgPane parent, @Nonnull LocoNetSystemConnectionMemo memo) {
        this.parent = parent;
        this.memo = memo;
        lnsv1dm = memo.getLnSv1DevicesManager();
        _roster = Roster.getDefault();
        lnsv1dm.addPropertyChangeListener(this);
    }

    public void initTable(javax.swing.JTable lncvModulesTable) {
       TableColumnModel assignmentColumnModel = lncvModulesTable.getColumnModel();
       TableColumn idColumn = assignmentColumnModel.getColumn(0);
       idColumn.setMaxWidth(8);
    }

   @Override
   public String getColumnName(int c) {
       switch (c) {
           case MODADDRL_COLUMN:
               return Bundle.getMessage("HeadingAddressLow");
           case MODADDRH_COLUMN:
               return Bundle.getMessage("HeadingAddressHigh");
           case VERSION_COLUMN:
               return Bundle.getMessage("HeadingVersion");
           case CV_COLUMN:
               return Bundle.getMessage("HeadingCvLastRead");
           case VALUE_COLUMN:
               return Bundle.getMessage("HeadingValue");
           case DEVICENAMECOLUMN:
               return Bundle.getMessage("HeadingDeviceModel");
           case ROSTERENTRYCOLUMN:
               return Bundle.getMessage("HeadingDeviceId");
           case OPENPRGMRBUTTONCOLUMN:
               return Bundle.getMessage("ButtonProgram");
           case COUNT_COLUMN:
           default:
               return "#";
       }
   }

   @Override
   public Class<?> getColumnClass(int c) {
       switch (c) {
           case COUNT_COLUMN:
           case VERSION_COLUMN:
           case MODADDRL_COLUMN:
           case MODADDRH_COLUMN:
           case CV_COLUMN:
           case VALUE_COLUMN:
               return Integer.class;
           case OPENPRGMRBUTTONCOLUMN:
               return javax.swing.JButton.class;
           case DEVICENAMECOLUMN:
           case ROSTERENTRYCOLUMN:
           default:
               return String.class;
       }
   }

   @Override
   public boolean isCellEditable(int r, int c) {
       return (c == OPENPRGMRBUTTONCOLUMN);
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
       LnSv1Device dev = memo.getLnSv1DevicesManager().getDeviceList().getDevice(r);
       try {
          switch (c) {
              case VERSION_COLUMN:
                  assert dev != null;
                  return dev.getSwVersion();
              case MODADDRH_COLUMN:
                  assert dev != null;
                  return dev.getDestAddrLow();
              case MODADDRL_COLUMN:
                  assert dev != null;
                  return dev.getDestAddrHigh();
              case CV_COLUMN:
                  assert dev != null;
                  return dev.getCvNum();
              case VALUE_COLUMN:
                  assert dev != null;
                  return dev.getCvValue();
              case DEVICENAMECOLUMN:
                  assert dev != null;
                  if (dev.getDeviceName().isEmpty()) { // not yet filled in, look for a candidate
                      List<DecoderFile> l =
                          InstanceManager.getDefault(
                              DecoderIndexFile.class).
                              matchingDecoderList(
                                      null,
                                      null,
                                      null,
                                      null,
                                      String.valueOf(dev.getSwVersion()), // a bit risky to check just 1 value
                                      null,
                                      null,
                                      null,
                                      null
                              );
                      //log.debug("found {} possible decoder matches for LNCV device", l.size());
                      String lastModelName = "";
                      if (!l.isEmpty()) {
                          for (DecoderFile d : l) {
                              // we do not check for LNSV1 programmingMode support since we do not expect replies from non-LNSV1 devices
                              // (and there is currently no access to supported modes in the DecoderIndexFile)
                              if (d.getModel().isEmpty()) {
                                  log.warn("Empty model(name) in decoderfile {}", d.getFileName());
                                  continue;
                              }
                              lastModelName = d.getModel();
                          }
                          dev.setDevName(lastModelName);
                          dev.setDecoderFile(l.get(l.size() - 1));
                      }
                      return lastModelName;
                  }
                  return dev.getDeviceName();
              case ROSTERENTRYCOLUMN:
                  assert dev != null;
                  return dev.getRosterName();
              case OPENPRGMRBUTTONCOLUMN:
                  assert dev != null;
                  if (!dev.getDeviceName().isEmpty()) {
                      if ((dev.getRosterName() != null) && (dev.getRosterName().isEmpty())) {
                          return Bundle.getMessage("ButtonCreateEntry");
                      }
                      return Bundle.getMessage("ButtonProgram");
                  }
                  return Bundle.getMessage("ButtonNoMatchInRoster");
              default: // column 1
                 return r + 1;
          }
      } catch (NullPointerException npe) {
        log.warn("Caught NPE reading Module {}", r);
        return "";
      }
   }

    @Override
    public void setValueAt(Object value, int r, int c) {
        if (getRowCount() < r + 1) {
            // prevent update of a row that does not (yet) exist
            return;
        }
        LnSv1Device dev = memo.getLnSv1DevicesManager().getDeviceList().getDevice(r);
        if (c == OPENPRGMRBUTTONCOLUMN) {
            if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonCreateEntry")) == 0) {
                //createRosterEntry(dev); // Too risky!
                if (dev.getRosterEntry() != null) {
                    setValueAt(dev.getRosterName(), r, c);
                } else {
                    log.warn("Failed to connect RosterEntry to device {}", dev.getRosterName());
                }
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonProgram")) == 0) {
                openProgrammer(r);
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonNoMatchInRoster")) == 0){
                // need to rebuild decoderIndex, tooltip?
                warnRecreate();
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
        LnSv1Device dev = memo.getLnSv1DevicesManager().getDeviceList().getDevice(r);

        LnSv1DevicesManager.ProgrammingResult result = lnsv1dm.prepareForSymbolicProgrammer(dev, this);
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
            case FAIL_NO_LNCV_PROGRAMMER:
                JmriJOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_LNCV_PROGRAMMER"),
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

//    private void createRosterEntry(LnSv1Device dev) {
//        if (dev.getDestAddrLow() == 0) {
//            JmriJOptionPane.showMessageDialog(parent,
//                    Bundle.getMessage("FAIL_ADD_ENTRY_0"),
//                    Bundle.getMessage("ButtonCreateEntry"), JmriJOptionPane.ERROR_MESSAGE);
//        } else {
//            String s = null;
//            while (s == null) {
//                s = JmriJOptionPane.showInputDialog(parent,
//                        Bundle.getMessage("DialogEnterEntryName"),
//                        Bundle.getMessage("EnterEntryNameTitle"),JmriJOptionPane.QUESTION_MESSAGE);
//                if (s == null) {
//                    // Cancel button hit
//                    return;
//                }
//            }
//
//            RosterEntry re = new RosterEntry(dev.getDecoderFile().getFileName());
//            re.setDccAddress(Integer.toString(dev.getDestAddr()));
//            re.setDecoderModel(dev.getDecoderFile().getModel());
//            re.setVersion(Integer.toString(dev.getSwVersion()));
//            re.setId(s);
//            _roster.addEntry(re);
//            dev.setRosterEntry(re);
//        }
//    }

    private void warnRecreate() {
        // show dialog to inform and allow rebuilding index
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonRecreateIndex"),
                Bundle.getMessage("ButtonCancel")};
        int userReply = JmriJOptionPane.showOptionDialog(parent,
                Bundle.getMessage("DialogWarnRecreate"),
                Bundle.getMessage("TitleOpenRosterEntry"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.QUESTION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[0]);
        if (userReply == 0) { // array position 0
            DecoderIndexFile.forceCreationOfNewIndex(false); // faster
        }
    }

    /*
     * Process the "property change" events from LnSv1DevicesManager.
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
        if ((memo != null) && (memo.getLnSv1DevicesManager() != null)) {
            memo.getLnSv1DevicesManager().removePropertyChangeListener(this);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Lnsv1ProgTableModel.class);

}
