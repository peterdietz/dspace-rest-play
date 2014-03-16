package controllers;

import models.*;
import org.codehaus.jackson.JsonNode;
import play.Logger;
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
  //public static String baseRestUrl = "http://ec2-75-101-213-28.compute-1.amazonaws.com:8080/rest";
  //public static String baseRestUrl = "http://localhost:8080/rest";
  //public static String baseRestUrl = "http://kb.osu.edu/rest";
  public static String baseRestUrl = "http://mds-demo.modrepo.org:8080/webapi";

  
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

  public static Result showCommunity(String prefix, String id) {
      StringBuilder contentString = new StringBuilder();
      HttpURLConnection conn = null;
      BufferedReader reader = null;

      try {
          conn = connectToURL("content/" + prefix.toString() + "/" + id.toString());
          String endpoint = conn.getURL().toString();

          JsonNode comm = Json.parse(connectionToString(conn));
          Community community = new Community();

          if (comm.size() > 0) {
              community = parseCommunityFromJSON(comm);

              //Look for more data, more api calls
              conn = connectToURL("content/" + prefix.toString() + "/" + id.toString() + "/collections");
              JsonNode collections = Json.parse(connectionToString(conn));

              for(JsonNode collectionNode : collections) {
                  Collection collection = parseCollectionFromJSON(collectionNode);
                  community.collections.add(collection);
              }
          }



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

    public static String connectionToString(HttpURLConnection connection) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder contentString = new StringBuilder();
        String output;
        while ((output = reader.readLine()) != null) {
            contentString.append(output);
        }

        return contentString.toString();
    }

    public static Result showCollection(String prefix, String id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = connectToURL("content/" + prefix.toString() + "/" + id.toString() + "?expand=all");

            JsonNode collNode = Json.parse(connectionToString(conn));
            Collection collection = new Collection();

            if (collNode.size() > 0) {
                collection = parseCollectionFromJSON(collNode);

                //Look for more data, more api calls
                conn = connectToURL("content/" + prefix.toString() + "/" + id.toString() + "/items");
                JsonNode items = Json.parse(connectionToString(conn));

                for(JsonNode itemNode : items) {
                    Item item = parseItemFromJSON(itemNode);
                    collection.items.add(item);
                }
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

    public static Result showItem(String prefix, String id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = connectToURL("content/" + prefix.toString() + "/" + id.toString());
            JsonNode node = Json.parse(connectionToString(conn));
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
      community.pid = communityJSON.get("pid").asText();
      community.name = communityJSON.get("name").asText();
      community.handle = community.pid;

      //TODO if has(), then get()...
    //List<String> copyrightText = communityJSON.findValuesAsText("copyrightText");
    //List<String> countItems = communityJSON.findValuesAsText("countItems");
    //List<String> introductoryText = communityJSON.findValuesAsText("introductoryText");
    //List<String> shortDescription = communityJSON.findValuesAsText("shortDescription");
    //List<String> sidebarText = communityJSON.findValuesAsText("sidebarText");

    //  if(communityJSON.has("logo")) {
    //      JsonNode logoNode = communityJSON.get("logo");
    //      if(!logoNode.isNull()) {
    //        community.logo = parseBitstreamFromJSON(logoNode);
    //      }
    //  }


    //  JsonNode commNodes = communityJSON.get("parentCommunityList");
    //  if(commNodes != null) {
    //      for(JsonNode comm : commNodes) {
    //          Community parentCommunity = parseCommunityFromJSON(comm);
    //          community.parentCommunities.add(parentCommunity);
    //      }
    //  }
    /*
      JsonNode subCommNodes = communityJSON.get("subcommunities");
      if(subCommNodes != null) {

          for(JsonNode subComm : subCommNodes) {
              if(subComm.has("id")) {
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
      }*/

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

        collection.pid = collectionJSON.get("pid").asText();
        collection.name = collectionJSON.get("name").asText();
        collection.handle = collection.pid;

        /*
        collection.handle = collectionJSON.get("handle").asText();

        if(collectionJSON.has("copyrightText")) {
            collection.copyrightText = collectionJSON.get("copyrightText").asText();
        }


        if(collectionJSON.has("numberItems")) {
            collection.countItems = collectionJSON.get("numberItems").asInt();
        }

        if(collectionJSON.has("logo")) {
            JsonNode logoNode = collectionJSON.get("logo");
            if(!logoNode.isNull()) {
                collection.logo = parseBitstreamFromJSON(logoNode);
            }
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

        JsonNode commNodes = collectionJSON.get("parentCommunityList");
        if(commNodes != null) {
            for(JsonNode comm : commNodes) {
                Community community = parseCommunityFromJSON(comm);
                collection.parentCommunities.add(community);
            }
        }

        JsonNode itemNodes = collectionJSON.get("items");
        if(itemNodes != null) {
            for(JsonNode itemNode : itemNodes) {
                Item item = parseItemFromJSON(itemNode);
                collection.items.add(item);
            }
        }*/


        return collection;
    }

    private static Item parseItemFromJSON(JsonNode itemNode) {
        Item item = new Item();

        item.pid = itemNode.get("pid").asText();
        item.name = itemNode.get("name").asText();

        item.handle = item.pid;

        /*
        if(itemNode.has("archived")) {
            item.isArchived = itemNode.get("archived").asBoolean();
        }

        //item.isWithdrawn = itemNode.get("isWithdrawn").asBoolean();
        
        //item.submitterFullName = itemNode.get("submitter").get("fullName").asText();

        if(itemNode.has("metadata")) {
            JsonNode metadataNode = itemNode.get("metadata");
            item.metadata = new ArrayList<MetadataField>();

            for(JsonNode field : metadataNode) {
                String key = field.get("key").asText();
                String value = field.get("value").asText();
                item.metadata.add(new MetadataField(key, value));
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

        if(itemNode.has("parentCollectionList")) {
            JsonNode collectionNodes = itemNode.get("parentCollectionList");
            for(JsonNode collectionNode : collectionNodes) {
                Collection collection = parseCollectionFromJSON(collectionNode);
                item.collections.add(collection);
            }
        }

        if(itemNode.has("parentCommunityList")) {
            JsonNode communityNodes = itemNode.get("parentCommunityList");
            for(JsonNode communityNode : communityNodes) {
                Community community = parseCommunityFromJSON(communityNode);
                item.communities.add(community);
            }
        }*/

        return item;
    }

    private static Bitstream parseBitstreamFromJSON(JsonNode bitstreamNode) {
        Bitstream bitstream = new Bitstream();
        bitstream.name = bitstreamNode.get("name").asText();
        bitstream.description = bitstreamNode.get("description").asText();
        bitstream.format = bitstreamNode.get("format").asText();
        bitstream.mimeType = bitstreamNode.get("mimeType").asText();
        bitstream.bundleName = bitstreamNode.get("bundleName").asText();
        bitstream.sizeBytes = bitstreamNode.get("sizeBytes").asLong();
        bitstream.retrieveLink = bitstreamNode.get("retrieveLink").asText();

        return bitstream;
    }


    private static HttpURLConnection connectToURL(String endpoint) throws IOException {
        HttpURLConnection conn;
        URL url = new URL(baseRestUrl + "/" + endpoint);
        Logger.info(url.toString());

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new MalformedURLException("Non-200 response: " + conn.getResponseMessage());
        }

        return conn;
    }
}