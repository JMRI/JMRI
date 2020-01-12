package jmri.jmrix.qsi;

/**
 * Carries the reply to an QsiMessage.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class QsiReply extends jmri.jmrix.AbstractMessage {

    static final int MAXREPLYLENGTH = 200;

    // create a new one
    public QsiReply() {
        super(MAXREPLYLENGTH);
        _isBoot = false;
        _nDataChars = 0;
    }

    // copy one
    public QsiReply(QsiReply m) {
        super(m);
        _isBoot = m._isBoot;
    }

    // from String
    public QsiReply(String s) {
        super(s);
    }

    public QsiReply(String s, boolean b) {
        super(s);
        _isBoot = b;
    }

    public void setOpCode(int i) {
        _dataChars[0] = (char) i;
    }

    public int getOpCode() {
        return _dataChars[0];
    }

    // accessors to the bulk data
    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = v;
        _nDataChars = Math.max(_nDataChars, n + 1);
    }

    static public QsiMessage getAck(QsiReply r) {
        // send ack to received (unsolicited) message m
        QsiMessage m = new QsiMessage(1);
        m.setElement(0, r.getElement(1));
        return m;
    }

    // Check and strip framing characters and DLE from a QSI bootloader reply
    public boolean strip() {
        int tmp[] = new int[_nDataChars];
        int j = 0;
        _isBoot = true; // definitely a boot message
        // Check framing characters
        if (_dataChars[0] != QsiMessage.STX) {
            return false;
        }
        if (_dataChars[1] != QsiMessage.STX) {
            return false;
        }
        if (_dataChars[_nDataChars - 1] != QsiMessage.ETX) {
            return false;
        }

        // Ignore framing characters and strip DLEs
        for (int i = 2; i < _nDataChars - 1; i++) {
            if (_dataChars[i] == QsiMessage.DLE) {
                i++;
            }
            tmp[j++] = _dataChars[i];
        }

        // Copy back to original QsiReply
        System.arraycopy(tmp, 0, _dataChars, 0, j);
        _nDataChars = j;
        return true;
    }

    // Check and strip checksum from a QSI bootloader reply
    // Assumes framing and DLE chars have been stripped
    public boolean getChecksum() {
        int checksum = 0;
        for (int i = 0; i < _nDataChars; i++) {
            checksum += _dataChars[i] & 0xff;
        }
        _nDataChars--;
        return ((checksum & 0xff) == 0);
    }

    // display format
    @Override
    public String toString() {
        QsiSystemConnectionMemo memo = jmri.InstanceManager.getDefault(jmri.jmrix.qsi.QsiSystemConnectionMemo.class);
        return toString(memo.getQsiTrafficController());
    }

    public String toString(QsiTrafficController controller) {
        StringBuilder s = new StringBuilder();
        if (_dataChars == null) {
            return "<none>";
        }
        if (controller == null || controller.isSIIBootMode()) {
            for (int i = 0; i < _nDataChars; i++) {
                s.append(jmri.util.StringUtil.twoHexFromInt(_dataChars[i]));
                s.append(" ");
            }
        } else {
            for (int i = 0; i < _nDataChars; i++) {
                s.append("<");
                s.append(_dataChars[i]);
                s.append(">");
            }
        }
        return new String(s);
    }

    /**
     * Extracts Read-CV returned value from a message.
     * <p>
     * QSI is assumed to not be echoing commands. A reply to a command may
     * include the prompt that was printed after the previous command Reply to a
     * CV read is of the form " = hvv" where vv is the CV value in hex.
     *
     * @return the value of the read CV or -1 if the reply cannot be parsed.
     */
    public int value() {
        return getElement(5) & 0xFF;
    }

    int match(String s) {
        // find a specific string in the reply
        String rep = new String(_dataChars, 0, _nDataChars);
        return rep.indexOf(s);
    }

    int skipWhiteSpace(int index) {
        // start at index, passing any whitespace & control characters at the start of the buffer
        while (index < getNumDataElements() - 1
                && ((char) getElement(index) <= ' ')) {
            index++;
        }
        return index;
    }

    int skipEqual(int index) {
        // start at index, skip over the equals and hex prefix
        int len = "= h".length();
        if (getNumDataElements() >= index + len - 1
                && '=' == (char) getElement(index)
                && ' ' == (char) getElement(index + 1)
                && 'h' == (char) getElement(index + 2)) {
            index += len;
        }
        return index;
    }

    // Longest boot reply is 256bytes each preceded by DLE + 2xSTX + ETX
    static public final int MAXSIZE = 515;

    // contents (private)
    private boolean _isBoot = false;

}
