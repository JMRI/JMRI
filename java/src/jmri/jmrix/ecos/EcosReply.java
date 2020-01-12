package jmri.jmrix.ecos;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the reply to an EcosMessage.
 * <p>
 * Some rudimentary support is provided for the "binary" option.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Daniel Boudreau Copyright (C) 2007
 */
public class EcosReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public EcosReply() {
        super();
    }

    public EcosReply(String s) {
        super(s);
    }

    public EcosReply(EcosReply l) {
        super(l);
    }

    // these can be very large
    @Override
    public int maxSize() {
        return 10000;
    }

    // no need to do anything
    @Override
    protected int skipPrefix(int index) {
        return index;
    }

    @Override
    public int value() {
        if (isBinary()) {
            return getElement(0) & 0xFF;  // avoid stupid sign extension
        } else {
            return super.value();
        }
    }

    //knowing where the end is we can then determine the error code
    int endAtElement = -1;

    /**
     * Check for last line starts with {@code "<END "}
     */
    public boolean containsEnd() {
        for (int i = 0; i < getNumDataElements() - 6; i++) {
            if ((getElement(i) == 0x0A)
                    && (getElement(i + 1) == '<')
                    && (getElement(i + 2) == 'E')
                    && (getElement(i + 3) == 'N')
                    && (getElement(i + 4) == 'D')
                    && (getElement(i + 5) == ' ')) {
                endAtElement = i;
                return true;
            }
        }
        return false;
    }

    /**
     * returns -1 if the end code has not been found.
     */
    public int getResultCode() {
        if (!containsEnd()) {
            log.error("Trying to get message end code before message is complete");
            return -1;
        }
        if (endAtElement == -1) {
            //just a double check incase endAtElement never got set.
            return -1;
        }
        String resultCode = Character.toString((char) (getElement(endAtElement + 6)));
        resultCode = resultCode + (char) (getElement(endAtElement + 7));
        resultCode = resultCode.trim();

        try {
            return Integer.parseInt(resultCode);
        } catch (java.lang.NumberFormatException ex) {
            log.error("Unable to convert result code to a number " + resultCode);
            return -1;
        }
    }

    /**
     * Is this EcosReply actually an independent {@code <EVENT} message?
     */
    boolean isEvent() {
        if (getNumDataElements() < 8) {
            return false;
        }
        if (getElement(0) != '<') {
            return false;
        }
        if (getElement(1) != 'E') {
            return false;
        }
        if (getElement(2) != 'V') {
            return false;
        }
        if (getElement(3) != 'E') {
            return false;
        }
        if (getElement(4) != 'N') {
            return false;
        }
        if (getElement(5) != 'T') {
            return false;
        }
        if (getElement(6) != ' ') {
            return false;
        }
        return true;
    }

    //An event message is Unsolicited
    @Override
    public boolean isUnsolicited() {
        if (isEvent()) {
            setUnsolicited();
            return true;
        } else {
            return false;
        }
    }

    public boolean isReply() {
        if (getNumDataElements() < 8) {
            return false;
        }
        if (getElement(0) != '<') {
            return false;
        }
        if (getElement(1) != 'R') {
            return false;
        }
        if (getElement(2) != 'E') {
            return false;
        }
        if (getElement(3) != 'P') {
            return false;
        }
        if (getElement(4) != 'L') {
            return false;
        }
        if (getElement(5) != 'Y') {
            return false;
        }
        if (getElement(6) != ' ') {
            return false;
        }
        return true;
    }

    String replyType = null;

    public String getReplyType() {
        if (!isReply()) {
            return "";
        }
        if (replyType != null) {
            return replyType;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i < getNumDataElements(); i++) {
            if (getElement(i) == '(') {
                break;
            }
            sb.append((char) getElement(i));
        }
        replyType = sb.toString();
        return sb.toString();
    }

    List<String> headerDetails = null;

    public List<String> getReplyHeaderDetails() {
        if (!isReply()) {
            return Arrays.asList(new String[0]);
        }
        if (headerDetails != null) {
            return headerDetails;
        }
        StringBuilder sb = new StringBuilder();
        int iOffSet = 8 + getReplyType().length() + ("" + getEcosObjectId()).length();
        if (getElement(iOffSet) == ',') {
            iOffSet++;
        }
        if (getElement(iOffSet) == ' ') {
            iOffSet++;
        }
        for (int i = iOffSet; i < getNumDataElements(); i++) {
            if (getElement(i) == ')') {
                break;
            }
            sb.append((char) getElement(i));
        }
        headerDetails = Arrays.asList(sb.toString().split(","));
        return headerDetails;
    }

    int objectId = -1;

    public int getEcosObjectId() {
        if (objectId != -1) {
            return objectId;
        }

        StringBuilder sb = new StringBuilder();
        int iOffSet = 7 + getReplyType().length();
        if (!isEvent()) {
            iOffSet = iOffSet + 1;
        }
        for (int i = iOffSet; i < getNumDataElements(); i++) {
            if (getElement(i) == '>') {
                break;
            } else if (getElement(i) == ',') {
                break;
            } else if (getElement(i) == ')') {
                break;
            }
            sb.append((char) getElement(i));
        }
        objectId = Integer.parseInt(sb.toString());
        return objectId;
    }

    /**
     * get the contents of the reply, excluding the header and footer
     */
    public String[] getContents() {
        String[] lines = toString().split("\n");
        int length = lines.length - 2;

        String[] returnval = new String[length];
        for (int i = 1; i <= length; i++) {
            returnval[i - 1] = lines[i];
        }
        return returnval;
    }

    static public String getContentDetail(String str) {
        int start = str.indexOf("[") + 1;
        int end = str.indexOf("]");
        return str.substring(start, end);
    }

    static public String getContentDetails(String line, String item) {
        int startval = line.indexOf(item) + item.length() + 1;
        int endval = (line.substring(startval)).indexOf("]") + startval;
        return line.substring(startval, endval);
    }

    private final static Logger log = LoggerFactory.getLogger(EcosReply.class);

}
