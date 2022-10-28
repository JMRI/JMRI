/**
 * A command line tool to maintain properties files for translation.
 * Takes the names of two properties files.  The first is the master, default
 * English version, and the second is a partially translated file.
 *
 * When run, the program will re-write the 2nd file with
 *   All the key/value pairs in the order they appear in the first file
 *   With k/v pairs missing in the 2nd file filled out with the k/v from the first file.
 *
 * Effectively, this puts English into the 2nd file for later translation, while
 * maintaining the files in the same order for readability.
 *
 * Note that comments in the 2nd file are not preserved.  If desired, those have
 * to be added back in by hand. Also note that multi-line values will be
 * converted to a single (perhaps very long) values.
 *
 * Usage:
 * java java/test/jmri/util/i18n/PropertiesMergeTool.java java/src/jmri/NamedBeanBundle.properties java/src/jmri/NamedBeanBundle_es.properties
 */
package jmri.util.i18n;

import java.util.*;
import java.io.*;

class PropertiesMergeTool {
    static public void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Needs two file names");
            return;
        }

        try {

            var stream1 = new FileInputStream(args[0]);
            var stream2 = new FileInputStream(args[1]);
            ResourceBundle bundle1 = new PropertyResourceBundle(stream1);
            ResourceBundle bundle2 = new PropertyResourceBundle(stream2);

            // now that bundle is read, open same file for output
            outFile = new File(args[1]);
            outWriter = new FileWriter(outFile);

            // put pathname at top of output file
            outWriter.write("# "+args[1]+"\n");

            // Apparently there's no way to address these in order, so we separately
            // read the file to do that.
            File inFile  = new File(args[0]);
            try (var fileReader = new BufferedReader(new FileReader(inFile))) {
                // process every line
                for(String line; (line = fileReader.readLine()) != null; ) {
                    // decode line types
                    int index;
                    if (line.startsWith("#")) {
                        writeOutLine(line);
                    } else if ((index = line.indexOf("=")) >= 0) {
                        String part1 = line.substring(0, index);
                        String key = part1.trim();
                        var lookUpKey = key.replace("\\", "");
                        String part2 = line.substring(index+1);
                        StringBuilder pad = new StringBuilder();
                        // pad with same number of leading spaces
                        while (part2.startsWith(" ") || part2.startsWith("\t")) {
                            pad.append(part2.substring(0,1));
                            part2 = part2.substring(1);
                        }

                        // check if translation in 2nd file
                        if (bundle2.containsKey(key)) {
                            // write translated value
                            writeOutLine(part1+"="+pad.toString()+bundle2.getString(lookUpKey));
                        } else {
                            // write untranslated value
                            writeOutLine(part1+"="+pad.toString()+bundle1.getString(lookUpKey));
                        }
                    } else if (isBlankLine(line)) {
                        // blank line
                        writeOutLine("");
                    } else {
                        // don't understand the line, so skip it
                        // usually part of multiline output
                     }
                }
            }

            // done, flush and close output
            outWriter.close();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Exception: "+e);
            e.printStackTrace();
        } finally {
            try {
                if (outWriter != null) outWriter.close();
            } catch (IOException ex) {
                System.err.println("exception closing output");
            }
        }
    }

    static public boolean isBlankLine(String line) {
        while (line.startsWith(" ")) {
            line = line.substring(1);
        }
        return line.isEmpty();
    }

    static File outFile = null;
    static Writer outWriter = null;
    static public void writeOutLine(String line) throws IOException {

        // double \ characters to have them go through explicitly
        var literalLine = line.replaceAll("\\n", "\\\\n");
        literalLine = literalLine.replaceAll("\\r", "\\\\r");
        outWriter.write(literalLine+"\n");
    }
}
