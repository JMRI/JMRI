package jmri.jmrix.cmri.serial.serialmon;

import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialListener;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Frame displaying (and logging) CMRI serial command messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    private CMRISystemConnectionMemo _memo = null;

    public SerialMonFrame(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return Bundle.getMessage("SerialCommandMonTitle");
    }

    @Override
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
 
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated message of length " + l.getNumDataElements() + "\n",
                    l.toString());
            return;
        } else if (l.isPoll()) {
            nextLine("Poll ua=" + l.getUA() + "\n", l.toString());
        } else if (l.isXmt()) {
            StringBuilder sb = new StringBuilder("Transmit ua=");
            sb.append(l.getUA());
            sb.append(" OB=");
            for (int i = 2; i < l.getNumDataElements(); i++) {
                sb.append(Integer.toHexString(l.getElement(i) & 0x000000ff));
                sb.append(" ");
            }
            sb.append("\n");
            nextLine(new String(sb), l.toString());
        } else if (l.isInit()) {
            StringBuilder sb = new StringBuilder("Init ua=");
            sb.append(l.getUA());
            sb.append(" type=");
            sb.append((char) l.getElement(2));
            int len = l.getNumDataElements();
            if (len >= 5) {
                sb.append(" DL=");
                sb.append(l.getElement(3) * 256 + l.getElement(4));
            }
            if (len >= 6) {
                sb.append(" NS=");
                sb.append(l.getElement(5));
                sb.append(" CT: ");
                for (int i = 6; i < l.getNumDataElements(); i++) {
                    sb.append(Integer.toHexString(l.getElement(i) & 0x000000ff));
                    sb.append(" ");
                }
            }
            sb.append("\n");
            nextLine(new String(sb), l.toString());
        } else {
            nextLine("unrecognized cmd: \"" + l.toString() + "\"\n", "");
        }
    }

/*
  /********************
     Transmit Packets
  *********************
    public synchronized void message(SerialMessage l) 
    { 
       int aPacketTypeID = 0;
     // Test if message is for a monitored node
     //----------------------------------------
		
 //      SerialNode monitorNode = null;       
//       monitorNode = (SerialNode) _memo.l.getUA());
       SerialNode monitorNode = (SerialNode)_memo.getTrafficController().getNode(l.getUA());
		
       if (monitorNode == null) return;       
       if (!monitorNode.getMonitorNodePackets()) return;

       aPacketTypeID = l.getElement(1);
//       if (aPacketTypeID == monitorNode.monPktTypeID[SerialNode.monPktTransmit])
//        System.out.println("Saw "+l.getElement(1)+":"+aPacketTypeID);
        
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
            nextLine("Poll ua="+l.getUA()+"\n", l.toString());
	break;

	case 0x54:        // (T) Transmit
            if (monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktTransmit))
            {
                StringBuilder sb = new StringBuilder("Transmit ua=");
                sb.append(l.getUA());
                sb.append(" OB=");
                for (int i=2; i<l.getNumDataElements(); i++)
                {
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
                        if (l.getElement(i) != 16) // skip DLE
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
			if (l.getElement(i) != 16) // skip DLE
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

 */   

    @Override

    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        // check for valid length
        if (l.getNumDataElements() < 2) {
            nextLine("Truncated reply of length " + l.getNumDataElements() + "\n",
                    l.toString());
            return;
        } else if (l.isRcv()) {
            StringBuilder sb = new StringBuilder("Receive ua=");
            sb.append(l.getUA());
            sb.append(" IB=");
            for (int i = 2; i < l.getNumDataElements(); i++) {
                sb.append(Integer.toHexString(l.getElement(i) & 0x000000ff));
                sb.append(" ");
            }
            sb.append("\n");
            nextLine(new String(sb), l.toString());
        } else {
            nextLine("unrecognized rep: \"" + l.toString() + "\"\n", "");
        }
    }

/*
  /********************
     Receive Packets
  *********************
    public synchronized void reply(SerialReply l) 
    { 
       int aPacketTypeID = 0;

//       SerialNode monitorNode = null;       
//       monitorNode = (SerialNode) _memo.getNodeFromAddress(l.getUA());
       SerialNode monitorNode = (SerialNode)_memo.getTrafficController().getNode(l.getUA());		
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
                if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktTransmit))
                {
                    StringBuilder sb = new StringBuilder("Receive ua=");
                    sb.append(l.getUA());
                    sb.append(" IB=");
                    for (int i=2; i<l.getNumDataElements(); i++)
                    {
                        sb.append(Integer.toHexString(l.getElement(i)&0x000000ff).toUpperCase());  //c2
                        sb.append(" ");
                    }
                sb.append("\n");
                nextLine(new String(sb), l.toString());
                }
            break; 
				
            case 0x45:  // (E) EOT c2
                if(monitorNode.getMonitorPacketBit(SerialFilterFrame.monPktTransmit))
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
 */  
}

