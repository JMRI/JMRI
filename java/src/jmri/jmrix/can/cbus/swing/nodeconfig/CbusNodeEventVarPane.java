package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
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
    private JButton newEvButton;
    private final NodeConfigToolPane mainpane;
    private CbusNode nodeOfInterest;

    /**
     * Create a new instance of CbusEventHighlightPanel.
     * @param main the main NodeConfigToolPane this is a pane of.
     */
    protected CbusNodeEventVarPane( NodeConfigToolPane main ) {
        super();
        mainpane = main;
    }
    
    public void initComponents(CanSystemConnectionMemo memo) {
        _memo = memo;
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

        infoPane = newInfoPane();
        // Pane to hold Event
        
        setLayout(new BorderLayout() );
        this.add(infoPane);
        
        validate();
        repaint();
        
    }
    
    private JPanel newInfoPane(){
    
        JPanel newPane = new JPanel();
        newPane.setLayout(new BorderLayout() );
        
        JPanel evMenuPane = new JPanel();
      
        newEvButton = new JButton(Bundle.getMessage("AddNodeEvent"));
        newEvButton.setToolTipText(Bundle.getMessage("AddNodeEventTip"));
        addButtonListener();
        
        evMenuPane.add(newEvButton);
        
        // check number of event variables per node event
        if ( nodeOfInterest.getParameter(5) < 1 ) {
            newEvButton.setEnabled(false);
        }
        
        CbusNodeEventTablePane genericEvTable = new CbusNodeEventTablePane(nodeEvModel);
        genericEvTable.initComponents(_memo);
        genericEvTable.setNode( nodeOfInterest );
        
        newPane.add(evMenuPane, BorderLayout.PAGE_START);
        newPane.add(genericEvTable, BorderLayout.CENTER);
        
        return newPane;
    }
    
    private void addButtonListener(){
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
