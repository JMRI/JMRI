package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeEventTableDataModel;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEventVarPane extends CbusNodeConfigTab {
    
    private CbusNodeEventTableDataModel nodeEvModel;
    private JButton newEvButton;
    private CbusNodeEventTablePane genericEvTable;

    /**
     * Create a new instance of CbusNodeEventVarPane.
     * @param main the main NodeConfigToolPane this is a pane of.
     */
    protected CbusNodeEventVarPane( NodeConfigToolPane main ) {
        super(main);
        initPane();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(){
        return "Node Events";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void disposeOfNode(CbusNode node){
        super.disposeOfNode(node);
        nodeEvModel.dispose();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void changedNode( CbusNode node ) {
        // nodeEvModel.setNode( node );
        genericEvTable.setNode(node);
        nodeEvModel.fireTableDataChanged();
        newEvButton.setEnabled(nodeOfInterest.getNodeParamManager().getParameter(5) > -1 );
        
        validate();
        repaint();
    }
    
    private void initPane(){
    
        JPanel evMenuPane = new JPanel();
      
        newEvButton = new JButton(Bundle.getMessage("AddNodeEvent"));
        newEvButton.setToolTipText(Bundle.getMessage("AddNodeEventTip"));
        
        addButtonListener(newEvButton);
        
        evMenuPane.add(newEvButton);
        
        nodeEvModel = new CbusNodeEventTableDataModel(  getMainPane(), memo, 10,
            CbusNodeEventTableDataModel.MAX_COLUMN); // mainpane, controller, row, column
        
        genericEvTable = new CbusNodeEventTablePane(nodeEvModel);
        genericEvTable.initComponents(memo);
        genericEvTable.setVisible(true);
        
        add(evMenuPane, BorderLayout.PAGE_START);
        add(genericEvTable, BorderLayout.CENTER);
        
    }
    
    private void addButtonListener(JButton button){
        ActionListener newEvButtonClicked = ae -> {
            CbusNodeEvent newevent = new CbusNodeEvent( memo,
                -1,-1,nodeOfInterest.getNodeNumber(),-1,nodeOfInterest.getNodeParamManager().getParameter(5));
            java.util.Arrays.fill(newevent.getEvVarArray(),0);
            getMainPane().getEditEvFrame().initComponents(memo,newevent);
        };
        button.addActionListener(newEvButtonClicked);
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventVarPane.class);
    
}
