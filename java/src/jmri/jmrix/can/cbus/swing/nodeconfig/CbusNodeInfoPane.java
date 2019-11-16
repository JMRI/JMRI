package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2018
 */
public class CbusNodeInfoPane extends JPanel {
    
    private JPanel infoPane;
    private JButton nodesupportlinkbutton;
    private URI supportlink;
    private CbusNode nodeOfInterest;
    private JLabel header;
    private JPanel menuPane;
    private JTextArea textArea;

    /**
     * Create a new instance of CbusNodeInfoPane.
     */
    public CbusNodeInfoPane() {
        super();
        
        nodesupportlinkbutton = new JButton();
        nodesupportlinkbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUri(supportlink);
            }
        });
        
    }
    
    /**
     * Initialise the pane for a particular CbusNode ( or CbusBackupNode )
     * @param node the node to display info for
     */
    public void initComponents(CbusNode node) {
        
        if ( node == null ){
            if (infoPane != null ){ 
                infoPane.setVisible(false);
            }
            return;
        }
        if ( node == nodeOfInterest ){
            return;
        }
        if ( nodeOfInterest != null ) {
            nodeOfInterest.removeInfoPane(this);
        }
        nodeOfInterest = node;
        nodeOfInterest.addInfoPane(this); // register for updates in case still loading from network
        
        menuPane = new JPanel();
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setMargin( new java.awt.Insets(10,10,10,10) );
        JScrollPane textAreaPanel = new JScrollPane(textArea);
        
        header = new JLabel("");
        menuPane.add(header);
        menuPane.add(nodesupportlinkbutton);
        
        this.setLayout(new BorderLayout() );
        
        // sets the text area text and support link button etc.
        paramsHaveUpdated();
        
        if (infoPane != null ){ 
            infoPane.setVisible(false);
        }
        infoPane = null;
        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );
        
        infoPane.add(menuPane, BorderLayout.PAGE_START);
        infoPane.add(textAreaPanel, BorderLayout.CENTER);
        this.add(infoPane);
        validate();
        repaint();
        
    }
    
    /**
     * Recalculates pane following notification from CbusNode that parameters have changed
     */
    public void paramsHaveUpdated() {
        
        nodesupportlinkbutton.setVisible(false);
                
        header.setText("<html><h3>" 
            + CbusNodeConstants.getManu(nodeOfInterest.getParameter(1)) 
            + " " 
            + nodeOfInterest.getNodeTypeName()
            + "</h3></html>");
        
        StringBuilder textAreaString = new StringBuilder();
        
        textAreaString.append(Bundle.getMessage("NodeNumberTitle"));
        textAreaString.append(": " );
        textAreaString.append(nodeOfInterest.getNodeNumber());
        textAreaString.append(System.getProperty("line.separator"));
        
        if (nodeOfInterest.getParameter(1) > -1 && nodeOfInterest.getParameter(3) > -1 ) {
        
            textAreaString.append(Bundle.getMessage("ManufacturerType",
                nodeOfInterest.getParameter(1),
                CbusNodeConstants.getManu(nodeOfInterest.getParameter(1)),
                nodeOfInterest.getParameter(3)));
                
            textAreaString.append(System.getProperty("line.separator"));
        
        }
        
        if (!nodeOfInterest.getNodeTypeName().isEmpty()){
            textAreaString.append(Bundle.getMessage("IdentifiesAs",
                nodeOfInterest.getNodeTypeName(),
                CbusNodeConstants.getModuleTypeExtra(
                    nodeOfInterest.getParameter(1),nodeOfInterest.getParameter(3)))
            );
            textAreaString.append(System.getProperty("line.separator"));
        }
        
        if ((nodeOfInterest.getParameter(2)>0) && (nodeOfInterest.getParameter(7)>0)) {
            textAreaString.append (Bundle.getMessage("FirmwareVer",
                nodeOfInterest.getParameter(7),
                Character.toString((char) nodeOfInterest.getParameter(2))));
            
            if ((nodeOfInterest.getParameter(0)>19) && (nodeOfInterest.getParameter(20)>0) ){
                textAreaString.append (Bundle.getMessage("FWBeta")); 
                textAreaString.append (nodeOfInterest.getParameter(20));
            }
            textAreaString.append(System.getProperty("line.separator"));
        }

        if (nodeOfInterest.getParameter(6)>0) {
            textAreaString.append (Bundle.getMessage("NodeVariables"));
            textAreaString.append (" : ");
            textAreaString.append ( nodeOfInterest.getParameter(6) );
            textAreaString.append(System.getProperty("line.separator"));
        }            
        
        if (nodeOfInterest.getParameter(0) > -1) {
            textAreaString.append (Bundle.getMessage("Parameters"));
            textAreaString.append (" : ");
            textAreaString.append ( nodeOfInterest.getParameter(0) );
            textAreaString.append(System.getProperty("line.separator"));
        }
        
        if ( nodeOfInterest.getTotalNodeEvents()> -1 ) {
            textAreaString.append ("Current Events : ");
            textAreaString.append ( nodeOfInterest.getTotalNodeEvents() );
            textAreaString.append(System.getProperty("line.separator"));
        }

        if (nodeOfInterest.getParameter(4)>0) {
            textAreaString.append ("Max Events : ");
            textAreaString.append (nodeOfInterest.getParameter(4));
            textAreaString.append(System.getProperty("line.separator"));
        
        }            
        
        if (nodeOfInterest.getParameter(5)>0) {
            textAreaString.append ("Max Event Variables per Event : ");
            textAreaString.append (nodeOfInterest.getParameter(5));
            textAreaString.append(System.getProperty("line.separator"));
        }
        
        if ((nodeOfInterest.getParameter(0)>9) && (nodeOfInterest.getParameter(10)>0)) {           
            textAreaString.append (CbusNodeConstants.getBusType(nodeOfInterest.getParameter(10)));
            textAreaString.append (" ");
            textAreaString.append (Bundle.getMessage("BusType"));
            textAreaString.append(System.getProperty("line.separator"));
        }
        
        textAreaString.append ("Current Node Data Bytes : ");
        textAreaString.append ( Math.max(0,nodeOfInterest.totalNodeBytes()) );
        textAreaString.append(System.getProperty("line.separator"));
        
        if ( nodeOfInterest.getNodeBackupFile() != null ) {
            textAreaString.append(System.getProperty("line.separator"));
            
            textAreaString.append ("Entries in Node xml file : ");
            textAreaString.append (nodeOfInterest.getNodeBackupFile().getBackups().size());
            textAreaString.append(System.getProperty("line.separator"));
            
            textAreaString.append ("Num Backups in Node xml file : ");
            textAreaString.append (nodeOfInterest.getNumBackups());
            textAreaString.append(System.getProperty("line.separator"));
            
            textAreaString.append ("First entry : ");
            textAreaString.append (nodeOfInterest.getFirstBackupTime());
            textAreaString.append(System.getProperty("line.separator"));
            
            textAreaString.append ("Last entry : ");
            textAreaString.append (nodeOfInterest.getLastBackupTime());
            
            textAreaString.append(System.getProperty("line.separator"));
        
        }
        
        if ( !nodeOfInterest.getsendsWRACKonNVSET() ) {
            textAreaString.append ("Sends WRACK Following NV Set : ");             
            textAreaString.append ( nodeOfInterest.getsendsWRACKonNVSET() );
            textAreaString.append(System.getProperty("line.separator"));
        }
        
        if (!nodeOfInterest.getParameterHexString().isEmpty()) {
            textAreaString.append(System.getProperty("line.separator"));
            textAreaString.append ("Parameter Hex String : ");
            textAreaString.append (nodeOfInterest.getParameterHexString());
            textAreaString.append(System.getProperty("line.separator"));
        }
        
        textAreaString.append(System.getProperty("line.separator"));
        for (int i = 1; i <= nodeOfInterest.getParameter(0); i++) {
            if ( nodeOfInterest.getParameter(i) > -1 ) {
                textAreaString.append ("Parameter ");
                textAreaString.append (i);
                textAreaString.append (" : ");
                textAreaString.append ( nodeOfInterest.getParameter(i) );
                textAreaString.append (" (dec)");
                textAreaString.append(System.getProperty("line.separator"));
            }
        }
        
        //   nodePartTwobuilder.append ("<p> Is Bootable Y / N</p>");
        //   nodePartTwobuilder.append ("<p> Processor : </p>");
        //   nodePartTwobuilder.append ("<p> Flags </p>");
        
        nodesupportlinkbutton.setToolTipText("<html>" + CbusNodeConstants.getManu(nodeOfInterest.getParameter(1)) + 
        " " + CbusNodeConstants.getModuleType(nodeOfInterest.getParameter(1),nodeOfInterest.getParameter(3)) + 
        " " + Bundle.getMessage("Support") + "</html>");
        
        String supportLinkStr = CbusNodeConstants.getModuleSupportLink(nodeOfInterest.getParameter(1),nodeOfInterest.getParameter(3));
        
        if ( !supportLinkStr.isEmpty() ) {
            nodesupportlinkbutton.setText(supportLinkStr);
            try {
                supportlink=new URI(supportLinkStr);
                nodesupportlinkbutton.setVisible(true);
            } 
            catch (URISyntaxException ex) {
                log.warn("Unable to create support link URI for module type {} {}", nodeOfInterest.getParameter(3), ex);
            }
            
        } else {
            nodesupportlinkbutton.setVisible(false);
        }
        
        textArea.setText(textAreaString.toString());
        textArea.setCaretPosition(0);
        
    }
    
    private static void openUri(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                log.warn("Unable to get URI for {} {}", uri, e);
            }
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeInfoPane.class);
    
}
