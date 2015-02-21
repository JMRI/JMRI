/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.profile;

import jmri.JmriException;

/**
 *
 * @author rhwood
 */
public class ProfileException extends JmriException {

    /**
     *
     */
    private static final long serialVersionUID = -7993146814601813130L;

    ProfileException(String string) {
        super(string);
    }

    ProfileException() {
        super();
    }

}
