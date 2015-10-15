package controllers;

import models.ZefixNotifierInput;
import play.data.DynamicForm;
import play.data.Form;
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
        DynamicForm requestData = Form.form().bindFromRequest();
        String keyword = requestData.get("keyword");
        //System.out.println(keyword);
        String email = requestData.get("email");
        //System.out.println(email);
        ZefixNotifierInput zefixNotifierInput = new ZefixNotifierInput();
        zefixNotifierInput.setInput(keyword);
        zefixNotifierInput.setEmail(email);
        zefixNotifierInput.save();
//        String callback = requestData.get("callback");
//        DataSource ds = DB.getDataSource();
//        Connection connection = DB.getConnection();
        return ok(views.html.notified.render("notified"));
    }
}
