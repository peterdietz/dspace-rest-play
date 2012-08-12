package models;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 8/11/12
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataField {
    public String schema, element, qualifier, value;
    
    public MetadataField(String schema, String element, String qualifier, String value) {
        this.schema = schema;
        this.element = element;
        this.qualifier = qualifier;
        this.value = value;
    }
    
    public String getKey() {
        return schema + "." + element + ((qualifier != null) ? "." + qualifier : "");
    }
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        return getKey() + " : " + getValue();
    }
}
