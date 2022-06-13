package jmri.jmrit.symbolicprog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

    public CsvImporter(File file, CvTableModel cvModel)
            throws IOException, NumberFormatException {

        try (FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] lineStrings = line.split(" *, *");
                if (lineStrings.length < 2) {
                    bufferedReader.close();
                    throw new IOException();
                } else if (lineStrings[0].contains("CV")) {
                    log.debug("Header OK");
                } else {
                    String name = lineStrings[0].trim();
                    int value = Integer.parseInt(lineStrings[1].trim());
                    CvValue cvObject = cvModel.allCvMap().get(name);
                    if (cvObject == null) {
                        log.warn("CV {} was in import file, but not defined by the decoder definition", name);
                        cvModel.addCV(name, false, false, false);
                        cvObject = cvModel.allCvMap().get(name);
                    }
                    cvObject.setValue(value);
                }
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CsvImporter.class);
}
