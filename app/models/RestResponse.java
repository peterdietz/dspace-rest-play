package models;

import org.apache.http.HttpResponse;

/**
 * Created by peterdietz on 10/10/14.
 */
public class RestResponse {
    public Object modelObject;
    public String endpoint;
    public String jsonString;
    public HttpResponse httpResponse;
}
