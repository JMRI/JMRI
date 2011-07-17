package jmri.jmrit.display.palette;

//import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.Transferable; 
import javax.swing.TransferHandler;


//import java.util.Hashtable;
//import java.util.Iterator;

import javax.swing.*;

//import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;

/**
*  ItemPanel for text labels
*/
public class TextItemPanel extends ItemPanel implements ActionListener {

    JTextField _text;
    DecoratorPanel _decorator;

    public TextItemPanel(ItemPalette parentFrame, String  type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragText"));
    }

    public void init() {
        initTextPanel();
        _decorator = new DecoratorPanel(_editor);
        _decorator.initDecoratorPanel(null);
        add(_decorator);
    }

    /**
    */
    protected void initTextPanel() {
        _text = new JTextField();
        _text.addActionListener(this);
        _text.setDragEnabled(true);
        _text.setTransferHandler(new DnDTextItemHandler());
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        JLabel label = new JLabel(ItemPalette.rbp.getString("textLabel"));
        panel.add(label, c);
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        panel.add(_text, c);
        label.setToolTipText(ItemPalette.rbp.getString("ToolTipEnterText"));
        _text.setToolTipText(ItemPalette.rbp.getString("ToolTipEnterText"));
        panel.setToolTipText(ItemPalette.rbp.getString("ToolTipEnterText"));
        add(panel);
    }

    public void actionPerformed(ActionEvent e) {
        _decorator.setText(_text.getText());
    }

    /**
    * Export a Positionable item from icon panel 
    */
    protected class DnDTextItemHandler extends TransferHandler {

        DnDTextItemHandler() {
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from "+_text.getText());
            return new java.awt.datatransfer.StringSelection(_text.getText());
        }

        public void exportDone(JComponent c, Transferable t, int action) {
            if (log.isDebugEnabled()) log.debug("TransferHandler.exportDone ");
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TextItemPanel.class.getName());
}
