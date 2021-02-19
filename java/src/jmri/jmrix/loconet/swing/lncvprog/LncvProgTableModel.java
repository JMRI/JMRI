package jmri.jmrix.loconet.swing.lncvprog;

import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrix.ProgrammingTool;
import jmri.jmrix.loconet.LnProgrammerManager;
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

    public void initTable(javax.swing.JTable lncvModulesTable) {
       TableColumnModel assignmentColumnModel = lncvModulesTable.getColumnModel();
       TableColumn idColumn = assignmentColumnModel.getColumn(0);
       idColumn.setMaxWidth(8);
    }

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
                              // we do not check for LNCV programmingMode support since we do not expect replies from non-LNCV devices
                              // (and there is currently no access to supported modes in the DecoderIndexFile)
                              if (d.getModel().equals("")) {
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
                  if (dev.getDeviceName().length() != 0) {
                      if ((dev.getRosterName() != null) && (dev.getRosterName().length() == 0)) {
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
        LncvDevice dev = memo.getLncvDevicesManager().getDeviceList().getDevice(r);
        if (c == OPENPRGMRBUTTONCOLUMN) {
            if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonCreateEntry")) == 0) {
                createRosterEntry(dev);
                if (dev.getRosterEntry() != null) {
                    setValueAt(dev.getRosterName(), r, c);
                } else {
                    log.warn("Failed to connect RosterEntry to device {}", dev.getRosterName());
                }
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonProgram")) == 0) {
                openProgrammer(r);
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
        LncvDevice dev = memo.getLncvDevicesManager().getDeviceList().getDevice(r);

        LncvDevicesManager.ProgrammingResult result = lncvdm.prepareForSymbolicProgrammer(dev, this);
        switch (result) {
            case SUCCESS_PROGRAMMER_OPENED:
                return;
            case FAIL_NO_SUCH_DEVICE:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_SUCH_DEVICE"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_APPROPRIATE_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_APPROPRIATE_PROGRAMMER"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_MATCHING_ROSTER_ENTRY:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_MATCHING_ROSTER_ENTRY"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_DESTINATION_ADDRESS_IS_ZERO:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_DESTINATION_ADDRESS_IS_ZERO"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS", dev.getDestAddr()),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_ADDRESSED_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_ADDRESSED_PROGRAMMER"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
                return;
            case FAIL_NO_LNCV_PROGRAMMER:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_NO_LNCV_PROGRAMMER"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
                return;
            default:
                JOptionPane.showMessageDialog(parent,
                        Bundle.getMessage("FAIL_UNKNOWN"),
                        Bundle.getMessage("TitleOpenRosterEntry"), JOptionPane.ERROR_MESSAGE);
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
                    Bundle.getMessage("FAIL_ADD_ENTRY_0"),
                    Bundle.getMessage("ButtonCreateEntry"), JOptionPane.ERROR_MESSAGE);
        } else {
            String s = null;
            while (s == null) {
                s = JOptionPane.showInputDialog(parent,
                        Bundle.getMessage("DialogEnterEntryName"), "");
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
        //String eventName = evt.getPropertyName();
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
