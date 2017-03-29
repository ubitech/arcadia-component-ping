package eu.arcadia.ping;

import eu.arcadia.agentglue.ChainingInfo;
import eu.arcadia.annotations.ArcadiaBehavioralProfile;
import eu.arcadia.annotations.ArcadiaChainableEndpointBindingHandler;
import eu.arcadia.annotations.ArcadiaComponent;
import eu.arcadia.annotations.ArcadiaExecutionRequirement;
import eu.arcadia.annotations.ArcadiaLifecycleInitialize;
import eu.arcadia.annotations.ArcadiaLifecycleStart;
import eu.arcadia.annotations.ArcadiaLifecycleStop;
import eu.arcadia.annotations.ArcadiaMetric;
import eu.arcadia.annotations.ScaleBehavior;
import eu.arcadia.annotations.ValueType;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.logging.Logger;

/**
 *
 * @author Christos Paraskeva (ch.paraskeva at gmail dot com)
 */
@ArcadiaComponent(componentname = "Ping", componentversion = "0.1.0", componentdescription = "Sample arcadia native component which sends ping request", isNative = true, tags = {"ping", "sample"})
@ArcadiaMetric(name = "pings", description = "Number of pings", unitofmeasurement = "Number of ping requests", valuetype = ValueType.Integer, maxvalue = "1000", minvalue = "0", higherisbetter = false)
@ArcadiaBehavioralProfile(scalability = ScaleBehavior.VERTICAL_HORIZONTAL)
@ArcadiaExecutionRequirement(memory = 128, vcpu = 2)
public class NativeComponent {

    private static Integer PINGS = 0;
    private static final Logger LOGGER = Logger.getLogger(NativeComponent.class.getName());

    public static ConfigurableApplicationContext appContext;

    public static synchronized void ping() {
        PINGS++;
    }

    /*
     * Arcadia Configuration Parameters 
     * 
     */

 /*
     * Arcadia Metrics 
     * 
     */
    public static String getPings() {
        return String.valueOf(PINGS);
    }

    /*
     * Component Lifecycle Management Methods
     * 
     */
    @ArcadiaLifecycleInitialize
    public static void init() {
        System.setProperty("Component.sentRequests", "0");
        System.setProperty("Component.receivedRequests", "0");
        System.setProperty("app.port", PingApplication.DEFAULT_PORT);
        LOGGER.info("----INIT END---");
    }

    @ArcadiaLifecycleStart
    public static String start() {
        if (appContext == null) {
            appContext = SpringApplication.run(new Class[]{PingApplication.class, CustomizationBean.class}, new String[]{});
        } else {
            LOGGER.severe("AppContext is not null ! Application is already started !");
        }
        LOGGER.info("----START END---");
        return String.valueOf(appContext.isActive());
    }

    @ArcadiaLifecycleStop
    public static String stop() {
        if (appContext != null) {
            SpringApplication.exit(appContext);
            appContext.close();
            appContext = null;
        } else {
            LOGGER.severe("AppContext is null ! Application has not been started !");
        }
        return String.valueOf((appContext == null));
    }


    /*
    DependencyExport-related methods
     */
    public static String getPonguri() {
        return System.getProperty("ponguri");
    }

    public static String getPongport() {
        return System.getProperty("pongport");
    }

    //leaf
    @ArcadiaChainableEndpointBindingHandler(CEPCID = "pong")
    public static void bindDependency(ChainingInfo chainingInfo) {
        LOGGER.info("BINDED COMPONENT:" + chainingInfo.toString());
        String connectedEndpoint = "http://" + chainingInfo.getPrivateIP() + ":" + chainingInfo.getParameterValues().get("port") + "/" + chainingInfo.getParameterValues().get("uri");
        LOGGER.info("Connected Server URI:" + connectedEndpoint);
        System.setProperty("Component.connectedEndpoint", connectedEndpoint);
    }
}
