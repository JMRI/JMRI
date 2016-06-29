package jmri.jmrix.cmri.serial.serialmon;

import jmri.jmrix.cmri.serial.SerialListener;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.cmrinetmetrics.CMRInetMetricsData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) CMRI serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001
 * @author	    Chuck Catania  Copyright (C) 2014, 2015, 2016
 * @version         $Revision: 17977 $
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    private CMRISystemConnectionMemo _memo = null;
    protected JButton packetFilterButton = new JButton();  //c2

    public SerialMonFrame(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return "CMRI Serial Command Monitor";
    }

    protected void init() {
        // connect to TrafficController
        _memo.getTrafficController().addSerialListener(this);
    }

    @Override
    public void dispose() {
        _memo.getTrafficController().removeSerialListener(this);
        super.dispose();
    }

    @Override
    public void initComponents() throws Exception {
         super.initComponents();

        packetFilterButton.setText("Filter Packets");
        packetFilterButton.setVisible(true);
        packetFilterButton.setToolTipText("Opens CMRInet Packet Filter");

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(packetFilterButton);
        getContentPane().add(pane1);

        packetFilterButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               openPacketFilterPerformed(e);
           }
       });


    }



    public void openPacketFilterPerformed(ActionEvent e) {
                // create a SerialFilterFrame
                SerialFilterFrame f = new SerialFilterFrame();
                try {
                        f.initComponents();
                        }
                catch (Exception ex) {
                        log.warn("SerialFilterAction starting SerialFilterFrame: Exception: "+ex.toString());
                        }
                f.setVisible(true);
        }

  /********************
     Transmit Packets
  *********************/
    public synchronized void message(SerialMessage l) 
    { 
       int aPacketTypeID = 0;
     // Test if message is for a monitored node
     //----------------------------------------
       SerialNode monitorNode = null;       
       monitorNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(l.getUA());
       if (monitorNode == null) return;       
       if (!monitorNode.getMonitorNodePackets()) return;

       aPacketTypeID = l.getElement(1);
//       if (aPacketTypeID == monitorNode.monPktTypeID[SerialNode.monPktTransmit])
//        System.out.println("Saw "+l.getElement(1)+":"+aPacketTypeID);
        
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated message of length "+l.getNumDataElements()+"\n",
                            l.toString());
//            CMRIMetricsData.incMetricErrValue( CMRIMetricsData.CMRIMetricTruncRecv );
            return;
        } else if (l.isPoll())
        {
            if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktPoll))
            nextLine("Poll ua="+l.getUA()+"\n", l.toString());
        } else if (l.isXmt())
        {
            if (monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktTransmit))
            {
            StringBuilder sb = new StringBuilder("Transmit ua=");
            sb.append(l.getUA());
            sb.append(" OB=");
            for (int i=2; i<l.getNumDataElements(); i++) {
                sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase());  //c2
                sb.append(" ");
            }
            sb.append("\n");
            nextLine(new String(sb), l.toString());
            }
        } else if (l.isInit())
        {
            if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktInit))
            {
            StringBuilder sb = new StringBuilder("Init ua=");
            sb.append(l.getUA());
            sb.append(" type=");
            int ua=l.getElement(2);
            sb.append((char)ua);
            int len = l.getNumDataElements();
            
            //  SMINI/SUSIC/USIC
            if (ua != 67)
            {
                //int len = l.getNumDataElements();
                if (len>=5) {
                    sb.append(" DL=");
                    sb.append(l.getElement(3)*256+l.getElement(4));
               }
                
                if (len>=6) {
                    sb.append(" NS=");
                    sb.append(l.getElement(5));
                    sb.append(" CT: ");
                    for (int i=6; i<l.getNumDataElements(); i++) {
                        sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase()); //c2
                        sb.append(" ");
                 }
               }
            }
            else
             // CPNODE
             {
                if (len>=5) {                    
                    sb.append(" DL=");
                    sb.append(l.getElement(3)*256+l.getElement(4));
                }
               sb.append(" Opts=");
               int i=5;
               while (i<l.getNumDataElements())
               {
                 if (l.getElement(i) != 16) // skip DLE
                 {    
                    sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase()); //c2
                    sb.append(" ");
                 }
                 i++;
               }
             }
             sb.append("\n");
            nextLine(new String(sb), l.toString());
            }
        } else
        {
//            CMRIMetricsData.incMetricErrValue( CMRIMetricsData.CMRIMetricUnrecCommand );
            nextLine("unrecognized cmd: \""+l.toString()+"\"\n", "");
        }
    }
    
  /********************
     Receive Packets
  *********************/
    public synchronized void reply(SerialReply l) 
    { 
       SerialNode monitorNode = null;       
       monitorNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(l.getUA());
       if (monitorNode == null) return;
       if (!monitorNode.getMonitorNodePackets()) return;

        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length "+l.getNumDataElements()+"\n",
                            l.toString());
//           CMRIMetricsData.incMetricErrValue( CMRIMetricsData.CMRIMetricTruncReply );
           return;
        } else
            if (l.isRcv())
            {
                if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktTransmit))
                {
                StringBuilder sb = new StringBuilder("Receive ua=");
                sb.append(l.getUA());
                sb.append(" IB=");
                for (int i=2; i<l.getNumDataElements(); i++) {
                    sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase());  //c2
                    sb.append(" ");
                }
                sb.append("\n");
                nextLine(new String(sb), l.toString());
                }
            } 
            else 
            if (l.isEOT())  //c2
            {    //c2 
                if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktTransmit))
                {
                StringBuilder sb = new StringBuilder("Receive ua=");
                sb.append(l.getUA());
                sb.append(" eot");            
                sb.append("\n");
                nextLine(new String(sb), l.toString());
                }
            } 
            else
            {
//                CMRIMetricsData.incMetricErrValue( CMRIMetricsData.CMRIMetricUnrecResponse );
                nextLine("unrecognized rep: \""+l.toString()+"\"\n", "");
            }
    }

    private static final Logger log = LoggerFactory.getLogger(SerialMonFrame.class.getName());

}
