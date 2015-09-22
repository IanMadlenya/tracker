package ca.uhnresearch.pughlab.tracker.sockets;

import org.apache.shiro.subject.Subject;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.Heartbeat;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.security.SpringShiroInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

@Configurable
@ManagedService(path = "/events",
                interceptors = {
		AtmosphereResourceLifecycleInterceptor.class,
		HeartbeatInterceptor.class,
		SuspendTrackerInterceptor.class,
		SpringShiroInterceptor.class
})
public class TrackerSocketHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private SocketEventHandler server;

	private ObjectMapper mapper = new ObjectMapper();


	@Heartbeat
    public void onHeartbeat(final AtmosphereResourceEvent event) {
        logger.info("Heartbeat send by {}", event.getResource().uuid());
    }
	
    /**
     * Invoked when the connection as been fully established and suspended, e.g ready for receiving messages.
     *
     * @param r
     */
    @Ready
    public void onReady(final AtmosphereResource r) {

        getEventManager().registerAtmosphereResource(r);
        
        Subject subject = (Subject) r.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
        if (subject != null) {
        	
            // When we are ready, we should actually send a welcome message to the client. This starts off
            // much of the protocol.
            
            logger.info("Browser connected: {}, subject principal: {}", r.uuid(), subject.getPrincipals().getPrimaryPrincipal());
            Event event = new Event(Event.EVENT_WELCOME);
            getEventManager().sendMessage(event, r);
        } else {
        	logger.error("Browser connected: {}, subject principal unavailable", r.uuid());
        }
        
    }

    /**
     * Invoked when the client disconnect or when an unexpected closing of the underlying connection happens.
     *
     * @param event
     */
    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        if (event.isCancelled()) {
            logger.info("Browser {} unexpectedly disconnected", event.getResource().uuid());
        } else if (event.isClosedByClient()) {
            logger.info("Browser {} closed the connection", event.getResource().uuid());
        }
        getEventManager().unregisterAtmosphereResource(event.getResource());
    }

    /**
	 * Handles a message from the client. This is where most of the actual logic goes here. 
	 * 
     * @param message an instance of {@link Event}
     * @return
     * @throws IOException
     */
    @Message
    public String onMessage(AtmosphereResource r, String input) throws IOException {
    	
    	logger.debug("Message received: {}", input);
    	Event message = mapper.readValue(input, Event.class);
    	logger.debug("Translated message: {}", message);
    	getEventManager().receivedMessage(message, r);

    	return input;
    }

    /**
     * Gets the current event manager.
     * @return the event manager
     */
	public SocketEventHandler getEventManager() {
		return server;
	}

	/**
	 * Sets the current event manager. Used with dependency injection. 
	 * @param eventManager
	 */
	@Inject
	@Named("socketEventHandler")
	public void setEventManager(SocketEventHandler server) {
		logger.debug("Setting SocketEventHandler to: {}", server);
		this.server = server;
	}
}