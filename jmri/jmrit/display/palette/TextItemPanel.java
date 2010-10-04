package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
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

    public TextItemPanel(ItemPalette parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragText"));
    }

    public void init() {
        initTextPanel();
        _decorator = new DecoratorPanel(_editor);
        add(_decorator, BorderLayout.SOUTH);
        initButtonPanel();          // SOUTH Panel
    }

    /**
    */
    protected void initTextPanel() {
        _text = new JTextField();
        _text.addActionListener(this);
        _text.setToolTipText(ItemPalette.rbp.getString("ToolTipDragText"));
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
        panel.add(new JLabel(ItemPalette.rbp.getString("textLabel")), c);
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        panel.add(_text, c);
        add(panel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        _decorator.setText(_text.getText());
    }

    /**
    *  SOUTH Panel
    */
    public void initButtonPanel() {
        /*
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)

        _editIconsButton = new JButton(ItemPalette.rbp.getString("EditIcons"));
        _editIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    openEditDialog();
                }
        });
        _editIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
        bottomPanel.add(_editIconsButton);

        add(bottomPanel, BorderLayout.SOUTH);
        */
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
