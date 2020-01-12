package jmri.jmrix.dcc4pc;

import java.util.ArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement BoardManager for Dcc4Pc systems.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class Dcc4PcBoardManager implements Dcc4PcListener {

    public Dcc4PcBoardManager(Dcc4PcTrafficController tc, Dcc4PcSensorManager senManager) {
        this.tc = tc;
        this.senManager = senManager;
        for (int x = MINRC; x < MAXRC; x++){
            addBoard(x);
        }
    }
    
    private final static int MINRC = 0;
    private final static int MAXRC = 4;
    
    ArrayList<Integer> boardsToDiscover = new ArrayList<>();

    Dcc4PcTrafficController tc;
    Dcc4PcSensorManager senManager;

    public void notifyReply(Dcc4PcReply m) {
        // is this a list of sensors?
    }

    public void notifyMessage(Dcc4PcMessage m) {
        // messages are ignored
    }
   
    protected void addBoard(int newBoard){
        if(!senManager.isBoardCreated(newBoard) && !boardsToDiscover.contains(newBoard)){
            boardsToDiscover.add(newBoard);
            Dcc4PcMessage m = Dcc4PcMessage.getInfo(newBoard);
            m.setTimeout(1000);
            m.setRetries(2);
            tc.sendDcc4PcMessage(m, this);  
        }
    }
    
    @Override
    @SuppressFBWarnings(value="DLS_DEAD_LOCAL_STORE", justification="See issue #6132")
    public void reply(Dcc4PcReply r) {
        if (log.isDebugEnabled()) {
            log.debug("Reply details sm: " + r.toHexString());
        }
        if(r.getBoard() == -1){
            return;
        }
        if(r.getNumDataElements()==0 && r.getElement(0) == 0x00) {
            //Simple acknowledgement reply, no further action required
            return;
        }
        int board = r.getBoard();
        if (r.isError()) {
            boardsToDiscover.remove((Integer)board);
        } else {
            if (r.getMessageType()==Dcc4PcMessage.INFO) {
                log.debug("Get Info for board {}: {}", board, r.toString());
                String version;
                int inputs;
                int encoding;

                int i = 0;
                StringBuilder buf = new StringBuilder();
                while (i < 4) {
                    buf.append((char) r.getElement(i));
                    i++;
                }
                //skip supported speeds for now
                String str = buf.toString(); // DLS_DEAD_LOCAL_STORE here: str is unused
                //We have a reader, now to find out other information about it
                if (str.equals("RCRD")) {
                    i = i + 2;
                    str = str + " ver ";
                    str = str + r.getElement(i) + ".";
                    version = r.getElement(i) + "." + r.getElement(i + 1);
                    i++;
                    str = str + r.getElement(i) + ", Inputs : ";
                    i++;
                    inputs = r.getElement(i);
                    str = str + r.getElement(i) + ", Encoding : ";
                    i++;
                    encoding = r.getElement(i);
                    if ((r.getElement(i) & 0x01) == 0x01) {
                        str = str + "Supports Cooked RailCom Encoding";
                    } else {
                        str = str + "Supports Raw RailCom Encoding";
                    }

                    senManager.addActiveBoard(board, version, inputs, encoding);

                    Dcc4PcMessage m = Dcc4PcMessage.getDescription(board);
                    m.setTimeout(2000);
                    tc.sendDcc4PcMessage(m, this);
                    boardsToDiscover.remove((Integer)board);
                }
            } else if (r.getMessageType()==Dcc4PcMessage.DESC) {
                log.debug("Get Description for board {}: {}", board, r.toString());

                senManager.setBoardDescription(board, r.toString());
                Dcc4PcMessage m = Dcc4PcMessage.getEnabledInputs(board);
                m.setTimeout(2000);
                m.setRetries(2);
                log.debug(m.toString());
                tc.sendDcc4PcMessage(m, this);
            } else if (r.getMessageType()==Dcc4PcMessage.CHILDENABLEDINPUTS) {
                log.debug("Make Sensors for board {}: {}", board, r.toString());
                senManager.createSensorsForBoard(r);
            }
        }
    }

    @Override
    public void handleTimeout(Dcc4PcMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("timeout received to our last message " + m.toString());
        }
        log.debug("Timeout to message {} for board {}", m.toString(), m.getBoard());
    }

    @Override
    public void message(Dcc4PcMessage m) {

    }
    
    private final static Logger log = LoggerFactory.getLogger(Dcc4PcBoardManager.class);
}
