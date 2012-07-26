package controllers;

import models.Community;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import play.*;
import play.api.templates.Html;
import play.mvc.*;
import play.libs.Json;

import views.html.*;
import views.html.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import views.html.main;

import javax.swing.text.html.HTML;


public class Application extends Controller {
    private static String baseRestUrl = "http://localhost:8280/rest/";

  
  public static Result index() {
      StringBuilder sb = new StringBuilder();

      try {
          URL url = new URL(baseRestUrl + "communities.json?topLevelOnly=true");

          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", "application/json");

          if (conn.getResponseCode() != 200) {
              throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
          }

          BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

          String output;
          while ((output = br.readLine()) != null) {
              sb.append(output);

          }

          JsonNode jn = Json.parse(sb.toString());

          List<Community> communities = new ArrayList<Community>();

          List<JsonNode> rootNode = jn.findValues("communities_collection");

          if(rootNode.size()>0) {
              // Have the root
              JsonNode communityNodes = rootNode.get(0);
              
              // Have all nodes, each node is a collection.

              for(JsonNode comm : communityNodes) {
                  List<String> ids = comm.findValuesAsText("id");
                  List<String> names = comm.findValuesAsText("name");

                  int firstID = Integer.parseInt(ids.get(0));
                  String firstName = names.get(0);

                  Community community = new Community();
                  community.id = (long) firstID;
                  community.name = firstName;
                  communities.add(community);
              }
          }

          conn.disconnect();

          return ok(views.html.index.render(communities, "Top Level Communities", sb.toString()));

      } catch (MalformedURLException e) {

          e.printStackTrace();

      } catch (IOException e) {

          e.printStackTrace();

      }

      return ok("Error");
  }
}