package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table model for the pin assignment table.
 *
 * @author Paul Bender Copyright (C) 2013,2016
 */
public class AssignmentTableModel extends AbstractTableModel {

   private XBeeNode curNode = null;

   public static final int BIT_COLUMN = 0;
   public static final int SYSNAME_COLUMN = 1;
   public static final int USERNAME_COLUMN = 2;

   private String[] assignmentTableColumnNames = {Bundle.getMessage("HeadingBit"),
   Bundle.getMessage("HeadingSystemName"),
   Bundle.getMessage("HeadingUserName")};

   private String free = Bundle.getMessage("AssignmentFree");

   public void setNode(XBeeNode node) {
      curNode = node;
      fireTableDataChanged();
   }

   public void initTable(javax.swing.JTable assignmentTable) {
      TableColumnModel assignmentColumnModel = assignmentTable.getColumnModel();
      TableColumn bitColumn = assignmentColumnModel.getColumn(BIT_COLUMN);
      bitColumn.setMinWidth(20);
      bitColumn.setMaxWidth(40);
      bitColumn.setResizable(true);
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
      return assignmentTableColumnNames[c];
   }

   @Override
   public Class<?> getColumnClass(int c) {
      if (c == BIT_COLUMN) {
         return Integer.class;
      } else {
         return String.class;
      }
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
      Integer pin = Integer.valueOf(r);
      try {
         switch (c) {
            case BIT_COLUMN:
               return pin;
            case SYSNAME_COLUMN:
               if(curNode==null) {
                 return free;
               } 
               if (curNode.getPinAssigned(pin)) {
                  return curNode.getPinBean(pin).getSystemName();
               } else {
                  return free;
               }
            case USERNAME_COLUMN:
               if(curNode==null) {
                 return "";
               } 
               if (curNode.getPinAssigned(pin)) {
                  return curNode.getPinBean(pin).getUserName();
               } else {
                  return "";
               }
            default:
               return "";
        }
     } catch (java.lang.NullPointerException npe) {
        log.debug("Caught NPE getting pin assignment for pin {}", pin);
        return "";
     }
  }

   @Override
  public void setValueAt(Object type, int r, int c) {
     // nothing is stored here
  }

    private final static Logger log = LoggerFactory.getLogger(AssignmentTableModel.class);

}
