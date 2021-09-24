package jmri.jmrix.can.cbus.swing.modules;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;

/**
 * Node Variable edit frame for an unknown CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class UnknownEditNVPane extends AbstractEditNVPane {
    
    protected UnknownEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getContent() {
       
        JPanel newPane = new JPanel(new BorderLayout());
        
        JPanel gridPane = new JPanel(new GridBagLayout());
        
        JScrollPane scroll = new JScrollPane(gridPane);
        
        newPane.add(scroll, BorderLayout.CENTER);
        newPane.validate();
        newPane.repaint();
        
        return newPane;
    }
    
    /** {@inheritDoc} */
    @Override
    public void tableChanged(TableModelEvent e) {
    }

}
