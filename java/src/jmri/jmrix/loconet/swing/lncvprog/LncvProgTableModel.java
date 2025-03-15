package jmri.jmrix.loconet.swing.lncvprog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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
import jmri.util.swing.JmriJOptionPane;

/**
 * Table model for the programmed LNCV Modules table.
 * See Sv2f Programing tool
 *
 * @author Egbert Broerse Copyright (C) 2020, 2025
 */
public class LncvProgTableModel extends AbstractTableModel implements PropertyChangeListener, ProgrammingTool {

    public static final int COUNT_COLUMN = 0;
    public static final int ARTICLE_COLUMN = 1;
    public static final int MODADDR_COLUMN = 2;
    public static final int CV_COLUMN = 3;
    public static final int VALUE_COLUMN = 4;
    public static final int DEVICENAME_COLUMN = 5;
    public static final int ROSTERENTRY_COLUMN = 6;
    public static final int OPENPRGMRBUTTON_COLUMN = 7;
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
       idColumn.setMaxWidth(3);
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
           case DEVICENAME_COLUMN:
               return Bundle.getMessage("HeadingDeviceModel");
           case ROSTERENTRY_COLUMN:
               return Bundle.getMessage("HeadingDeviceId");
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
           case COUNT_COLUMN:
           case ARTICLE_COLUMN:
           case MODADDR_COLUMN:
           case CV_COLUMN:
           case VALUE_COLUMN:
               return Integer.class;
           case OPENPRGMRBUTTON_COLUMN:
               return javax.swing.JButton.class;
           case DEVICENAME_COLUMN:
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
              case DEVICENAME_COLUMN:
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
                                      String.valueOf(dev.getProductID()), // a bit risky to check just 1 value
                                      null,
                                      null,
                                      null,
                                      null
                              );
                      //log.debug("found {} possible decoder matches for LNCV device", l.size());
                      String lastModelName = "";
                      if (!l.isEmpty()) {
                          for (DecoderFile d : l) {
                              // we do not check for LNCV programmingMode support
                              // we do not expect replies from non-LNCV devices
                              // TODO check using new access to getProgrammingModes() in the DecoderIndexFile
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
              case ROSTERENTRY_COLUMN:
                  assert dev != null;
                  return dev.getRosterName();
              case OPENPRGMRBUTTON_COLUMN:
                  if (dev != null && !dev.getDeviceName().isEmpty()) {
                      if ((dev.getRosterName() != null) && (dev.getRosterName().isEmpty())) {
                          return Bundle.getMessage("ButtonCreateEntry");
                      }
                      if (dev.getDecoderFile().isProgrammingMode("LOCONETLNCVMODE")) {
                          return Bundle.getMessage("ButtonProgram");
                      } else {
                          return Bundle.getMessage("ButtonWrongMode");
                      }
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
        if (c == OPENPRGMRBUTTON_COLUMN) {
            if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonCreateEntry")) == 0) {
                createRosterEntry(dev);
                if (dev.getRosterEntry() != null) {
                    setValueAt(dev.getRosterName(), r, c);
                } else {
                    log.warn("Failed to connect RosterEntry to device {}", dev.getRosterName());
                }
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonProgram")) == 0) {
                openProgrammer(r);
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonWrongMode")) == 0) {
                infoNotForLncv(getValueAt(r, 1).toString()); // TODO once we check for LNCV progMode this can be removed
            } else if (((String) getValueAt(r, c)).compareTo(Bundle.getMessage("ButtonNoMatchInRoster")) == 0){
                // suggest to rebuild decoderIndex
                warnRecreate(getValueAt(r, 1).toString());
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

    private void createRosterEntry(LncvDevice dev) {
        if (dev.getDestAddr() == 0) {
            JmriJOptionPane.showMessageDialog(parent,
                    Bundle.getMessage("FAIL_ADD_ENTRY_0"),
                    Bundle.getMessage("ButtonCreateEntry"), JmriJOptionPane.ERROR_MESSAGE);
        } else {
            String s = null;
            while (s == null) {
                s = JmriJOptionPane.showInputDialog(parent,
                        Bundle.getMessage("DialogEnterEntryName"),
                        Bundle.getMessage("EnterEntryNameTitle"),JmriJOptionPane.QUESTION_MESSAGE);
                if (s == null) {
                    // Cancel button hit
                    return;
                }
            }

            RosterEntry re = getRosterEntry(dev, s);
            _roster.addEntry(re);
            dev.setRosterEntry(re);
        }
    }

    @Nonnull
    private static RosterEntry getRosterEntry(LncvDevice dev, String s) {
        RosterEntry re = new RosterEntry(dev.getDecoderFile().getFileName());
        re.setDccAddress(Integer.toString(dev.getDestAddr()));
        re.setDecoderModel(dev.getDecoderFile().getModel());
        re.setProductID(Integer.toString(dev.getProductID()));
        // add some details that won't be picked up otherwise from definition
        re.setDecoderFamily(dev.getDecoderFile().getFileName());
        re.setProgrammingModes(dev.getDecoderFile().getProgrammingModes());
        re.setId(s);
        return re;
    }

    private void warnRecreate(String address) {
        // show dialog to inform and allow rebuilding index
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonRecreateIndex"),
                Bundle.getMessage("ButtonCancel")};
        int userReply = JmriJOptionPane.showOptionDialog(parent,
                Bundle.getMessage("DialogWarnRecreate", address),
                Bundle.getMessage("TitleOpenRosterEntry"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.QUESTION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[0]);
        if (userReply == 0) { // array position 0
            try {
                DecoderIndexFile.forceCreationOfNewIndex(false); // faster
            } catch (Exception exq) {
                log.error("exception updating decoderIndexFile", exq);
            }
        }
    }

    /**
     * Show dialog to inform that address matched decoder doesn't support LNSV1 mode.
     */
    private void infoNotForLncv(String address) {
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonOK")};
        JmriJOptionPane.showOptionDialog(parent,
                Bundle.getMessage("DialogInfoMatchNotX", address, "LNCV"),
                Bundle.getMessage("TitleOpenRosterEntry"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.INFORMATION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[0]);
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LncvProgTableModel.class);

}
