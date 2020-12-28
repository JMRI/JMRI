package jmri.jmrix.loconet.swing.lncvprog;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.ProgrammingTool;
import jmri.jmrix.loconet.LncvDevicesManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.uhlenbrock.LncvDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Table model for the programmed LNCV Modules table.
 * See Sv2f Programing tool
 *
 * @author Egbert Broerse Copyright (C) 2020
 */
public class LncvProgTableModel extends AbstractTableModel implements PropertyChangeListener, ProgrammingTool {

    public static final int ARTICLE_COLUMN = 1;
    public static final int MODADDR_COLUMN = 2;
    public static final int CV_COLUMN = 3;
    public static final int VALUE_COLUMN = 4;
    public static final int ROSTERENTRYCOLUMN = 5;
    public static final int OPENPRGMRBUTTONCOLUMN = 6;
    static public final int NUMCOLUMNS = 7;
    private final String[] columnNames = {"",
            Bundle.getMessage("HeadingArticle"),
            Bundle.getMessage("HeadingAddress"),
            Bundle.getMessage("HeadingCvLastRead"),
            Bundle.getMessage("HeadingValue"),
            "Roster Entry",
            ""};
    private final LncvProgPane parent;
    private final transient LocoNetSystemConnectionMemo memo;
    protected Roster _roster;
    protected LncvDevicesManager lncvdm;

    LncvProgTableModel(LncvProgPane parent, LocoNetSystemConnectionMemo memo) {
        this.parent = parent;
        this.memo = memo;
        log.debug("LNCV TABLE created, parent = {} null", (parent == null ? "" : "not"));
    }

    public void initTable(javax.swing.JTable lncvModulesTable) {
       TableColumnModel assignmentColumnModel = lncvModulesTable.getColumnModel();
       TableColumn idColumn = assignmentColumnModel.getColumn(0);
       idColumn.setMaxWidth(8);
       TableColumn articleColumn = assignmentColumnModel.getColumn(ARTICLE_COLUMN);
       articleColumn.setMinWidth(10);
       articleColumn.setMaxWidth(50);
       articleColumn.setResizable(true);
       TableColumn addressColumn = assignmentColumnModel.getColumn(MODADDR_COLUMN);
       addressColumn.setMinWidth(10);
       addressColumn.setMaxWidth(50);
       addressColumn.setResizable(true);
       TableColumn cvColumn = assignmentColumnModel.getColumn(CV_COLUMN);
       cvColumn.setMinWidth(10);
       cvColumn.setMaxWidth(50);
       cvColumn.setResizable(true);
       TableColumn valueColumn = assignmentColumnModel.getColumn(VALUE_COLUMN);
       valueColumn.setMinWidth(10);
       valueColumn.setMaxWidth(50);
       valueColumn.setResizable(true);
    }

   @Override
   public String getColumnName(int c) {
      return columnNames[c];
   }

   @Override
   public Class<?> getColumnClass(int c) {
       if (c == OPENPRGMRBUTTONCOLUMN) {
           return JButton.class;
       }
       return String.class;
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
      return Math.max(2, parent.getCount());
   }

   @Override
   public Object getValueAt(int r, int c) {
       LncvDevice dev = memo.getLncvDevicesManager().getDeviceList().getDevice(r);
      try {
          switch (c) {
              case ARTICLE_COLUMN:
                 if (parent.getModule(r) == null) {
                     log.debug("null module r={} c={}", r, c);
                     return "-";
                 }
                 return dev.getClassNum();
              case MODADDR_COLUMN:
                 return dev.getAddress();
              case CV_COLUMN:
                 return dev.getCvNum();
              case VALUE_COLUMN:
                 return dev.getCvValue();
              case ROSTERENTRYCOLUMN:
                  return dev.getRosterName();
//              case DEVICENAMECOLUMN:
//                  if (dev.getDeviceName().length() == 0) {
//                      log.trace("dev.getDevName() = {}",dev.getDeviceName());
//                      List<DecoderFile> l =
//                              InstanceManager.getDefault(
//                                      DecoderIndexFile.class).
//                                      matchingDecoderList(
//                                              null,
//                                              null,
//                                              null,
//                                              null,
//                                              null,
//                                              null,
//                                              String.valueOf(dev.getDeveloperID()),
//                                              String.valueOf(dev.getManufacturerID()),
//                                              String.valueOf(dev.getProductID())
//                                      );
//                      log.trace("found {} possible matches", l.size());
//                      String lastModelName="";
//                      if (l.size()>0) {
//                          for (DecoderFile d: l) {
//                              if (d.getModel().equals("")) {
//                                  log.debug("model is empty");
//                              }
//                              log.debug("possible match: {}",
//                                      d.titleString(), d.getModel(), d.getFamily());
//                              lastModelName=d.getModel();
//                          }
//                          dev.setDevName(lastModelName);
//                          dev.setDecoderFile(l.get(l.size()-1));
//                      }
//                      return lastModelName;
//                  }
//                  return dev.getDeviceName();
              case OPENPRGMRBUTTONCOLUMN:
                  if (dev.getRosterName().length() == 0) {
                      return "Create Roster Entry";
                  }
                  return Bundle.getMessage("ButtonProgram");
              default:
                 return r + 1;
          }
      } catch (NullPointerException npe) {
        log.debug("Caught NPE reading Module {}", r);
        return "";
      }
   }

    @Override
    public void setValueAt(Object value, int r, int c) {
        if (getRowCount() <= r) {
            // prevent update of a row that does not yet exist
            return;
        }
        LncvDevice dev = memo.getLncvDevicesManager().getDeviceList().getDevice(r);
        switch(c) {
            case ROSTERENTRYCOLUMN:
                dev.setRosterName((String) value);
                break;
            case OPENPRGMRBUTTONCOLUMN:
                if (((String)getValueAt(r, c)).compareTo("Create Roster Entry") == 0) { // I18N
                    createRosterEntry(dev);
                    if (dev.getRosterEntry() != null) {
                        setValueAt(dev.getRosterName(), r, c);
                    }
                } else {
                    openProgrammer(r);
                }
                // Gadget: copy parameters to Program screen
                parent.copyEntry((Integer) getValueAt(r, ARTICLE_COLUMN), (Integer) getValueAt(r, MODADDR_COLUMN));
                break;
        }
        if (getRowCount() >= 1) {
            this.fireTableRowsUpdated(r, r);
        }
    }

    private void openProgrammer(int row) {
        LncvDevice dev = memo.getLncvDevicesManager().getDeviceList().getDevice(row);

        LncvDevicesManager.ProgrammingResult result = lncvdm.prepareForSymbolicProgrammer(dev, this);
        switch (result) {
            case SUCCESS_PROGRAMMER_OPENED:
                return;
            case FAIL_NO_SUCH_DEVICE:
                JOptionPane.showMessageDialog(parent,
                        "Device no found on LocoNet. Re-try the &quot;SV2 "
                                + "Device Discovery&quot' process and try again. "
                                + "Cannot open the programmer!",
                        "Open Roster Entry", 0);
                return;
            case FAIL_NO_APPROPRIATE_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        "No suitable programmer available for this LocoNet connection."
                                + " Cannot open the programmer!", "Open Roster Entry", 0);
                return;
            case FAIL_NO_MATCHING_ROSTER_ENTRY:
                JOptionPane.showMessageDialog(parent,
                        "There does not appear to be a roster entry for this "
                                + "device.  Cannot open the programmer!", "Open Roster Entry", 0);
                return;
            case FAIL_DESTINATION_ADDRESS_IS_ZERO:
                JOptionPane.showMessageDialog(parent,
                        "Device is at address 0.  Re-configure device address to a non-zero"
                                + "value before programming! "
                                + "Canceling operation!", "Open Roster Entry", 0);
                return;
            case FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS:
                JOptionPane.showMessageDialog(parent,
                        "Should not program as there are multiple devices with device"
                                + " address "+dev.getDestAddr()+" present on LocoNet. "
                                + "Canceling operation!", "Open Roster Entry", 0);
                return;
            case FAIL_NO_ADDRESSED_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        "No addressed programmer available for this LocoNet connection."
                                + " Cannot open the programmer!", "Open Roster Entry", 0);
                return;
            case FAIL_NO_LNCV_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        "LNSV2 programming mode is not available on this connection."
                                + " Cannot open the programmer!", "Open Roster Entry", 0);
                return;
            default:
                JOptionPane.showMessageDialog(parent,
                        "Unknown error occured.  Cannot open programmer."
                                + " Cannot open the programmer!", "Open Roster Entry", 0);
                return;

        }
    }

    public void openPaneOpsProgFrame(RosterEntry re, String name,
                                     String programmerFile, Programmer p) {
        // would be better if this was a new task on the GUI thread...
        log.warn("attempting to open programmer, re={}, name={}, prorammerFile={}, programmer={}",
                re, name, programmerFile, p);

        DecoderFile decoderFile = InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle(re.getDecoderModel());

        PaneOpsProgFrame progFrame =
                new PaneOpsProgFrame(decoderFile, re, name, programmerFile, p);

        progFrame.pack();
        progFrame.setVisible(true);
    }

    private void createRosterEntry(LncvDevice dev) {
        if (dev.getDestAddr() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "Cannot create a roster entry when the destination address"
                            + " is 0.  Canceling operation."
                    , "Create Roster Entry", 0);
        } else {
            String s = null;
            while (s == null) {
                s = JOptionPane.showInputDialog(parent,
                        "Enter a name for the roster entry", "");
                if (s == null) {
                    // cancel button hit
                    return;
                }
            }

            log.warn("got here");
            RosterEntry re = new RosterEntry(dev.getDecoderFile().getFileName());
            re.setDccAddress(Integer.toString(dev.getDestAddr()));
            re.setDecoderModel(dev.getDecoderFile().getModel());
            re.setMfg(dev.getDecoderFile().getMfgID());
            re.setProductID(Integer.toString(dev.getProductID()));
            re.setId(s);
            _roster.addEntry(re);
        }
    }

    /*
     * Process the "property change" events from LncvDevicesManager.
     *
     * @param evt event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // these messages can arrive without a complete
        // GUI, in which case we just ignore them
        log.debug("property change received event name{} old{} new{}",
                evt.getPropertyName(),evt.getOldValue(), evt.getNewValue());
        String eventName = evt.getPropertyName();
        log.debug("Property change seen {}", eventName);
        /* always use fireTableDataChanged() because it does not always
            resize columns to "preferred" widths!
            This may slow things down, but that is a small price to pay!
        */
        fireTableDataChanged();
    }

    public void dispose() {
        if ((memo != null) && (memo.getLncvDevicesManager() != null)) {
            memo.getLncvDevicesManager().removePropertyChangeListener(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LncvProgTableModel.class);

}
