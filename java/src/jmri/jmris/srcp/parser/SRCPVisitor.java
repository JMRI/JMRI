package jmri.jmris.srcp.parser;

import jmri.InstanceManager;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class provides an interface between the JavaTree/JavaCC
 * parser for the SRCP protocol and the JMRI back end.
 * @author Paul Bender Copyright (C) 2010
 */
public class SRCPVisitor extends SRCPParserDefaultVisitor {

    private String outputString = null;

    public String getOutputString() {
        return outputString;
    }

    // note that the isSupported function has the side
    // effect of setting an error message to outputString if
    // it returns false.
    private boolean isSupported(int bus, String devicegroup) {
        // get the system memo coresponding to the bus.
        // and ask it what is supported
        try {
            jmri.jmrix.SystemConnectionMemo memo
                    = InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class).get(bus - 1);
            if (memo != null) {
                log.debug("devicegroup " + devicegroup);
                if (devicegroup.equals("FB")) {
                    if (memo.provides(jmri.SensorManager.class)) {
                        return true;
                    } else {
                        // respond this isn't supported
                        outputString = "422 ERROR unsupported device group";
                    }
                } else if (devicegroup.equals("GA")) {
                    if (memo.provides(jmri.TurnoutManager.class)) {
                        return true;
                    } else {
                        // respond this isn't supported
                        outputString = "422 ERROR unsupported device group";
                    }
                } else if (devicegroup.equals("GL")) {
                    if (memo.provides(jmri.ThrottleManager.class)) {
                        return true;
                    } else {
                        // respond this isn't supported
                        outputString = "422 ERROR unsupported device group";
                    }
                } else if (devicegroup.equals("POWER")) {
                    if (memo.provides(jmri.PowerManager.class)) {
                        return true;
                    } else {
                        // respond this isn't supported
                        outputString = "422 ERROR unsupported device group";
                    }
                } else if (devicegroup.equals("SM")) {
                    if (memo.provides(jmri.GlobalProgrammerManager.class)) {
                        return true;
                    } else {
                        // respond this isn't supported
                        outputString = "422 ERROR unsupported device group";
                    }
                } else {
                    // respond this isn't supported
                    outputString = "422 ERROR unsupported device group";

                }
            } else {
                // no memo registered for this bus.
                outputString = "416 ERROR no data";
            }
        } catch (java.lang.IndexOutOfBoundsException obe) {
            outputString = "412 ERROR wrong value";
        }
        return false;
    }

    @Override
    public Object visit(ASTgo node, Object data) {
        log.debug("Go " + node.jjtGetValue());
        jmri.jmris.srcp.JmriSRCPServiceHandler handle = (jmri.jmris.srcp.JmriSRCPServiceHandler) data;
        // The GO command should switch the server into runmode, but
        // only if the client has set the protocol version.  (if no mode
        // is set, the default is command mode).
        if (handle.getClientVersion().startsWith("0.8")) {
            handle.setRunMode();
            outputString = "200 OK GO " + ((jmri.jmris.srcp.JmriSRCPServiceHandler) data).getSessionNumber();
        } else {
            outputString = "402 ERROR insufficient data";
        }
        return data;
    }

    @Override
    public Object visit(ASThandshake_set node, Object data) {
        log.debug("Handshake Mode SET ");
        jmri.jmris.srcp.JmriSRCPServiceHandler handle = (jmri.jmris.srcp.JmriSRCPServiceHandler) data;
        if (node.jjtGetChild(0).getClass() == ASTprotocollitteral.class) {
            String version = (String) ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();
            if (version.startsWith("0.8")) {
                handle.setClientVersion(version);
                outputString = "201 OK PROTOCOL SRCP";
            } else {
                outputString = "400 ERROR unsupported protocol";
            }
        } else if (node.jjtGetChild(0).getClass() == ASTconnectionlitteral.class) {
            String mode = (String) ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();
            outputString = "202 OK CONNECTIONMODEOK";
            if (mode.equals("COMMAND")) {
                handle.setCommandMode(true);
            } else if (mode.equals("INFO")) {
                handle.setCommandMode(false);
            } else {
                outputString = "401 ERROR unsupported connection mode";
            }
        } else {
            outputString = "500 ERROR out of resources";
        }
        return data;
    }

    @Override
    public Object visit(ASTget node, Object data) {
        log.debug("Get " + ((SimpleNode) node.jjtGetChild(1)).jjtGetValue());
        int bus = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue()));
        if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("POWER")
                && isSupported(bus, "POWER")) {
            // This is a message asking for the power status
            try {
                ((jmri.jmris.ServiceHandler) data).getPowerServer().sendStatus(
                        InstanceManager.getDefault(jmri.PowerManager.class).getPower());
            } catch (jmri.JmriException je) {
                // We shouldn't have any errors here.
                // If we do, something is horibly wrong.
            } catch (java.io.IOException ie) {
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("GA")
                && isSupported(bus, "GA")) {
            // This is a message asking for the status of a "General Accessory".
            int address = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
            // our implementation ignores the port, but maybe we shouldn't to
            // follow the letter of the standard.
            //int port = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
            try {
                ((jmri.jmris.srcp.JmriSRCPTurnoutServer) ((jmri.jmris.ServiceHandler) data).getTurnoutServer()).sendStatus(bus, address);
            } catch (java.io.IOException ie) {
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("FB")
                && isSupported(bus, "FB")) {
            // This is a message asking for the status of a FeedBack sensor.
            int address = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
            try {
                ((jmri.jmris.srcp.JmriSRCPSensorServer) ((jmri.jmris.ServiceHandler) data).getSensorServer()).sendStatus(bus, address);
            } catch (java.io.IOException ie) {
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("SM")
                && isSupported(bus, "SM")) {
            // This is a Service Mode read request.
            ProgrammingMode modeno = ProgrammingMode.REGISTERMODE;
            if (node.jjtGetChild(3).getClass() == ASTcv.class) {
                modeno = ProgrammingMode.DIRECTBYTEMODE;
            } else if (node.jjtGetChild(3).getClass() == ASTcvbit.class) {
                modeno = ProgrammingMode.DIRECTBITMODE;
            }

            int cv = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(4)).jjtGetValue()));
            //try {
            ((jmri.jmris.srcp.JmriSRCPProgrammerServer) ((jmri.jmris.ServiceHandler) data).getProgrammerServer()).readCV(modeno, cv);
            //} catch(java.io.IOException ie) {
            //}

        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("GL")
                && isSupported(bus, "GL")) {
            // This is a Generic Loco request
            // the 3rd child is the address of the locomotive we are
            // requesting status of.
            int address=Integer.parseInt(((String) ((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
            // This is a Throttle Status request
            try {
                ((jmri.jmris.srcp.JmriSRCPThrottleServer)(((jmri.jmris.ServiceHandler) data).getThrottleServer())).sendStatus(bus,address);
            } catch (java.io.IOException ie) {
            }

        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("TIME")) {
            // This is a Time request
            try {
                ((jmri.jmris.ServiceHandler) data).getTimeServer().sendTime();
            } catch (java.io.IOException ie) {
            }

        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("SERVER")) {
            // for the GET <bus> SERVER request, we return the current server
            // state.  In JMRI, we always return "Running".
            outputString = "100 INFO 0 SERVER RUNNING";
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("DESCRIPTION")) {
            // for the GET <bus> DESCRIPTION request, what we return depends on
            // the number of arguments passed.
            SimpleNode descriptionnode = (SimpleNode) node.jjtGetChild(1);
            int children = descriptionnode.jjtGetNumChildren();
            if (children == 0) {
                // with no arguments, we send a list of supported groups.
                if (bus == 0) {
                    // the groups supported by bus 0 are fixed
                    outputString = "100 INFO 0 DESCRIPTION SERVER SESSION TIME";
                } else {
                    outputString = "100 INFO " + bus;
                    // get the system memo coresponding to the bus.
                    // and ask it what is supported
                    try {
                        jmri.jmrix.SystemConnectionMemo memo
                                = InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class).get(bus - 1);
                        if (memo != null) {
                            outputString = outputString + " DESCRIPTION";
                            if (memo.provides(jmri.SensorManager.class)) {
                                outputString = outputString + " FB";
                            }
                            if (memo.provides(jmri.TurnoutManager.class)) {
                                outputString = outputString + " GA";
                            }
                            if (memo.provides(jmri.ThrottleManager.class)) {
                                outputString = outputString + " GL";
                            }
                            if (memo.provides(jmri.PowerManager.class)) {
                                outputString = outputString + " POWER";
                            }
                            if (memo.provides(jmri.GlobalProgrammerManager.class)) {
                                outputString = outputString + " SM";
                            }
                        } else {
                            // no memo registered for this bus.
                            outputString = "416 ERROR no data";
                        }
                    } catch (java.lang.IndexOutOfBoundsException obe) {
                        outputString = "412 ERROR wrong value";
                    }
                }
            } else if (children == 1) {
                // with one argument, we respond with data only for device groups
                // that have no addresses.
                String devicegroup = (String) ((SimpleNode) descriptionnode.jjtGetChild(0)).jjtGetValue();
                outputString = "100 INFO " + bus;
                log.debug("devicegroup " + devicegroup);
                if (devicegroup.equals("FB") && isSupported(bus, devicegroup)) {
                    outputString = "419 ERROR list too short";
                } else if (devicegroup.equals("GA") && isSupported(bus, devicegroup)) {
                    outputString = "419 ERROR list too short";
                } else if (devicegroup.equals("GL") && isSupported(bus, devicegroup)) {
                    outputString = "419 ERROR list too short";
                } else if (devicegroup.equals("POWER") && isSupported(bus, devicegroup)) {
                    // we are supposed to return the init string,
                    // and the POWER group has no parameters, so
                    // just return POWER
                    outputString = outputString + " POWER";
                } else if (devicegroup.equals("SM") && isSupported(bus, devicegroup)) {
                    outputString = "419 ERROR list too short";
                } else {
                    // respond this isn't supported
                    outputString = "422 ERROR unsupported device group";
                }  // end if(chidren==1)
            } else if (children == 2) {
                outputString = "100 INFO " + bus;
                // get the system memo coresponding to the bus.
                // and ask it what is supported
                // with 2 arguments, we send a description of a specific device.
                jmri.jmrix.SystemConnectionMemo memo
                        = InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class).get(bus - 1);
                if (memo != null) {
                    String devicegroup = (String) ((SimpleNode) descriptionnode.jjtGetChild(0)).jjtGetValue();
                    String address = (String) ((SimpleNode) descriptionnode.jjtGetChild(1)).jjtGetValue();
                    if (devicegroup.equals("FB") && isSupported(bus, devicegroup)) {
                        jmri.SensorManager mgr = memo.get(jmri.SensorManager.class);
                        try {
                            String searchName = mgr.createSystemName(address,
                                    memo.getSystemPrefix());
                            if (mgr.getBySystemName(searchName) != null) {
                                // add the initialization parameter list.
                                // we don't expect parameters, so just return
                                // the bus and address.
                                outputString = outputString + " FB " + address;
                            } else {
                                // the device wasn't found.
                                outputString = "412 ERROR wrong value";
                            }
                        } catch (jmri.JmriException je) {
                            // the device wasn't found.
                            outputString = "412 ERROR wrong value";
                        }
                    } else if (devicegroup.equals("GA") && isSupported(bus, devicegroup)) {
                        jmri.TurnoutManager mgr = memo.get(jmri.TurnoutManager.class);
                        try {
                            String searchName = mgr.createSystemName(address, memo.getSystemPrefix());
                            if (mgr.getBySystemName(searchName) != null) {
                                // add the initialization parameter list.
                                // the only other required parameter is
                                // the protocol, and we treat all of our
                                // turnouts as NMRA-DCC turnouts, so return
                                // the fixed "N" protocol value.
                                // other valid options are:
                                //    "M" (Mareklin/Motorola format)
                                //    "S" (Selectrix Format)
                                //    "P" (Protocol by server)
                                outputString = outputString + " GA " + address + " N";
                            } else {
                                // the device wasn't found.
                                outputString = "412 ERROR wrong value";
                            }
                        } catch (jmri.JmriException je) {
                            // the device wasn't found.
                            outputString = "412 ERROR wrong value";
                        }
                    } else if (devicegroup.equals("GL") && isSupported(bus, devicegroup)) {
                        // outputString=outputString + " GL " +address;
                        // this one needs some tought on how to proceed,
                        // since the throttle manager differs from
                        // other JMRI managers.
                        // for now, just say no data.
                        outputString = "416 ERROR no data";
                    } else if (devicegroup.equals("POWER") && isSupported(bus, devicegroup)) {
                        outputString = "418 ERROR list too long";
                    } else if (devicegroup.equals("SM") && isSupported(bus, devicegroup)) {
                        //outputString=outputString + " SM " +address;
                        // this one needs some tought on how to proceed                                 // since we have both service mode and ops mode
                        // programmers, but the service mode programmer is
                        // not addressed on DCC systems.
                        // for now, just say no data.
                        outputString = "416 ERROR no data";
                    }
                } // end if(children==2)
            } else {
                outputString = "418 ERROR list too long";
            } // end of DESCRIPTION device group.
        } else {
            outputString = "422 ERROR unsupported device group";
        }
        return data;
    }

    @Override
    public Object visit(ASTset node, Object data) {
        SimpleNode target = (SimpleNode) node.jjtGetChild(1);

        log.debug("Set " + target.jjtGetValue());
        int bus = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue()));

        if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("POWER")
                && isSupported(bus, "POWER")) {
            try {
                ((jmri.jmris.ServiceHandler) data).getPowerServer().parseStatus(
                        ((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
            } catch (java.io.IOException ie) {
            } catch (jmri.JmriException je) {
                // We shouldn't have any errors here.
                // If we do, something is horibly wrong.
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("GA")
                && isSupported(bus, "GA")) {
            int address = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
            int port = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue()));
            // we expect to get both the value and delay, but JMRI only cares about
            // the port which indicates which output of a pair we are using.
            // leave the values below commented out, unless we decide to use them
            // later.
            //int value = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
            //int delay = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(5)).jjtGetValue()));

            try {
                ((jmri.jmris.srcp.JmriSRCPTurnoutServer) ((jmri.jmris.ServiceHandler) data).getTurnoutServer()).parseStatus(bus, address, port);
            } catch (jmri.JmriException je) {
                // We shouldn't have any errors here.
                // If we do, something is horibly wrong.
            } catch (java.io.IOException ie) {
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("FB")
                && isSupported(bus, "FB")) {
            int address = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
            int value = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue()));
            try {
                ((jmri.jmris.srcp.JmriSRCPSensorServer) ((jmri.jmris.ServiceHandler) data).getSensorServer()).parseStatus(bus, address, value);
            } catch (jmri.JmriException je) {
                // We shouldn't have any errors here.
                // If we do, something is horibly wrong.
            } catch (java.io.IOException ie) {
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("SM")
                && isSupported(bus, "SM")) {
            // This is a Service Mode write request
            ProgrammingMode modeno = ProgrammingMode.REGISTERMODE;
            if (node.jjtGetChild(3).getClass() == ASTcv.class) {
                modeno = ProgrammingMode.DIRECTBYTEMODE;
            } else if (node.jjtGetChild(3).getClass() == ASTcvbit.class) {
                modeno = ProgrammingMode.DIRECTBITMODE;
            }
            int cv = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(4)).jjtGetValue()));
            int value = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(5)).jjtGetValue()));

            //try {
            ((jmri.jmris.srcp.JmriSRCPProgrammerServer) ((jmri.jmris.ServiceHandler) data).getProgrammerServer()).writeCV(modeno, cv, value);
            //} catch(java.io.IOException ie) {
            //}

        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("GL")
                && isSupported(bus, "GL")) {
            // This is a Generic Loco request
            int address = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
            String drivemode = (String) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue();

            int speedstep = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(4)).jjtGetValue()));

            int maxspeedstep = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(5)).jjtGetValue()));
            ((jmri.jmris.srcp.JmriSRCPThrottleServer) ((jmri.jmris.ServiceHandler) data).getThrottleServer()).setThrottleSpeedAndDirection(bus,address,(float)speedstep/(float)maxspeedstep,drivemode.equals("0"));
            // setup the array list of function values.

            int numFunctions = node.jjtGetNumChildren() - 6;
            java.util.ArrayList<Boolean> functionList = new java.util.ArrayList<Boolean>();
            for(int i = 0; i < numFunctions;i++){
                // the functions start at the 7th child (index 6) of the node.
                String functionMode = (String) ((SimpleNode) node.jjtGetChild(i+6)).jjtGetValue();
                functionList.add(Boolean.valueOf(functionMode.equals("1")));
            }
            ((jmri.jmris.srcp.JmriSRCPThrottleServer) ((jmri.jmris.ServiceHandler) data).getThrottleServer()).setThrottleFunctions(bus,address,functionList);

        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("TIME")) {
            // This is a Time request
            try {
                jmri.jmris.srcp.JmriSRCPTimeServer ts = (jmri.jmris.srcp.JmriSRCPTimeServer) (((jmri.jmris.ServiceHandler) data).getTimeServer());
                int julDay = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
                int hour = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue()));
                int minute = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(4)).jjtGetValue()));
                int second = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(4)).jjtGetValue()));

                // set the time
                ts.parseTime(julDay, hour, minute, second);
                // and start the clock.
                ts.startTime();
                ts.sendTime();
            } catch (java.io.IOException ie) {
            }
        } else {
            outputString = "422 ERROR unsupported device group";
        }
        return data;
    }

    @Override
    public Object visit(ASTterm node, Object data) {
        SimpleNode target = (SimpleNode) node.jjtGetChild(1);
        int bus = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue()));
        log.debug("TERM " + bus + " " + target.jjtGetValue());
        if (target.jjtGetValue().equals("SERVER")) {
            // for the TERM <bus> SERVER request, the protocol requries that
            // we terminate all connections and reset the state to the initial
            // state.  Since we may have a local GUI controlling things, we
            // ignore the request, but send the proper return value to the
            // requesting client.
            outputString = "200 OK";
            return data;
        } else if (target.jjtGetValue().equals("SESSION")) {
            // for the TERM <bus> SERVER request, the protocol requries that
            // we terminate all connections and reset the state to the initial
            // state.  Since we may have a local GUI controlling things, we
            // ignore the request, but send the proper return value to the
            // requesting client.
            outputString = "102 INFO " + bus + " SESSION " + ((jmri.jmris.srcp.JmriSRCPServiceHandler) data).getSessionNumber();  // we need to set session IDs.
            return data;
        } else if(target.jjtGetValue().equals("GL")) {
               // terminate a locomotive
               int address = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
               try {
                  ((jmri.jmris.srcp.JmriSRCPThrottleServer)(((jmri.jmris.ServiceHandler) data).getThrottleServer())).releaseThrottle(bus,address);
               } catch (java.io.IOException ioe){
                 log.error("Error writing to network port");
               }
               return data;
        }

        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTreset node, java.lang.Object data) {
        log.debug("RESET " + ((SimpleNode) node.jjtGetChild(1)).jjtGetValue());
        if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("SERVER")) {
            // for the RESET <bus> SERVER request, the protocol requries that
            // we re-initialize the server.  Since we may have a local GUI
            // controlling things, we ignore the request, but send a prohibited
            // response to the requesting client.
            outputString = "413 ERROR temporarily prohibited";
            return data;
        } else {
            outputString = "422 ERROR unsupported device group";
        }
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTinit node, java.lang.Object data) {
        int bus = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue()));
        log.debug("INIT " + ((SimpleNode) node.jjtGetChild(1)).jjtGetValue());
        if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("POWER")
                && isSupported(bus, "POWER")) {
            /* Power really has nothing to do in JMRI */
            outputString = "200 OK";
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("GA")
                && isSupported(bus, "GA")) {
            /* Initilize a new accessory */
            int address = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
            String protocol = ((String) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue());
            try {
                ((jmri.jmris.srcp.JmriSRCPTurnoutServer) ((jmri.jmris.ServiceHandler) data).getTurnoutServer()).initTurnout(bus, address, protocol);
            } catch (jmri.JmriException je) {
                // We shouldn't have any errors here.
                // If we do, something is horibly wrong.
            } catch (java.io.IOException ie) {
            }

        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("GL")
                && isSupported(bus, "GL")) {
            /* Initilize a new locomotive */
            int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
            SimpleNode protocolNode = (SimpleNode)node.jjtGetChild(3);
            String protocol = (String)(protocolNode.jjtGetValue());
            switch(protocol){
            case "N": // NMRA DCC
                 int protocolversion = Integer.parseInt(((String)((SimpleNode)protocolNode.jjtGetChild(0)).jjtGetValue()));
                 int speedsteps = Integer.parseInt(((String)((SimpleNode)protocolNode.jjtGetChild(1)).jjtGetValue()));
                 int functions = Integer.parseInt(((String)((SimpleNode)protocolNode.jjtGetChild(2)).jjtGetValue()));
                 try {
                   ((jmri.jmris.srcp.JmriSRCPThrottleServer)(((jmri.jmris.ServiceHandler) data).getThrottleServer())).initThrottle(bus,address,protocolversion==2,speedsteps,functions);
                 } catch (java.io.IOException ie) {
                 }
                 break;
            case "A": // analog operation
                      // the documentation says this is reserved for address 0.
                      // but this could be used if we ever build support for
                      // analog non-dcc throttles.
            case "P": // protocol by server.  The documentation indicates
                      // the server gets to choose the type of decoder,
                      // but otherwise is silent on what parameters this
                      // should take.
            case "F": // Fleischmann
            case "L": // LocoNet
            case "M": // Maerklin/Motorola
            case "S": // Selectrix
            case "Z": // zimo
            default:
               outputString = "420 ERROR unsupported device protocol";
               return data;
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("TIME")) {
            /* Initilize fast clock ratio */
            try {
                /* the two parameters form a ration of modeltime:realtime */
                int modeltime = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue()));
                int realtime = Integer.parseInt(((String) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue()));
                jmri.jmris.srcp.JmriSRCPTimeServer ts = (jmri.jmris.srcp.JmriSRCPTimeServer) (((jmri.jmris.ServiceHandler) data).getTimeServer());
                ts.parseRate(modeltime, realtime);
                ts.sendRate();
            } catch (java.io.IOException ie) {
            }
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("SM")
                && isSupported(bus, "SM")) {
            /* Initilize service mode */
            outputString = "200 OK";
        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("FB")
                && isSupported(bus, "FB")) {
            /* Initilize feedback on a particular bus */
            outputString = "200 OK";
        }

        return data;
    }

    @Override
    public Object visit(ASTwait_cmd node, Object data) {
        log.debug("Received WAIT CMD " + node.jjtGetValue());
        if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("TIME")) {
            long julday = Long.parseLong((String) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue());
            int Hour = Integer.parseInt((String) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue());
            int Minute = Integer.parseInt((String) ((SimpleNode) node.jjtGetChild(4)).jjtGetValue());
            int Second = Integer.parseInt((String) ((SimpleNode) node.jjtGetChild(5)).jjtGetValue());
            ((jmri.jmris.srcp.JmriSRCPTimeServer) ((jmri.jmris.ServiceHandler) data).getTimeServer()).setAlarm(julday, Hour, Minute, Second);

        } else if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().equals("FB")) {
            outputString = "425 ERROR not supported";
        } else {
            outputString = "423 ERROR unsupported operation";
        }
        return data;
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPVisitor.class);

}
