package jmri.jmrit.symbolicprog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import CV values from a generic CSV format CV list file such as those written
 * by the CsvExportAction class.
 *
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Alex Shepherd Copyright (C) 2003
 * @author Dave Heap Copyright (C) 2014
 */
public class CsvImporter {

    private final static Logger log = LoggerFactory.getLogger(CsvImporter.class);

    public CsvImporter(File file, CvTableModel cvModel) throws IOException {
        FileReader fileReader=null;
        BufferedReader bufferedReader=null;

        try {
            CvValue cvObject;
            String line = null;
            String name = null;
            int value = 0;

            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                String[] lineStrings = line.split(" *, *");
                if (lineStrings.length < 2) {
                    bufferedReader.close();
                    throw new IOException();
                } else if (lineStrings[0].equals("CV")) {
                    log.debug("Header OK");
                } else {
                    name = lineStrings[0].trim();
                    value = Integer.parseInt(lineStrings[1].trim());
                    cvObject = cvModel.allCvMap().get(name);
                    if (cvObject == null) {
                        log.warn("CV " + name + " was in import file, but not defined by the decoder definition");
                        cvModel.addCV(name, false, false, false);
                        cvObject = cvModel.allCvMap().get(name);
                    }
                    cvObject.setValue(value);
                }
            }
        } catch (IOException e) {
            log.error("Error reading file: " + e);
        } finally {
            if(bufferedReader!=null) {
               bufferedReader.close();
            }
            if(fileReader!=null) {
               fileReader.close();
            }
        }
    }

}
