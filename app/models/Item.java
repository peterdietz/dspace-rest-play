package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 8/10/12
 * Time: 11:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class Item {
    /*
    bitstreams (complex)
    bundles (complex)
    collections - list
    communities -
    lastModified datestamp
    owningCollection - detail
     */
    
    public Long id;
    public String pid;
    public String name;
    public List<MetadataField> metadata = new ArrayList<MetadataField>();

    public List<Bitstream> bitstreams  = new ArrayList<Bitstream>();

    public String handle;
    public boolean isArchived, isWithdrawn;
    public String submitterFullName;
    
    public List<Collection> collections = new ArrayList<Collection>();
    public List<Community>  communities = new ArrayList<Community>();

}
