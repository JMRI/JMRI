package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeVarPane extends JPanel implements TableModelListener {
    
    private JPanel infoPane = new JPanel();
    private CanSystemConnectionMemo _memo;
    public CbusNodeNVTableDataModel nodeNVModel;
    protected JButton editButton;
    private NodeConfigToolPane mainpane;
    protected CbusNodeNVTablePane genericNVTable;

    /**
     * Create a new instance of CbusEventHighlightPanel.
     */
    protected CbusNodeVarPane( NodeConfigToolPane main ) {
        super();
        mainpane = main;
    }
    
    public void initComponents(CanSystemConnectionMemo memo) {
        _memo = memo;
        this.add(infoPane);
    }
    
    public void setNode( CbusNode node ) {
        
        if ( nodeNVModel !=null ) {
            nodeNVModel.removeTableModelListener(this);
            nodeNVModel.dispose();
        }
        
        nodeNVModel = new CbusNodeNVTableDataModel(_memo, 10,
            CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
            nodeNVModel.addTableModelListener(this);
        
        if (infoPane != null ){ 
            infoPane.setVisible(false);
            infoPane = null;
        }

        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );
        // Pane to hold Event
        JPanel nvMenuPane = new JPanel();
        
        nvMenuPane.setLayout(new BoxLayout(nvMenuPane, BoxLayout.X_AXIS));
      
        editButton = new JButton(("Edit"));
        editButton.setToolTipText(("Edit Node Variables"));
        
        editButton.setEnabled( ( nodeNVModel.isTableLoaded() ) && ( !mainpane.getEditNvActive() ) );
        
        nvMenuPane.add(editButton);
      //  nvMenuPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        genericNVTable = new CbusNodeNVTablePane(nodeNVModel);
        genericNVTable.initComponents(_memo);
        genericNVTable.setNode( node );
        
      //  genericNVTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPane.add(nvMenuPane, BorderLayout.PAGE_START);
        infoPane.add(genericNVTable, BorderLayout.CENTER);
        
        setLayout(new BorderLayout() );
        
        this.add(infoPane);
        
        validate();
        repaint();
        
        ActionListener editButtonClicked = ae -> {
            log.debug("edit Button Clicked");
            CbusNodeEditNVarFrame editNvFrame = new CbusNodeEditNVarFrame(mainpane);
            editNvFrame.initComponents(_memo);
            editNvFrame.setNode(node);
            editButton.setEnabled(false);
        };
        editButton.addActionListener(editButtonClicked);
    }
    
    // refresh edit button in case state has been changed since initialisation
    // and the gui has not updated as tab not selected
    public void refreshEditButton() {
        editButton.setEnabled(  ( nodeNVModel.isTableLoaded() ) && ( !mainpane.getEditNvActive() )  );
    }
    
    @Override
    public void tableChanged(TableModelEvent e) {
        log.debug("table changed tb loaded {} ",nodeNVModel.isTableLoaded() );
        editButton.setEnabled(  ( nodeNVModel.isTableLoaded() ) && ( !mainpane.getEditNvActive() )  );
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeVarPane.class);
    
}
