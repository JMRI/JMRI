package jmri.jmrix.loconet.swing.lncvprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Table model for the programmed LNCV values table.
 *
 * @author Egbert Broerse Copyright (C) 2020
 */
public class LncvProgTableModel extends AbstractTableModel {

    public static final int ARTICLE_COLUMN = 1;
    public static final int MODADDR_COLUMN = 2;
    public static final int CV_COLUMN = 3;
    public static final int VALUE_COLUMN = 4;
    private final String[] columnNames = {"",
            Bundle.getMessage("HeadingArticle"),
            Bundle.getMessage("HeadingAddress"),
            Bundle.getMessage("HeadingCv"),
            Bundle.getMessage("HeadingValue")};
    private final LncvProgPane parent;

    LncvProgTableModel(LncvProgPane parent) {
        this.parent = parent;
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
             case ARTICLE_COLUMN:
                 if (parent.getModule(r) == null) {
                     log.debug("null module r={} c={}", r, c);
                     return "-";
                 }
                 return parent.getModule(r).getClassNum();
             case MODADDR_COLUMN:
                 return parent.getModule(r).getAddress();
             case CV_COLUMN:
                 return parent.getModule(r).getCvNum();
             case VALUE_COLUMN:
                 return parent.getModule(r).getCvValue();
             default:
                 return r + 1;
          }
      } catch (NullPointerException npe) {
        log.debug("Caught NPE reading Module {}", r);
        return "";
      }
   }

    @Override
    public void setValueAt(Object type, int r, int c) {
        // nothing is stored here
    }

    private final static Logger log = LoggerFactory.getLogger(LncvProgTableModel.class);

}
