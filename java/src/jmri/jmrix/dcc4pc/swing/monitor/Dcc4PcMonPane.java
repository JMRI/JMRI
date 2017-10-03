package jmri.jmrix.dcc4pc.swing.monitor;

import jmri.jmrix.dcc4pc.Dcc4PcListener;
import jmri.jmrix.dcc4pc.Dcc4PcMessage;
import jmri.jmrix.dcc4pc.Dcc4PcReply;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.swing.Dcc4PcPanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a MonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class Dcc4PcMonPane extends jmri.jmrix.AbstractMonPane implements Dcc4PcListener, Dcc4PcPanelInterface {

    public Dcc4PcMonPane() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        return "Dcc4PC Command Monitor";
    }

    @Override
    public void dispose() {
        // disconnect from the DCC4PCTrafficController
        if(memo.getDcc4PcTrafficController()!=null){
            memo.getDcc4PcTrafficController().removeDcc4PcListener(this);
        }
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    Dcc4PcSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof Dcc4PcSystemConnectionMemo) {
            initComponents((Dcc4PcSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(Dcc4PcSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the DCC4PCTrafficController
        if(memo.getDcc4PcTrafficController()!=null){
            memo.getDcc4PcTrafficController().addDcc4PcListener(this);
        } else {
            log.error("Connection has not been initiallised");
        }
    }

    @Override
    public synchronized void message(Dcc4PcMessage l) {  // receive a message and log it
        if (l.isBinary()) {
            nextLine("cmd: " + l.toHexString() + "\n", null);
        } else {
            nextLine("cmd: \"" + l.toHexString() + "\"\n", null);
        }
    }

    @Override
    public synchronized void reply(Dcc4PcReply l) {  // receive a reply message and log it
        String raw = "";
        for (int i = 0; i < l.getNumDataElements(); i++) {
            if (i > 0) {
                raw += " ";
            }
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i) & 0xFF, raw);
        }

        if (l.isUnsolicited()) {
            nextLine("msg: \"" + l.toHexString() + "\"\n", raw);
        } else {
            nextLine("rep: \"" + l.toHexString() + "\"\n", raw);
        }
    }

    public synchronized void notifyMessage(Dcc4PcMessage l) {  // receive a message and log it
        if (l.isBinary()) {
            nextLine("binary cmd: " + l.toString() + "\n", null);
        } else {
            nextLine("cmd: \"" + l.toString() + "\"\n", null);
        }
    }

    public synchronized void notifyReply(Dcc4PcReply l) {  // receive a reply message and log it
        String raw = "";
        for (int i = 0; i < l.getNumDataElements(); i++) {
            if (i > 0) {
                raw += " ";
            }
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i) & 0xFF, raw);
        }

        if (l.isUnsolicited()) {
            nextLine("msg: \"" + l.toHexString() + "\"\n", raw);
        } else {
            nextLine("rep: \"" + l.toHexString() + "\"\n", raw);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.dcc4pc.swing.Dcc4PcNamedPaneAction {

        public Default() {
            super("Dcc4PC Command Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    Dcc4PcMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(Dcc4PcSystemConnectionMemo.class));
        }
    }

    @Override
    public void handleTimeout(Dcc4PcMessage m) {
        log.info("timeout recieved to our last message " + m.toString());
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcMonPane.class);

}



