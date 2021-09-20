package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane providing a Cbus event editing gui
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusNodeNVEditGuiPane extends jmri.jmrix.can.swing.CanPanel {

    private final CbusNodeNVTableDataModel nodeNVModel;
    private JScrollPane nvVarScroll;
    private JPanel pane1;
    private JPanel editGui;
    private CbusNode _node;
    private AbstractEditNVPane editGuiInstance;

    protected CbusNodeNVEditGuiPane(CbusNodeNVTableDataModel nVModel) {
        super();
        super.initComponents();
        nodeNVModel = nVModel;
        this.setLayout(new BorderLayout());
    }
    
    /**
     * Helper function
     * 
     * @param node node to display
     */
    protected void setNode(CbusNode node) {
        _node = node;
        
        if (pane1!=null){
            this.removeAll();
            this.initComponents();
        }
        
        CbusConfigPaneProvider provider = CbusConfigPaneProvider.getProviderByNode(_node);
        editGui = provider.getEditNVFrame(nodeNVModel, _node);
        editGuiInstance = provider.getEditNVFrameInstance();
        showGui(editGui);

        this.setVisible(!(_node == null));
    }
    
    protected void showGui(JPanel editGui){
        
        pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
   
        nvVarScroll = new JScrollPane(editGui);

        pane1.add(nvVarScroll, BorderLayout.CENTER);
        
        add(pane1);
    }
    
    protected void tableChanged(TableModelEvent e) {
        if (editGuiInstance != null) {
            editGuiInstance.tableChanged(e);
        }
    }
    
//    private final static Logger log = LoggerFactory.getLogger(CbusNodeNVEditGuiPane.class);

}
