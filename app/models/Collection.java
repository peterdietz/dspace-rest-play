package models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.Application;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import play.libs.Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.internalServerError;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 8/10/12
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Collection {
    /*
    communities list
    logo
    type
    entityReference, entityURL, entityId
     */

    public Long id;
    public String name;
           
    public String handle, introText, license, provenance, shortDescription, sidebarText, copyrightText;
    public Bitstream logo;
    public Integer countItems;

    public List<Item> items = new ArrayList<Item>();
    public List<Community> parentCommunities = new ArrayList<Community>();

    public static RestResponse findByID(Long id, String token) throws IOException {
        RestResponse restResponse = new RestResponse();
        //TODO insecure ssl hack
        HttpClient httpClient = new DefaultHttpClient();
        SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
                .getSchemeRegistry().getScheme("https").getSocketFactory();
        sf.setHostnameVerifier(new AllowAllHostnameVerifier());

        HttpGet request = new HttpGet(Application.baseRestUrl + "/collections/" + id + "?expand=all");
        request.setHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("rest-dspace-token", token);
        HttpResponse httpResponse = httpClient.execute(request);

        JsonNode collNode = Json.parse(httpResponse.getEntity().getContent());

        Collection collection = new Collection();

        if (collNode.size() > 0) {
            collection = Collection.parseCollectionFromJSON(collNode);
        }

        restResponse.httpResponse = httpResponse;
        restResponse.endpoint = request.getURI().toString();

        ObjectMapper mapper = new ObjectMapper();
        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection);
        restResponse.jsonString = pretty;

        restResponse.modelObject = collection;

        return restResponse;
    }


    public static Collection parseCollectionFromJSON(JsonNode collectionJSON) {
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
                collection.logo = Bitstream.parseBitstreamFromJSON(logoNode);
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
                Community community = Community.parseCommunityFromJSON(comm);
                collection.parentCommunities.add(community);
            }
        }

        JsonNode itemNodes = collectionJSON.get("items");
        if(itemNodes != null) {
            for(JsonNode itemNode : itemNodes) {
                Item item = Item.parseItemFromJSON(itemNode);
                collection.items.add(item);
            }
        }


        return collection;
    }
}
