package controllers;

import models.*;
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
          conn = connectToURL("communities/" + id.toString() + "?expand=all");

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
            conn = connectToURL("collections/" + id.toString() + "?expand=all");

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
            conn = connectToURL("items/" + id.toString() + "?expand=all");

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


      JsonNode subCommNodes = communityJSON.get("subcommunities");
      if(subCommNodes != null) {

          for(JsonNode subComm : subCommNodes) {
              if(! subComm.get("communityID").isNull()) {
                community.subCommunities.add(parseCommunityFromJSON(subComm));
              }
          }
      }

      JsonNode subCollNodes = communityJSON.get("collections");
      if(subCollNodes != null) {
          for(JsonNode subColl : subCollNodes) {
              community.collections.add(parseCollectionFromJSON(subColl));
          }
      }


      community.name = names.get(0);
      community.handle = handle.get(0);

      if(! copyrightText.isEmpty()) {
          community.copyrightText = copyrightText.get(0);
      }

      if(! countItems.isEmpty()) {
          community.countItems = countItems.get(0);
      }

      if(! introductoryText.isEmpty()) {
          community.introductoryText = introductoryText.get(0);
      }

      if(! shortDescription.isEmpty()) {
          community.shortDescription = shortDescription.get(0);
      }

      if(! sidebarText.isEmpty()) {
          community.sidebarText = sidebarText.get(0);
      }

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

        collection.id = collectionJSON.get("collectionID").asLong();
        collection.name = collectionJSON.get("name").asText();
        collection.handle = collectionJSON.get("handle").asText();

        List<String> copyrightText = collectionJSON.findValuesAsText("copyrightText");
        if(! copyrightText.isEmpty()) {
            collection.copyrightText = collectionJSON.get("copyrightText").asText();
        }

        if(collectionJSON.has("numberItems")) {
            collection.countItems = collectionJSON.get("numberItems").asInt();
        }

        //@TODO Is it comm.introductoryText and coll.introText ?
        List<String> introductoryText = collectionJSON.findValuesAsText("introductoryText");
        if(! introductoryText.isEmpty()) {
            collection.introText = introductoryText.get(0);
        }

        List<String> shortDescription = collectionJSON.findValuesAsText("shortDescription");
        if(! shortDescription.isEmpty()) {
            collection.shortDescription = shortDescription.get(0);
        }

        List<String> sidebarText = collectionJSON.findValuesAsText("sidebarText");
        if(! sidebarText.isEmpty()) {
            collection.sidebarText = sidebarText.get(0);
        }


        //Not sure what communities means for an item. Its parents?
        JsonNode commNodes = collectionJSON.get("communities");
        if(commNodes != null) {
            for(JsonNode comm : commNodes) {
                collection.communities.add(comm.get("communityID").asInt());
            }
        }

        JsonNode itemNodes = collectionJSON.get("items");
        if(itemNodes != null) {
            for(JsonNode itemNode : itemNodes) {
                Item item = parseItemFromJSON(itemNode);
                collection.items.add(item);
            }
        }


        return collection;
    }

    private static Item parseItemFromJSON(JsonNode itemNode) {
        Item item = new Item();

        item.id = itemNode.get("itemID").asLong();
        item.name = itemNode.get("name").asText();

        item.handle = itemNode.get("handle").asText();

        List<String> isArchived = itemNode.findValuesAsText("isArchived");
        if(! isArchived.isEmpty()) {
            item.isArchived = Boolean.getBoolean(isArchived.get(0));
        }

        //item.isWithdrawn = itemNode.get("isWithdrawn").asBoolean();
        
        //item.submitterFullName = itemNode.get("submitter").get("fullName").asText();

        if(itemNode.has("metadata")) {
            JsonNode metadataNode = itemNode.get("metadata");
            if(metadataNode.has("fields")) {
                JsonNode fields = metadataNode.get("fields");
                //TODO parseMetadataFieldFromJSON
                item.metadata = new ArrayList<MetadataField>();

                for(JsonNode field : fields) {
                    String key = field.get("key").asText();
                    String value = field.get("value").asText();
                    item.metadata.add(new MetadataField(key, value));
                }
            }
        }

        if(itemNode.has("bitstreams")) {
            item.bitstreams = new ArrayList<Bitstream>();
            JsonNode bitstreamsNode = itemNode.get("bitstreams");
            for(JsonNode bitstreamNode : bitstreamsNode) {
                Bitstream bitstream = parseBitstreamFromJSON(bitstreamNode);
                item.bitstreams.add(bitstream);
            }
        }

        if(itemNode.has("collections")) {
            JsonNode collectionNodes = itemNode.get("collections");
            for(JsonNode collectionNode : collectionNodes) {
                Collection collection = parseCollectionFromJSON(collectionNode);
                item.collections.add(collection);
            }
        }

        return item;
    }

    private static Bitstream parseBitstreamFromJSON(JsonNode bitstreamNode) {
        Bitstream bitstream = new Bitstream();
        bitstream.name = bitstreamNode.get("name").asText();
        bitstream.description = bitstreamNode.get("description").asText();
        bitstream.format = bitstreamNode.get("format").asText();
        bitstream.bundleName = bitstreamNode.get("bundleName").asText();
        bitstream.sizeBytes = bitstreamNode.get("sizeBytes").asText();

        return bitstream;
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