/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.blueprint.context;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.blueprint.context.EventConstants;
import org.osgi.service.blueprint.context.BlueprintContext;
import org.osgi.service.blueprint.context.BlueprintContextListener;
import org.apache.geronimo.blueprint.BlueprintConstants;
import org.apache.geronimo.blueprint.ModuleContextEventSender;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:dev@geronimo.apache.org">Apache Geronimo Project</a>
 * @version $Rev: 760378 $, $Date: 2009-03-31 11:31:38 +0200 (Tue, 31 Mar 2009) $
 */
public class DefaultModuleContextEventSender implements ModuleContextEventSender {

    private final Bundle extenderBundle;
    private final ServiceTracker eventAdminServiceTracker;
    private final ServiceTracker contextListenerTracker;

    public DefaultModuleContextEventSender(BundleContext bundleContext) {
        this.extenderBundle = bundleContext.getBundle();
        this.eventAdminServiceTracker = new ServiceTracker(bundleContext, EventAdmin.class.getName(), null);
        this.eventAdminServiceTracker.open();
        this.contextListenerTracker = new ServiceTracker(bundleContext, BlueprintContextListener.class.getName(), null);
        this.contextListenerTracker.open();
    }

    public void sendCreating(BlueprintContext moduleContext) {
        sendEvent(moduleContext, TOPIC_CREATING, null, null, null);
    }

    public void sendCreated(BlueprintContext moduleContext) {
        sendEvent(moduleContext, TOPIC_CREATED, null, null, null);
    }

    public void sendDestroying(BlueprintContext moduleContext) {
        sendEvent(moduleContext, TOPIC_DESTROYING, null, null, null);
    }

    public void sendDestroyed(BlueprintContext moduleContext) {
        sendEvent(moduleContext, TOPIC_DESTROYED, null, null, null);
    }

    public void sendWaiting(BlueprintContext moduleContext, String[] serviceObjectClass, String serviceFilter) {
        sendEvent(moduleContext, TOPIC_WAITING, null, serviceObjectClass, serviceFilter);
    }

    public void sendFailure(BlueprintContext moduleContext, Throwable cause) {
        sendEvent(moduleContext, TOPIC_FAILURE, cause, null, null);
    }

    public void sendFailure(BlueprintContext moduleContext, Throwable cause, String[] serviceObjectClass, String serviceFilter) {
        sendEvent(moduleContext, TOPIC_FAILURE, cause, serviceObjectClass, serviceFilter);
    }

    public void sendEvent(BlueprintContext moduleContext, String topic, Throwable cause, String[] serviceObjectClass, String serviceFilter) {

        if (topic == TOPIC_CREATED || topic == TOPIC_FAILURE) {
            Object[] listeners = contextListenerTracker.getServices();
            if (listeners != null) {
                for (Object listener : listeners) {
                    try {
                        if (topic == TOPIC_CREATED) {
                            ((BlueprintContextListener) listener).contextCreated(moduleContext.getBundleContext().getBundle());
                        } else if (topic == TOPIC_FAILURE) {
                            ((BlueprintContextListener) listener).contextCreationFailed(moduleContext.getBundleContext().getBundle(), cause);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace(); // TODO: log
                    }
                }
            }
        }

        EventAdmin eventAdmin = getEventAdmin();
        if (eventAdmin == null) {
            return;
        }

        Bundle bundle = moduleContext.getBundleContext().getBundle();

        Dictionary props = new Hashtable();
        props.put(org.osgi.service.event.EventConstants.BUNDLE_SYMBOLICNAME, bundle.getSymbolicName());
        props.put(org.osgi.service.event.EventConstants.BUNDLE_ID, bundle.getBundleId());
        props.put(org.osgi.service.event.EventConstants.BUNDLE, bundle);
        Version version = getBundleVersion(bundle);
        if (version != null) {
            props.put(BlueprintConstants.BUNDLE_VERSION, version);
        }
        props.put(org.osgi.service.event.EventConstants.TIMESTAMP, System.currentTimeMillis());
        props.put(EventConstants.EXTENDER_BUNDLE, extenderBundle);
        props.put(EventConstants.EXTENDER_ID, extenderBundle.getBundleId());
        props.put(EventConstants.EXTENDER_SYMBOLICNAME, extenderBundle.getSymbolicName());
        
        if (cause != null) {
            props.put(org.osgi.service.event.EventConstants.EXCEPTION, cause);
        }
        if (serviceObjectClass != null) {
            props.put(org.osgi.service.event.EventConstants.SERVICE_OBJECTCLASS, serviceObjectClass);
        }
        if (serviceFilter != null) {
            props.put(BlueprintConstants.SERVICE_FILTER, serviceFilter);
        }

        Event event = new Event(topic, props);
        eventAdmin.postEvent(event);
        System.out.println("Event sent: " + topic);
    }

    private static Version getBundleVersion(Bundle bundle) {
        Dictionary headers = bundle.getHeaders();
        String version = (String)headers.get(Constants.BUNDLE_VERSION);
        return (version != null) ? Version.parseVersion(version) : null;
    }
    
    private EventAdmin getEventAdmin() {
        return (EventAdmin)this.eventAdminServiceTracker.getService();
    }

    public void destroy() {
        this.eventAdminServiceTracker.close();
        this.contextListenerTracker.close();
    }
}
