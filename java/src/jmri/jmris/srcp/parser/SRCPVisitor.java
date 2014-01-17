//SRCPVisitor.java

package jmri.jmris.srcp.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;

/* This class provides an interface between the JavaTree/JavaCC 
 * parser for the SRCP protocol and the JMRI back end.
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */

public class SRCPVisitor implements SRCPParserVisitor {

  private String outputString=null;

  public String getOutputString(){
       return outputString;
  }

  public Object visit(SimpleNode node, Object data)
  {
    log.debug("Generic Visit " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASThandshakecommand node,Object data)
  {
    log.debug("Handshake Mode Command " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcommand node,Object data)
  {
    log.debug("Command " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTgo node,Object data)
  {
    log.debug("Go " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTget node, Object data)
  {
    log.debug("Get " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("POWER")) {
       // This is a message asking for the power status
       try {
       ((jmri.jmris.ServiceHandler)data).getPowerServer().sendStatus(
                           InstanceManager.powerManagerInstance().getPower());
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GA"))
    {
       // This is a message asking for the status of a "General Accessory".
       int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       try {
       ((jmri.jmris.srcp.JmriSRCPTurnoutServer)((jmri.jmris.ServiceHandler)data).getTurnoutServer()).sendStatus(bus,address);
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("FB"))
    {
       // This is a message asking for the status of a FeedBack sensor.
       int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       try {
       ((jmri.jmris.srcp.JmriSRCPSensorServer)((jmri.jmris.ServiceHandler)data).getSensorServer()).sendStatus(bus,address);
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SM"))
    {
      // This is a Service Mode read request.
      int modeno=jmri.Programmer.REGISTERMODE;
      if(node.jjtGetChild(3).getClass()==ASTcv.class)
         modeno=jmri.Programmer.DIRECTBYTEMODE;
      else if(node.jjtGetChild(3).getClass()==ASTcvbit.class)
         modeno=jmri.Programmer.DIRECTBITMODE;
     
      int cv = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
       //try {
       ((jmri.jmris.srcp.JmriSRCPProgrammerServer)((jmri.jmris.ServiceHandler)data).getProgrammerServer()).readCV(modeno,cv);
       //} catch(java.io.IOException ie) {
       //}
       
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GL"))
    {
      // This is a Generic Loco request
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("TIME"))
    {
      // This is a Time request
       try {
       ((jmri.jmris.ServiceHandler)data).getTimeServer().sendTime();
       } catch(java.io.IOException ie) {
       }

    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SERVER"))
    {
       // for the GET <bus> SERVER request, we return the current server 
       // state.  In JMRI, we always return "Running".
       outputString="100 INFO 0 SERVER RUNNING";
    }
    return data;
  }


  public Object visit(ASTset node, Object data)
  {
    log.debug("Set " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("POWER"))
    {
       try {
       ((jmri.jmris.ServiceHandler)data).getPowerServer().parseStatus(
                  ((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       } catch(java.io.IOException ie) {
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GA"))
    {
       int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       //int port = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       int value = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));

       try {
       ((jmri.jmris.srcp.JmriSRCPTurnoutServer)((jmri.jmris.ServiceHandler)data).getTurnoutServer()).parseStatus(bus,address,value);
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("FB"))
    {
       int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       int value = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       try {
       ((jmri.jmris.srcp.JmriSRCPSensorServer)((jmri.jmris.ServiceHandler)data).getSensorServer()).parseStatus(bus,address,value);
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SM"))
    {
      // This is a Service Mode write request
      int modeno=jmri.Programmer.REGISTERMODE;
      if(node.jjtGetChild(3).getClass()==ASTcv.class)
         modeno=jmri.Programmer.DIRECTBYTEMODE;
      else if(node.jjtGetChild(3).getClass()==ASTcvbit.class)
         modeno=jmri.Programmer.DIRECTBITMODE;
      int cv = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
      int value = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(5)).jjtGetValue()));
     
       //try {
       ((jmri.jmris.srcp.JmriSRCPProgrammerServer)((jmri.jmris.ServiceHandler)data).getProgrammerServer()).writeCV(modeno,cv,value);
       //} catch(java.io.IOException ie) {
       //}
       
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GL"))
    {
      // This is a Generic Loco request
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("TIME"))
    {
      // This is a Time request
       try {
         jmri.jmris.srcp.JmriSRCPTimeServer ts=(jmri.jmris.srcp.JmriSRCPTimeServer)(((jmri.jmris.ServiceHandler)data).getTimeServer());
         int julDay = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       int hour = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       int minute = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
       int second = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
         
         // set the time
         ts.parseTime(julDay,hour,minute,second);
         // and start the clock.
         ts.startTime();
         ts.sendTime();
       } catch(java.io.IOException ie) {
       }
    }
    return data;
  }


  public Object visit(ASTterm node, Object data)
  {
    log.debug("TERM " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SERVER"))
    {
       // for the TERM <bus> SERVER request, the protocol requries that
       // we terminate all connections and reset the state to the initial
       // state.  Since we may have a local GUI controlling things, we
       // ignore the request, but send the proper return value to the
       // requesting client.
       outputString="200 OK";
       return data;
    }
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTcheck node, Object data)
  {
    log.debug("CHECK " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTverify node,java.lang.Object data)
  {
    log.debug("VERIFY " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTreset node,java.lang.Object data)
  {
    log.debug("RESET " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SERVER"))
    {
       // for the RESET <bus> SERVER request, the protocol requries that
       // we re-initialize the server.  Since we may have a local GUI 
       // controlling things, we ignore the request, but send a prohibited
       // response to the requesting client.
       outputString="413 ERROR temporarily prohibited";
       return data;
    }
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTinit node,java.lang.Object data)
  {
    log.debug("INIT " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("POWER")) {
        /* Power really has nothing to do in JMRI */
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GA")) {
        /* Initilize a new accessory */
       int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       try {
       ((jmri.jmris.srcp.JmriSRCPTurnoutServer)((jmri.jmris.ServiceHandler)data).getTurnoutServer()).initTurnout(bus,address,((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
       
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GL")) {
        /* Initilize a new locomotive */
       //int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
       //int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("TIME")) {
        /* Initilize fast clock ratio */
       try {
       //int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
        /* the two parameters form a ration of modeltime:realtime */
       int modeltime = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       int realtime = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
         jmri.jmris.srcp.JmriSRCPTimeServer ts=(jmri.jmris.srcp.JmriSRCPTimeServer)(((jmri.jmris.ServiceHandler)data).getTimeServer());
         ts.parseRate(modeltime,realtime);
         ts.sendRate();
       } catch(java.io.IOException ie) {
       }
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SM")) {
        /* Initilize service mode */
       //int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("FB")) {
        /* Initilize feedback on a particular bus */
       //int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
    } 

    return data;
  }
  public Object visit(ASTcomment node,java.lang.Object data)
  {
    log.debug("COMMENT " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTgl node, Object data)
  {
    log.debug("GL " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsm node, Object data)
  {
    log.debug("SM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTga node, Object data)
  {
    log.debug("GA" +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTfb node, Object data)
  {
    log.debug("FB " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTtime node, Object data)
  {
    log.debug("TIME " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTpower node, Object data)
  {
    log.debug("POWER " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTserver node, Object data)
  {
    log.debug("SERVER " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsession node, Object data)
  {
    log.debug("SESION " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTlock node, Object data)
  {
    log.debug("LOCK " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTwait_cmd node, Object data)
  {
    log.debug("Received WAIT CMD " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTbus node, Object data)
  {
    log.debug("Received Bus " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTvalue node, Object data)
  {
    log.debug("Received Value " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTzeroaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTnonzeroaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTport node, Object data)
  {
    log.debug("Received Port " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdevicegroup node, Object data)
  {
    log.debug("Received Bus " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTonoff node, Object data)
  {
    log.debug("Received ON/OFF " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdescription node, Object data)
  {
    log.debug("Description " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdelay node, Object data)
  {
    log.debug("Delay " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTtimeout node, Object data)
  {
    log.debug("Timeout " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTzeroone node, Object data)
  {
    log.debug("ZeroOne " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTserviceversion node, Object data)
  {
    log.debug("Service Version " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTconnectionmode node, Object data)
  {
    log.debug("Connection Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcvno node, Object data)
  {
    log.debug("CV Number " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTprogmode node, Object data)
  {
    log.debug("Programming Mode Production" +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcv node, Object data)
  {
    log.debug("CV Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcvbit node, Object data)
  {
    log.debug("CVBIT Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTreg node, Object data)
  {
    log.debug("REG Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  
  public Object visit(ASTprotocol node, Object data)
  {
    log.debug("Protocol Production " +node.jjtGetValue() );
    //return node.childrenAccept(this,data);
    return data;
  }

  static Logger log = LoggerFactory.getLogger(SRCPVisitor.class.getName());

}
