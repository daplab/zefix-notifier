package models;

import play.db.ebean.Model;

import javax.persistence.Entity;

/**
 * Created by Aliya Ibragimova on 15/10/15.
 */
@Entity
public class ZefixNotifierInput extends Model {

    String input;
    String email;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
