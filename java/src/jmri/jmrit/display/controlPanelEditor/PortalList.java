package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2014
 * 
 */

public class PortalList extends JList {

    private PortalListModel _portalListModel;
    
    PortalList(OBlock block) {
    	super();
        _portalListModel =  new PortalListModel(block);
        setModel(_portalListModel);
        setCellRenderer(new PortalCellRenderer());
        setPreferredSize(new Dimension(300,120));
        setVisibleRowCount(5);    	
    }
    
    void dataChange() {
        _portalListModel.dataChange();    	
    }
    
    private static class PortalCellRenderer extends JLabel implements ListCellRenderer {
        
        public Component getListCellRendererComponent(
           JList list,              // the list
           Object value,            // value to display
           int index,               // cell index
           boolean isSelected,      // is the cell selected
           boolean cellHasFocus)    // does the cell have focus
        {
             String s = ((Portal)value).getDescription();
             setText(s);
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
             setEnabled(list.isEnabled());
             setFont(list.getFont());
             setOpaque(true);
             return this;
        }
    }
    
    class PortalListModel extends AbstractListModel {
    	OBlock _homeBlock;
    	PortalListModel(OBlock block) {
    		_homeBlock = block;
    	}
        public int getSize() {
            return _homeBlock.getPortals().size();
        }
        public Object getElementAt(int index) {
            return _homeBlock.getPortals().get(index);
        }
        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }
    }
	
static Logger log = LoggerFactory.getLogger(EditPortalFrame.class.getName());
}
