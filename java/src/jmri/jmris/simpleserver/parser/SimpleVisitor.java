//SimpleVisitor.java
package jmri.jmris.simpleserver.parser;

import jmri.InstanceManager;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class provides an interface between the JavaTree/JavaCC 
 * parser for the SimpleServer protocol and the JMRI back end.
 * @author Paul Bender Copyright (C) 2016
 * @version $Revision$
 */
public class SimpleVisitor implements JmriServerParserVisitor {

    private String outputString = null;

    public String getOutputString() {
        return outputString;
    }

    public Object visit(SimpleNode node, Object data){
        log.debug("SimpleNode Production " + node.jjtGetValue());
        return data;
    }

    public Object visit(ASTcommand node, Object data){
        log.debug("Command Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTaddress node, Object data){
        log.debug("Address Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTpowercmd node, Object data){
        log.debug("Power Command Production " + node.jjtGetValue());
        if(node.jjtGetNumChildren()==0) {
            // this is just a request for status
               try{
                  ((jmri.jmris.simpleserver.SimplePowerServer)
                   data).sendStatus(InstanceManager.getDefault(jmri.PowerManager.class).getPower());
               } catch(java.io.IOException ioe){
               } catch(jmri.JmriException je){
               }
        }else{
            if (((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("ON"))
               ((jmri.jmris.simpleserver.SimplePowerServer)data).setOnStatus();
            if (((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("OFF"))
               ((jmri.jmris.simpleserver.SimplePowerServer)data).setOffStatus();
        }
            
        return data;
    }
    public Object visit(ASTpower node, Object data){
        log.debug("Power Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTonoff node, Object data){
        log.debug("On/Off Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTturnoutcmd node, Object data){
        log.debug("Turnout Command Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTturnout node, Object data){
        log.debug("Turnout Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTturnoutdevice node, Object data){
        log.debug("Turnout Device Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTthrownclosed node, Object data){
        log.debug("Thrown/Closed Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTlightcmd node, Object data){
        log.debug("Light Command Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTlight node, Object data){
        log.debug("Light Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTlightdevice node, Object data){
        log.debug("Light Device Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTreportercmd node, Object data){
        log.debug("Reporter Command Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTreporter node, Object data){
        log.debug("Reporter Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTreporterdevice node, Object data){
        log.debug("Reporter Device Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTsensorcmd node, Object data){
        log.debug("Sensor Command Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTsensor node, Object data){
        log.debug("Sensor Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTsensordevice node, Object data){
        log.debug("Sensor Device Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASToperationscmd node, Object data){
        log.debug("Operations Command Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASToperations node, Object data){
        log.debug("Operations Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTtrains node, Object data){
        log.debug("Trains Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTlocations node, Object data){
        log.debug("Locations Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTterminate node, Object data){
        log.debug("Termininate Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTtraincmd node, Object data){
        log.debug("Train Command Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTattributelist node, Object data){
        log.debug("Attribute List Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTattributename node, Object data){
        log.debug("Attribute Production " + node.jjtGetValue());
        return data;
    }
    public Object visit(ASTvalue node, Object data){
        log.debug("Value Production " + node.jjtGetValue());
        return data;
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleVisitor.class.getName());

}
