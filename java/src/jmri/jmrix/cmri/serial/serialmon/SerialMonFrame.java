package jmri.jmrix.cmri.serial.serialmon;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialListener;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.serial.SerialNode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Frame displaying (and logging) CMRI serial command messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Chuck Catania  Copyright (C) 2014, 2016, 2017
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {
    // member declarations
    String deltaTCheck = this.getClass().getName()+".DeltaT"; // NOI18N
   
    protected JButton packetfilterButton = new JButton(Bundle.getMessage("FilterPacketsText") );  // NOI18N
    protected static int _DLE    = 0x10;    
   
    private CMRISystemConnectionMemo _memo = null;    

    public SerialMonFrame(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;        
    }

    @Override
    public void dispose() { 
        super.dispose();
  }

    @Override
    protected String title() {
        return Bundle.getMessage("SerialCommandMonTitle")+" "+Bundle.getMessage("Connection")+_memo.getUserName();  // NOI18N
    }

    @Override
    protected void init() {
        // connect to TrafficController
        _memo.getTrafficController().addSerialListener(this);
        // Load the packet filter bits
        initializePacketFilters();
        
        // Add additional controls to the super class window
        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));
        
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        
        packetfilterButton.setText(Bundle.getMessage("FilterPacketsText"));  // NOI18N
        packetfilterButton.setVisible(true);
        packetfilterButton.setToolTipText(Bundle.getMessage("FilterPacketTip"));  // NOI18N
        pane1.add(packetfilterButton);
        packetfilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPacketFilterPerformed(e);
            }
        });
        
        paneA.add(pane1);
        getContentPane().add(paneA);
        pack();
        paneA.setMaximumSize(paneA.getSize());
        pack();
        
        // Move the filter packets button to the middle
        getContentPane().setComponentZOrder(paneA,1);
    }
    
    /**
     * Define system-specific help item
     */
    @Override
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.cmri.serial.serialmon.SerialMonFrame", true);  // NOI18N
    }

    /**
     * Method to initialize packet type filters
     */  
    public void initializePacketFilters()
    {
        // get all configured nodes
        SerialNode node = (SerialNode) _memo.getTrafficController().getNode(0);
        int index = 1,
            pktTypeIndex = 0;
        
        while (node != null)
        {
         // Set all nodes and packet types to be monitored
         //-----------------------------------------------
	 do
         {
            node.setMonitorPacketBit(pktTypeIndex, true);
            pktTypeIndex++;
         } while ( pktTypeIndex < SerialFilterFrame.numMonPkts);


         node = (SerialNode) _memo.getTrafficController().getNode(index);
         index ++;
         pktTypeIndex = 0;
	}
    }
    /**
    * Open the node/packet filter window
    */
    public void openPacketFilterPerformed(ActionEvent e) {
		// create a SerialFilterFrame
		SerialFilterFrame f = new SerialFilterFrame(_memo);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SerialMonFrame starting SerialFilterFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}
    
    //-------------------
    //  Transmit Packets
    //-------------------
    @Override
    public synchronized void message(SerialMessage l) 
    { 
        int aPacketTypeID = 0;
        SerialNode monitorNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(l.getUA());
        
     // Test for node and packets being monitored 
     //------------------------------------------
        if (monitorNode == null) return;       
        if (!monitorNode.getMonitorNodePackets()) return;

        aPacketTypeID = l.getElement(1);
        
	 // check for valid length
        if (l.getNumDataElements() < 2)
	{
            nextLine("Truncated message of length "+l.getNumDataElements()+"\n",l.toString());
            return;
        }
        		
	switch(aPacketTypeID)
	{
	case 0x50:        // (P) Poll
            if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktPoll))
            {                
                nextLine("Poll ua="+l.getUA()+"\n", l.toString());
            }
	break;

	case 0x54:        // (T) Transmit
            if (monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktTransmit))
            {
                StringBuilder sb = new StringBuilder("Transmit ua=");
                sb.append(l.getUA());
                sb.append(" OB=");
                for (int i=2; i<l.getNumDataElements(); i++)
                {                
                    if ((rawCheckBox.isSelected()) && ( l.getElement(i) == _DLE )) //c2
                    {
                        sb.append("<dle>");  // Convert DLE (0x10) to text
                        i++;
                    }
                    
                    sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase());  //c2
                    sb.append(" ");
                }   
                sb.append("\n");
                nextLine(new String(sb), l.toString());
            }
	break;

	case 0x49:        // (I) Initialize 
            if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktInit))
            {
                StringBuilder sb = new StringBuilder("Init ua=");
                sb.append(l.getUA());
                sb.append(" type=");
                int ndp=l.getElement(2); // ndp node type
                sb.append((char)ndp);
                int len = l.getNumDataElements();

		switch (ndp)
		{
                // SMINI/SUSIC/USIC
                    case SerialNode.NDP_USICSUSIC24:
                    case SerialNode.NDP_USICSUSIC32:
                    case SerialNode.NDP_SMINI:
					
                    if (len>=5) 
                    {
                        sb.append(" DL=");
                        sb.append(l.getElement(3)*256+l.getElement(4));
                    }
                
                    if (len>=6) 
                    {
                        sb.append(" NS=");
                        sb.append(l.getElement(5));
                        sb.append(" CT: ");
                        for (int i=6; i<l.getNumDataElements(); i++)
                        {
                            sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase()); //c2
                            sb.append(" ");
                        }
                    }
                    break;
					
		// CPNODE
                    case SerialNode.NDP_CPNODE:
                    if (len>=5) 
                    {                    
                        sb.append(" DL=");
                        sb.append(l.getElement(3)*256+l.getElement(4));
                    }
                    sb.append(" Opts=");
                    int i=5;
                    while (i<l.getNumDataElements())
                    {                   
                        if (l.getElement(i) != _DLE) // skip DLE
                        {    
                            sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase()); //c2
                            sb.append(" ");
                        }
                    i++;
                    }
                    break;
					
                // CPMEGA
                    case SerialNode.NDP_CPMEGA:
                    if (len>=5) 
                    {                    
                        sb.append(" DL=");
                        sb.append(l.getElement(3)*256+l.getElement(4));
                    }
                    sb.append(" Opts=");
                    i=5;
                    while (i<l.getNumDataElements())
                    {
			if (l.getElement(i) != _DLE) // skip DLE
			{    
                            sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase()); //c2
                            sb.append(" ");
			}
                    i++;
                    }
                    break;
					
                    default:
			sb.append("Unrecognized node type NDP: ["+ndp+"] ");
                    break;

                } //ndp case
				
            sb.append("\n");
            nextLine(new String(sb), l.toString());
            }
        break;
		
	default: 
            nextLine("Unrecognized cmd: \""+l.toString()+"\"\n", "");
          
        }  // end packet ID case
    }

    //-------------------
    //  Receive Packets
    //-------------------
    @Override
    public synchronized void reply(SerialReply l) 
    { 
       int aPacketTypeID = 0;
       
     // Test for node and packets being monitored 
     //------------------------------------------
       SerialNode monitorNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(l.getUA());
       
       if (monitorNode == null) return;
       if (!monitorNode.getMonitorNodePackets()) return; 
		
	aPacketTypeID = l.getElement(1);

        // check for valid length
        if (l.getNumDataElements() < 2) 
        {
            nextLine("Truncated reply of length "+l.getNumDataElements()+"\n",l.toString());
//       CMRInetMetricsData.incMetricErrValue( CMRInetMetricsData.CMRInetMetricTruncReply );
		return;
        }		
	switch(aPacketTypeID)
	{
            case 0x52:  // (R) Receive (poll reply)
                if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktRead))
                {
                    StringBuilder sb = new StringBuilder("Receive ua=");
                    sb.append(l.getUA());
                    sb.append(" IB=");
                    for (int i=2; i<l.getNumDataElements(); i++)
                    {
                        if ((rawCheckBox.isSelected()) && ( l.getElement(i) == _DLE))  //c2
                        {
                            sb.append("<dle>");
                            i++;
                        }
                        sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase());  //c2
                        sb.append(" ");
                    }
                sb.append("\n");
                nextLine(new String(sb), l.toString());
                }
            break; 
				
            case 0x45:  // (E) EOT c2
                if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktEOT))
                {
                StringBuilder sb = new StringBuilder("Receive ua=");
                sb.append(l.getUA());
                sb.append(" eot");            
                sb.append("\n");
                nextLine(new String(sb), l.toString());
                }
            break; 
				
            default:
//                CMRInetMetricsData.incMetricErrValue( CMRInetMetricsData.CMRInetMetricUnrecResponse );
                nextLine("Unrecognized response: \""+l.toString()+"\"\n", "");
            break;
        }
    }

    volatile PrintStream logStream = null;

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    StringBuffer linesBuffer = new StringBuffer();
    private final static Logger log = LoggerFactory.getLogger(SerialMonFrame.class);

}

