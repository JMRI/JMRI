// ItemDialog.java
package jmri.jmrit.display.palette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.util.JmriJFrame;

/**
 * @author Pete Cressman  Copyright (c) 2010
 */

public class ItemDialog extends JmriJFrame {

    protected ItemPanel _parent;
    protected String    _type;
    protected String    _family;
    
    private static ItemDialog _instance = null;		// only let one dialog at a time

    /**
    */
    public ItemDialog(String type, String family, String title, ItemPanel parent) {
        super(title, true, true);
        if (_instance!=null) {
        	_instance.dispose();
        }
        _instance = this;
        _type = type;
        _family = family;
        _parent = parent;
        setAlwaysOnTop(true);
    }

    protected void sizeLocate() {
        setSize(_parent.getSize().width, this.getPreferredSize().height);
        setLocationRelativeTo(_parent);
        setVisible(true);
        pack();
    }

    protected String getDialogType() {
        return _type;
    }
    
    public void dispose() {
    	super.dispose();
    	_instance = null;
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(ItemDialog.class.getName());
}
