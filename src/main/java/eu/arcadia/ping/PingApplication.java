package eu.arcadia.ping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.arcadia.ArcadiaConstants;
import eu.arcadia.agentglue.GroundedComponentInfo;
import java.io.BufferedReader;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Christos Paraskeva (ch.paraskeva at gmail dot com)
 */
@Configuration
@ComponentScan(basePackages = "eu.arcadia.ping")
@EnableAutoConfiguration
@RestController
public class PingApplication extends SpringBootServletInitializer {

    private static final Logger LOGGER = Logger.getLogger(PingApplication.class.getName());
    public static final String DEFAULT_PORT = "8080";
    public static final String INFO_URI = "/getInfo";

    @ResponseBody
    @RequestMapping(value = INFO_URI, method = RequestMethod.GET)
    public String getInfo() {
        String serverEndpoint = System.getProperty("Component.connectedEndpoint");
        System.setProperty("Component.receivedRequests", String.valueOf(Integer.parseInt(System.getProperty("Component.receivedRequests")) + 1));
        System.setProperty("Component.sentRequests", String.valueOf(Integer.parseInt(System.getProperty("Component.sentRequests")) + 1));

        String ret = "[--- PING Info ---]\n";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            GroundedComponentInfo groundedComponentInfo = gson.fromJson(new FileReader(ArcadiaConstants.basePathAgent + File.separator + ArcadiaConstants.groundedInfoJsonFn), GroundedComponentInfo.class);
            ret += gson.toJson(groundedComponentInfo);
        } catch (FileNotFoundException e) {
            ret += "Error getting information about PING !";
            LOGGER.severe(e.getMessage());
        }
        try {
            HttpResponse<String> response = Unirest.get(serverEndpoint).asString();
            ret += response.getBody();
        } catch (UnirestException e) {
            ret += "Error getting information about PONG !";
            LOGGER.severe(e.getMessage());
        }
        return ret;

    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index() {
        return String.format(new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("index.html"))).lines().collect(Collectors.joining("\n")), NativeComponent.getPings(), getPongs());
    }

    @RequestMapping(path = "/ping", method = RequestMethod.POST)
    public String ping() {
        try {
            HttpResponse<String> response = Unirest.post(String.format("http://%s:%s/pong", NativeComponent.getPonguri(), NativeComponent.getPongport())).asString();
            if (response.getStatus() == 200) {
                NativeComponent.ping();
                System.out.println("Pings: "+NativeComponent.getPings());
                return "Ping is successful!";
            }
        } catch (UnirestException ex) {
            Logger.getLogger(PingApplication.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "Error occured during ping.";
    }

    private static String getPongs() {
        try {
            HttpResponse<String> response = Unirest.get(String.format("http://%s:%s/pongs", NativeComponent.getPonguri(), NativeComponent.getPongport())).asString();
            System.out.println("Pongs: "+response.getBody());
            return response.getBody();
        } catch (UnirestException ex) {
            LOGGER.severe(ex.getMessage());
        }

        return "N/A";
    }

    //Just needed to compile the jar. Never gets called.
    public static void main(String[] args) {
    }
}
