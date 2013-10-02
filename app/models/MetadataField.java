package models;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 8/11/12
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataField {
    public String key, value;
    
    public MetadataField(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
        //return schema + "." + element + ((qualifier != null) ? "." + qualifier : "");
    }
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        return getKey() + " : " + getValue();
    }
}
