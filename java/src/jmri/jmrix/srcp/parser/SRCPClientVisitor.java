package jmri.jmrix.srcp.parser;

import jmri.jmrix.srcp.SRCPBusConnectionMemo;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class provides an interface between the JavaTree/JavaCC
 * parser for the SRCP protocol and the JMRI front end.
 * @author Paul Bender Copyright (C) 2011
 */
public class SRCPClientVisitor implements jmri.jmrix.srcp.parser.SRCPClientParserVisitor {

    @Override
    public Object visit(SimpleNode node, Object data) {
        log.debug("Generic Visit " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTcommandresponse node, Object data) {
        log.debug("Command Response " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASThandshakeresponse node, Object data) {
        log.debug("Handshake Response " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTgl node, Object data) {
        log.debug("GL " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTsm node, Object data) {
        log.debug("SM " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTga node, Object data) {
        log.debug("GA" + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTfb node, Object data) {
        log.debug("FB " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTtime node, Object data) {
        log.debug("TIME " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTpower node, Object data) {
        log.debug("POWER " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTlock node, Object data) {
        log.debug("LOCK " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTserver node, Object data) {
        log.debug("SERVER " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTsession node, Object data) {
        log.debug("SESION " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTbus node, Object data) {
        log.debug("Received Bus " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTnonzeroaddress node, Object data) {
        log.debug("Received NonZeroAddress " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTzeroaddress node, Object data) {
        log.debug("Received ZeroAddress " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTport node, Object data) {
        log.debug("Received Port " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTdevicegroup node, Object data) {
        log.debug("Received Bus " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTonoff node, Object data) {
        log.debug("Received ON/OFF " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTdescription node, Object data) {
        log.debug("Description " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTdelay node, Object data) {
        log.debug("Delay " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }
//  public Object visit(ASTzeroone node, Object data)
//  {
//    log.debug("ZeroOne " +node.jjtGetValue() );
//    return node.childrenAccept(this,data);
//  }

    @Override
    public Object visit(ASTserviceversion node, Object data) {
        log.debug("Service Version " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTconnectionmode node, Object data) {
        log.debug("Connection Mode " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTinforesponse node, Object data) {
        log.debug("Information Response " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTinfo node, Object data) {
        log.debug("Info Response " + node.jjtGetValue());
        int bus = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue()));
        SRCPBusConnectionMemo busMemo = ((SRCPSystemConnectionMemo) data).getMemo(bus);

        SimpleNode group = (SimpleNode) node.jjtGetChild(1);

        log.debug("Info Response Group: " + group.jjtGetValue());

        if (group instanceof ASTfb) {
            if (busMemo.provides(jmri.SensorManager.class)) {
                int address = Integer.parseInt((String) (((SimpleNode) group.jjtGetChild(0)).jjtGetValue()));
                ((jmri.jmrix.srcp.SRCPSensor) ((jmri.jmrix.srcp.SRCPSensorManager) busMemo.getSensorManager()).provideSensor("" + address)).reply(node);
            }
        } else if (group instanceof ASTga) {
            if (busMemo.provides(jmri.TurnoutManager.class)) {
                if (group.jjtGetNumChildren() >= 2) {
                    //int address = Integer.parseInt((String)(((SimpleNode)group.jjtGetChild(0)).jjtGetValue()));
                    //boolean thrown = ((String)((SimpleNode)group.jjtGetChild(1)).jjtGetValue()).equals("1");
                } else {
                    // just returning the protocol.
                }
            }
        } else if (group instanceof ASTgl) {
            if (busMemo.provides(jmri.ThrottleManager.class)) {
                //int address = Integer.parseInt((String)(((SimpleNode)group.jjtGetChild(0)).jjtGetValue()));
            }
        } else if (group instanceof ASTsm) {
            if (busMemo.provides(jmri.GlobalProgrammerManager.class)) {
                jmri.jmrix.srcp.SRCPProgrammer programmer = (jmri.jmrix.srcp.SRCPProgrammer) (busMemo.getProgrammerManager().getGlobalProgrammer());
                if( programmer != null) {
                   programmer.reply(node);
                }
            }
        } else if (group instanceof ASTpower) {
            if (busMemo.provides(jmri.PowerManager.class)) {
                //String state = (String)((SimpleNode)group.jjtGetChild(1)).jjtGetValue();
                //busMemo.getPowerManager().setPower(state.equals("ON")?jmri.PowerManager.ON:jmri.PowerManager.OFF);
                ((jmri.jmrix.srcp.SRCPPowerManager) busMemo.getPowerManager()).reply(node);
            }
        } else if (group instanceof ASTtime) {
          log.debug("INFO Response for TIME group with bus " + bus);
        } else if (group instanceof ASTsession) {
	  log.debug("INFO Response for SESSION group with bus " + bus);
        } else if (group instanceof ASTserver) {
	  log.debug("INFO Response for SERVER group with bus " + bus);
        } else if (group instanceof ASTdescription) {
	  log.debug("INFO Response for DESCRIPTION group with bus " + bus);
        } else if (group instanceof ASTlock) {
	  log.debug("INFO Response for LOCK group with bus " + bus);
        }
        return data;
    }

    @Override
    public Object visit(ASTok node, Object data) {
        log.debug("Ok Response " + node.jjtGetValue());
        SRCPSystemConnectionMemo memo = (SRCPSystemConnectionMemo) data;
        if (((String) ((SimpleNode) node).jjtGetValue()).contains("GO")) {
            memo.setMode(jmri.jmrix.srcp.SRCPTrafficController.RUNMODE);
            return data;
        }
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTerror node, Object data) {
        log.debug("Error Response " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTtimeout node, Object data) {
        log.debug("Timeout " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTcvno node, Object data) {
        log.debug("CV Number " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTprogmode node, Object data) {
        log.debug("Programming Mode Production" + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTcv node, Object data) {
        log.debug("CV Programming Mode " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTcvbit node, Object data) {
        log.debug("CVBIT Programming Mode " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTreg node, Object data) {
        log.debug("REG Programming Mode " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTtimestamp node, Object data) {
        log.debug("Timestamp Node " + node.jjtGetValue());
        return node.childrenAccept(this, data);
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPClientVisitor.class);

}
