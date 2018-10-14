package jmri.jmrix.qsi.qsimon;

import jmri.jmrix.qsi.QsiListener;
import jmri.jmrix.qsi.QsiMessage;
import jmri.jmrix.qsi.QsiReply;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;

/**
 * Frame displaying (and logging) QSI command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 */
public class QsiMonFrame extends jmri.jmrix.AbstractMonFrame implements QsiListener {

    private QsiSystemConnectionMemo _memo = null;
    public QsiMonFrame(QsiSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return "QSI Command Monitor";
    }

    @Override
    protected void init() {
        // connect to TrafficController
        _memo.getQsiTrafficController().addQsiListener(this);
    }

    @Override
    public void dispose() {
        _memo.getQsiTrafficController().removeQsiListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(QsiMessage l) {  // receive a message and log it
        int opcode = l.getElement(0);
        String text = null;
        if (l.getNumDataElements() == 1) {
            // special case - assume this is a reply ack
            text = "Ack of message for function " + l.getElement(0);
        } else {
            switch (opcode) {
                case 9:
                    text = "OP_REQ_READ_CV with CV=" + l.getElement(3);
                    break;
                case 30:
                    text = "OP_REQ_WRITE_CV with CV=" + l.getElement(3) + " data=" + l.getElement(4);
                    break;
                case 17:
                    text = "OP_REQ_CLEAR_ERROR_STATUS";
                    break;
                default:
                    text = "Unrecognized message with code " + opcode + ": " + l.toString(_memo.getQsiTrafficController());
                    break;
            }
        }
        nextLine("M: " + text + "\n", l.toString(_memo.getQsiTrafficController()));
    }

    @Override
    public synchronized void reply(QsiReply l) {  // receive a reply message and log it
        String text;

        if (l.getElement(0) == 'A') {
            text = "A: Ack of " + l.getElement(1) + " with status " + l.getElement(2);
        } else if (l.getElement(0) == 'S') {
            switch (l.getElement(1) & 0xFF) {
                case 10:
                    text = "S: OP_RPL_READ_CV status " + l.getElement(4) + " value " + l.getElement(5);
                    break;
                case 31:
                    text = "S: OP_RPL_WRITE_CV status " + l.getElement(4);
                    break;
                default:
                    text = "S: Response type " + l.getElement(1) + " with length " + (l.getElement(2) + 256 * l.getElement(3));
                    break;
            }

        } else {
            text = "U: Unrecognized reply: " + l.toString(_memo.getQsiTrafficController());
        }
        nextLine(text + "\n", l.toString(_memo.getQsiTrafficController()));
    }

}
