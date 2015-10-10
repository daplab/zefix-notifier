package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by Aliya Ibragimova on 10/10/15.
 */
public class Application extends Controller{

    public static Result index() {
        return ok(views.html.index.render("Welcome to Zefix"));
    }

    public static Result getNotified() {
        return ok(views.html.notified.render("notified"));
    }
}
