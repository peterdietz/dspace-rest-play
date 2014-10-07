package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Item;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

/**
 * Created by peterdietz on 10/6/14.
 */
public class Items extends Controller {


    public static Result show(Long id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = Application.connectToURL("items/" + id.toString() + "?expand=all");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));



            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode node = Json.parse(contentString.toString());
            Item item = new Item();

            if (node.size() > 0) {
                item = Item.parseItemFromJSON(node);
            }

            String endpoint = conn.getURL().toString();
            return ok(views.html.item.detail.render(item, "Single Item", contentString.toString(), endpoint));

        } catch (MalformedURLException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return internalServerError(e.getMessage());
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
