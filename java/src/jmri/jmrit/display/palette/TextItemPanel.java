package jmri.jmrit.display.palette;

//import java.awt.Color;
//import java.awt.event.ActionListener;
//import java.awt.event.ActionEvent;

//import java.awt.datatransfer.Transferable; 

import org.apache.log4j.Logger;
import javax.swing.BoxLayout;
//import javax.swing.TransferHandler;


//import java.util.Hashtable;
//import java.util.Iterator;

import javax.swing.*;

//import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;

/**
*  ItemPanel for text labels
*/
public class TextItemPanel extends ItemPanel /*implements ActionListener */{

//    JTextField _text;
    DecoratorPanel _decorator;

    public TextItemPanel(ItemPalette parentFrame, String  type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
        setToolTipText(Bundle.getMessage("ToolTipDragText"));
    }

    public void init() {
    	if (!_initialized) {
        	Thread.yield();
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel(Bundle.getMessage("addTextAndAttrs")));
            panel.add(new JLabel(Bundle.getMessage("ToolTipDragText")));
            JPanel p = new JPanel();
            p.add(panel);
            add(p);
            _decorator = new DecoratorPanel(_editor);
            _decorator.initDecoratorPanel(null);
            add(_decorator);
            _paletteFrame.pack();
            super.init();
    	}
    }

    static Logger log = Logger.getLogger(TextItemPanel.class.getName());
}
