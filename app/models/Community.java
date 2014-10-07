package models;

import com.fasterxml.jackson.databind.JsonNode;

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


        JsonNode commNodes = communityJSON.get("parentCommunityList");
        if(commNodes != null) {
            for(JsonNode comm : commNodes) {
                Community parentCommunity = parseCommunityFromJSON(comm);
                community.parentCommunities.add(parentCommunity);
            }
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
