package jmri.jmrit.signalsystemeditor;

/**
 * An author
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class Author {

    private String _firstName;
    private String _surName;
    private String _email;

    public Author(String firstName, String lastName, String email) {
        this._firstName = firstName;
        this._surName = lastName;
        this._email = email;
    }

    public void setFirstName(String firstName) {
        this._firstName = firstName;
    }

    public String getFirstName() {
        return this._firstName;
    }

    public void setSurName(String lastName) {
        this._surName = lastName;
    }

    public String getSurName() {
        return this._surName;
    }

    public void setEmail(String email) {
        this._email = email;
    }

    public String getEmail() {
        return this._email;
    }

}
