package com.epam.eco.kafkamanager.core.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for audit logging of Kafka management actions.
 * All log messages are prefixed with "AUDIT:" and include user, IP, action, and object details.
 */
public class AuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogger.class);
    private static final String AUDIT_PREFIX = "AUDIT:";
    public static final String CONSUMER_GROUP_OBJECT = "consumer-group";
    public static final String PERMISSION_OBJECT = "permission";
    public static final String TOPIC_OBJECT = "topic";

    private AuditLogger() {
    }

    /**
     * Logs an audit event with the specified action and object details.
     *
     * @param action         The action being performed (e.g., "CREATE_TOPIC",
     *                       "DELETE_CONSUMER_GROUP")
     * @param objectType     The type of object being acted upon (e.g., "topic",
     *                       "consumer-group", "permission")
     * @param objectName     The name of the object being acted upon
     * @param additionalInfo Additional details about the action (can be null)
     */
    public static void log(
            String action,
            String objectType,
            String objectName,
            Map<String, Object> additionalInfo
    ) {
        try {
            String username = getCurrentUsername();
            String ipAddress = getClientIpAddress();

            StringBuilder message = new StringBuilder(AUDIT_PREFIX)
                    .append(" user=").append(username)
                    .append(" | ip=").append(ipAddress)
                    .append(" | action=").append(action)
                    .append(" | objectType=").append(objectType)
                    .append(" | objectName=").append(objectName);

            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                message.append(", details=").append(additionalInfo);
            }

            LOGGER.info(message.toString());
        } catch (Exception e) {
            LOGGER.error("Failed to log audit event", e);
        }
    }


    private static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }

    private static String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    // Convenience methods for common audit log scenarios

    public static void logTopicCreate(
            String topicName,
            Map<String, Object> details
    ) {
        log("CREATE_TOPIC", TOPIC_OBJECT, topicName, details);
    }

    public static void logTopicDelete(String topicName) {
        log("DELETE_TOPIC", TOPIC_OBJECT, topicName, null);
    }

    public static void logTopicConfigUpdate(
            String topicName,
            Map<String, Object> configChanges
    ) {
        log("UPDATE_TOPIC_CONFIG", TOPIC_OBJECT, topicName, configChanges);
    }


    public static void logConsumerGroupDelete(String groupName) {
        log("DELETE_CONSUMER_GROUP", CONSUMER_GROUP_OBJECT, groupName, null);
    }

    public static void logConsumerGroupOffsetsReset(
            String groupName,
            Map<String, Object> resetDetails
    ) {
        log("RESET_CONSUMER_GROUP_OFFSETS", CONSUMER_GROUP_OBJECT, groupName, resetDetails);
    }

    public static void logPermissionCreate(
            String permissionName,
            Map<String, Object> permissionDetails
    ) {
        log("CREATE_PERMISSION", PERMISSION_OBJECT, permissionName, permissionDetails);
    }

    public static void logPermissionPrincipalDelete(String permissionName) {
        log("DELETE_PRINCIPAL_PERMISSIONS", PERMISSION_OBJECT, permissionName, null);
    }

    public static void logPermissionResourcesDelete(String permissionName) {
        log("DELETE_RESOURCE_PERMISSIONS", PERMISSION_OBJECT, permissionName, null);
    }

    public static void logPurgeTopic(String topicName) {
        log("PURGE_TOPIC", TOPIC_OBJECT, topicName, null);
    }
}
