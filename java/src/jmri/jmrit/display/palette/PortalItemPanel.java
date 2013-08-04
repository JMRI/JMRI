package jmri.jmrit.display.palette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JPanel;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.util.JmriJFrame;

/**
*  ItemPanel for for PortalIcons.  Since this class has been introduced after users may have
*  customized the defaultPanelIcons, the default family, "Standard" is added by overriding
*  the initIconFamiliesPanel method.
*   
* @author Pete Cressman  Copyright (c) 2013
*/
public /*abstract*/ class PortalItemPanel extends FamilyItemPanel {

    /**
    * Constructor types with multiple families and multiple icon families
    */
    public PortalItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
    }

    /**
    * Init for creation
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    * subclasses will insert other panels
    */
    public void init() {
       	if (!_initialized) {
       		Thread.yield();
       		_update = false;
       		_supressDragging = true;
            _bottom1Panel = makeBottom1Panel();
            _bottom2Panel = makeBottom2Panel();
            initIconFamiliesPanel();
            add(new JPanel());		// space holder to make _iconFamilyPanel 2nd component
            add(_iconFamilyPanel);
            JPanel bottomPanel = new JPanel(new FlowLayout());
            bottomPanel.add(_bottom1Panel);
            bottomPanel.add(_bottom2Panel);
            add(bottomPanel);
            _initialized = true;
       	}
    }

    static Logger log = LoggerFactory.getLogger(PortalItemPanel.class.getName());
}