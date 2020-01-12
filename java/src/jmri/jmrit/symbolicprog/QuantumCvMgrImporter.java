package jmri.jmrit.symbolicprog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import CV values from a .qcv file written by the QSI
 * Quantum CV Manager software.
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
 * @author Dave Heap Copyright (C) 2015
 */
public class QuantumCvMgrImporter {

    private final static Logger log = LoggerFactory.getLogger(QuantumCvMgrImporter.class);
    private static final String SEARCH_STRING = "^CV([0-9.]+)=([0-9.]+)\\s*(//)?\\s*(.*)$";

    public QuantumCvMgrImporter(File file, CvTableModel cvModel) throws IOException {
        try (
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
            ){
            CvValue cvObject;
            String line = null;
            String name = null;
            int value = 0;

            while ((line = bufferedReader.readLine()) != null) {
                log.debug("Line='"+line+"'");
                Pattern pattern = Pattern.compile(SEARCH_STRING);

                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    log.debug("I found the text {} and  {} and  {}\n" +
                        "starting at index {} and ending at index {}",
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(4),
                        matcher.start(),
                        matcher.end());
                    name = matcher.group(1);
                    value = Integer.parseInt(matcher.group(2));
                    cvObject = cvModel.allCvMap().get(name);
                    if (cvObject == null) {
                        log.warn("Adding CV " + name + " description \"" + matcher.group(4) +
                                "\", which was in import file but not defined by the decoder definition");
                        cvModel.addCV(name, false, false, false);
                        cvObject = cvModel.allCvMap().get(name);
                    }
                    cvObject.setValue(value);
                }
            }
            fileReader.close();
        } catch (IOException e) {
            log.error("Error reading file: " + e);
        }
    }

}
