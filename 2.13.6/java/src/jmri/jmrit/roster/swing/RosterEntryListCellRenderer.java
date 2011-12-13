package jmri.jmrit.roster.swing;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jmri.InstanceManager;


public class RosterEntryListCellRenderer extends JLabel implements ListCellRenderer {	
	public RosterEntryListCellRenderer() {
		super();
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value==null) return this;
		String rosterEntryTitle = value.toString();

		ImageIcon icon = InstanceManager.rosterIconFactoryInstance().getIcon( rosterEntryTitle );
		if (icon != null) 
			icon.setImageObserver(list);
		setIcon(icon);
		setText(rosterEntryTitle);
		return this;	
	}
}
