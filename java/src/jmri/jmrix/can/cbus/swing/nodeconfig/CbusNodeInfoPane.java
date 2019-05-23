package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2018
 */
public class CbusNodeInfoPane extends JPanel {
    
    private JScrollPane infoPane;
    private JButton nodesupportlinkbutton;
    private URI supportlink;

    /**
     * Create a new instance of CbusEventHighlightPanel.
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

    public void initComponents(CbusNode node) {
        
        if ( node == null ){
            
            this.setVisible(false);
            
            return;
        }
        
        // Pane to hold Node
        JPanel evPane = new JPanel();
        evPane.setLayout(new BoxLayout(evPane, BoxLayout.Y_AXIS));

        nodesupportlinkbutton.setVisible(false);

        JLabel contentTwo;
        StringBuilder nodepropbuilder;
        StringBuilder nodePartTwobuilder = new StringBuilder();
        
        CbusNode nodeOfInterest = node;
        

        JLabel header = new JLabel("<html><h2>" 
            + CbusNodeConstants.getManu(nodeOfInterest.getParameter(1)) 
            + " " 
            + nodeOfInterest.getNodeTypeName()
            + "</h2></html>");
        
        nodepropbuilder = new StringBuilder();
        nodepropbuilder.append("<html>" );
        
        nodepropbuilder.append ("<p>");
        nodepropbuilder.append( CbusNodeConstants.getModuleTypeExtra(nodeOfInterest.getParameter(1),nodeOfInterest.getParameter(3)));
        nodepropbuilder.append ("</p>");
        
        nodepropbuilder.append("<p> Node Number : " );
        nodepropbuilder.append( nodeOfInterest.getNodeNumber() );
        nodepropbuilder.append("</p> " );
        
        // part 2
        nodePartTwobuilder.append("<html>" );
        
        if ((nodeOfInterest.getParameter(2)>0) && (nodeOfInterest.getParameter(7)>0)) {
        
            nodePartTwobuilder.append ("<p>");
            nodePartTwobuilder.append (Bundle.getMessage("FirmwareVer"));
            nodePartTwobuilder.append (nodeOfInterest.getParameter(7));
            int converttochar = nodeOfInterest.getParameter(2);
            nodePartTwobuilder.append(Character.toString((char) converttochar));
            
            if ((nodeOfInterest.getParameter(0)>19) && (nodeOfInterest.getParameter(20)>0) ){
                nodePartTwobuilder.append (" "); 
                nodePartTwobuilder.append (Bundle.getMessage("FWBeta")); 
                nodePartTwobuilder.append (nodeOfInterest.getParameter(20));
            }
            nodePartTwobuilder.append ("</p>");
        }

        if (nodeOfInterest.getParameter(6)>0) {
            nodePartTwobuilder.append ("<p>Total Node Variables: ");
            nodePartTwobuilder.append ( nodeOfInterest.getParameter(6) );
            nodePartTwobuilder.append ("</p>");
        }            
        
        if (nodeOfInterest.getParameter(0) > -1) {
            nodePartTwobuilder.append ("<p> Total Node Parameters : " );
            nodePartTwobuilder.append ( nodeOfInterest.getParameter(0) );
            nodePartTwobuilder.append ("</p>");
            nodePartTwobuilder.append ("<p> </p>");
        }
        
        nodePartTwobuilder.append ("<p>Current Node Data Bytes: ");
        nodePartTwobuilder.append ( Math.max(0,nodeOfInterest.totalNodeBytes()) );
        nodePartTwobuilder.append ("</p>");
        
        if ( nodeOfInterest.getTotalNodeEvents()> -1 ) {
            nodePartTwobuilder.append ("<p> Current Events: ");
            nodePartTwobuilder.append ( nodeOfInterest.getTotalNodeEvents() );
            nodePartTwobuilder.append ("</p>");
        }

        if (nodeOfInterest.getParameter(4)>0) {
            nodePartTwobuilder.append ("<p> Max Events: ");
            nodePartTwobuilder.append (nodeOfInterest.getParameter(4));
            nodePartTwobuilder.append ("</p>");
        
        }            
        
        if (nodeOfInterest.getParameter(5)>0) {
            nodePartTwobuilder.append ("<p>Event Variables per event: ");
            nodePartTwobuilder.append (nodeOfInterest.getParameter(5));
            nodePartTwobuilder.append ("</p>");
        }
        
        if ((nodeOfInterest.getParameter(0)>9) && (nodeOfInterest.getParameter(10)>0)) {
            nodePartTwobuilder.append ("<p>");             
            nodePartTwobuilder.append (CbusNodeConstants.getBusType(nodeOfInterest.getParameter(10)));
            nodePartTwobuilder.append (" ");
            nodePartTwobuilder.append (Bundle.getMessage("BusType"));
            nodePartTwobuilder.append ("</p><p></p>");
        }
        
        
        if ( !nodeOfInterest.getsendsWRACKonNVSET() ) {
            nodePartTwobuilder.append ("<p>Sends WRACK Following NV Set : ");             
            nodePartTwobuilder.append ( nodeOfInterest.getsendsWRACKonNVSET() );
            nodePartTwobuilder.append ("</p><p></p>");
        }
        //   nodePartTwobuilder.append ("<p> Is Bootable Y / N</p>");
        //   nodePartTwobuilder.append ("<p> Processor : </p>");
        
        //   nodePartTwobuilder.append ("<p> Flags </p>");
        
        nodePartTwobuilder.append("</html>" );
        
        JLabel content = new JLabel(nodepropbuilder.toString());
        contentTwo = new JLabel(nodePartTwobuilder.toString());
        
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
        
        evPane.add(header);
        evPane.add(content);
        evPane.add(contentTwo);
        evPane.add(nodesupportlinkbutton);
        
        evPane.setBorder(new EmptyBorder(0, 100, 20, 100));
        
        if (infoPane != null ){ 
            infoPane.setVisible(false);
        }
        infoPane = null;
        infoPane = new JScrollPane(evPane);
        
        setLayout(new BorderLayout() );
        
        
        this.add(infoPane);
        
        validate();
        repaint();
        this.setVisible(true);
        
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
