package jmri.jmrit.consisttool;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComboBox;

import jmri.*;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  
 * A JComboBox with JMRI consists
 * Entries can be a String or a DccLocoAddress 
 * 
 * @author Lionel Jeanson Copyright (c) 2023
 * 
 */
public class ConsistComboBox extends JComboBox<Object> implements ConsistListListener {
    ConsistManager consistManager = InstanceManager.getDefault(ConsistManager.class);    
    private static boolean consistMgrInitFromXmlFile = false;
    private boolean isUpdatingList = false;
        
    public ConsistComboBox() {
        super();        
        init();
    }    

    private void init() {
        setToolTipText(Bundle.getMessage("ConsistAddressBoxToolTip"));
        initConsistList();
        setSelectedIndex(0);
        setRenderer(new ConsistListCellRenderer());
        consistManager.addConsistListListener(this);
        if (!consistMgrInitFromXmlFile) {
            try {
                new ConsistFile().readFile();
                consistMgrInitFromXmlFile =true;
            } catch (IOException | JDOMException e) {
                log.warn("error reading consist file: {}", e.getMessage());
            }
            consistMgrInitFromXmlFile =true;
        }
    }
            
    public void dispose() {
        consistManager.removeConsistListListener(this);        
    }

    private void initConsistList() {
        if (isUpdatingList) {
            return;
        }
        isUpdatingList = true;
        Object selectedItem = getSelectedItem();
        int selectedIndex = getSelectedIndex();
        ArrayList<LocoAddress> existingConsists = consistManager.getConsistList();
        removeAllItems();
        if (!existingConsists.isEmpty()) {
            java.util.Collections.sort(existingConsists, new jmri.util.LocoAddressComparator()); // sort the consist list.            
            existingConsists.forEach(consist -> addItem(consist));
            setEnabled(true);
        } else {
            setEnabled(false);
        }
        insertItemAt(Bundle.getMessage("NoConsistSelected"), 0);
        if (selectedIndex > getItemCount() || (selectedItem != getItemAt(selectedIndex)) ) {
            setSelectedIndex(0);            
        } else {
            setSelectedIndex(selectedIndex);
        }
        isUpdatingList = false;
    }
    
    /**
     * Limit action firing to user actions
     */
    @Override
    protected void fireActionEvent() {
        if (! isUpdatingList ) {
            super.fireActionEvent();
        }
    }

    @Override
    public void notifyConsistListChanged() {
        initConsistList();
    }

    private static final Logger log = LoggerFactory.getLogger(ConsistComboBox.class);
}
