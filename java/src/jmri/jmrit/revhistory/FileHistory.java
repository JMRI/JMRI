package jmri.jmrit.revhistory;

import java.util.ArrayList;
import jmri.InstanceManagerAutoDefault;

/**
 * Memo class to remember a file revision history.
 * <p>
 * These can be nested: A revision can come with a history.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 */
public class FileHistory implements InstanceManagerAutoDefault {

    ArrayList<OperationMemo> list = new ArrayList<>();

    /**
     * Add a revision from complete information created elsewhere.
     *
     * @param type     operation type
     * @param date     operation date
     * @param filename file operated on
     * @param history  source history instance
     */
    public void addOperation(String type, String date, String filename, FileHistory history) {
        OperationMemo r = new OperationMemo();
        r.type = type;
        r.date = date;
        r.filename = filename;
        r.history = history;

        list.add(r);
    }

    public void addOperation(OperationMemo r) {
        list.add(r);
    }

    public void addOperation(String type, String filename, FileHistory history) {
        OperationMemo r = new OperationMemo();
        r.type = type;
        r.date = (new java.util.Date()).toString();
        r.filename = filename;
        r.history = history;

        list.add(r);
    }

    /**
     * @param keep Number of levels to keep
     */
    public void purge(int keep) {
        for (int i = 0; i < list.size(); i++) {
            OperationMemo r = list.get(i);
            if (keep <= 1) {
                r.history = null;
            }
            if (r.history != null) {
                r.history.purge(keep - 1);
            }
        }
    }

    public String toString(String prefix) {
        StringBuilder retval = new StringBuilder();
        list.stream().forEachOrdered((r) -> {
            retval.append(prefix).append(r.date).append(": ").append(r.type).append(" ").append(r.filename).append("\n");
            if (r.history != null) {
                retval.append(r.history.toString(prefix + "    "));
            }
        });
        return retval.toString();
    }

    @Override
    public String toString() {
        return toString("");
    }

    public ArrayList<OperationMemo> getList() {
        return list;
    }

    /**
     * Memo class for each revision itself.
     */
    public class OperationMemo {

        public String type;  // load, store
        public String date;
        public String filename;
        public FileHistory history;  // only with load
    }

}
