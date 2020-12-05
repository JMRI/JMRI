package jmri.jmrix.loconet.lnsvf2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Table model for the pin assignment table.
 *
 * @author Paul Bender Copyright (C) 2013,2016
 */
public class Sv2ModulesTableModel extends AbstractTableModel {

   public static final int SYSNAME_COLUMN = 1;
   public static final int USERNAME_COLUMN = 2;

//   private final String[] assignmentTableColumnNames = {Bundle.getMessage("HeadingBit"),
//   Bundle.getMessage("HeadingSystemName"),
//   Bundle.getMessage("HeadingUserName")};


   public void initTable(javax.swing.JTable sv2ModulesTable) {
      TableColumnModel assignmentColumnModel = sv2ModulesTable.getColumnModel();
      TableColumn sysColumn = assignmentColumnModel.getColumn(SYSNAME_COLUMN);
      sysColumn.setMinWidth(75);
      sysColumn.setMaxWidth(100);
      sysColumn.setResizable(true);
      TableColumn userColumn = assignmentColumnModel.getColumn(USERNAME_COLUMN);
      userColumn.setMinWidth(90);
      userColumn.setMaxWidth(450);
      userColumn.setResizable(true);
   }

   @Override
   public String getColumnName(int c) {
      return c + "";//assignmentTableColumnNames[c];
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
      return 3;
   }

   @Override
   public int getRowCount() {
      return 8;
   }

   @Override
   public Object getValueAt(int r, int c) {
      try {
         switch (c) {
            case SYSNAME_COLUMN:
                return "free";
            case USERNAME_COLUMN:
                return "";
            default:
                return "";
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
