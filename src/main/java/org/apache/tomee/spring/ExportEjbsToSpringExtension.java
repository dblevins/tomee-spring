/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.spring;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.springframework.context.ConfigurableApplicationContext;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @version $Rev$ $Date$
 */
public class ExportEjbsToSpringExtension {

    // Any logging works.  Doesn't need to be this one
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ExportEjbsToSpringExtension.class);

    /**
     * TODO  The one piece of work, get or create the ConfigurableApplicationContext
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     * This might not be the exact right CDI startup event, but should be close
     * @param discovery
     */
    public void observe(@Observes AfterBeanDiscovery discovery) {

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        for (BeanContext beanContext : containerSystem.deployments()) {

            Map<String, EJB> bindings = getEjbBindings(beanContext);

            for (Map.Entry<String, EJB> entry : bindings.entrySet()) {
                String beanName = entry.getKey();
                if (!applicationContext.containsBean(beanName)) {
                    EJB ejb = entry.getValue();
                    applicationContext.getBeanFactory().registerSingleton(beanName, ejb);
                    logger.info("Exported EJB " + beanContext.getEjbName() + " with interface " + ejb.getInterface().getName() + " to Spring bean " + entry.getKey());
                }
            }

        }
    }

    public Map<String, EJB> getEjbBindings(BeanContext deployment) {
        if (!deployment.getComponentType().isSession()) return Collections.EMPTY_MAP;

        final Map<String, EJB> bindings = new TreeMap<String, EJB>();

        if (deployment.isLocalbean()) {
            bindings.put(deployment.getDeploymentID() + "", new EJB(deployment, InterfaceType.LOCALBEAN, deployment.getBeanClass()));
        }

        for (Class businessLocal : deployment.getBusinessLocalInterfaces()) {
            bindings.put(deployment.getDeploymentID() + "!" + businessLocal.getName(), new EJB(deployment, InterfaceType.BUSINESS_LOCAL, businessLocal));
        }

        for (Class businessRemote : deployment.getBusinessRemoteInterfaces()) {
            bindings.put(deployment.getDeploymentID() + "!" + businessRemote.getName(), new EJB(deployment, InterfaceType.BUSINESS_REMOTE, businessRemote));
        }

        return bindings;
    }
}
