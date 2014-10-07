package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
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
import java.util.ArrayList;
import java.util.List;


public class Application extends Controller {
  //public static String baseRestUrl = "http://localhost:8080/rest";
  public static String baseRestUrl = "https://localhost:8443/rest";

    static {
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
      StringBuilder contentString = new StringBuilder();
      HttpURLConnection conn = null;
      BufferedReader reader = null;

      try {
          conn = connectToURL("communities");

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

          return ok(views.html.index.render(communities, "Top Level Communities", contentString.toString(), endpoint));

      } catch (MalformedURLException e) {
          return badRequest("MalformedURLException: " + e.getMessage());
      } catch (IOException e) {
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

  public static Result showCommunity(Long id) {
      StringBuilder contentString = new StringBuilder();
      HttpURLConnection conn = null;
      BufferedReader reader = null;

      try {
          conn = connectToURL("communities/" + id.toString() + "?expand=all");

          reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

          String output;
          while ((output = reader.readLine()) != null) {
              contentString.append(output);
          }

          JsonNode comm = Json.parse(contentString.toString());
          Community community = new Community();

          if (comm.size() > 0) {
              community = Community.parseCommunityFromJSON(comm);
          }

          String endpoint = conn.getURL().toString();

          return ok(views.html.community.detail.render(community, "Single Community", contentString.toString(), endpoint));
          
      } catch (MalformedURLException e) {
          return badRequest(e.getMessage());
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

    public static Result showCollection(Long id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = connectToURL("collections/" + id.toString() + "?expand=all");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode collNode = Json.parse(contentString.toString());
            Collection collection = new Collection();

            if (collNode.size() > 0) {
                collection = Collection.parseCollectionFromJSON(collNode);
            }

            String endpoint = conn.getURL().toString();
            return ok(views.html.collection.detail.render(collection, "Single Collection", contentString.toString(), endpoint));

        } catch (MalformedURLException e) {
            return badRequest(e.getMessage());
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

    public static Result showItem(Long id) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            conn = connectToURL("items/" + id.toString() + "?expand=all");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));



            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode node = Json.parse(contentString.toString());
            Item item = new Item();

            if (node.size() > 0) {
               item = Item.parseItemFromJSON(node);
            }

            String endpoint = conn.getURL().toString();
            return ok(views.html.item.detail.render(item, "Single Item", contentString.toString(), endpoint));

        } catch (MalformedURLException e) {
            return badRequest(e.getMessage());
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

    public static HttpURLConnection connectToURL(String endpoint) throws IOException {
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