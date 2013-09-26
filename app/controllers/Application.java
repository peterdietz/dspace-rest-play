package controllers;

import models.Collection;
import models.Community;
import models.Item;
import models.MetadataField;
import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Application extends Controller {
  private static String baseRestUrl = "http://localhost:8080/rest/";

  
  public static Result index() {
      StringBuilder contentString = new StringBuilder();
      HttpURLConnection conn = null;
      BufferedReader reader = null;

      try {
          conn = connectToURL("communities");

          reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

          String output;
          while ((output = reader.readLine()) != null) {
              contentString.append(output);
          }

          JsonNode jsonNode = Json.parse(contentString.toString());

          List<Community> communities = new ArrayList<Community>();

          if(jsonNode.size()>0) {
              for(JsonNode comm : jsonNode) {
                  Community community = parseCommunityFromJSON(comm);
                  communities.add(community);
              }
          }

          String endpoint = conn.getURL().toString();
          conn.disconnect();

          return ok(views.html.index.render(communities, "Top Level Communities", contentString.toString(), endpoint));

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
          conn = connectToURL("communities/" + id.toString());

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

          String endpoint = conn.getURL().toString();

          return ok(views.html.detail.render(community, "Single Community", contentString.toString(), endpoint));
          
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

            String endpoint = conn.getURL().toString();
            return ok(views.html.collection.detail.render(collection, "Single Collection", contentString.toString(), endpoint));

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

    public static Result showItem(Long id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = connectToURL("items/" + id.toString() + ".json");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));



            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode node = Json.parse(contentString.toString());
            Item item = new Item();

            if (node.size() > 0) {
               item = parseItemFromJSON(node);
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

  private static Community parseCommunityFromJSON(JsonNode communityJSON) {
    //Other elements include
    // administrators, canEdit, collections, copyrightText, countItems, handle, id, introductoryText
    // name, parentCommunity, recentSubmissions, shortDescription, sidebarText, subcommunities
    // type, entityReference, entityURL, entityId

      Community community = new Community();
      community.id = Long.decode(communityJSON.get("communityID").toString());

    List<String> names = communityJSON.findValuesAsText("name");

    List<String> copyrightText = communityJSON.findValuesAsText("copyrightText");
    List<String> countItems = communityJSON.findValuesAsText("countItems");
    List<String> handle = communityJSON.findValuesAsText("handle");
    List<String> introductoryText = communityJSON.findValuesAsText("introductoryText");
    List<String> shortDescription = communityJSON.findValuesAsText("shortDescription");
    List<String> sidebarText = communityJSON.findValuesAsText("sidebarText");

      /*
      JsonNode subCommNodes = communityJSON.get("subCommunities");
      for(JsonNode subComm : subCommNodes) {
          community.subCommunities.add(subComm.get("id").asInt());
      }

      JsonNode subCollNodes = communityJSON.get("collections");
      for(JsonNode subColl : subCollNodes) {
          community.collections.add(subColl.get("id").asInt());
      } */

    community.name = names.get(0);
    //community.copyrightText = copyrightText.get(0);
    //community.countItems = countItems.get(0);
    community.handle = handle.get(0);
    //community.introductoryText = introductoryText.get(0);
    //community.shortDescription = shortDescription.get(0);
    //community.sidebarText = sidebarText.get(0);

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

    private static Item parseItemFromJSON(JsonNode itemNode) {
        Item item = new Item();

        item.id = itemNode.get("id").asLong();
        item.name = itemNode.get("name").asText();

        item.handle = itemNode.get("handle").asText();
        
        item.isArchived = itemNode.get("isArchived").asBoolean();
        item.isWithdrawn = itemNode.get("isWithdrawn").asBoolean();
        
        item.submitterFullName = itemNode.get("submitter").get("fullName").asText();

        JsonNode metadataNodes = itemNode.get("metadata");
        for(JsonNode metadata : metadataNodes) {
            String schema = metadata.get("schema").asText();
            String element = metadata.get("element").asText();
            String qualifier = metadata.get("qualifier").asText();
            String value = metadata.get("value").asText();

            MetadataField field = new MetadataField(schema, element, qualifier, value);
            
            item.metadata.add(field);
        }
        
        JsonNode collectionNodes = itemNode.get("collections");
        for(JsonNode collectionNode : collectionNodes) {
            Collection collection = parseCollectionFromJSON(collectionNode);
            item.collections.add(collection);
        }

        return item;
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