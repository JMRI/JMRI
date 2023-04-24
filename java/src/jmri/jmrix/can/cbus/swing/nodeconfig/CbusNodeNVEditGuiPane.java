package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;


/**
 * Pane providing a Cbus NV editing gui
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusNodeNVEditGuiPane extends jmri.jmrix.can.swing.CanPanel {

    private final CbusNodeNVTableDataModel nodeNVModel;
    private JScrollPane nvVarScroll;
    private JPanel pane1;
    private JPanel editGui;
    private CbusNode _node;
    private CbusConfigPaneProvider _provider;

    protected CbusNodeNVEditGuiPane(CbusNodeNVTableDataModel nVModel) {
        super();
        nodeNVModel = nVModel;
        _node = null;
        _provider = null;
    }
    
    /**
     * Set the current node, keeping existing gui provider
     * 
     * @param node node to display
     */
    protected void setNode(CbusNode node) {
        _node = node;
        
        if (pane1 != null) {
            this.removeAll();
            this.initComponents();
        }
        
        editGui = _provider.getEditNVFrame(nodeNVModel, _node);
        showGui(editGui);
        
        this.setVisible(!(_node == null));
    }
    
    /**
     * Set the current node and associated gui provider
     * 
     * @param node node to display
     * @param provider edit gui provider for the node
     */
    protected void setNode(CbusNode node, CbusConfigPaneProvider provider) {
        _provider = provider;
        setNode(node);
    }
    
    protected void showGui(JPanel editGui){
        
        this.setLayout(new BorderLayout());
        
        pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
   
        nvVarScroll = new JScrollPane(editGui);

        pane1.add(nvVarScroll, BorderLayout.CENTER);
        
        add(pane1);
    }
    
    protected void tableChanged(TableModelEvent e) {
        if (_provider != null) {
            _provider.getEditNVFrameInstance().tableChanged(e);
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(CbusNodeNVEditGuiPane.class);

}
