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

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Table model for the programmed LNCV Modules table.
 * See Sv2f Programing tool
 *
 * @author Egbert Broerse Copyright (C) 2020
 */
public class LncvProgTableModel extends AbstractTableModel implements PropertyChangeListener, ProgrammingTool {

    public static final int COUNT_COLUMN = 0;
    public static final int ARTICLE_COLUMN = 1;
    public static final int MODADDR_COLUMN = 2;
    public static final int CV_COLUMN = 3;
    public static final int VALUE_COLUMN = 4;
    public static final int DEVICENAMECOLUMN = 5;
    public static final int ROSTERENTRYCOLUMN = 6;
    public static final int OPENPRGMRBUTTONCOLUMN = 7;
    static public final int NUMCOLUMNS = 8;
    private final LncvProgPane parent;
    private final transient LocoNetSystemConnectionMemo memo;
    protected Roster _roster;
    protected LncvDevicesManager lncvdm;

    LncvProgTableModel(LncvProgPane parent, @Nonnull LocoNetSystemConnectionMemo memo) {
        this.parent = parent;
        this.memo = memo;
        lncvdm = memo.getLncvDevicesManager();
        _roster = Roster.getDefault();
        lncvdm.addPropertyChangeListener(this);
    }

//    public void initTable(javax.swing.JTable lncvModulesTable) {
//       TableColumnModel assignmentColumnModel = lncvModulesTable.getColumnModel();
//       TableColumn idColumn = assignmentColumnModel.getColumn(0);
//       idColumn.setMaxWidth(8);
//       TableColumn articleColumn = assignmentColumnModel.getColumn(ARTICLE_COLUMN);
//       articleColumn.setMinWidth(10);
//       articleColumn.setMaxWidth(50);
//       articleColumn.setResizable(true);
//       TableColumn addressColumn = assignmentColumnModel.getColumn(MODADDR_COLUMN);
//       addressColumn.setMinWidth(10);
//       addressColumn.setMaxWidth(50);
//       addressColumn.setResizable(true);
//       TableColumn cvColumn = assignmentColumnModel.getColumn(CV_COLUMN);
//       cvColumn.setMinWidth(10);
//       cvColumn.setMaxWidth(50);
//       cvColumn.setResizable(true);
//       TableColumn valueColumn = assignmentColumnModel.getColumn(VALUE_COLUMN);
//       valueColumn.setMinWidth(10);
//       valueColumn.setMaxWidth(50);
//       valueColumn.setResizable(true);
//    }

   @Override
   public String getColumnName(int c) {
       switch (c) {
           case ARTICLE_COLUMN:
               return Bundle.getMessage("HeadingArticle");
           case MODADDR_COLUMN:
               return Bundle.getMessage("HeadingAddress");
           case CV_COLUMN:
               return Bundle.getMessage("HeadingCvLastRead");
           case VALUE_COLUMN:
               return Bundle.getMessage("HeadingValue");
           case DEVICENAMECOLUMN:
               return "Device Name";
           case ROSTERENTRYCOLUMN:
               return "Roster Entry";
           case OPENPRGMRBUTTONCOLUMN:
               return "Program"; // TODO I18N
           case COUNT_COLUMN:
           default:
               return "#";
       }
   }

   @Override
   public Class<?> getColumnClass(int c) {
       switch (c) {
           case COUNT_COLUMN:
           case ARTICLE_COLUMN:
           case MODADDR_COLUMN:
           case CV_COLUMN:
           case VALUE_COLUMN:
               return Integer.class;
           case OPENPRGMRBUTTONCOLUMN:
               return JButton.class;
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
        if (lncvdm == null) {
            return 0;
        } else {
            return lncvdm.getDeviceCount();
        }
   }

   @Override
   public Object getValueAt(int r, int c) {
       LncvDevice dev = memo.getLncvDevicesManager().getDeviceList().getDevice(r);
       try {
          switch (c) {
              case ARTICLE_COLUMN:
                  assert dev != null;
                  return dev.getProductID();
              case MODADDR_COLUMN:
                  assert dev != null;
                  return dev.getDestAddr();
              case CV_COLUMN:
                  assert dev != null;
                  return dev.getCvNum();
              case VALUE_COLUMN:
                  assert dev != null;
                  return dev.getCvValue();
              case DEVICENAMECOLUMN:
                  assert dev != null;
                  if (dev.getDeviceName().length() == 0) { // not yet filled in, look for a candidate
                      List<DecoderFile> l =
                          InstanceManager.getDefault(
                              DecoderIndexFile.class).
                              matchingDecoderList(
                                      null,
                                      null,
                                      null,
                                      null,
                                      String.valueOf(dev.getProductID()), // a bit risky to check just 1 value
                                      null,
                                      null,
                                      null,
                                      null
                              );
                      //log.debug("found {} possible decoder matches for LNCV device", l.size());
                      String lastModelName = "";
                      if (l.size() > 0) {
                          for (DecoderFile d : l) {
                              if (d.getModel().equals("")) {
                                  log.warn("Empty model(name) in decoderfile {}", d.getFileName());
                              }
                              lastModelName = d.getModel();
                          }
                          dev.setDevName(lastModelName);
                          dev.setDecoderFile(l.get(l.size()-1));
                      }
                      return lastModelName;
                  }
                  return dev.getDeviceName();
              case ROSTERENTRYCOLUMN: // always empty??
                  assert dev != null;
                  return dev.getRosterName();
              case OPENPRGMRBUTTONCOLUMN:
                  assert dev != null;
                  if ((dev.getRosterName() != null) && (dev.getRosterName().length() == 0)) {
                      return "Create Roster Entry";
                  }
                  return Bundle.getMessage("ButtonProgram");
              default:
                 return r + 1;
          }
      } catch (NullPointerException npe) {
        log.warn("Caught NPE reading Module {}", r);
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
            case DEVICENAMECOLUMN:
                dev.setDevName((String) value);
                break;
            case ROSTERENTRYCOLUMN:
                dev.setRosterName((String) value);
                break;
            case OPENPRGMRBUTTONCOLUMN:
                if (((String)getValueAt(r, c)).compareTo("Create Roster Entry") == 0) { // TODO I18N
                    createRosterEntry(dev);
                    if (dev.getRosterEntry() != null) {
                        setValueAt(dev.getRosterName(), r, c);
                    } else {
                        log.warn("Failed to connect RosterEntry to device {}", dev.getRosterName());
                    }
                } else {
                    openProgrammer(r);
                }
                break;
            default:
                // no change, so do not fire a property change event
                return;
        }
        if (getRowCount() >= 1) {
            this.fireTableRowsUpdated(r, r);
        }
    }

    private void openProgrammer(int r) {
        LncvDevice dev = memo.getLncvDevicesManager().getDeviceList().getDevice(r);

        LncvDevicesManager.ProgrammingResult result = lncvdm.prepareForSymbolicProgrammer(dev, this);
        switch (result) {
            case SUCCESS_PROGRAMMER_OPENED:
                return;
            case FAIL_NO_SUCH_DEVICE:
                JOptionPane.showMessageDialog(parent,
                        "Device no found on LocoNet. Re-try the &quot;LNCV "
                                + "Device Discovery&quot' process and try again. "
                                + "Cannot open the programmer!",
                        "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_APPROPRIATE_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        "No suitable programmer available for this LocoNet connection."
                                + " Cannot open the programmer!", "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_MATCHING_ROSTER_ENTRY:
                JOptionPane.showMessageDialog(parent,
                        "There does not appear to be a roster entry for this "
                                + "device.  Cannot open the programmer!", "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_DESTINATION_ADDRESS_IS_ZERO:
                JOptionPane.showMessageDialog(parent,
                        "Device is at address 0.  Re-configure device address to a non-zero"
                                + "value before programming! "
                                + "Canceling operation!", "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS:
                JOptionPane.showMessageDialog(parent,
                        "Should not program as there are multiple devices with device"
                                + " address " + dev.getDestAddr() + " present on LocoNet. "
                                + "Canceling operation!", "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_ADDRESSED_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        "No addressed programmer available for this LocoNet connection."
                                + " Cannot open the programmer!", "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_LNCV_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        "LNCV programming mode is not available on this connection."
                                + " Cannot open the programmer!", "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
                return;
            default:
                JOptionPane.showMessageDialog(parent,
                        "Unknown error occurred.  Cannot open programmer." // TODO I18N
                                + " Cannot open the programmer!", "Open Roster Entry", JOptionPane.ERROR_MESSAGE);
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

    private void createRosterEntry(LncvDevice dev) {
        if (dev.getDestAddr() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "Cannot create a roster entry when the destination address"
                            + " is 0.  Canceling operation." // TODO I18N
                    , "Create Roster Entry", JOptionPane.ERROR_MESSAGE);
        } else {
            String s = null;
            while (s == null) {
                s = JOptionPane.showInputDialog(parent,
                        "Enter a name for the roster entry", ""); // TODO I18N
                if (s == null) {
                    // cancel button hit
                    return;
                }
            }

            RosterEntry re = new RosterEntry(dev.getDecoderFile().getFileName());
            re.setDccAddress(Integer.toString(dev.getDestAddr()));
            re.setDecoderModel(dev.getDecoderFile().getModel());
            re.setProductID(Integer.toString(dev.getProductID()));
            re.setId(s);
            _roster.addEntry(re);
            dev.setRosterEntry(re);
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
        String eventName = evt.getPropertyName();
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
