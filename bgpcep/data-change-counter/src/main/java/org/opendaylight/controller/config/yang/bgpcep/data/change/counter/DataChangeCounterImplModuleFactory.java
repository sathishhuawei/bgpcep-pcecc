/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/*
* Generated file
*
* Generated from: yang module name: odl-data-change-counter-cfg yang module local name: data-change-counter-impl
* Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
* Generated at: Wed Aug 20 13:09:16 CEST 2014
*
* Do not modify this file unless it is present under src/main directory
*/
package org.opendaylight.controller.config.yang.bgpcep.data.change.counter;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.osgi.framework.BundleContext;

public class DataChangeCounterImplModuleFactory extends org.opendaylight.controller.config.yang.bgpcep.data.change.counter.AbstractDataChangeCounterImplModuleFactory {
    public static final String SINGLETON_NAME = "data-change-counter-singleton";

    @Override
    public DataChangeCounterImplModule instantiateModule(String instanceName, DependencyResolver dependencyResolver,
            DataChangeCounterImplModule oldModule, AutoCloseable oldInstance, BundleContext bundleContext) {
        checkArgument(SINGLETON_NAME.equals(instanceName), "Illegal instance name '" + instanceName
                + "', only allowed name is " + SINGLETON_NAME);
        return super.instantiateModule(instanceName, dependencyResolver, oldModule,
                oldInstance, bundleContext);
    }

    @Override
    public DataChangeCounterImplModule instantiateModule(String instanceName, DependencyResolver dependencyResolver,
            BundleContext bundleContext) {
        checkArgument(SINGLETON_NAME.equals(instanceName), "Illegal instance name '" + instanceName
                + "', only allowed name is " + SINGLETON_NAME);
        return super.instantiateModule(instanceName, dependencyResolver, bundleContext);
    }
}
