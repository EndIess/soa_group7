package com.shenzhentagram.scheduler;

import com.shenzhentagram.exception.UnknownServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * Created by Meranote on 4/28/2017.
 */
@Component
public class ServiceConnectingTask {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(SampleScheduler.class);

    /**
     * Default timeout
     */
    private static final int TIMEOUT = 20 * 1000; // in milliseconds, 20 seconds

    /**
     * Default service names
     */
    private static final String[] SERVICE_NAMES = {"authentication", "user", "post", "comment", "notification", "reaction"};

    /**
     * REST template connection mapping
     */
    private HashMap<String, RestTemplate> serviceConnectionMapping;

    /**
     * REST template connected state mapping
     */
    private HashMap<String, Boolean> serviceConnectStateMapping;

    /**
     * Construct service connection checking task
     */
    public ServiceConnectingTask(Environment environment, RestTemplateBuilder restTemplateBuilder) {
        serviceConnectionMapping = new HashMap<>();
        serviceConnectStateMapping = new HashMap<>();

        for(String serviceName : SERVICE_NAMES) {
            String protocol = environment.getProperty("service." + serviceName + ".protocol");
            String ip = environment.getProperty("service." + serviceName + ".ip");
            String port = environment.getProperty("service." + serviceName + ".port");

            RestTemplate restTemplate = restTemplateBuilder.rootUri(protocol + "://" + ip + ":" + port).build();

            // Set components (request timeout)
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);

            restTemplate.setRequestFactory(requestFactory);

            serviceConnectionMapping.put(serviceName, restTemplate);
            serviceConnectStateMapping.put(serviceName, false);
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void CheckAuthenticationService() {
        checkConnectionToService("authentication");
    }

    @Scheduled(fixedDelay = 30000)
    public void CheckUserService() {
        checkConnectionToService("user");
    }

    @Scheduled(fixedDelay = 30000)
    public void CheckPostService() {
        checkConnectionToService("post");
    }

    @Scheduled(fixedDelay = 30000)
    public void CheckCommentService() {
        checkConnectionToService("comment");
    }

    @Scheduled(fixedDelay = 30000)
    public void CheckNotificationService() {
        checkConnectionToService("notification");
    }

    @Scheduled(fixedDelay = 30000)
    public void CheckReactionService() {
        checkConnectionToService("reaction");
    }

    /**
     * Fire the signal to service for checking (to each service "/ping")
     * @param serviceName
     */
    private void checkConnectionToService(String serviceName) {
        RestTemplate restTemplate = serviceConnectionMapping.get(serviceName);

        HttpEntity<Object> entity = new HttpEntity<>("ping", new HttpHeaders());
        try {
            // Fire signal
            restTemplate.exchange("/ping", HttpMethod.GET, entity, Void.class);
            serviceConnectStateMapping.put(serviceName, true);
        } catch(ResourceAccessException ignored) {
            // Can't access to service (maybe its down)
            serviceConnectStateMapping.put(serviceName, false);
        } catch(RestClientResponseException ignored) {
            // Response is invalid but can connected to service
            serviceConnectStateMapping.put(serviceName, true);
        }
    }

    /**
     * Is service alive (connected?)
     * @param serviceName
     * @return
     */
    public boolean isServiceAlive(String serviceName) {
        try {
            if (!serviceConnectStateMapping.containsKey(serviceName)) {
                throw new UnknownServiceException(serviceName);
            }

            logger.info("State to " + serviceName + " : " + serviceConnectStateMapping.get(serviceName));
            return serviceConnectStateMapping.get(serviceName);
        } catch (UnknownServiceException e) {
            logger.error("Unknown service", e);
            return false;
        }
    }

    /**
     * Force set the connection state to service
     * @param newState
     */
    public void forceSetServiceState(String serviceName, boolean newState) {
        try {
            if (!serviceConnectStateMapping.containsKey(serviceName)) {
                throw new UnknownServiceException(serviceName);
            }

            serviceConnectStateMapping.put(serviceName, newState);
        } catch (UnknownServiceException e) {
            logger.error("Unknown service", e);
        }
    }

}
