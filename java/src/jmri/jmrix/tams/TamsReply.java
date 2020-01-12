package jmri.jmrix.tams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the reply to a TamsMessage
 * <p>
 *
 * Based on work by Bob Jacobsen and Kevin Dickerson
 *
 * @author  Jan Boen - version 151220 - 1211
 * 
 * 
 */
public class TamsReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
	public TamsReply() {
		super();
	}
	
	//Do we need Tams source message?
	private TamsMessage source;
	
	public TamsMessage getSource() {
        return source;
    }
	
	public void setSource(TamsMessage source) {
        this.source = source;
    }

	public TamsReply(String s) {
		super(s);
	}
	

	// Maximum size of a reply packet is 157 bytes.
	// At least for 52 S88 modules each generating 3 bytes + a last byte 00
        @Override
	public int maxSize() {
		return 157;
	}

	// no need to do anything
        @Override
	protected int skipPrefix(int index) {
		return index;
	}

        @Override
	public int value() {
		if (isBinary()) {
			return getElement(0);//Jan thinks this should return the whole binary reply and not just element(0)
		} else {
			// Tams reply for a CV value, returns the decimal, hex then binary
			// value.
			// 15 = $0F = %00001111
			int index = 0;
			String s = "" + (char) getElement(index);
			if ((char) getElement(index++) != ' ') {
				s = s + (char) getElement(index);
			}
			if ((char) getElement(index++) != ' ') {
				s = s + (char) getElement(index);
			}
			s = s.trim();
			int val = -1;
			try {
				val = Integer.parseInt(s);
			} catch (Exception e) {
				log.error("Unable to get number from reply: \"" + s
						+ "\" index: " + index + " message: \"" + toString()
						+ "\"");
			}
			log.info(Integer.toString(val));
			return val;
		}
	}
    private final static Logger log = LoggerFactory.getLogger(TamsReply.class);

}


