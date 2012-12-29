package org.wuliang;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 *
 * @author wuliang
 */
public class YahooCalendarScribeDemo {
        static public void main(String[] args) throws Exception {
        //BasicConfigurator.configure();
        args = new String[]{"https://caldav.calendar.yahoo.com/dav/wuliang_org@yahoo.com/Calendar/Wu_Liang/",
        "username","password"};
        if (args.length != 3) {
            System.err.println("USAGE: java sample.caldav.Main URL username password");
            System.err.println("  URL:      CalDAV URL of the server.");
            System.err.println("  username: cadav username.");
            System.err.println("  password: password of the user.");
            return;
        }
        String uri = args[0];
        String username = args[1];
        String password = args[2];
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(uri);
        // define connection manager
        HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        int maxHostConnections = 20;
        params.setMaxConnectionsPerHost(hostConfig, maxHostConnections);
        connectionManager.setParams(params);
        // define the HttpClient with user and password
        HttpClient client = new HttpClient(connectionManager);
        client.setHostConfiguration(hostConfig);
        Credentials creds = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(AuthScope.ANY, creds);
        // create a EVENT
        System.err.println("Sending PUT...");
        String created = doCreateEvent(client, uri);
    }
    
    static public String doCreateEvent(
            HttpClient client, String uri) throws IOException {
        // create a new EVENT
        PutMethod put = null;
        try {
            UUID uuid = UUID.randomUUID();
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar c = new net.fortuna.ical4j.model.Calendar();
            c.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
            c.getProperties().add(Version.VERSION_2_0);
            c.getProperties().add(CalScale.GREGORIAN);
            TimeZoneRegistry registry = builder.getRegistry();
            VTimeZone tz = registry.getTimeZone("Europe/Madrid").getVTimeZone();
            c.getComponents().add(tz);
            VEvent vevent = new VEvent(new net.fortuna.ical4j.model.Date(),
                    new Dur(0, 1, 0, 0), "test");
            vevent.getProperties().add(new Uid(uuid.toString()));
            c.getComponents().add(vevent);
            String href = uri + uuid.toString() + ".ics";
            put = new PutMethod(href);
            put.addRequestHeader("If-None-Match", "*");
            put.setRequestEntity(new StringRequestEntity(c.toString(), "text/calendar", "UTF-8"));
            //client.executeMethod(put);
            runoauth(c.toString(), uri);
            return href;
        } finally {
            if (put != null) {
                put.releaseConnection();
            }
        }
    }
    
    //private static final String PROTECTED_RESOURCE_URL = "http://social.yahooapis.com/v1/user/A6ROU63MXWDCW3Y5MGCYWVHDJI/profile/status?format=json";
    private static final String PROTECTED_RESOURCE_URL = "http://social.yahooapis.com/v1/user/SI4NXTYBIRRWN5ISOSEH6TIA7Y/profile?format=json";

    public static void runoauth(String content, String url)
    {
      OAuthService service = new ServiceBuilder()
                                  .provider(YahooApi.class)
                                  //.apiKey("dj0yJmk9TXZDWVpNVVdGaVFmJmQ9WVdrOWMweHZXbkZLTkhVbWNHbzlNVEl5TWprd05qUTJNZy0tJnM9Y29uc3VtZXJzZWNyZXQmeD0wMw--")
                                  //.apiSecret("262be559f92a2be20c4c039419018f2b48cdfce9")
                                  .apiKey("dj0yJmk9U2FpR0U5UmNUcU05JmQ9WVdrOWVFUlpSSFpQTkdVbWNHbzlNVFkyTVRnNU5URTJNZy0tJnM9Y29uc3VtZXJzZWNyZXQmeD1lMw--")
                                  .apiSecret("baa7bdb6e4e1b609c94d7e48b27c30d65fccc6b3")
                                  .build();
      
     
      
      Scanner in = new Scanner(System.in);

      System.out.println("=== Yahoo's OAuth Workflow ===");
      System.out.println();

      // Obtain the Request Token
      System.out.println("Fetching the Request Token...");
      Token requestToken = service.getRequestToken();
      System.out.println("Got the Request Token!");
      System.out.println();

      System.out.println("Now go and authorize Scribe here:");
      System.out.println(service.getAuthorizationUrl(requestToken));
      System.out.println("And paste the verifier here");
      System.out.print(">>");
      Verifier verifier = new Verifier(in.nextLine());
      System.out.println();

      // Trade the Request Token and Verfier for the Access Token
      System.out.println("Trading the Request Token for an Access Token...");
      Token accessToken = service.getAccessToken(requestToken, verifier);
      System.out.println("Got the Access Token!");
      System.out.println("(if your curious it looks like this: " + accessToken + " )");
      System.out.println();

      // Now let's go and ask for a protected resource!
      System.out.println("Now we're going to access a protected resource...");
      OAuthRequest request1 = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
      service.signRequest(accessToken, request1);
      System.out.println("\r\n####################request hedaer:");
      for (String key : request1.getHeaders().keySet()) {
          System.out.println("key:" + key + " value:" + request1.getHeaders().get(key));
      }
      Response response1 = request1.send();
      System.out.println("Got it! Lets see what we found...");
      System.out.println();
      System.out.println("code"+response1.getCode());
      System.out.println("body"+response1.getBody());
      
      
      OAuthRequest request = new OAuthRequest(Verb.PUT, url);
      request.addPayload(content);
      service.signRequest(accessToken, request);
      System.out.println("\r\n####################request hedaer:");
      for (String key : request.getHeaders().keySet()) {
          System.out.println("key:" + key + " value:" + request.getHeaders().get(key));
      }
      Response response = request.send();
      System.out.println("Got it! Lets see what we found...");
      System.out.println();
      System.out.println("code"+response.getCode());
      System.out.println("body"+response.getBody());
      System.out.println("\r\n####################response hedaer:");
      for (String key : response.getHeaders().keySet()) {
          System.out.println("key:" + key + " value:" + response.getHeaders().get(key));
      }
      System.out.println();
      System.out.println("Thats it man! Go and build something awesome with Scribe! :)");

    }
}
