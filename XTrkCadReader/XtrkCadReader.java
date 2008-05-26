//  XtrkCadReader.java
/**
 * Utility program
 * Converts  layout schemes produced by the Open Source 
 * program XtrkCAD (freely available from  http://www.xtrkcad.org )
 * to JMRI Layout Editor format.
 * @author			Giorgio Terdina Copyright (C) 2008
 * @version			$Revision: 1.1.1.1 $
 *	2008-May-21		GT - Added support for negative radius (found in some track libraries)
 */
import java.util.*;
import java.io.*;

public class XtrkCadReader {

	// Some output costants
	static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<?xml-stylesheet href=\"http://jmri.sourceforge.net/xml/XSLT/panelfile.xsl\" type=\"text/xsl\"?>\n" +
		"<!DOCTYPE layout-config SYSTEM \"layout-config.dtd\">\n" +
		"<layout-config>\n<!--\n\nXtrkCadReader - XtrkCad to JMRI Layout Editor format conversion utility\n";
	static final String xml1 = "	<LayoutEditor class=\"jmri.jmrit.display.configurexml.LayoutEditorXml\" name=\"";
	static final String xml2 = "\" x=\"0\" y=\"0\" height=\"";
	static final String xml3 = "\" width=\"";
	static final String xml4 =	"\" editable=\"yes\" positionable=\"yes\" controlling=\"yes\" animating=\"yes\" " +
		"showhelpbar=\"yes\" mainlinetrackwidth=\"4\" xscale=\"1.00\" yscale=\"1.00\" sidetrackwidth=\"2\" defaulttrackcolor=\"black\">";
	static final String xmlFooter1 = "	</LayoutEditor>\n" +
		"	<!-- Written by XtrkCadReader on ";
	static final String xmlFooter2 = " -->\n" +
		"</layout-config>\n";

	// Files
	static String xtcFile;
	static String xmlFile;
	static PrintWriter out = null;
	
	// Input file parsing scanners
	static Scanner in = null;
	static Scanner line = null;
	static String keyword;

	// Set up a default name, just in case no title is found in the XtrkCad file
	static String layoutName = " Converted XtrkCad layout";
	
	// Coordinates conversion
	static double originalHeight = 600.0;
	static double originalWidth = 800.0;
	static double jmriMaxHeight = 600.0;
	static double jmriMaxWidth = 800.0;
	static double scale = 1.0;

	// Minimum and maximum chord length used for arcs rendering
	static final double minChord = 3.0;
	static double arcChord = 20.0;
	
	// Tolerance for automatic merging of unconnected end points.
	// If the distance between two unconnected end points 
	// is <= tolerance, the program merges them.
	static double tolerance = 2.0;

	// Track types
	static final int UNKNOWN = 0;
	static final int STRAIGHT = 1;
	static final int CURVE = 2;	
	static final int TURNOUT = 3;
	static final int CROSSING = 4;
	static final int BUMPER = 5;
	static final int TURNTABLE = 6;

	// Storage vectors
	static Vector anchors = new Vector(100,100);
	static int nAnchors = 0;	// Anchors counter
	static Vector tracks = new Vector(100,100);
	static int nTracks = 0;		// Tracks counter
	static Vector blockNames = new Vector(100,10);

	// Items numbering start values
	// can be changed by input options
	static int anchorIdent = 1;
	static int trackIdent = 1;
	static int turnoutIdent = 1;
	static int xingIdent = 1;
	static int bumperIdent = 1;
	static int turntableIdent = 1;
	static int startBlock = 1;
	
	// Hidden tracks related fields
	static boolean hiddenIgnore = false;		// Ignore XtrcCad hidden tracks settings
	static boolean hiddenDash = false;			// Render hidden tracks with dashed lines

	// Block related fields
	static int blockIdent;						// Blocks counter
	static double maxRange = 2.0;				// Maximum distance between turnouts in the same group
	static boolean enableBlockTurnouts = false;	// Enable automatic definition of blocks based on turnouts
	static boolean enableBlockGaps = false;		// Enable automatic definition of blocks based on XtrCAD block gaps
	static boolean enableBlockXing = false;		// Assign block numbers also to level crossings
	static boolean getBlockNames = false;		// Get block names from track descriptions
	static int gapMask = 0;						// Bitmask of block detection method 
												// 0 = no blocks
												// 3 = block-gaps only
												// 4 = turnouts only
												// 7 = block-gaps and turnouts
	// Work progress counters
	static int uTracks = 0;
	static int nTracks1;
	static int nTracks2;
	static int mergedPoints = 0;
	static int tCurved = 0;
	static int t3way = 0;
	static int nCurves = 0;
	static int rAnchors = 0;
	static int trackIDs = 0;
	static int turnoutIDs = 0;
	static int xingIDs = 0;
	static int anchorIDs = 0;
	static int bumperIDs = 0;
	static int turntableIDs = 0;
	
	// Highest ID number contained in the XtrkCad file
	static int maxNumber = 0;
	
	// Supported options
	static final String helpDescription = "\nXtrCadRead\rConverts XtrCAD files (.xtc) to JMRI Layout Edit format.";
	static Parser optionBlocks = new Parser("-sb", Parser.NUMBER, "Starting ID number for blocks (default " + startBlock + ")");
	static Parser optionNBlocks = new Parser("-bn", Parser.OPTION, "Obtain block names from track descriptions.");
	static Parser optionXBlocks = new Parser("-bx", Parser.OPTION, "Assign block numbers also to level crossings.");
	static Parser optionRBlocks = new Parser("-br", Parser.NUMBER, "Maximum range for inclusion of turnouts in the same block - see documentation (default " + maxRange +")");
	static Parser optionTBlocks = new Parser("-bt", Parser.OPTION, "Enable automatic definition of blocks based on turnouts.");
	static Parser optionGBlocks = new Parser("-bg", Parser.OPTION, "Enable automatic definition of blocks based on XtrCAD block gaps.");
	static Parser optionTurntables = new Parser("-stt", Parser.NUMBER, "Starting ID number for turntables (default " + turntableIdent + ")");
	static Parser optionBumpers = new Parser("-se", Parser.NUMBER, "Starting ID number for bumper end points (default " + bumperIdent + ")");
	static Parser optionXings = new Parser("-sx", Parser.NUMBER, "Starting ID number for crossings (default " + xingIdent + ")");
	static Parser optionTurnouts = new Parser("-st", Parser.NUMBER, "Starting ID number for turnouts (default " + turnoutIdent + ")");
	static Parser optionTracks = new Parser("-ss", Parser.NUMBER, "Starting ID number for track segments (default " + trackIdent + ")");
	static Parser optionAnchors = new Parser("-sa", Parser.NUMBER, "Starting ID number for anchor points (default " + anchorIdent + ")");
	static Parser optionHiddenDash = new Parser("-hd", Parser.OPTION, "Render hidden tracks with dashed lines");
	static Parser optionHiddenIgnore = new Parser("-hi", Parser.OPTION, "Ignore XtrcCad hidden tracks settings");
	static Parser optionTolerance = new Parser("-t", Parser.NUMBER, "Tolerance for automatic merging of end points (default " + tolerance + " pixels)");
	static Parser optionChord = new Parser("-c", Parser.NUMBER, "Maximum chord length for arcs rendering (default " + arcChord + " pixels, minimum " + minChord + ")");
	static Parser optionHeight = new Parser("-y", Parser.NUMBER, "Height of output frame (default " + (int)jmriMaxHeight + " pixels)");
	static Parser optionWidth = new Parser("-x", Parser.NUMBER, "Width of output frame (default " + (int)jmriMaxWidth + " pixels)");
	static Parser optionFile = new Parser("", Parser.STRING, "Input file name (mandatory).");
	static Parser optionH = new Parser("-h", Parser.HELP, helpDescription);
	static Parser optionHelp = new Parser("help", Parser.HELP, helpDescription);

	// Contructor
	public XtrkCadReader (){
		try {
			int i;
			System.out.println("\nXtrkCadReader\n\n\t" + (new java.util.Date()).toString() + 
			"\n\tConverting XtrcCAD file " + xtcFile + " to JMRI Layout Editor format\n");
			
	// 1. Files opening
			System.out.println("\t1 - Opening input and output files");
			// open input file
			in = new Scanner(new BufferedReader(new FileReader(xtcFile)));
			
			// open output file
			if((i = xtcFile.lastIndexOf('.')) < 0) {
				xmlFile = xtcFile + ".xml";
			} else {
					xmlFile = xtcFile.substring(0, i) + ".xml";
			}
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(xmlFile),"UTF-8"));
			
			// Write xml file header
			out.println(xmlHeader);
			out.println("\tInput file: " + xtcFile + "\n");
			out.println("\tOptions:");
			out.println("\t\tWidth of output frame:\t" + (int)jmriMaxWidth);
			out.println("\t\tHeight of output frame:\t" + (int)jmriMaxHeight);
			out.println("\t\tMaximum chord length for arcs rendering:\t" + arcChord);
			out.println("\t\tTolerance for end points merging:\t" + tolerance);
			if(hiddenIgnore) {
				out.println("\t\tIgnore XtrcCad hidden tracks settings");
			} else {
				if(hiddenDash) {
					out.println("\t\tRender hidden tracks with dashed lines");
				}
			}
			out.println("\t\tStarting ID number for anchor points:\t" + anchorIdent);
			out.println("\t\tStarting ID number for track segments:\t" + trackIdent);
			out.println("\t\tStarting ID number for turnouts:\t" + turnoutIdent);
			out.println("\t\tStarting ID number for crossings:\t" + xingIdent);
			out.println("\t\tStarting ID number for bumpers:\t" + bumperIdent);
			out.println("\t\tStarting ID number for turntables:\t" + turntableIdent);
			if(enableBlockGaps) {
				out.println("\t\tAutomatic definition of blocks based on XtrCAD block gaps:\tenabled");
			} else {
				out.println("\t\tAutomatic definition of blocks based on XtrCAD block gaps:\tdisabled");
			}
			if(enableBlockTurnouts) {
				out.println("\t\tAutomatic definition of blocks based on turnouts:\tenabled");
				out.println("\t\tMaximum range for inclusion of turnouts in the same block:\t" + maxRange);
			} else {
				out.println("\t\tAutomatic definition of blocks based on turnouts:\tdisabled");
			}
			if(enableBlockGaps || enableBlockTurnouts) {
				
				
				
				if(enableBlockXing) {
					out.println("\t\tAssign block numbers also to level crossings:\tenabled");
				}
				if(getBlockNames) {
					out.println("\t\tObtain block names from track descriptions:\tenabled");
				}
				out.println("\t\tStarting ID number for blocks:\t" + startBlock);
			}
			out.println("-->");

	// 2. Reading input file
			System.out.println("\t2 - Reading input file");
			
			// Main loop - read one line at the time
			while(in.hasNextLine()) {
				line = new Scanner(in.nextLine());
				line.useLocale(Locale.US);	// Make sure decimal points are properly interpreted
				if(line.hasNext()) {
					keyword = line.next();
					// Analyze the first level keyword
					if(keyword.equals("TITLE1")) {
						layoutName = line.next();
						while (line.hasNext()) layoutName += " " + line.next();
						System.out.println("\t\tLayout title: " + layoutName);
					} else if(keyword.equals("ROOMSIZE")) {
						// layout size - get X and Y values
						originalWidth = line.nextDouble();
						line.next();
						originalHeight = line.nextDouble();
						// Compute scale to fit the layout in the output area
						double sX = jmriMaxWidth / originalWidth;
						scale = jmriMaxHeight / originalHeight;
						if(sX < scale) scale = sX;
						System.out.println("\t\tOriginal layout size: " + originalWidth + " x " + originalHeight +
						"  Output scale: " + scale + "  Output size: " + (int)(originalWidth * scale + 0.5) + " x " +
						(int)(originalHeight * scale + 0.5));
						out.println("<!-- Original layout size: " + originalWidth + " x " + originalHeight +
						"  Output scale: " + scale + "  Output size: " + (int)(originalWidth * scale + 0.5) + " x " +
						(int)(originalHeight * scale + 0.5) + " -->");
					} else if(keyword.equals("TURNOUT")) {
						tracks.addElement(new XtrkCadElement(TURNOUT));
					} else if(keyword.equals("STRAIGHT") || keyword.equals("JOINT")) {
						tracks.addElement(new XtrkCadElement(STRAIGHT));
					} else if(keyword.equals("CURVE")) {
						tracks.addElement(new XtrkCadElement(CURVE));
					} else if(keyword.equals("TURNTABLE")) {
						tracks.addElement(new XtrkCadElement(TURNTABLE));
					} // Ignore other elements
				}
			}
			in.close();
			in = null;
			System.out.println("\t\t" + nTracks + " track elements and " + nAnchors + " end points read, " +
			uTracks + " unknown track elements found (see output file)");
			
	// 3. Converting data
			System.out.println("\t3 - Converting data");
			
		// 3.1 Merging unconnected end points
			System.out.println("\t\t3.1 - Merging unconnected end points closer than " + tolerance + " pixels");
			double tolerance2 = tolerance * tolerance;
			// Retrieve unconnected end points, if any
			for (int ind1 = 0; ind1 < nAnchors - 1; ind1++) {
				XtrkCadAnchor anchor1 = (XtrkCadAnchor)anchors.get(ind1);
				if(anchor1.ref[1] == 0) {
					// Stray end point found - Compare it with other unconnected end points
					for (int ind2 = ind1 + 1; ind2 < nAnchors; ind2++) {
						XtrkCadAnchor anchor2 = (XtrkCadAnchor)anchors.get(ind2);
						if(anchor2.ref[1] == 0) {
							// Another unconnected end point found
							// compute distance
							double dx = anchor1.x - anchor2.x;
							double dy = anchor1.y - anchor2.y;
							// Check if distance is within tolerance
							if((dx * dx + dy * dy) <= tolerance2) {
								// Yes - Link anchors
								anchor1.ref[1] = anchor2.ref[0];
								anchor2.ref[1] = anchor1.ref[0];
								anchor1.x = anchor2.x;
								anchor1.y = anchor2.y;
								mergedPoints++;
								break;
							}
						}
					}
				}
			}
			System.out.println("\t\t\t" + mergedPoints + " end points merged");
			
		// 3.2 Converting turnout types not supported by Layout Editor (curved and three-way)
			System.out.println("\t\t3.2 - Converting turnout types not supported by Layout Editor (curved and three-way)");
			// Limit the analysis to track elements read from XtrkCad file.
			int nTracks1 = nTracks; // nTracks can be incremented during the loop.
			for(i = 0; i < nTracks1 -1; i++) {
				XtrkCadElement track = (XtrkCadElement)tracks.get(i);
				// Is this a turnout?
				if(track.trackType == TURNOUT) {
					// Yes - what type?
					switch(track.turnoutType) {
						case 4:
						case 5:
						{
							// Curved turnout
							// Get starting point
							XtrkCadAnchor anchor1 = (XtrkCadAnchor)anchors.get(track.firstAnchor);
							// Identify end point of external arc
							// (it's normally the farer one from the starting point)
							XtrkCadAnchor anchor2 = (XtrkCadAnchor)anchors.get(track.firstAnchor+1);
							double distance = Math.pow(anchor2.x - anchor1.x, 2) + Math.pow(anchor2.y - anchor1.y, 2);
							anchor2 = (XtrkCadAnchor)anchors.get(track.firstAnchor+2);
							int extPoint = 1;
							if(Math.pow(anchor2.x - anchor1.x, 2) + Math.pow(anchor2.y - anchor1.y, 2) > distance) {
								extPoint = 2;
							} else {
								// read again first point
								anchor2 = (XtrkCadAnchor)anchors.get(track.firstAnchor+1);
							}
							// Now anchor2 contains the external point
							// Let's get again the third point 
							XtrkCadAnchor anchor3 = (XtrkCadAnchor)anchors.get(track.firstAnchor+2);
							// Compute the angle between start and end points
							double angle = anchor1.a - 180.0;
							if(angle < 0.0) angle += 360.0;
							double angle1 = anchor2.a - angle;
							// When radius is negative, the turnout is described CCW
							if(angle1 < 0.0) angle1 += 360.0;
							if(track.radius < 0) angle1 = 360.0 - angle1;
							// We will divide the arc into three segments and
							// place a normal turnout in the central one.
							angle1 /= 3.0;
							// In order to place the turnout, we need a radius!
							// Let's use the radius detected during the input phase.
							// It can correspond to the inner or to the outer radius,
							// but the difference should be irrelevant for our purposes.
							// Start point of the turnout
							double angle2 = (anchor1.a + angle1) * Math.PI / 180.0;
							double x1 = anchor1.x + Math.abs(track.radius) * (Math.cos(angle2) - Math.cos(anchor1.a * Math.PI / 180.0));
							double y1 = anchor1.y + Math.abs(track.radius) * (Math.sin(angle2) - Math.sin(anchor1.a * Math.PI / 180.0));
							// End point of the external branch
							angle2 = (anchor2.a - angle1) * Math.PI / 180.0;
							double x2 = anchor2.x - Math.abs(track.radius) * (Math.cos(angle2) - Math.cos(anchor2.a * Math.PI / 180.0));
							double y2 = anchor2.y - Math.abs(track.radius) * (Math.sin(angle2) - Math.sin(anchor2.a * Math.PI / 180.0));
							//Let's find the middle point
							double x3 = (x1 + x2) / 2.0;
							double y3 = (y1 + y2) / 2.0;
							// And the angle connecting it with the end point of the other arc (inner branch)
							angle2 = Math.atan2(anchor3.y - y3, anchor3.x - x3);
							// Let's compute the length of our turnout / 2
							distance = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2)) / 2.0;
							// We can now compute the last end point of the turnout
							x3 += distance * Math.cos(angle2);
							y3 += distance * Math.sin(angle2);
							// End of computations, now let's create track segments
							// First of all, a straight segment from the starting
							// point of the original turnout to the starting point
							// of the new one.
							maxNumber++;
							XtrkCadElement newTrack = new XtrkCadElement();
							newTrack.description = "Rendering of curved turnout " + track.description;
							newTrack.visible = track.visible;
							tracks.addElement(newTrack);
							anchors.addElement(new XtrkCadAnchor(maxNumber, anchor1.ref[1], anchor1.x, anchor1.y));
							anchors.addElement(new XtrkCadAnchor(maxNumber, maxNumber + 1, x1, x1));
							// Then the new turnout
							maxNumber++;
							newTrack = new XtrkCadElement();
							newTrack.trackType = TURNOUT;
							newTrack.visible = track.visible;
							newTrack.turnoutType = track.turnoutType - 3; // Left or right hand
							newTrack.lastAnchor++; // Increase anchor's number
							tracks.addElement(newTrack);
							anchors.addElement(new XtrkCadAnchor(maxNumber, maxNumber-1, x1, y1));
							anchors.addElement(new XtrkCadAnchor(maxNumber, track.originalNumber, x2, y2));
							anchors.addElement(new XtrkCadAnchor(maxNumber, maxNumber+1, x3, y3));
							// Connect the thrown branch using a straight track
							maxNumber++;
							newTrack = new XtrkCadElement();
							newTrack.visible = track.visible;
							tracks.addElement(newTrack);
							anchors.addElement(new XtrkCadAnchor(maxNumber, maxNumber-1, x3, y3));
							anchors.addElement(new XtrkCadAnchor(maxNumber, anchor3.ref[1], anchor3.x, anchor3.y));
							// Now transform the original curved turnout into a straight vector
							track.trackType = STRAIGHT;
							anchor1.x = x2;
							anchor1.y = y2;
							anchor1.ref[1] = maxNumber-1;
							if(extPoint == 2) {
								anchor2.x = anchor3.x;
								anchor2.y = anchor3.y;
								anchor2.ref[1] = anchor3.ref[1];
							}
							// Get rid of unused end point
							anchor3.skip = true;
							track.lastAnchor--;
							// Done!
							tCurved++;
						}
						break;
						case 6:
						{
							// Three-way turnout
							// Get anchor points
							XtrkCadAnchor anchor1 = (XtrkCadAnchor)anchors.get(track.firstAnchor);
							XtrkCadAnchor anchor2 = (XtrkCadAnchor)anchors.get(track.firstAnchor + 1);
							XtrkCadAnchor anchor3 = (XtrkCadAnchor)anchors.get(track.firstAnchor + 2);
							XtrkCadAnchor anchor4 = (XtrkCadAnchor)anchors.get(track.firstAnchor + 3);
							// Find out the central exit
							int central = 1;
							int first = 2;
							int second = 3;
							double angle = anchor1.a - 180.0;
							if(angle < 0.0) angle += 360.0;
							double angle1 = Math.abs(anchor2.a - angle);
							double angle2 = Math.abs(anchor3.a - angle);
							if(angle2 < angle1) {
								angle1 = angle2;
								central = 2;
								first = 1;
							}
							angle2 = Math.abs(anchor4.a - angle);
							if(angle2 < angle1) {
								central = 3;
								first = 1;
								second = 2;
							}
							// Compute new coordinates (median point)
							double xM = (anchor1.x + ((XtrkCadAnchor)anchors.get(track.firstAnchor + central)).x) / 2.0;
							double yM = (anchor1.y + ((XtrkCadAnchor)anchors.get(track.firstAnchor + central)).y) / 2.0;
							// Create a new turnout with three anchors (duplicate anchors will be removed later)
							maxNumber++;
							XtrkCadElement newTrack = new XtrkCadElement();
							newTrack.trackType = TURNOUT;
							newTrack.visible = track.visible;
							// Set turnout types (one will be left, the other right)
							if(anchor1.a > ((XtrkCadAnchor)anchors.get(track.firstAnchor + second)).a) newTrack.turnoutType = 2; // Left hand
							else newTrack.turnoutType = 1; // Right hand
							// The other is the reversed
							track.turnoutType = 3 - newTrack.turnoutType;
							// Add a comment to newly created turnout
							newTrack.description = "Rendering of three-way turnout #" + track.originalNumber;
							newTrack.lastAnchor++; // Increase anchor's number
							tracks.addElement(newTrack);
							anchors.addElement(new XtrkCadAnchor(maxNumber, anchor1.ref[0], xM, yM)); // Turnout entry
							anchors.addElement(new XtrkCadAnchor(maxNumber, ((XtrkCadAnchor)anchors.get(track.firstAnchor + central)).ref[1], 
								((XtrkCadAnchor)anchors.get(track.firstAnchor + central)).x,
								((XtrkCadAnchor)anchors.get(track.firstAnchor + central)).y)); // Straight exit
							anchors.addElement(new XtrkCadAnchor(maxNumber, ((XtrkCadAnchor)anchors.get(track.firstAnchor + second)).ref[1], 
								((XtrkCadAnchor)anchors.get(track.firstAnchor + second)).x,
								((XtrkCadAnchor)anchors.get(track.firstAnchor + second)).y)); // Thrown exit
							// Compute new coordinates to relocate the old turnout
							// Save old coordinates of thrown exit
							double xTold = ((XtrkCadAnchor)anchors.get(track.firstAnchor + first)).x;
							double yTold = ((XtrkCadAnchor)anchors.get(track.firstAnchor + first)).y;
							// Compute new coordinates of thrown exit
							double xTnew = (xM + anchor1.x) / 2.0;
							double yTnew = (yM + anchor1.y) / 2.0;
							xTnew += (xTold - xTnew) / 3.0;
							yTnew += (yTold - yTnew) / 3.0;
							// Now adjust end points of the old turnout
							int link = ((XtrkCadAnchor)anchors.get(track.firstAnchor + first)).ref[1];
							anchor2.ref[1] = maxNumber;
							anchor2.x = xM;
							anchor2.y = yM;
							maxNumber++; // ID of the next track
							anchor3.ref[1] = maxNumber;
							anchor3.x = xTnew;
							anchor3.y = yTnew;
							// Get rid the third exit point
							anchor4.skip = true;
							track.lastAnchor--;
							// And now add a straight track to connect the old turnout with its original end point
							newTrack = new XtrkCadElement();
							newTrack.visible = track.visible;
							tracks.addElement(newTrack);
							anchors.addElement(new XtrkCadAnchor(maxNumber, anchor1.ref[0], xTnew, yTnew));
							anchors.addElement(new XtrkCadAnchor(maxNumber, link, xTold, yTold));
							t3way ++;
						}
						break;
					}
				}
			}
			System.out.println("\t\t\t" + tCurved + " curved and " + t3way + " three-way turnouts converted");
			
		// 3.3 Inserting padding tracks to separate turnouts, crossings and turntables.
			System.out.println("\t\t3.3 - Inserting padding tracks to separate turnouts and crossings");
			// Layout Editor does not support turnout to turnout connections.
			nTracks2 = nTracks; // nTracks can be incremented during the loop.
			for(i = 0; i < nTracks2 -1; i++) {
				XtrkCadElement track = (XtrkCadElement)tracks.get(i);
				// Check all turnouts and crossings
				if(track.trackType == TURNOUT || track.trackType == CROSSING || track.trackType == TURNTABLE) {
					// Check for direct connection with another turnout or crossing
					// Scan end points
					for(int ind = track.firstAnchor; ind < track.lastAnchor; ind++) {
						XtrkCadElement track1 = track.getNextTrack(ind);
						if(track1 != null) {
							if(track1.trackType == TURNOUT || track1.trackType == CROSSING || track.trackType == TURNTABLE) {
								//Turnout-to-turnout direct connection - Insert an intermediate zero length track
								// Retrieve coordinates
								XtrkCadAnchor anchor = (XtrkCadAnchor)anchors.get(ind);
								// Create a new track with two anchors (duplicate anchors will be removed later)
								maxNumber++;
								XtrkCadElement newTrack = new XtrkCadElement();
								// Add a comment to newly created track
								newTrack.description = "Padding track between Turnouts/Crossings #"
								 + track.originalNumber + " and #" + track1.originalNumber;
								newTrack.visible = track.visible;
								tracks.addElement(newTrack);
								anchors.addElement(new XtrkCadAnchor(maxNumber, anchor.ref[0], anchor.x, anchor.y));
								anchors.addElement(new XtrkCadAnchor(maxNumber, anchor.ref[1], anchor.x, anchor.y));
								//Retrieve the end point of the other turnout
								for(int ind2 = track1.firstAnchor; ind2 < track1.lastAnchor; ind2++) {
									XtrkCadAnchor anchor1 = (XtrkCadAnchor)anchors.get(ind2);
									if(anchor1.ref[1] == track.originalNumber) {
										//Link turnout found with new segment
										anchor1.ref[1] = maxNumber;
										break;
									}
								}
								//Link present turnout with new segment
								anchor.ref[1] = maxNumber;
							}
						}
					}
				}
			}
			System.out.println("\t\t\t" + (nTracks - nTracks2) + " padding tracks inserted");

		// 3.4 Rendering curved tracks.
			System.out.println("\t\t3.4 - Rendering curved tracks (maximum chord = " + arcChord + " pixels)");
			nTracks2 = nTracks;
			for(i = 0; i < nTracks1; i++) {
				XtrkCadElement track = (XtrkCadElement)tracks.get(i);
				if(track.trackType == CURVE) {
					// Get end points
					XtrkCadAnchor anchorS = (XtrkCadAnchor)anchors.get(track.firstAnchor);
					XtrkCadAnchor anchorE = (XtrkCadAnchor)anchors.get(track.firstAnchor + 1);
					// Get starting angle
					double ss = anchorS.a;
					// Make sure angle is in 0-360 range
					if(ss < 0) ss += 360.0;
					// Get end angle (needs to be complemented)
					double ee = anchorE.a - 180.0;
					// Make sure angle is in 0-360 range
					if(ee < 0) ee += 360.0;
					// Make sure end angle is greater than start angle
					// taking into account arc direction
					// radius > 0 CW
					// radius < 0 CCW
					if(track.radius < 0) {
						if(ee >= ss) ee -= 360.0;
					} else {
						if(ee <= ss) ee += 360.0;
					}
					// Compute arc width
					ee -= ss;
					// Compute the angle corresponding to the chord
					double step = Math.atan2(arcChord, Math.abs(track.radius)) * 180.0 / Math.PI;
					// Compute the number of chords (make sure it's always truncated to the lowest integer)
					int nSteps = (int) (Math.abs(ee)/step -0.5);
					// Make sure we create at least two chords
					if(nSteps < 1) nSteps = 1;
					// Recompute the chord angle, in order to obtain chords of equal length
					step = ee / (double)(nSteps + 1);
					// Convert to radiants
					ss = ss * Math.PI / 180.0;
					step = step * Math.PI / 180.0;
					
					// Take note of the first track and anchor that will be created
					int arcAnchor0 = nAnchors;
					int arcTrack0 = nTracks;

					// Set starting point coordinates
					double newX = anchorS.x;
					double newY = anchorS.y;
					
					// Create intermediate anchors and connect them
					for(int ind = 0; ind < nSteps; ind++) {
						ss += step;
						maxNumber++;
						// Create a new track with two anchors (duplicate anchors will be removed later)
						XtrkCadElement newTrack = new XtrkCadElement();
						newTrack.visible = track.visible;
						tracks.addElement(newTrack);
						anchors.addElement(new XtrkCadAnchor(maxNumber, maxNumber - 1, newX, newY));
						newX = track.xReference + track.radius * Math.cos(ss);
						newY = track.yReference + track.radius * Math.sin(ss);
						anchors.addElement(new XtrkCadAnchor(maxNumber, maxNumber + 1, newX, newY));
					}
					
					// Add a comment to the first track created
					((XtrkCadElement)tracks.get(arcTrack0)).description = "Rendering of " + track.description;
					
					// Adjust link in first anchor point of first chord
					((XtrkCadAnchor)anchors.get(arcAnchor0)).ref[1] = anchorS.ref[1];

					// Adjust link in duplicate anchor of original track
					for (int ind2 = 0; ind2 < nAnchors; ind2++) {
						XtrkCadAnchor anchor2 = (XtrkCadAnchor)anchors.get(ind2);
						if(anchorS.ref[0] == anchor2.ref[1] && anchorS.ref[1] == anchor2.ref[0]) {
							// Duplicate found, change link
							anchor2.ref[1] = ((XtrkCadAnchor)anchors.get(arcAnchor0)).ref[0];
							break;
						}
					}

					// Adjust link in last anchor point of last chord created
					((XtrkCadAnchor)anchors.get(nAnchors - 1)).ref[1] = anchorE.ref[0];
					
					// Convert the orginal track into the last chord
					anchorS.ref[1] = maxNumber;
					anchorS.x = newX;
					anchorS.y = newY;
					nCurves++;
				}
			}			
			System.out.println("\t\t\t" + nCurves + " curves rendered, for a total of " + (nCurves + nTracks - nTracks2) + " chords");
			
		// 3.5 Removing duplicated end points.
			System.out.println("\t\t3.5 - Removing duplicated end points");
			for (int ind1 = 0; ind1 < nAnchors - 1; ind1++) {
				XtrkCadAnchor anchor1 = (XtrkCadAnchor)anchors.get(ind1);
				if(anchor1.duplicate < 0) {
					for (int ind2 = ind1 + 1; ind2 < nAnchors; ind2++) {
						XtrkCadAnchor anchor2 = (XtrkCadAnchor)anchors.get(ind2);
						if(anchor2.duplicate < 0) {
							if(anchor1.ref[0] == anchor2.ref[1] && anchor1.ref[1] == anchor2.ref[0]) {
								// Duplicated point found, link it
								anchor1.duplicate = ind2;
								anchor2.duplicate = ind1;
								// Compute block gap indicator
								// By adding the two indicators we obtain:
								//  1 if the gap is only on one track
								//  2 if the gap is on both tracks
								// For the time being we treat both cases in the same way
								// In the future, it may be useful if we introduce sub-blocks
								anchor1.blockGap += anchor2.blockGap;
								anchor2.blockGap = anchor1.blockGap;
								// And suppress output of one of the two points
								anchor1.skip = true;
								rAnchors++;
								break;
							}
						}
					}
				}
			}
			System.out.println("\t\t\t" + rAnchors + " duplicated end points removed");
			
		// 3.6 Assigning JMRI IDs to tracks
			System.out.println("\t\t3.6 - Assigning JMRI IDs to tracks");
			for(i = 0; i < nTracks; i++) {
				XtrkCadElement track = (XtrkCadElement)tracks.get(i);
				switch (track.trackType) {
					case BUMPER:	// A bumper is compose by a straight track and a bumper end point
					case STRAIGHT:
					case CURVE:
						track.jmriNumber = trackIdent++;
						trackIDs++;
						break;
					case TURNOUT:
						track.jmriNumber = turnoutIdent;
						// Propagate ID and turnout type to turnout's anchor points
						for(int ind = track.firstAnchor; ind < track.lastAnchor; ind++) {
							XtrkCadAnchor anchor = (XtrkCadAnchor)anchors.get(ind);
							anchor.jmriNumber = turnoutIdent;
							anchor.type = ind - track.firstAnchor + 3;
							// Suppress output
							anchor.skip = true;	
							if(anchor.duplicate >= 0) ((XtrkCadAnchor)anchors.get(anchor.duplicate)).skip = true;
						}
						turnoutIdent++;
						turnoutIDs++;
						break;
					case CROSSING:
						track.jmriNumber = xingIdent;
						// Set crossing anchor points in the order expected by JMRI layout editor
						for(int ind = track.firstAnchor; ind < track.lastAnchor; ind++) {
							XtrkCadAnchor anchor = (XtrkCadAnchor)anchors.get(ind);
							anchor.jmriNumber = xingIdent;
							anchor.type = ind - track.firstAnchor;
							switch (anchor.type) {
								case 0:
									anchor.type = 1;
									break;
								case 1:
									anchor.type = 3;
									break;
								case 2:
									anchor.type = 0;
									break;
								case 3:
									anchor.type = 2;
									break;
							}
							anchor.type += 7;
							anchor.skip = true;
							if(anchor.duplicate >= 0) ((XtrkCadAnchor)anchors.get(anchor.duplicate)).skip = true;
						}
						xingIdent++;
						xingIDs++;
						break;
					case TURNTABLE:
						track.jmriNumber = turntableIdent;
						// Set sequential numbering of turntable raytracks
						for(int ind = track.firstAnchor; ind < track.lastAnchor; ind++) {
							XtrkCadAnchor anchor = (XtrkCadAnchor)anchors.get(ind);
							anchor.jmriNumber = turntableIdent;
							anchor.type = ind - track.firstAnchor + 50;
						}
						turntableIdent++;
						turntableIDs++;
						break;
				}
			}
			System.out.println("\t\t\t" + trackIDs + " tracks, " + turnoutIDs + " turnouts, " + xingIDs + " level crossings and " + turntableIDs +" turntables");
			
		// 3.7 Assigning JMRI IDs to anchor points and bumpers.
			System.out.println("\t\t3.7 - Assigning JMRI IDs to anchor points and bumpers");
			for (int ind = 0; ind < nAnchors; ind++) {
				XtrkCadAnchor anchor = (XtrkCadAnchor)anchors.get(ind);
				if(!anchor.skip) {
					if(anchor.type == 1) {
						anchor.jmriNumber = anchorIdent++;
						anchorIDs++;
					} else {
						anchor.jmriNumber = bumperIdent++;
						bumperIDs++;
					}
				}
			}
			System.out.println("\t\t\t" + anchorIDs + " anchor points and " + bumperIDs + " bumpers");
			
		// 3.8 Dividing layout into blocks. Not for faint-hearted :-)
			System.out.println("\t\t3.8 - Dividing layout into blocks");
			blockIdent = startBlock;
			
			if(enableBlockGaps) gapMask = 3;
			if(enableBlockTurnouts) {
				gapMask |= 4;
				// Place, first of all, block gaps on turnouts (frog side)
				for(i = 0; i < nTracks2; i++) {
					XtrkCadElement track = (XtrkCadElement)tracks.get(i);
					if(track.trackType == TURNOUT) {
						// Turnout, compute its length and use it to compute the maximum distance between turnouts in the same block
						XtrkCadAnchor anchor1 = (XtrkCadAnchor)anchors.get(track.firstAnchor);
						XtrkCadAnchor anchor2 = (XtrkCadAnchor)anchors.get(track.firstAnchor + 1);
						double maxDistance = Math.pow((anchor2.x - anchor1.x) * maxRange, 2) + Math.pow((anchor2.y - anchor1.y) * maxRange, 2);
						// Now analyze divergent branches of the present turnout
						for(int ind = track.firstAnchor + 1; ind < track.lastAnchor; ind++) {
							anchor2 = (XtrkCadAnchor)anchors.get(ind);
							// Find out if along this branch:
							//		1 - there is another turnout;
							//		2 - that has the same orientation of this one; and
							//		3 - is placed within the range computed above
							if(!anchor2.getConnectedTrack(1).checkPath(track.originalNumber, maxDistance, anchor2.x, anchor2.y)) {
								// Other turnout not found - place block boundaries on this branch
								anchor2.blockGap |= 4;
								// and on the duplicate anchor (if any)
								if(anchor2.duplicate >= 0) ((XtrkCadAnchor)anchors.get(anchor2.duplicate)).blockGap |= 4;
							}
						}
					}
				}
			}
			
			// Now assign block numbers
			if(gapMask != 0) {
				for(i = 0; i < nTracks; i++) {
					XtrkCadElement track = (XtrkCadElement)tracks.get(i);
					if(track.trackType != CROSSING && track.trackType != TURNTABLE && track.block == 0) {
						track.setBlock(-1, blockIdent);
						blockIdent++;
					}
				}
			// Now assign block names
			// Set, first of all, default names
			for(i = startBlock; i < blockIdent; i++) {
				BlockName blockName = new BlockName();
				blockName.system = "ILB" + (i - startBlock + 1);
				blockName.user = "B" +  (i - startBlock + 1);
				blockNames.addElement(blockName);
			}
			// Now retrieve names from track descriptions (if required)
			if(getBlockNames) {
				for(i = 0; i < nTracks1; i++) {
					XtrkCadElement track = (XtrkCadElement)tracks.get(i);
					if(track.block != 0) {
						BlockName blockName = (BlockName)blockNames.get(track.block - startBlock);
						line = new Scanner(track.description);
						while(line.hasNext()) {
							keyword = line.next();
							if(keyword.equals("blocksystemname") && line.hasNext()) {
								blockName.system = line.next();
							} else if(keyword.equals("blockusername") && line.hasNext()) {
								blockName.user = line.next();
							}
						}
					}
				}
			}
			System.out.println("\t\t\t" + (blockIdent - startBlock) + " blocks created");
			} else {
				System.out.println("\t\t\tSkipped");
			}
			
	// 4. Writing data to output file
			System.out.println("\t4 - Writing data to output file " + xmlFile);

			// Write Blocks
			if(blockIdent > 1) {
				out.println("\t<layoutblocks class=\"jmri.jmrit.display.configurexml.LayoutBlockManagerXml\">");
				for(i = startBlock; i < blockIdent; i++) {
						BlockName blockName = (BlockName)blockNames.get(i - startBlock);
						out.println("\t\t<layoutblock systemName=\"" + blockName.system + "\" userName=\"" + blockName.user + 
					"\" occupiedsense=\"2\" trackcolor=\"black\" occupiedcolor=\"red\" />");
				}
				out.println("\t</layoutblocks>\n\t<blocks class=\"jmri.configurexml.BlockManagerXml\" />");
			}
			
			// Write LayoutEditor statement
			// NOTE: The height increase of 65 pixels has been experimentaly determined
			// and compensates for some apparent quirk in Layout Editor
			out.println(xml1 + layoutName + xml2 + (int) (originalHeight * scale + 65.5) + xml3 + (int) (originalWidth * scale + 0.5) + xml4);
			
			// Write Tracks and relevant Anchor Points
			for(i = 0; i < nTracks; i++) {
				((XtrkCadElement)tracks.get(i)).print();
			}
			
			// Write closure lines
			out.println(xmlFooter1 + (new java.util.Date()).toString() + xmlFooter2);
		}
		catch(java.io.FileNotFoundException e){System.out.println("File " + xtcFile + " not found!");}
		catch(java.io.IOException e){System.out.println("I/O error: " + e);}
		finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
					out.close();
			}
        }
		System.out.println("\n\t" + (new java.util.Date()).toString() + "\n\tConversion completed! Have fun!\n");
    }
	
	public static void main(String [] args){
		// Retrieve input file name from command line
		Parser.parse(args);
		if(optionFile.present) {
			xtcFile = optionFile.stringValue;
		} else {
			System.out.println("Error: missing input file name.\nRe-enter command as follows:\n\tjava -jar XtrkCad.jar [options] inputFileName" +
				"\nor\n\tjava -jar XtrkCad.jar -h\nfor help.");
			System.exit(3);
		}
		// Retrieve possible options from command line
		if(optionHeight.present && optionHeight.doubleValue >= 1.0) jmriMaxHeight = optionHeight.doubleValue;
		if(optionWidth.present && optionWidth.doubleValue >= 1.0) jmriMaxWidth = optionWidth.doubleValue;
		if(optionChord.present && optionChord.doubleValue >= minChord) arcChord = optionChord.doubleValue;
		if(optionTolerance.present) tolerance = optionTolerance.doubleValue;
		if(optionHiddenDash.present) hiddenDash = true;
		if(optionHiddenIgnore.present) {
			hiddenIgnore = true;
			hiddenDash = false;
		}
		if(optionAnchors.present) anchorIdent = optionAnchors.intValue;
		if(optionTracks.present) trackIdent = optionTracks.intValue;
		if(optionTurnouts.present) turnoutIdent = optionTurnouts.intValue;
		if(optionXings.present) xingIdent = optionXings.intValue;
		if(optionBumpers.present) bumperIdent = optionBumpers.intValue;
		if(optionTurntables.present) turntableIdent = optionTurntables.intValue;
		if(optionBlocks.present && optionBlocks.intValue > 0) startBlock = optionBlocks.intValue;
		if(optionTBlocks.present) enableBlockTurnouts = true;
		if(optionGBlocks.present) enableBlockGaps = true;
		if(optionRBlocks.present) maxRange = optionRBlocks.doubleValue;
		if(optionXBlocks.present) enableBlockXing = true;
		if(optionNBlocks.present) getBlockNames = true;

		// And now do the job!
		new XtrkCadReader();
	}

// INTERNAL CLASSES

	public class XtrkCadElement {
		// Track element
	
		// Pointers to end point anchors
		int firstAnchor, lastAnchor;

		int trackType;			// STRAIGHT, CURVE, etc.
		int originalNumber;		// Progressive ID number used by XtrkCAD
		int jmriNumber;			// New ID number, progressive within JMRI track types
		int block = 0;			// Block number
		int blockX = 0;			// Additional block number for crossings

		// Reference coordinates
		// Can assume different meaning, in accorance to trackType
		double xReference, yReference, angleReference;
		double radius;
		int	turnoutType;
		int visible;
		
		// Counters of straight and curved segments and paths
		// contained in the track element
		int iC = 0;
		int iS = 0;
		int iP = 0;

		
		String description = "";	// XtrkCAD track description 
		
		// Standard Constructor of XtrkCadElement class
		// Populates fields reading them from the XtrkCAD file
		public XtrkCadElement (int newType) {
			trackType = newType;
			// Increment number of tracks
			nTracks++;
			// Extract information from input file
			// Get item number
			originalNumber = line.nextInt();
			// Keep track of the highest ID encountered in order to avoid 
			// creating duplicate IDs in subsequent phases
			if(originalNumber > maxNumber) maxNumber = originalNumber;
			// Skip unused fields
			line.next();
			line.next();
			line.next();
			line.next();
			line.next();
			// Retrieve visibility idicator
			visible = line.nextInt();
			// Retrieve track type specific fields
			if(trackType == CURVE || trackType == TURNTABLE) {
				// Flexi-track curve or turntable
				// Get center and radius
				xReference = line.nextDouble() * scale;
				yReference = (originalHeight - line.nextDouble()) * scale;
				line.next();
				radius = line.nextDouble() * scale;
				if(trackType == TURNTABLE ) {
					description = "#" + originalNumber + " Turntable";
				} else {
					description = "#" + originalNumber + " Flexi-track arc segment";
				}
			} else if(trackType == TURNOUT) {
				// The TURNOUT term is misleading. It describes any type of track obtained from a library
				// Get starting point and starting angle (we may need them later)
				xReference = line.nextDouble() * scale;
				yReference = (originalHeight - line.nextDouble()) * scale;
				line.next();
				angleReference = line.nextDouble();
				description = "#" + originalNumber;
				while(line.hasNext()) description += " " + line.next();
			} else if(trackType == STRAIGHT) {
				description = "#" + originalNumber + " Flexi-track straight segment";
			}
			// Take note of the first anchor point
			firstAnchor = nAnchors;
			// Scan detail lines
			while(true) {
				if(!in.hasNextLine()) break;
				line = new Scanner(in.nextLine());
				line.useLocale(Locale.US);	// Make sure decimal points are properly interpreted
				if(line.hasNext()) {
					keyword = line.next();
					if(keyword.equals("T")) {
						// End point connected with another track
						anchors.addElement(new XtrkCadAnchor(originalNumber, true));
					} else if(keyword.equals("E")) {
						// End point not connected
						anchors.addElement(new XtrkCadAnchor(originalNumber, false));
					} else if(keyword.equals("S")) {
						// Straight segment - count it
						iS++;
						if(iC == 0) { // If a curve was already found, don't care about straight segment
							// Compute end point coordinates (we may need them in case this is a bumper)
							line.next();
							line.next();
							double x = line.nextDouble() * scale;
							double y = line.nextDouble() * scale;
							double c = Math.cos(angleReference * Math.PI / 180.0);
							double s = Math.sin(angleReference * Math.PI / 180.0);
							xReference += x * c + y * s;
							yReference -= y * c - x * s;
							x = line.nextDouble() * scale - x;
							y -= line.nextDouble() * scale; // Y is inverted
							xReference += x * c - y * s;
							yReference += y * c + x * s;
						}
					} else if(keyword.equals("C")) {
						// Curved segment - count it
						iC++;
						// Get radius and compute center coordinates (we may need them in case this is curve)
						line.next();
						line.next();
						radius = line.nextDouble() * scale;
						// Center coordinates are relative to starting point
						double x = line.nextDouble() * scale;
						double y = line.nextDouble() * scale;
						// and need to be rotated
						double c = Math.cos(angleReference * Math.PI / 180.0);
						double s = Math.sin(angleReference * Math.PI / 180.0);
						xReference -= x * c - y * s;
						yReference -= x * s + y * c;
					} else if(keyword.equals("P")) {
					// Path - simply count it
					//We may need to know the number of paths to distinguish crossings from three-way turnouts
						iP++;
					} else if(keyword.equals("END")) {
						// End of description lines for this track
						break;
					}
				}
			}
			// Take note of the last anchor point
			lastAnchor = nAnchors;
			// Identify element type
			// Elements marked as TURNOUTS can actually be any item from tracks library
			// (XtrkCAD apparently uses STRAIGHT and CURVE only for flexi-track)
			if(trackType == TURNOUT) {
				// Analyze number of end point
				switch(lastAnchor - firstAnchor) {
					case 1:
						trackType = BUMPER;
						// Create an additional anchor point
						anchors.addElement(new XtrkCadAnchor(originalNumber, xReference, yReference));
						lastAnchor = nAnchors;
						break;
					case 2:
						if(iS == 0) {
							trackType = CURVE;
						} else {
							trackType = STRAIGHT;
						}
						break;
					case 3:
						// Turnout // Determine type
						XtrkCadAnchor anchor1 = (XtrkCadAnchor)anchors.get(firstAnchor);
						XtrkCadAnchor anchor2 = (XtrkCadAnchor)anchors.get(firstAnchor + 1);
						XtrkCadAnchor anchor3 = (XtrkCadAnchor)anchors.get(firstAnchor + 2);
						double angle = anchor1.a - 180.0;
						if(angle < 0.) angle += 360.0;
						if(Math.abs(angle - anchor2.a) < 0.5) {
							// Normal turnout
							turnoutType = 1;
						} else if((anchor2.a - angle) * (anchor3.a - angle) > 0) {
							// Curved turnout
							turnoutType = 4;
						} else {
							// WYE turnout
							turnoutType = 3;
						}
						// Determine turnout direction
						if(turnoutType != 3) {
							if(angle > anchor3.a) turnoutType += 1; // Left hand
						}
						break;
					case 4:
						if(iP < 3) {
							trackType = CROSSING;
						} else if(iP == 3) {
							// Three-way turnout
							turnoutType = 6;
						} else {
							trackType = UNKNOWN;
							uTracks++;
						}
						break;
					default:
						trackType = UNKNOWN;
						uTracks++;
				}
			}
		}
		
		// Alternate constructor of XtrkCadElement class
		// (used only for arcs rendering and padding tracks)
		public XtrkCadElement () {
			// Increment number of tracks
			nTracks++;
			trackType = STRAIGHT;
			originalNumber = maxNumber;
			firstAnchor = nAnchors;
			lastAnchor = nAnchors + 2;
		}
		
		// Methods of the XtrkCadElement class
		
		public void print() {
			// Output track to XML file
			double xcen, ycen, xa, ya, xb, yb, xc, yc;
			String nameA, nameB, nameC, nameD;
			XtrkCadAnchor anchor1, anchor2;
			
			// Add a leading comment line
			if(!description.equals("")) out.println("	 <!-- " + description + " -->");
			// Turntables require separate handling
			if(trackType == TURNTABLE) {
				out.println("\t\t<layoutturntable ident=\"TUR" + jmriNumber + "\" radius=\"" + radius + "\" xcen=\"" + xReference + 
					"\" ycen=\"" + yReference + "\" class=\"jmri.jmrit.display.configurexml.LayoutTurntableXml\">");
				XtrkCadAnchor anchor;
				for (int ind1 = firstAnchor; ind1 < lastAnchor; ind1++) {
					anchor = (XtrkCadAnchor)anchors.get(ind1);
					out.println("\t\t\t<raytrack angle=\"" + anchor.a + "\" connectname=\"" + 
						anchor.getConnectedName(1) + "\" index=\"" + (anchor.type - 50) + "\" />");
				}
				out.println("\t\t</layoutturntable>");
				return;
			}
			// Write Anchor Points
			for (int ind1 = firstAnchor; ind1 < lastAnchor; ind1++) {
				((XtrkCadAnchor)anchors.get(ind1)).print();
			}
			String blockString = "";
			if(block != 0) blockString = " blockname=\"" + ((BlockName)blockNames.get(block - startBlock)).user + "\"";

			// Now write the track
			switch(trackType) {
				case TURNOUT:
					// Although not explicitely stated, the first anchor
					// seems to be the point of the turnout
					anchor1 = (XtrkCadAnchor)anchors.get(firstAnchor);
					xcen = anchor1.x;
					ycen = anchor1.y;
					// Second anchor seems to be the end of the straight segment
					anchor1 = (XtrkCadAnchor)anchors.get(firstAnchor+1);
					xb = anchor1.x;
					yb = anchor1.y;
					// Third anchor seems to be the end of the curved segment
					anchor2 = (XtrkCadAnchor)anchors.get(firstAnchor+2);
					xc = anchor2.x;
					yc = anchor2.y;
					// Compute the center of the turnout, as required by JMRI Layout Editor
					if(turnoutType == 3) {
						// WYE turnout
						xcen = (xcen + (xb + xc)/2.0)/2.0;
						ycen = (ycen + (yb + yc)/2.0)/2.0;
					} else {
						// Normal turnout
						xcen = (xcen + xb)/2.0;
						ycen = (ycen + yb)/2.0;
					}
					// Get references to neighbor tracks
					nameA = ((XtrkCadAnchor)anchors.get(firstAnchor)).getConnectedName(1);
					if(!nameA.equals("")) nameA = " connectaname=\"" + nameA +"\"";
					nameB = ((XtrkCadAnchor)anchors.get(firstAnchor+1)).getConnectedName(1);
					if(!nameB.equals("")) nameB = " connectbname=\"" + nameB +"\"";
					nameC = ((XtrkCadAnchor)anchors.get(firstAnchor+2)).getConnectedName(1);
					if(!nameC.equals("")) nameC = " connectcname=\"" + nameC +"\"";
					out.println("		<layoutturnout ident=\"TO" + jmriNumber + "\"" + blockString + " type=\"" + turnoutType + "\"" + nameA + nameB + nameC + 
						" continuing=\"2\" disabled=\"no\" xcen=\"" + xcen + "\" ycen=\"" + ycen + "\" xb=\"" + xb + "\" yb=\"" + yb + 
						"\" xc=\"" + xc + "\" yc=\"" + yc + "\" class=\"jmri.jmrit.display.configurexml.LayoutTurnoutXml\" />");
					break;
				case CROSSING:
					blockString = "";
					if(blockX != 0) blockString = " blocknameac=\"" + ((BlockName)blockNames.get(blockX - startBlock)).user + "\"";
					if(block != 0) blockString += " blocknamebd=\"" + ((BlockName)blockNames.get(block - startBlock)).user + "\"";
					// First anchor seems to be the start of the second segment - b
					anchor1 = (XtrkCadAnchor)anchors.get(firstAnchor);
					xb = anchor1.x;
					yb = anchor1.y;
					// Second anchor seems to be the end of the second segment - d
					anchor1 = (XtrkCadAnchor)anchors.get(firstAnchor+1);
					xcen = anchor1.x;
					ycen = anchor1.y;
					// Third anchor seems to be the start of the first segment - a
					anchor1 = (XtrkCadAnchor)anchors.get(firstAnchor+2);
					xa = anchor1.x;
					ya = anchor1.y;
					// Compute the center of the crossing, as required by JMRI Layout Editor
					xcen = (xcen + xb)/2.0;
					ycen = (ycen + yb)/2.0;
					// Get references to neighbor tracks in the order expected by JMRI Layout Editor
					nameA = ((XtrkCadAnchor)anchors.get(firstAnchor+2)).getConnectedName(1);
					if(!nameA.equals("")) nameA = " connectaname=\"" + nameA +"\"";
					nameB = ((XtrkCadAnchor)anchors.get(firstAnchor)).getConnectedName(1);
					if(!nameB.equals("")) nameB = " connectbname=\"" + nameB +"\"";
					nameC = ((XtrkCadAnchor)anchors.get(firstAnchor+3)).getConnectedName(1);
					if(!nameC.equals("")) nameC = " connectcname=\"" + nameC +"\"";
					nameD = ((XtrkCadAnchor)anchors.get(firstAnchor+1)).getConnectedName(1);
					if(!nameD.equals("")) nameD = " connectdname=\"" + nameD +"\"";
					out.println("		<levelxing ident=\"X" + jmriNumber + "\"" + blockString + nameA + nameB + nameC + nameD + " xcen=\"" +
						xcen + "\" ycen=\"" + ycen + "\" xa=\"" + xa + "\" ya=\"" + ya + "\" xb=\"" +
						xb + "\" yb=\"" + yb + "\" class=\"jmri.jmrit.display.configurexml.LevelXingXml\" />");
					break;
				case STRAIGHT:
				case BUMPER:
				case CURVE:
					String hidden = "hidden=\"no\" dashed=\"no\"";
					if(visible == 0 && !hiddenIgnore) {
						if(hiddenDash) {
							hidden = "hidden=\"no\" dashed=\"yes\"";
						} else {
							hidden = "hidden=\"yes\" dashed=\"no\"";
						}
					}
					// Simply connect start and end anchors
					anchor1 = (XtrkCadAnchor)anchors.get(firstAnchor);
					anchor2 = (XtrkCadAnchor)anchors.get(firstAnchor+1);
					out.println("		<tracksegment ident=\"T" + jmriNumber + "\"" + blockString + " connect1name=\"" + anchor1.getIdent() + 
						"\" type1=\"" + anchor1.getTurnoutBranch() + "\" connect2name=\"" + anchor2.getIdent() + 
						"\" type2=\"" + anchor2.getTurnoutBranch() + 
						"\" mainline=\"no\" " + hidden + " class=\"jmri.jmrit.display.configurexml.TrackSegmentXml\" />");
					break;
				default:
					out.println("		<!-- UNKNOWN item: ignored -->");
			}
		}
		
		public String getName(){
		// Return the JMRI ID of the track as String 
			if(jmriNumber == 0) return "";
			switch(trackType) {
				case TURNOUT:
					return "TO" + jmriNumber;
				case CROSSING:
					return "X" + jmriNumber;
				case TURNTABLE:
					return "TUR" + jmriNumber;
				default:
					return "T" + jmriNumber;
			}
		}
		
		public XtrkCadElement getNextTrack(int ind){
		// Return the track connected to anchor point "ind" 
			if(ind < firstAnchor || ind >= lastAnchor) return null;
			return ((XtrkCadAnchor)anchors.get(ind)).getConnectedTrack(1);
		}
		
		public XtrkCadAnchor crossingThru(int source, int newBlock) {
		// Find the exit point of a crossing, when entering from the "source" track
		// Optionally sets also the block number
			for(int ind = firstAnchor; ind < lastAnchor; ind++) {
				if(((XtrkCadAnchor)anchors.get(ind)).ref[1] == source) {
					if(ind < firstAnchor + 2) {
						if(blockX != 0) return null;
						if(newBlock != 0) blockX = newBlock;
						ind = firstAnchor * 2 + 1 - ind;
					} else {
						if(block != 0) return null;
						if(newBlock != 0) block = newBlock;
						ind = firstAnchor * 2 + 5 - ind;
					}
					return (XtrkCadAnchor)anchors.get(ind);
				}
			}
			return null;
		}

		
		public void setBlock(int caller, int newBlock) {
		// Sets the block number and extends it to all neighbor track elements (unless separated by block-gaps)
			if(trackType == TURNTABLE) return;
			if(trackType == CROSSING) {
				if(enableBlockXing) {
					XtrkCadAnchor anchor = crossingThru(caller, newBlock);
					if(anchor != null && ((anchor.blockGap & gapMask) == 0)) anchor.getConnectedTrack(1).setBlock(originalNumber, newBlock);
				}
				return;
			}
			if(block == 0) {
				block = newBlock;
				for(int ind = firstAnchor; ind < lastAnchor; ind++) {
					XtrkCadAnchor anchor = (XtrkCadAnchor)anchors.get(ind);
					if(anchor.ref[1] != caller && anchor.ref[1] != 0 && (anchor.blockGap & gapMask) == 0) {
						anchor.getConnectedTrack(1).setBlock(originalNumber, newBlock);
					}
				}
			}
		}
		
		public boolean checkPath(int caller, double range2, double x0, double y0) {
		// Checks if along a path there is the point of a turnout within a given range
			switch(trackType) {
				case TURNOUT:
					return ((XtrkCadAnchor)anchors.get(firstAnchor)).ref[1] == caller;
				case CROSSING:
					if(!enableBlockXing) return true;
					XtrkCadAnchor anchor1 = crossingThru(caller, 0);
					if(anchor1 != null) {
						if(Math.pow(anchor1.x - x0, 2) + Math.pow(anchor1.y -y0, 2) <= range2) {
							if((anchor1.blockGap & gapMask) != 0 || anchor1.ref[1] == 0) return true;
							return anchor1.getConnectedTrack(1).checkPath(originalNumber, range2, x0, y0);
						}
					}
					break;
				case BUMPER:
				case STRAIGHT:
				case CURVE:
				for(int ind = firstAnchor; ind < lastAnchor; ind++) {
					XtrkCadAnchor anchor = (XtrkCadAnchor)anchors.get(ind);
					if(anchor.ref[1] != caller) {
						if(Math.pow(anchor.x - x0, 2) + Math.pow(anchor.y -y0, 2) <= range2) {
							if((anchor.blockGap & gapMask) != 0 || anchor.ref[1] == 0) return true;
							return anchor.getConnectedTrack(1).checkPath(originalNumber, range2, x0, y0);
						}
					}
				}
			}
			return false;
		}

	}
	
	public class XtrkCadAnchor {
		// Internal class
		// Anchor point
		
		// Original XtrkCad ID of the two track items connected by this anchor
		int[] ref = new int[2];
		
		// In XtrkCad, the same node is normally recorded twice (once per track)
		// We will thus reduce their number, by marking duplicates
		int duplicate = -1;
		
		// Angle and coordinates
		public double a, x, y;
		
		// XtrcCAD block gap indicator
		int blockGap = 0;
		
		// End point type:
		//	1 track
		//	2 bumper
		//  3-5 turnout
		//	6-9 crossing
		int type = 1;
		
		// Anchor ID number 
		int jmriNumber;
		
		// Drawing indicator
		//	false	The anchor must be drawn
		//	true	The anchor must be skipped (duplicate or belonging to turnout or crossing)
		boolean skip = false;

		// Printing indicator (to avoid printing an anchor twice)
		//	false	The anchor has not been printed yet
		//	true	The anchor has already been printed
		boolean printed = false;
		
		// Standard constructor of the XtrkCadAnchor class
		// Populates fields reading them from the XtrkCAD file
		public XtrkCadAnchor(int callingItem, boolean otherRef) {
			// Count anchors
			nAnchors++;
			// Set main reference to the calling track
			ref[0] = callingItem;
			// Extract information from input file
			if(otherRef) {
				ref[1] = line.nextInt();	// XtrkCad keyword = "T"
			} else {
				ref[1] = 0;					// XtrkCad keyword = "E"
			}
			x = line.nextDouble() * scale;
			y = (originalHeight - line.nextDouble()) * scale;
			a = line.nextDouble();
			if(line.hasNext() && ((line.nextInt() & 256) != 0)) blockGap = 1;
		}
		
		// Alternate constructor of the XtrkCadAnchor class (used for bumpers)
		public XtrkCadAnchor(int callingItem, double xEnd, double yEnd) {
			nAnchors++;
			ref[0] = callingItem;
			ref[1] = 0;
			x = xEnd;
			y = yEnd;
			type = 2;
		}
		
		// Alternate constructor of the XtrkCadAnchor class (used for arcs rendering)
		public XtrkCadAnchor(int callingItem, int nextItem, double xEnd, double yEnd) {
			nAnchors++;
			ref[0] = callingItem;
			ref[1] = nextItem;
			x = xEnd;
			y = yEnd;
		}
		
		// Methods of the XtrkCadAnchor class
		
		public int getTurnoutBranch() {
		// Return the turnout branch corresponding to the anchor
			if(duplicate < 0 || !skip) return 1;		// Not a turnout
			// This anchor is a duplicate.  Retrieve the original
			XtrkCadAnchor duplicateAnchor = (XtrkCadAnchor)anchors.get(duplicate);
			if(duplicateAnchor.type < 3) return 1;	// Not a turnout
			return duplicateAnchor.type - 1;		// Turnout or Crossing
		}
		
		public XtrkCadElement getConnectedTrack(int ind) {
		// Return the element connected to the node
		// Attention: ind is not checked. It must be in the range 0-1
			if(ref[ind] > 0) {
				XtrkCadElement track;
				// Reference found - Retrieve the  corresponding track
				for(int ind1 = 0; ind1 < nTracks; ind1++) {
					track = (XtrkCadElement)tracks.get(ind1);
					if(track.originalNumber == ref[ind]) return track;
				}
			}
			return null;	// No reference found!	
		}
		
		public String getConnectedName(int ind) {
		// Return the name of the element connected to the node
		// Attention: ind is not checked. It must be in the range 0-1
			XtrkCadElement track;
			if((track = getConnectedTrack(ind)) != null) return track.getName();
			return "";	// No reference found!	
		}
		public String getIdent() {
		// Return the identification string of the anchor
			if(duplicate < 0 || !skip) {
				if(type == 2) return "EB" + jmriNumber;	// Bumper
				return "A" + jmriNumber;				// Normal anchor
			} else {
				// This anchor is a duplicate.  Retrieve the original
				XtrkCadAnchor duplicateAnchor = (XtrkCadAnchor)anchors.get(duplicate);
				if(duplicateAnchor.type == 1) return "A" + duplicateAnchor.jmriNumber;	// Normal anchor
				if(duplicateAnchor.type == 2) return "EB" + duplicateAnchor.jmriNumber;	// Bumper (actually a bumper can unlikely have duplicates!)
				if(duplicateAnchor.type > 6) return "X" + duplicateAnchor.jmriNumber;	// Crossing
				return "TO" + duplicateAnchor.jmriNumber;								// Turnout
			}
		}

		public void print() {
			// Output anchor to XML file
			if(!skip && !printed && type < 50) {
				// Get names of track items connected to the anchor
				String name1 = getConnectedName(0);
				if(!name1.equals("")) name1 = " connect1name=\"" + name1 +"\"";
				String name2 = getConnectedName(1);
				if(!name2.equals("")) name2 = " connect2name=\"" + name2 +"\"";
				out.println("\t\t<positionablepoint ident=\"" + getIdent() + "\" type=\"" + type + 
					"\" x=\"" + x + "\" y=\"" + y + "\"" + name1 + name2 + 
					" class=\"jmri.jmrit.display.configurexml.PositionablePointXml\" />");
				printed = true;
			}
		}
	}
	public class BlockName {
		// Internal class
		// Keeps track of names assigned to a block
		String system = "";
		String user = "";
	}
}
