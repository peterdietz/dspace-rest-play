package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Community;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peterdietz on 10/6/14.
 */
public class Communities extends Controller {

    public static Result index() {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = Application.connectToURL("communities/top-communities");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode jsonNode = Json.parse(contentString.toString());

            List<Community> communities = new ArrayList<Community>();

            if(jsonNode.size()>0) {
                for(JsonNode comm : jsonNode) {
                    Community community = Community.parseCommunityFromJSON(comm);
                    communities.add(community);
                }
            }

            String endpoint = conn.getURL().toString();
            conn.disconnect();

            return ok(views.html.community.index.render(communities, "Top Level Communities", contentString.toString(), endpoint));

        } catch (MalformedURLException e) {
            return badRequest("MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            return internalServerError("IOException :" + e.getMessage());
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

    public static Result show(Long id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = Application.connectToURL("communities/" + id.toString() + "?expand=all");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode comm = Json.parse(contentString.toString());
            Community community = new Community();

            if (comm.size() > 0) {
                community = Community.parseCommunityFromJSON(comm);
            }

            String endpoint = conn.getURL().toString();

            return ok(views.html.community.detail.render(community, "Single Community", contentString.toString(), endpoint));

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
