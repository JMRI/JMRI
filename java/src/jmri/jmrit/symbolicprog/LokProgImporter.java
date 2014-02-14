// LokProgImporter.java

package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import jmri.JmriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import CV values from a LokProgrammer CV list file written by the ESU LokProgrammer software.
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
public class LokProgImporter {
  static Logger log = LoggerFactory.getLogger(LokProgImporter.class.getName());
  private static final String INDEX_PREFIX = "Index:" ;
  private static final String INDEX_1 = "CV31=" ;
  private static final String INDEX_1_TERMINATOR = "," ;
  private static final String INDEX_2 = "CV32=" ;
  private static final String INDEX_2_TERMINATOR = ")" ;
  private static final String CV_PREFIX = "CV " ;
  private static final String CV_SEPARATOR = " = " ;

  public LokProgImporter( File file, CvTableModel cvModel ) throws IOException {
    try {
        CvValue cvObject;
        String CVindex = "";
        String line = null;
        String name = null;
        int value = 0;

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        
        while ((line = bufferedReader.readLine()) != null) {
            if ( line.startsWith(INDEX_PREFIX) ) {
                CVindex = line.substring(line.indexOf(INDEX_1)+INDEX_1.length(), line.indexOf(INDEX_1_TERMINATOR)) + ".";
                CVindex = CVindex + line.substring(line.indexOf(INDEX_2)+INDEX_2.length(), line.indexOf(INDEX_2_TERMINATOR)) + ".";
            } else if ( line.startsWith(CV_PREFIX) && line.regionMatches(6, CV_SEPARATOR, 0, 3) ) {
                name = CVindex + String.valueOf( Integer.parseInt(line.substring(3, 6)) );
                value = Integer.parseInt(line.substring(9, 12));
                cvObject = cvModel.allCvMap().get(name);
                if (cvObject == null) {
                    log.warn("CV "+name+" was in loco file, but not defined by the decoder definition");
                    cvModel.addCV(name, false, false, false);
                    cvObject = cvModel.allCvMap().get(name);
                }
                cvObject.setValue(value);
            }        
        }
        fileReader.close();
    } catch (IOException e) {
        log.error("Error reading file: "+e);
    }
  }

}
