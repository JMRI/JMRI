package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEditNVarFrame extends JmriJFrame implements TableModelListener {
    
    private JPanel infoPane = new JPanel();
    private JTabbedPane tabbedPane;
    private CbusNodeTableDataModel nodeModel = null;
    private int _nodeNum;
    private CanSystemConnectionMemo _memo;
    private CbusNodeNVTableDataModel nodeNVModel;
    private JButton saveNvButton;
    private JButton resetNvButton;
    private NodeConfigToolPane mainpane;
    private jmri.util.swing.BusyDialog busy_dialog;

    /**
     * Create a new instance of CbusEventHighlightPanel.
     */
    protected CbusNodeEditNVarFrame( NodeConfigToolPane main ) {
        super();
        mainpane = main;
    }

    public void initComponents(CanSystemConnectionMemo memo) {
        
        _memo = memo;
        mainpane.setEditNvActive(true);
        
        try {
            nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
            
            nodeNVModel = new CbusNodeNVTableDataModel(memo, 5,
            CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
            
            nodeNVModel.addTableModelListener(this);
            
        } catch (NullPointerException e) {
            log.error("Unable to get Node Table from Instance Manager");
        }
        
        this.add(infoPane);
        
    }
    
    public void setNode( CbusNode node ) {
        
        log.debug("setnode {}",node);
        
        _nodeNum = node.getNodeNumber();

        if (infoPane != null ){ 
            infoPane.setVisible(false);
            infoPane = null;
        }

        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );

        JPanel nvMenuPane = new JPanel();
        nvMenuPane.setLayout(new BoxLayout(nvMenuPane, BoxLayout.Y_AXIS));
        JPanel buttonPane = new JPanel();
      
        saveNvButton = new JButton(("Save"));
        saveNvButton.setToolTipText(("Update Node"));
        resetNvButton = new JButton(Bundle.getMessage("Reset"));
        resetNvButton.setToolTipText(("Reset table New NV values"));
        setSaveCancelButtonsActive ( false );
        
        buttonPane.add(saveNvButton );
        buttonPane.add(resetNvButton ); 
        
        nvMenuPane.add(buttonPane);
        nvMenuPane.add( new JSeparator(SwingConstants.HORIZONTAL) );
        
        tabbedPane = new JTabbedPane();
        
        JPanel generic = new JPanel();
        JPanel template = new JPanel();
        
        generic.setLayout( new BorderLayout() );
        template.setLayout( new BorderLayout() );
        
        CbusNodeNVEditTablePane genericNVTable = new CbusNodeNVEditTablePane(nodeNVModel);
        genericNVTable.initComponents(_memo);
        genericNVTable.setNode( node , mainpane );
        generic.add( genericNVTable );
        
        tabbedPane.addTab(("Template"), template);
        tabbedPane.addTab(("Generic"), generic);
        
        tabbedPane.setEnabledAt(0,false);
        tabbedPane.setSelectedIndex(1);
        
        infoPane.add(nvMenuPane, BorderLayout.PAGE_START);
        infoPane.add(tabbedPane, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(530, 300));
        
        this.add(infoPane);
        
        pack();
        this.setResizable(true);
        
        validate();
        repaint();
        
        setTitle(getTitle());
        setVisible(true);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                mainpane.setEditNvActive(false);
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                mainpane.setEditNvActive(false);
            }
        });
        
        ActionListener reset = ae -> {
            nodeNVModel.resetNewNvs();
        };
        resetNvButton.addActionListener(reset);
        
        ActionListener save = ae -> {
            showConfirmThenSave();
        };
        saveNvButton.addActionListener(save);
        
    }
    
    private void showConfirmThenSave(){
        
        String nodeName = nodeModel.getNodeNumberName( _nodeNum );
        
        busy_dialog = new jmri.util.swing.BusyDialog(this, "Write NV "+nodeName, false);
        
        int changedtot = nodeNVModel.getCountDirty();
        StringBuffer buf = new StringBuffer();
        buf.append("<html> ");
        buf.append( changedtot );
        buf.append(" NV");
        if (  changedtot >1 ) {
            buf.append("'s");
        }
        buf.append(" changed.<br>");
        buf.append( Bundle.getMessage("NVConfirmWrite",nodeName) );
        buf.append("</html>");
        
        int response = JOptionPane.showConfirmDialog(null,
                ( buf.toString() ),
                ( Bundle.getMessage("NVConfirmWrite",nodeName)),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if ( response != JOptionPane.YES_OPTION ) {
            busy_dialog = null;
            return;
        } else {
            
            busy_dialog.start();
            
            // request the local nv model pass the nv update request to the CbusNode
            
            jmri.util.ThreadingUtil.runOnLayout( ()->{
                nodeNVModel.passChangedNvsToNode(this);
            });
        }
    }
    
    public void nVTeachComplete(int numErrors){
        
        this.dispose();
        mainpane.setEditNvActive(false);
        busy_dialog.finish();
        
        if ( numErrors > 0 ) {
            
            JOptionPane.showMessageDialog(null, 
                Bundle.getMessage("NVSetFailTitle",numErrors), Bundle.getMessage("WarningTitle"),
                JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    @Override
    public String getTitle() {
        if ( nodeModel != null ) {
            return "Edit NVs " + nodeModel.getNodeNumberName( _nodeNum );
        }
        else {
            return("Edit NVs");
        }
    }
    
    public void setSaveCancelButtonsActive ( Boolean newstate ) {
        saveNvButton.setEnabled(newstate);
        resetNvButton.setEnabled(newstate);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        setSaveCancelButtonsActive( nodeNVModel.isTableDirty() );
    }
    
    @Override
    public void dispose(){
        if ( nodeNVModel !=null ) {
            nodeNVModel.removeTableModelListener(this);
            nodeNVModel.dispose();
        }
        
        super.dispose();
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarFrame.class);
    
}
