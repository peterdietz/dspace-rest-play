package controllers;

import models.Collection;
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
      StringBuilder contentString = new StringBuilder();
      HttpURLConnection conn = null;
      BufferedReader reader = null;

      try {
          conn = connectToURL("communities.json?topLevelOnly=true");

          reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

          String output;
          while ((output = reader.readLine()) != null) {
              contentString.append(output);

          }

          JsonNode jn = Json.parse(contentString.toString());

          List<Community> communities = new ArrayList<Community>();

          List<JsonNode> rootNode = jn.findValues("communities_collection");

          if(rootNode.size()>0) {
              // Have the root
              JsonNode communityNodes = rootNode.get(0);

              for(JsonNode comm : communityNodes) {
                  Community community = parseCommunityFromJSON(comm);
                  communities.add(community);
              }
          }

          conn.disconnect();

          return ok(views.html.index.render(communities, "Top Level Communities", contentString.toString()));

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

  public static Result showCommunity(Long id) {
      StringBuilder contentString = new StringBuilder();
      HttpURLConnection conn = null;
      BufferedReader reader = null;

      try {
          conn = connectToURL("communities/" + id.toString() + ".json");

          reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

          String output;
          while ((output = reader.readLine()) != null) {
              contentString.append(output);
          }

          JsonNode comm = Json.parse(contentString.toString());
          Community community = new Community();

          if (comm.size() > 0) {
              community = parseCommunityFromJSON(comm);
          }

          return ok(views.html.detail.render(community, "Single Community", contentString.toString()));
          
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

    public static Result showCollection(Long id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = connectToURL("collections/" + id.toString() + ".json");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode collNode = Json.parse(contentString.toString());
            Collection collection = new Collection();

            if (collNode.size() > 0) {
                collection = parseCollectionFromJSON(collNode);
            }

            return ok(views.html.collection.detail.render(collection, "Single Collection", contentString.toString()));

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

  private static Community parseCommunityFromJSON(JsonNode communityJSON) {
    //Other elements include
    // administrators, canEdit, collections, copyrightText, countItems, handle, id, introductoryText
    // name, parentCommunity, recentSubmissions, shortDescription, sidebarText, subcommunities
    // type, entityReference, entityURL, entityId

      Community community = new Community();
      community.id = Long.decode(communityJSON.get("id").toString());

    List<String> names = communityJSON.findValuesAsText("name");

    List<String> copyrightText = communityJSON.findValuesAsText("copyrightText");
    List<String> countItems = communityJSON.findValuesAsText("countItems");
    List<String> handle = communityJSON.findValuesAsText("handle");
    List<String> introductoryText = communityJSON.findValuesAsText("introductoryText");
    List<String> shortDescription = communityJSON.findValuesAsText("shortDescription");
    List<String> sidebarText = communityJSON.findValuesAsText("sidebarText");

      JsonNode subCommNodes = communityJSON.get("subCommunities");
      for(JsonNode subComm : subCommNodes) {
          community.subCommunities.add(subComm.get("id").asInt());
      }

      JsonNode subCollNodes = communityJSON.get("collections");
      for(JsonNode subColl : subCollNodes) {
          community.collections.add(subColl.get("id").asInt());
      }

    community.name = names.get(0);
    community.copyrightText = copyrightText.get(0);
    community.countItems = countItems.get(0);
    community.handle = handle.get(0);
    community.introductoryText = introductoryText.get(0);
    community.shortDescription = shortDescription.get(0);
    community.sidebarText = sidebarText.get(0);

    return community;
  }

    private static Collection parseCollectionFromJSON(JsonNode collectionJSON) {
        /*communities list
        copyrightText
                countItems
        handle
                id
        introText
        items list ids
                license
        logo
                name
        provenance
                shortDescription
        sidebarText
                type
        entityReference, entityURL, entityId
          */

        Collection collection = new Collection();

        collection.id = collectionJSON.get("id").asLong();
        collection.name = collectionJSON.get("name").asText();

        collection.copyrightText = collectionJSON.get("copyrightText").asText();
        collection.countItems = collectionJSON.get("countItems").asInt();
        collection.handle = collectionJSON.get("handle").asText();

        //@TODO Is it comm.introductoryText and coll.introText ?
        collection.introText = collectionJSON.get("introText").asText();
        collection.shortDescription = collectionJSON.get("shortDescription").asText();
        collection.sidebarText = collectionJSON.get("sidebarText").asText();

        //Not sure what communities means for an item. Its parents?
        JsonNode commNodes = collectionJSON.get("communities");
        for(JsonNode comm : commNodes) {
            collection.communities.add(comm.get("id").asInt());
        }

        JsonNode itemNodes = collectionJSON.get("items");
        for(JsonNode item : itemNodes) {
            collection.items.add(item.get("id").asInt());
        }


        return collection;
    }
    
    private static HttpURLConnection connectToURL(String endpoint) throws IOException {
        HttpURLConnection conn;
        URL url = new URL(baseRestUrl + endpoint);

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new MalformedURLException("Non-200 response: " + conn.getResponseMessage());
        }

        return conn;
    }
}