package jmri.jmrix.srcp.parser;

import jmri.jmrix.srcp.SRCPBusConnectionMemo;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class provides an interface between the JavaTree/JavaCC
 * parser for the SRCP protocol and the JMRI front end.
 *
 * @author Paul Bender Copyright (C) 2011
 */
public class SRCPClientVisitor extends SRCPClientParserDefaultVisitor {

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

    private final static Logger log = LoggerFactory.getLogger(SRCPClientVisitor.class);

}
