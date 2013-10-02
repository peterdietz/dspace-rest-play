package models;

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

    public String copyrightText, countItems, handle, introductoryText, shortDescription, sidebarText;

    public static List<Community> all() {
        return new ArrayList<Community>();
    }

    public static void create(Community community) {
    }


    public static void delete(Long id) {
    }
}
