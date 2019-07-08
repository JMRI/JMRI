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
public class CbusNodeEditNVarPane extends JPanel implements TableModelListener {
    
    private JPanel infoPane;
    private JTabbedPane tabbedPane;
    private CanSystemConnectionMemo _memo;
    private CbusNodeNVTableDataModel nodeNVModel;
    private JButton saveNvButton;
    private JButton resetNvButton;
    private jmri.util.swing.BusyDialog busy_dialog;
    private CbusNode nodeOfInterest = null;

    /**
     * Create a new instance of CbusEventHighlightPanel.
     */
    protected CbusNodeEditNVarPane( NodeConfigToolPane main ) {
        super();
    }

    public void initComponents(CanSystemConnectionMemo memo) {
        
        _memo = memo;
        
        try {
            nodeNVModel = new CbusNodeNVTableDataModel(memo, 5,
            CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
            
            nodeNVModel.addTableModelListener(this);
            
        } catch (NullPointerException e) {
            log.error("Unable to get Node Table from Instance Manager");
        }
        infoPane = new JPanel();
        setLayout(new BorderLayout() );
        
        // this.add(infoPane);
        
    }
    
    public void setNode( CbusNode node ) {
        
        if (node == nodeOfInterest) {
            return;
        }
        
        nodeOfInterest = node;
        
        nodeNVModel.setNode(nodeOfInterest);
        
        log.debug("setnode {}",nodeOfInterest);

        if (infoPane != null ){ 
            infoPane.setVisible(false);
            
        }
        infoPane = null;

        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );

        JPanel nvMenuPane = new JPanel();
       // nvMenuPane.setLayout(new BoxLayout(nvMenuPane, BoxLayout.Y_AXIS));
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
        
      //  generic.setLayout( new BorderLayout() );
      //  template.setLayout( new BorderLayout() );
        
        CbusNodeNVEditTablePane genericNVTable = new CbusNodeNVEditTablePane(nodeNVModel);
        genericNVTable.initComponents(_memo);
        genericNVTable.setNode( nodeOfInterest );
        generic.add( genericNVTable );
        
        tabbedPane.addTab(("Generic"), genericNVTable);
        tabbedPane.addTab(("Template"), template);
        
        tabbedPane.setEnabledAt(1,false);
        tabbedPane.setSelectedIndex(0);
        
        infoPane.add(nvMenuPane, BorderLayout.PAGE_START);
        infoPane.add(tabbedPane, BorderLayout.CENTER);
        
       // setPreferredSize(new Dimension(530, 300));
        
        this.add(infoPane);
        
        validate();
        repaint();
        
        setVisible(true);
        
        ActionListener reset = ae -> {
            resetNVs();
        };
        resetNvButton.addActionListener(reset);
        
        ActionListener save = ae -> {
            showConfirmThenSave();
        };
        saveNvButton.addActionListener(save);
        
    }
    
    public boolean areNvsDirty(){
        log.debug("Table Dirty {}",nodeNVModel.isTableDirty());
        return nodeNVModel.isTableDirty();
    }
    
    public void resetNVs(){
        nodeNVModel.resetNewNvs();
    }
    
    private void showConfirmThenSave(){
        
        String nodeName = nodeOfInterest.getNodeNumberName();
        
        busy_dialog = new jmri.util.swing.BusyDialog(null, "Write NV "+nodeName, false);
        
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
        
       // this.dispose();
        busy_dialog.finish();
        
        if ( numErrors > 0 ) {
            
            JOptionPane.showMessageDialog(null, 
                Bundle.getMessage("NVSetFailTitle",numErrors), Bundle.getMessage("WarningTitle"),
                JOptionPane.ERROR_MESSAGE);
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
    
    public void dispose(){
        if ( nodeNVModel !=null ) {
            nodeNVModel.removeTableModelListener(this);
            nodeNVModel.dispose();
        }
        
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarPane.class);
    
}
