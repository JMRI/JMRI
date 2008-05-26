//  Parser.java
/**
 * Simple command line parsing class, used by XtrkCadReader utility
 * @author			Giorgio Terdina Copyright (C) 2008
 * @version			$Revision: 1.1.1.1 $
 */

public class Parser {

// Type constants
	public final static int		OPTION = 0;
	public final static int		HELP = 1;
	public final static int		STRING = 2;
	public final static int		NUMBER = 3;

// Linked chain
	static Parser	lastParser = null;
	private Parser	previousParser = null;
	
// Static field
	static Parser defaultParser = null;

// Fields
	private String		mnemonic;
	private int			type;
	private String		description;
	public	boolean		present = false;
	public	String		stringValue = null;
	public	int			intValue = 0;
	public	long		longValue = 0;
	public	double		doubleValue = 0;

// Constructor
	Parser (String m, int t, String d) {
	mnemonic = m;
	type = t;
	description = d;
	previousParser = lastParser;
	lastParser = this;
	if(m.equals("")) defaultParser = this;
	}

// Static methods

	public static void parse(String[] args) {
		int i = 0;
		Parser found;
		if(lastParser == null) return;
		while (i < args.length) {
			found = lastParser.find(args[i]);
			if(found == null) {
				if(defaultParser == null) {
					System.out.println("Unknown argument: " + args[i]);
					System.exit(1);
				}
				found = defaultParser;
			} else {
				i++;
			}
			found.present = true;
			if(found.type > HELP && i >= args.length) {
				System.out.println("Missing value for option: " + args[i-1]);
				System.exit(2);
			}
			switch(found.type) {
				case STRING:
					found.stringValue = args[i++];
					break;
				case NUMBER:
					try {
						found.doubleValue = Double.parseDouble(args[i]);
					} 
					catch (NumberFormatException e) {
						System.out.println("Wrong number: " + args[i]);
						System.exit(2);
					}
					found.longValue = Math.round(found.doubleValue);
					found.intValue = (int)found.longValue;
					i++;
					break;
				case HELP:
					System.out.println(found.description);
					System.out.println("\r\nOptions:");
					found = lastParser;
					while (found != null) {
						if(found.type != HELP) System.out.println(found.mnemonic + " " + found.type() + found.description);
						found = found.previousParser;
					}
					System.exit(0);
			}
		}
	}
	
// Internal methods
	private Parser find(String arg) {
		if(arg.equals(mnemonic)) return this;
		if(previousParser == null) return null;
		return previousParser.find(arg);
	}

	private String type() {
		switch(type) {
			case STRING: return "string :\t";
			case NUMBER: return "number :\t";
		}
		return ":\t\t";
	}
}
