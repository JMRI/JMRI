// CsvImporter.java

package jmri.jmrit.symbolicprog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import CV values from a generic CSV format CV list file
 * such as those written by the CsvExportAction class.
 *
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Alex Shepherd   Copyright (C) 2003
 * @author			Dave Heap   Copyright (C) 2014
 * @version			$Revision: 24747 $
 */
public class CsvImporter {
  static Logger log = LoggerFactory.getLogger(CsvImporter.class.getName());

  public CsvImporter( File file, CvTableModel cvModel ) throws IOException {
    try {
        CvValue cvObject;
        String line = null;
        String name = null;
        int value = 0;

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        
        while ((line = bufferedReader.readLine()) != null) {
            String[] lineStrings = line.split(" *, *");
            if ( lineStrings.length < 2) {
                throw new IOException();
            } else if ( lineStrings[0].equals("CV") ) {
                log.debug("Header OK");
            } else {
                name = lineStrings[0];
                value = Integer.parseInt(lineStrings[1]);
                cvObject = cvModel.allCvMap().get(name);
                if (cvObject == null) {
                    log.warn("CV "+name+" was in import file, but not defined by the decoder definition");
                    cvModel.addCV(name, false, false, false);
                    cvObject = cvModel.allCvMap().get(name);
                }
                cvObject.setValue(value);
            }
        }
        bufferedReader.close();
        fileReader.close();
    } catch (IOException e) {
        log.error("Error reading file: "+e);
    }
  }

}
