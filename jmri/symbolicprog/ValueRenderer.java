/** 
 * ValueRenderer.java
 *
 * Description:		Renders enum table cells
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import javax.swing.table.TableCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.Component;

public class ValueRenderer implements TableCellRenderer {

	public ValueRenderer() {
		super();
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, 
													boolean isSelected, boolean hasFocus, 
													int row, int column) {
		if (value instanceof Component)
			return (Component) value;
		else if (value instanceof String)
			return new JLabel((String)value);
		else
			return new JLabel("Unknown value type!");
	}
}
