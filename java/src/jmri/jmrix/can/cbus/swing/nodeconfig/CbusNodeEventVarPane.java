package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
    
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeEventTableDataModel;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEventVarPane extends JPanel {
    
    private JPanel infoPane = new JPanel();
    private CanSystemConnectionMemo _memo;
    private CbusNodeEventTableDataModel nodeEvModel;
    protected JButton newEvButton;
    private NodeConfigToolPane mainpane;
    private CbusNode nodeOfInterest;

    /**
     * Create a new instance of CbusEventHighlightPanel.
     */
    protected CbusNodeEventVarPane( NodeConfigToolPane main ) {
        super();
        mainpane = main;
    }
    
    public void initComponents(CanSystemConnectionMemo memo) {
        _memo = memo;
        this.add(infoPane);
        nodeOfInterest = null;
    }
    
    public void setNode( CbusNode node ) {
        
        if ( node == nodeOfInterest){
            return;
        }
        
        if ( nodeEvModel != null ){
            nodeEvModel.dispose();
        }
        
        if ( node == null ){
            return;
        }
        
        nodeEvModel = new CbusNodeEventTableDataModel(  mainpane, _memo, 10,
            CbusNodeEventTableDataModel.MAX_COLUMN); // mainpane, controller, row, column
        
        nodeOfInterest= node;

        if (infoPane != null ){ 
            infoPane.setVisible(false);
            infoPane = null;
        }

        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );
        // Pane to hold Event
        JPanel evMenuPane = new JPanel();
      
        newEvButton = new JButton(("Add Node Event"));
        newEvButton.setToolTipText(("Add Event and configure the event variables"));
        
        evMenuPane.add(newEvButton);
        
        if ( nodeOfInterest.getParameter(5) < 1 ) {
            newEvButton.setEnabled(false);
        }
        
        CbusNodeEventTablePane genericEvTable = new CbusNodeEventTablePane(nodeEvModel);
        genericEvTable.initComponents(_memo);
        genericEvTable.setNode( nodeOfInterest );
        
        infoPane.add(evMenuPane, BorderLayout.PAGE_START);
        infoPane.add(genericEvTable, BorderLayout.CENTER);
        
        setLayout(new BorderLayout() );
        
        this.add(infoPane);
        
        validate();
        repaint();
        
        ActionListener newEvButtonClicked = ae -> {
            if (nodeOfInterest == null){
                return;
            }
            CbusNodeEvent newevent = new CbusNodeEvent(
                -1,-1,nodeOfInterest.getNodeNumber(),-1,nodeOfInterest.getParameter(5));
            java.util.Arrays.fill(newevent._evVarArr,0);
            
            mainpane.getEditEvFrame().initComponents(_memo,newevent);

        };
        newEvButton.addActionListener(newEvButtonClicked);
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventVarPane.class);
    
}
