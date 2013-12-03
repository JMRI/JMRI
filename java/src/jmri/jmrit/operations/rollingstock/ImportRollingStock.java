// ImportRollingStock.java

package jmri.jmrit.operations.rollingstock;

import java.io.File;

/**
 * Provides common routes for importing cars and locomotives
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 24463 $
 */
public class ImportRollingStock extends Thread {

	protected static final String NEW_LINE = "\n"; // NOI18N

	protected String[] parseCommaLine(String line, int arraySize) {
		String[] outLine = new String[arraySize];
		if (line.contains("\"")) { // NOI18N
			// log.debug("line number "+lineNum+" has escape char \"");
			String[] parseLine = line.split(",");
			int j = 0;
			for (int i = 0; i < parseLine.length; i++) {
				if (parseLine[i].contains("\"")) { // NOI18N
					StringBuilder sb = new StringBuilder(parseLine[i++]);
					sb.deleteCharAt(0); // delete the "
					outLine[j] = sb.toString();
					while (i < parseLine.length) {
						if (parseLine[i].contains("\"")) { // NOI18N
							sb = new StringBuilder(parseLine[i]);
							sb.deleteCharAt(sb.length() - 1); // delete the "
							outLine[j] = outLine[j] + "," + sb.toString();
							// log.debug("generated string: "+outLine[j]);
							j++;
							break; // done!
						} else {
							outLine[j] = outLine[j] + "," + parseLine[i++];
						}
					}

				} else {
					// log.debug("outLine: "+parseLine[i]);
					outLine[j++] = parseLine[i];
				}
			}
		} else {
			outLine = line.split(",");
		}
		return outLine;
	}

	public static class ImportFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String name = f.getName();
			if (name.matches(".*\\.txt")) // NOI18N
				return true;
			if (name.matches(".*\\.csv")) // NOI18N
				return true;
			else
				return false;
		}

		public String getDescription() {
			return Bundle.getMessage("Text&CSV");
		}
	}
}
