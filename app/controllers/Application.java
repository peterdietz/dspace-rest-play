package controllers;

import controllers.*;
import models.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.util.EntityUtils;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class Application extends Controller {
  public static String baseRestUrl = "http://localhost:8080/rest";
  //public static String baseRestUrl = "https://localhost:8443/rest";

    static {
        //TODO delete before production
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier(){

                    public boolean verify(String hostname,
                                          javax.net.ssl.SSLSession sslSession) {
                        if (hostname.equals("localhost")) {
                            return true;
                        }
                        return false;
                    }
                });
    }


    public static Result index() {
        return redirect(controllers.routes.Communities.index());
    }

    public static Result test() {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = Application.connectToURL("test");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            if(contentString.toString().equals("REST api is running.")) {
                User user = new User();
                user = user.getUserFromSession(session());
                return ok(views.html.test.render(user, "SUCCESS: [" + contentString.toString() + "]", "Test", contentString.toString(), conn.getURL().toString()));
            } else {
                return internalServerError("HMM: [" + contentString.toString() + "]");
            }
        } catch (IOException e) {
            return internalServerError(e.getMessage());
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

    public static Result login() {
        HttpClient httpClient = new DefaultHttpClient();
        SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
                .getSchemeRegistry().getScheme("https").getSocketFactory();
        sf.setHostnameVerifier(new AllowAllHostnameVerifier());

        try {
            HttpPost request = new HttpPost(baseRestUrl + "/login");
            //{"email":"admin@dspace.org","password":"s3cret"}
            StringEntity params =new StringEntity("{\"email\":\"admin@dspace.org\",\"password\":\"s3cret\"} ");
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            String responseBody = EntityUtils.toString(response.getEntity());
            String token = responseBody;

            session("userToken", token);
            return redirect(controllers.routes.Application.status());
            //return ok(views.html.login.render(user, "token: [" + responseBody + "]", "Test", responseBody, request.getURI().toString()));

            //return ok("token:" + responseBody);


            // handle response here...
        }catch (Exception ex) {
            // handle exception here
            Logger.error(ex.getMessage());
            return internalServerError(ex.getMessage());
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public static Result logout() {
        HttpClient httpClient = new DefaultHttpClient();
        SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
                .getSchemeRegistry().getScheme("https").getSocketFactory();
        sf.setHostnameVerifier(new AllowAllHostnameVerifier());

        try {
            HttpPost request = new HttpPost(baseRestUrl + "/logout");
            request.addHeader("Content-Type", "application/json");
            String token = session("userToken");
            request.addHeader("rest-dspace-token", token);
            Logger.info("Logging out with token:[" + token + "]");
            for(Header header : request.getAllHeaders()) {
                Logger.info("["+header.getName()+"] == [" + header.getValue()+"]");
            }

            HttpResponse response = httpClient.execute(request);

            if(response.getStatusLine().getStatusCode() == 200) {

                String responseBody = EntityUtils.toString(response.getEntity());
                session().remove("userEmail");
                session().remove("userFullname");
                session().clear();
                Logger.info("Properly Logged Out");
                return redirect(routes.Application.status());
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                Logger.info("Unsuccessful logout");
                return unauthorized("Wrong token to logout with?" + response.getStatusLine().getStatusCode()+ responseBody);
            }


            // handle response here...
        }catch (Exception ex) {
            // handle exception here
            Logger.error(ex.getMessage());
            return internalServerError(ex.getMessage());
        } finally {
            Logger.info("finall");
            httpClient.getConnectionManager().shutdown();
        }
    }

    public static Result status() {
        HttpClient httpClient = new DefaultHttpClient();
        SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
                .getSchemeRegistry().getScheme("https").getSocketFactory();
        sf.setHostnameVerifier(new AllowAllHostnameVerifier());

        try {
            HttpGet request = new HttpGet(baseRestUrl + "/status");
            request.setHeader("Accept", "application/json");
            request.addHeader("content-type", "application/json");
            String token = session("userToken");
            request.addHeader("rest-dspace-token", token);

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            User user = new User();
            user = user.parseUserFromJSON(Json.parse(responseBody));
            setSessionFromUser(user);


            return ok(views.html.status.render(user, "Status", responseBody, request.getURI().toString()));

            // handle response here...
        }catch (IOException ex) {
            // handle exception here

            Logger.error(ex.getMessage() + " cause:" + ex.getCause());
            return internalServerError(ex.getMessage());
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }



    public static void setSessionFromUser(User user) {
        if(user != null && user.email() != null && user.fullname() != null & !user.email().isEmpty() && !user.fullname().isEmpty()) {
            Logger.info("not empty");
            session("userEmail", user.email());
            session("userFullname", user.fullname());
            session("userToken", user.token());
        }
    }

    public static HttpURLConnection connectToURL(String endpoint) throws IOException {
        //TODO Delete this before production
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        //TODO Delete this before production
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        // Now you can access an https URL without having the certificate in the truststore

        HttpURLConnection conn;
        URL url = new URL(baseRestUrl + "/" + endpoint);

        if(url.getProtocol().contains("https")) {
            conn = (HttpsURLConnection) url.openConnection();
            Logger.info("https");
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new MalformedURLException("Non-200 response: " + conn.getResponseMessage());
        }

        return conn;
    }
}