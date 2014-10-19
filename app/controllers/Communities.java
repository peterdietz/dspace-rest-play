package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Community;
import models.RestResponse;
import models.User;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static play.data.Form.form;

/**
 * Created by peterdietz on 10/6/14.
 */
public class Communities extends Controller {

    public static Result index() {
        Logger.info("INDEX");
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = Application.connectToURL("communities/top-communities");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode jsonNode = Json.parse(contentString.toString());

            List<Community> communities = new ArrayList<Community>();

            if(jsonNode.size()>0) {
                for(JsonNode comm : jsonNode) {
                    Community community = Community.parseCommunityFromJSON(comm);
                    communities.add(community);
                }
            }

            String endpoint = conn.getURL().toString();
            conn.disconnect();

            User user = new User();
            user = user.getUserFromSession(session());

            return ok(views.html.community.index.render(user, communities, "Top Level Communities", contentString.toString(), endpoint));

        } catch (MalformedURLException e) {
            Logger.error(e.getMessage(), e);
            return badRequest("MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
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

    public static Result show(Long id) {
        Logger.info("SHOW");
        RestResponse response = Community.findByID(id);
        if(response.modelObject instanceof Community) {
            Community community = (Community) response.modelObject;
            User user = new User();
            user = user.getUserFromSession(session());

            String flash = flash("success");
            return ok(views.html.community.detail.render(user, community, "Single Community", response.jsonString, response.endpoint, flash));
        } else {
            return internalServerError();
        }
    }
    public static Result editForm(Long id) {
        Logger.info("EDITFORM");
        User user = new User();
        user = user.getUserFromSession(session());
        Form<Community> communityForm = form(Community.class);

        RestResponse response = Community.findByID(id);
        if(response.modelObject instanceof Community) {
            Community community = (Community) response.modelObject;
            communityForm = communityForm.fill(community);
            return ok(views.html.community.edit.render(user, communityForm, "Edit Community", response.jsonString, response.endpoint));
        } else {
            return internalServerError();
        }
    }

    public static Result edit(Long id) {
        Logger.info("EDIT");
        try {
            Logger.info("Community Edit for id:" + id);
            User user = new User();
            user = user.getUserFromSession(session());
            //specify which fields are allowed to be set, to prevent against mass-assignment
            Form<Community> filledForm = form(Community.class).bindFromRequest("name", "copyrightText", "introductoryText", "shortDescription", "sidebarText");
            if(filledForm.hasErrors()){
                return badRequest(views.html.community.edit.render(user, filledForm, "Edit Comm", "", ""));
            }

            Community editCommunity = filledForm.get();

            //Determine if the edited community is changed from original. i.e. don't update unless necessary
            RestResponse originalCommunityResponse = Community.findByID(id);
            Community originalCommunity = null;
            if(originalCommunityResponse.modelObject instanceof Community) {
                originalCommunity = (Community) originalCommunityResponse.modelObject;
            }

            if(editCommunity.equals(originalCommunity)) {
                Logger.info("Communities are equal, nothing to do");
                flash("success", "No changes to community detected");
                return redirect(routes.Communities.show(id));
            } else {
                editCommunity.id = originalCommunity.id;

                RestResponse restResponse = editCommunity.update(user.token());
                HttpResponse httpResponse = restResponse.httpResponse;
                if(httpResponse.getStatusLine().getStatusCode() == 200) {
                    Logger.info("ok");
                    flash("success", "Community has been updated.");
                    return redirect(routes.Communities.show(id));
                } else {
                    Logger.info("EditCommunity not ok: " + httpResponse.getStatusLine().getStatusCode());
                    return badRequest();
                }
            }
        } catch (ClientProtocolException e) {
            Logger.error(e.getMessage(), e);
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        }
        return internalServerError();
    }

    public static Result createForm() {
        Logger.info("CREATEFORM");
        User user = new User();
        user = user.getUserFromSession(session());
        Form<Community> communityForm = form(Community.class);
        return ok(views.html.community.create.render(user, communityForm, "Create Community", "", ""));
    }

    public static Result create() {
        Logger.info("CREATE");
        HttpClient httpClient = new DefaultHttpClient();
        SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
                .getSchemeRegistry().getScheme("https").getSocketFactory();
        sf.setHostnameVerifier(new AllowAllHostnameVerifier());

        try {
            Logger.info("Creating comm");
            HttpPost request = new HttpPost(Application.baseRestUrl + "/communities");
            request.setHeader("Accept", "application/json");
            request.addHeader("Content-Type", "application/json");
            String token = session("userToken");
            request.addHeader("rest-dspace-token", token);


            Community community = form(Community.class).bindFromRequest().get();

            StringEntity communityEntity = new StringEntity("{\"name\":\""+ community.name +"\"}");

            request.setEntity(communityEntity);
            Logger.info("ready");
            HttpResponse response = httpClient.execute(request);

            Logger.info("response: " + response.toString());
            if(response.getStatusLine().getStatusCode() == 200) {
                Logger.info("ok");
                Community parsedCommunity = Community.parseCommunityFromJSON(Json.parse(EntityUtils.toString(response.getEntity())));
                return redirect(routes.Communities.show(parsedCommunity.id));
            } else {
                Logger.info("not ok");
                return badRequest();
            }

        } catch (ClientProtocolException e) {
            Logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.error(e.getMessage());
            e.printStackTrace();
        }
        return internalServerError();
    }
}
