package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by peterdietz on 10/6/14.
 */
public class Communities extends Controller {

    public static Result index() {
        return ok("Hello Communities");
    }
}
