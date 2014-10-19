package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Application;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import play.Logger;
import play.api.libs.json.JsValue;
import play.libs.Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 7/20/12
 * Time: 10:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class Community {
    public Long id;
    public String name;
    
    public List<Community> subCommunities = new ArrayList<Community>();
    public List<Collection> collections = new ArrayList<Collection>();
    public List<Community> parentCommunities = new ArrayList<Community>();

    public Bitstream logo;

    public String copyrightText, countItems, handle, introductoryText, shortDescription, sidebarText;

    public static List<Community> all() {
        return new ArrayList<Community>();
    }

    public static void create(Community community) {
    }


    public static void delete(Long id) {
    }

    public static RestResponse findByID(Long id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        RestResponse restResponse = new RestResponse();

        try {
            conn = Application.connectToURL("communities/" + id.toString() + "?expand=all");
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode comm = Json.parse(contentString.toString());
            restResponse.endpoint = conn.getURL().toString();

            ObjectMapper mapper = new ObjectMapper();
            String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(comm);
            restResponse.jsonString = pretty;

            if (comm.size() > 0) {
                Community community = Community.parseCommunityFromJSON(comm);
                restResponse.modelObject = community;
            }
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Logger.error(e.getMessage(),e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return restResponse;
    }

    public RestResponse update(String token) throws IOException {
        //TODO insecure ssl hack
        HttpClient httpClient = new DefaultHttpClient();
        SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
                .getSchemeRegistry().getScheme("https").getSocketFactory();
        sf.setHostnameVerifier(new AllowAllHostnameVerifier());

        HttpPut request = new HttpPut(Application.baseRestUrl + "/communities/" + this.id);
        request.setHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("rest-dspace-token", token);

        //Only allow certain attributes... "name", "copyrightText", "introductoryText", "shortDescription", "sidebarText"
        Logger.info("EditCommunity json: " + Json.toJson(this).toString());
        ObjectNode jsonObjectNode = Json.newObject().put("name", this.name).put("copyrightText", this.copyrightText)
                .put("introductoryText", this.introductoryText)
                .put("shortDescription", this.shortDescription)
                .put("sidebarText", this.sidebarText);
        StringEntity stringEntity = new StringEntity(jsonObjectNode.toString());
        Logger.info("EditCommunity certain attributes: " + jsonObjectNode.toString());

        request.setEntity(stringEntity);
        HttpResponse httpResponse = httpClient.execute(request);
        RestResponse restResponse = new RestResponse();
        restResponse.httpResponse = httpResponse;
        restResponse.endpoint = request.getURI().toString();
        return restResponse;
    }

    public static Community parseCommunityFromJSON(JsonNode communityJSON) {
        //Other elements include
        // administrators, canEdit, collections, copyrightText, countItems, handle, id, introductoryText
        // name, parentCommunity, recentSubmissions, shortDescription, sidebarText, subcommunities
        // type, entityReference, entityURL, entityId

        Community community = new Community();
        community.id = communityJSON.get("id").asLong();
        community.name = communityJSON.get("name").asText();
        community.handle = communityJSON.get("handle").asText();

        //TODO if has(), then get()...
        List<String> copyrightText = communityJSON.findValuesAsText("copyrightText");
        List<String> countItems = communityJSON.findValuesAsText("countItems");
        List<String> introductoryText = communityJSON.findValuesAsText("introductoryText");
        List<String> shortDescription = communityJSON.findValuesAsText("shortDescription");
        List<String> sidebarText = communityJSON.findValuesAsText("sidebarText");

        if(communityJSON.has("logo")) {
            JsonNode logoNode = communityJSON.get("logo");
            if(!logoNode.isNull()) {
                community.logo = Bitstream.parseBitstreamFromJSON(logoNode);
            }
        }


        JsonNode parentCommunityNode = communityJSON.get("parentCommunity");
        if(parentCommunityNode != null && parentCommunityNode.has("id")) {
            Community parentCommunity = Community.parseCommunityFromJSON(parentCommunityNode);
            community.parentCommunities.add(parentCommunity);
        }

        JsonNode subCommNodes = communityJSON.get("subCommunities");
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
                community.collections.add(Collection.parseCollectionFromJSON(subColl));
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
        }

        return community;
    }
}
