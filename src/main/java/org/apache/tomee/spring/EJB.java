/**
 *
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
import org.springframework.beans.factory.FactoryBean;

public class EJB<T> implements FactoryBean {

    private final BeanContext beanContext;
    private final Class<T> intf;
    private final InterfaceType interfaceType;

    public EJB(BeanContext beanContext, InterfaceType interfaceType, Class<T> intf) {
        if (intf == null) throw new NullPointerException("interface is null");
        if (beanContext == null) throw new NullPointerException("beanContext is null");
        if (beanContext == null) throw new NullPointerException("interfaceType is null");

        this.beanContext = beanContext;
        this.intf = intf;
        this.interfaceType = interfaceType;
    }

    public Class<T> getInterface() {
        return intf;
    }

    public T getObject() throws Exception {
        switch (interfaceType) {
            case LOCALBEAN: {
                return (T) beanContext.getBusinessLocalBeanHome().create();
            }
            case BUSINESS_LOCAL: {
                return (T) beanContext.getBusinessLocalHome(intf).create();
            }
            case BUSINESS_REMOTE: {
                return (T) beanContext.getBusinessRemoteHome(intf).create();
            }
            default:
                throw new IllegalStateException("Unsupported interface type: " + interfaceType);
        }
    }

    public Class<T> getObjectType() {
        return getInterface();
    }

    public boolean isSingleton() {
        return false;
    }
}
