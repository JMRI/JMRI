package jmri.server.json.logs;

import java.io.File;

/**
 * Constants used by the JSON logging service.
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonLogs {

    /**
     * {@value #LOGS}
     */
    public final static String LOGS = "logs";
    /**
     * The JSON formatted log file.
     */
    public final static File LOG_FILE = new File(System.getProperty("jmri.log.path"), "session.json");

}
