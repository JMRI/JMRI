
// IndicatorTOIconDialog.java
package jmri.jmrit.display.palette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrit.catalog.NamedIcon;

/**
 *
 * @author Pete Cressman  Copyright (c) 2010
 */

public class IndicatorTOIconDialog extends IconDialog {
    
    String _key;

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public IndicatorTOIconDialog(String type, String family, IndicatorTOItemPanel parent, String key, 
    				HashMap <String, NamedIcon> iconMap) {
        super(type, key, parent, iconMap);	// temporarily use key for family to set JL
        _family = family;
        _key = key;
        sizeLocate();
        if (log.isDebugEnabled()) log.debug("IndicatorTOIconDialog ctor done. type= \""+
        		type+"\", family= \""+family+"\", key= \""+key+"\"");
    }

    /**
    * Add/Delete icon family for types that may have more than 1 family
    */
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        JButton addFamilyButton = new JButton(Bundle.getMessage("addMissingStatus"));
        addFamilyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	addFamilySet();
                    dispose();
                }
        });
        addFamilyButton.setToolTipText(Bundle.getMessage("ToolTipMissingStatus"));
        panel1.add(addFamilyButton);

        JButton deleteButton = new JButton(Bundle.getMessage("deleteStatus"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deleteFamilySet();
                    dispose();
                }
        });
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteStatus"));
        panel1.add(deleteButton);
        buttonPanel.add(panel1);
    }

    /**
    * NOT add a new family.  Create a status family when previous status was deleted
    */
    private void createNewStatusSet() {
    	if (log.isDebugEnabled()) log.debug("createNewFamily: type= \""+
    				_type+"\", family= \""+_family+"\" key= "+_key);
        //check text        
        HashMap<String, NamedIcon> iconMap = ItemPanel.makeNewIconMap("Turnout");
        ItemPalette.addLevel4FamilyMap(_type, _parent._family, _key, iconMap);
        addFamilySet(_parent._family, iconMap, _key);
        dispose();
    }

    /**
    * Action item for add new status set in makeAddIconButtonPanel
    */
    private void addFamilySet() {
    	if (log.isDebugEnabled()) log.debug("addFamilySet: type= \""+
    				_type+"\", family= \""+_family+"\" key= "+_key);
        setVisible(false);
        IndicatorTOItemPanel parent = (IndicatorTOItemPanel)_parent;
        if (parent._iconGroupsMap.size() < IndicatorTOItemPanel.STATUS_KEYS.length) {
        	Set<String> keys = ItemPalette.getLevel4Family(_type, _family).keySet();
        	ArrayList<String> options = new ArrayList<String>();
        	for (int i=0; i<IndicatorTOItemPanel.STATUS_KEYS.length; i++) {
        		if (!keys.contains(IndicatorTOItemPanel.STATUS_KEYS[i])) {
        			options.add(IndicatorTOItemPanel.STATUS_KEYS[i]);
        		}
        	}
        	Object[] selections = options.toArray();
        	String key = (String)JOptionPane.showInputDialog(_parent._paletteFrame,
        			Bundle.getMessage("PickStatus"), Bundle.getMessage("questionTitle"), JOptionPane.QUESTION_MESSAGE, null,
        			selections, selections[0]);
        	if (key!=null) {
        		_key = key;
            	createNewStatusSet();
//                new IndicatorTOIconDialog(_type, null, parent, _key, _iconMap);        		
        	}
        } else {
            JOptionPane.showMessageDialog(_parent._paletteFrame, 
                    Bundle.getMessage("AllStatus"), 
                    Bundle.getMessage("infoTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
    * Action item for add delete status set in makeAddIconButtonPanel
    */
    private void deleteFamilySet() {
        ItemPalette.removeLevel4IconMap(_type, _parent._family, _key);
        _family = null;
        _parent.updateFamiliesPanel();
    }

    /**
    * Action item for makeDoneButtonPanel
    */
    protected boolean doDoneAction() {
        //check text
        String subFamily = _key;  // actually the key to status icon
        if (_family!=null && _family.equals(subFamily)) {
            ItemPalette.removeLevel4IconMap(_type, _parent._family, subFamily);
        }
        jmri.jmrit.catalog.ImageIndexEditor.indexChanged(true);
        return addFamilySet(_parent._family, _iconMap, subFamily);
    }

    private boolean addFamilySet(String family, HashMap<String, NamedIcon> iconMap, String subFamily) {
    	if (log.isDebugEnabled()) log.debug("addFamily _type= \""+_type+"\", family= \""+family+"\""+
    			", key= \""+_key+"\", _iconMap.size= "+_iconMap.size());
        IndicatorTOItemPanel parent = (IndicatorTOItemPanel)_parent;
        parent.updateIconGroupsMap(subFamily, _iconMap);
        _parent.updateFamiliesPanel();
        _parent._family = family;
        return true;
    }

    static Logger log = LoggerFactory.getLogger(IndicatorTOIconDialog.class.getName());
}
