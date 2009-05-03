package org.springframework.flex.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import flex.messaging.config.ConfigMap;
import flex.messaging.log.AbstractTarget;
import flex.messaging.log.LogEvent;

/**
 * BlazeDS Logging target that logs messages using standard apache commons-logging.
 * Configuration:
 * <b>services-config.xml</b>
 * <pre>
 *  &lt;logging&gt;
 *      &lt;target class="org.springframework.flex.core.CommonsLoggingTarget" level="All"&gt;
 *      	&lt;properties&gt;
 *      		&lt;categoryPrefix>blazeds&lt;/categoryPrefix&gt;
 *      	&lt;/properties&gt;
 *      &lt;/target&gt;
 *  &lt;/logging&gt;
 * </pre>
 * <b>Underlying Logger Configuration (e.g. log4j.xml)</b>
 * <pre>
 * &lt;logger name="blazeds" additivity="false"&gt;
 *      &lt;level value="DEBUG" /&gt;
 *  &lt;/logger&gt;
 * </pre>  
 * 
 * <b>Following Categories are available in BlazeDS/LCDS:</b>
 * <ul>
 *  <li>Configuration</li>
 *  <li>DataService.General</li>
 *  <li>DataService.Hibernate</li>
 *  <li>DataService.Transaction</li>
 *  <li>Endpoint.*</li>
 *  <li>Endpoint.AMF</li>
 *  <li>Endpoint.HTTP</li>
 *  <li>Endpoint.RTMP</li>
 *  <li>Endpoint.Deserialization</li>
 *  <li>Endpoint.General</li>
 *  <li>Message.*</li>
 *  <li>Message.Command.*</li>
 *  <li>Message.Command.operation-name where operation-name is one of the following:
 *      subscribe, unsubscribe, poll, poll_interval, client_sync, server_ping,client_ping, cluster_request, login, logout</li>
 *  <li>Message.General</li>
 *  <li>Message.Data.*</li>
 *  <li>Message.Data.operation-name where operation-name is one of the following:
 *      create, fill get, update, delete, batched, multi_batch, transacted, page, count, get_or_create, create_and_sequence, get_sequence_id, association_add, association_remove, fillids, refresh_fill, update_collection</li>
 *  <li>Message.RPC</li>
 *  <li>MessageSelector</li>
 *  <li>Resource</li>
 *  <li>Service.*</li>
 *  <li>Service.Cluster</li>
 *  <li>Service.HTTP</li>
 *  <li>Service.Message</li>
 *  <li>Service.Message.JMS (logs a warning if a durable JMS subscriber can't be unsubscribed successfully.)</li>
 *  <li>Service.Remoting</li>
 *  <li>Security</li>
 * </ul>
 * 
 * @author Isaac Levin
 */
public class CommonsLoggingTarget extends AbstractTarget {
	protected String categoryPrefix;
	
	
	public CommonsLoggingTarget() {
		super();
		categoryPrefix = null;
	}
	
	public void initialize(String id, ConfigMap properties) {
		super.initialize(id, properties);
		categoryPrefix = properties.getPropertyAsString("categoryPrefix", null);
	}
	
	public void logEvent(LogEvent logevent) {
		String category = logevent.logger.getCategory();
		if (categoryPrefix != null) {
			category = categoryPrefix + "." + category;
		}
		Log log = LogFactory.getLog(category);
		switch (logevent.level) {
		case LogEvent.FATAL:
			if (log.isFatalEnabled()) {
				log.fatal(logevent.message, logevent.throwable);
			}
			break;
		case LogEvent.ERROR:
			if (log.isErrorEnabled()) {
				log.error(logevent.message, logevent.throwable);
			}
			break;
		case LogEvent.WARN:
			if (log.isWarnEnabled()) {
				log.warn(logevent.message, logevent.throwable);
			}
			break;
		case LogEvent.INFO:
			if (log.isInfoEnabled()) {
				log.info(logevent.message, logevent.throwable);
			}
			break;
		case LogEvent.DEBUG:
			if (log.isDebugEnabled()) {
				log.debug(logevent.message, logevent.throwable);
			}
			break;
		case LogEvent.ALL:
			if (log.isTraceEnabled()) {
				log.trace(logevent.message, logevent.throwable);
			}
			break;
		default:
			break;
		}
	}
}