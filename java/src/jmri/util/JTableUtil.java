// JTableUtil.java

package jmri.util;

import org.apache.log4j.Logger;
import javax.swing.*;
import javax.swing.table.*;
import jmri.util.com.sun.TableSorter;

/**
 * Common utility methods for working with JTables
 * <P>
 * We needed a place to refactor common JTable-processing idioms in JMRI
 * code, so this class was created. It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 * <P>
 * In particular, this is intended to provide Java 2 functionality on a
 * Java 1.1.8 system, or at least try to fake it.
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision$
 */

public class JTableUtil {

    static public JTable sortableDataModel(TableModel dataModel) {
    	
    	TableSorter sorter;
    	
        try {   // following might fail due to a missing method on Mac Classic
	    	sorter = new TableSorter(dataModel);
	    } catch (Throwable e) { 
	    	return new JTable(dataModel);
	    }
	    
    	JTable dataTable = new JTable(sorter)  {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                boolean res = super.editCellAt(row, column, e);
                java.awt.Component c = this.getEditorComponent();
                if (c instanceof javax.swing.JTextField) {
                    ( (JTextField) c).selectAll();
                }            
                return res;
            }
        };
    	
        try {   // following might fail due to a missing method on Mac Classic
                sorter.setTableHeader(dataTable.getTableHeader());
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            // nothing to do here
            log.error("Unexpected error: "+e);
        }
        return dataTable;
    }

    static final Logger log = Logger.getLogger(JTableUtil.class.getName());
}
