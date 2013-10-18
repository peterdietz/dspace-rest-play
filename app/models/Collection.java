package models;

import java.util.ArrayList;
import java.util.List;

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
    public Integer countItems;

    public List<Item> items = new ArrayList<Item>();
    public List<Community> parentCommunities = new ArrayList<Community>();
    
}
