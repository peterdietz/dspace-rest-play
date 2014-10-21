package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Collection;
import models.RestResponse;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

/**
 * Created by peterdietz on 10/6/14.
 */
public class Collections extends Controller {

    //TODO index

    public static Result show(Long id) throws IOException {
        User user = new User();
        user = user.getUserFromSession(session());
        RestResponse response = Collection.findByID(id, user.token());
        if(response.httpResponse.getStatusLine().getStatusCode() == 200) {
            return ok(views.html.collection.detail.render(user, (Collection)response.modelObject, "Single Collection", response.jsonString, response.endpoint));
        } else {
            return internalServerError();
        }
    }
}
