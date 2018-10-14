package jmri.jmrit.operations.trains;

/**
 * Build failed exception.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013,
 * 2014
 */
class BuildFailedException extends Exception {

    public final static String NORMAL = "normal"; // NOI18N
    public final static String STAGING = "staging"; // NOI18N
    private String type = NORMAL;

    public BuildFailedException(String s, String type) {
        super(s);
        this.type = type;
    }

    public BuildFailedException(String s) {
        super(s);
    }

    public BuildFailedException(Exception ex) {
        super(ex);
    }

    public String getExceptionType() {
        return type;
    }

}
