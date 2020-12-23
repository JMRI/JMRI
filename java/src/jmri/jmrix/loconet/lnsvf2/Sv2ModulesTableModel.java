package jmri.jmrix.loconet.lnsvf2;

import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Table model for the discovered modules table.
 *
 * @author Egbert Broerse Copyright (C) 2020
 */
public class Sv2ModulesTableModel extends AbstractTableModel {

   public static final int MANUFACTURER_COLUMN = 1;
   public static final int SERIAL_COLUMN = 2;
   public static final int TYPE_COLUMN = 3;
   public static final int ADDRESS_COLUMN = 4;
   private final String[] columnNames = {"",
           Bundle.getMessage("HeadingManufacturer"),
           Bundle.getMessage("HeadingType"),
           Bundle.getMessage("HeadingSerial"),
           Bundle.getMessage("HeadingAddress")};
    private final Sv2DiscoverPane parent;

    Sv2ModulesTableModel(Sv2DiscoverPane parent) {
        this.parent = parent;
    }

   public void initTable(javax.swing.JTable sv2ModulesTable) {
      TableColumnModel assignmentColumnModel = sv2ModulesTable.getColumnModel();
      TableColumn idColumn = assignmentColumnModel.getColumn(0);
      idColumn.setMaxWidth(8);
      TableColumn manuColumn = assignmentColumnModel.getColumn(MANUFACTURER_COLUMN);
      manuColumn.setMinWidth(10);
      manuColumn.setMaxWidth(100);
      manuColumn.setResizable(true);
      TableColumn serialColumn = assignmentColumnModel.getColumn(SERIAL_COLUMN);
      serialColumn.setMinWidth(10);
      serialColumn.setMaxWidth(100);
      serialColumn.setResizable(true);
   }

   @Override
   public String getColumnName(int c) {
      return columnNames[c];
   }

   @Override
   public Class<?> getColumnClass(int c) {
       return String.class;
   }

   @Override
   public boolean isCellEditable(int r, int c) {
      return false;
   }

   @Override
   public int getColumnCount() {
      return 5;
   }

   @Override
   public int getRowCount() {
       return Math.max(5, parent.getCount());
   }

   @Override
   public Object getValueAt(int r, int c) {
      try {
         switch (c) {
             case MANUFACTURER_COLUMN:
                return parent.getModule(r).getManufacturer();
             case SERIAL_COLUMN:
                 return parent.getModule(r).getSerialNum();
             case TYPE_COLUMN:
                 return parent.getModule(r).getType();
             case ADDRESS_COLUMN:
                 return parent.getModule(r).getAddress();
            default:
                return r + 1;
        }
     } catch (NullPointerException npe) {
        log.debug("Caught NPE getting module {}", r);
        return "";
     }
  }

   @Override
  public void setValueAt(Object type, int r, int c) {
     // nothing is stored here
  }

    private final static Logger log = LoggerFactory.getLogger(Sv2ModulesTableModel.class);

}
