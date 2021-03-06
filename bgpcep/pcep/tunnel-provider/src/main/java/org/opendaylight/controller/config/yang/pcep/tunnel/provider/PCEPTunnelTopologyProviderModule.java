/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: config-pcep-tunnel-provider  yang module local name: pcep-tunnel-topology-impl
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Tue Nov 19 04:23:38 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.pcep.tunnel.provider;

import org.opendaylight.bgpcep.pcep.tunnel.provider.PCEPTunnelTopologyProvider;
import org.opendaylight.bgpcep.pcep.tunnel.provider.TunnelProgramming;
import org.opendaylight.bgpcep.topology.DefaultTopologyReference;
import org.opendaylight.controller.config.api.JmxAttributeValidationException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.topology.rev140113.NetworkTopologyContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.NetworkTopologyPcepService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.tunnel.pcep.programming.rev131030.TopologyTunnelPcepProgrammingService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public final class PCEPTunnelTopologyProviderModule extends AbstractPCEPTunnelTopologyProviderModule {

    public PCEPTunnelTopologyProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public PCEPTunnelTopologyProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            final PCEPTunnelTopologyProviderModule oldModule, final java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void validate() {
        super.validate();
        JmxAttributeValidationException.checkNotNull(getTopologyId(), "is not set.", topologyIdJmxAttribute);
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final PCEPTunnelTopologyProvider ttp = PCEPTunnelTopologyProvider.create(getDataProviderDependency(),
                getSourceTopologyDependency().getInstanceIdentifier(), getTopologyId());
        final NetworkTopologyPcepService ntps = getRpcRegistryDependency().getRpcService(NetworkTopologyPcepService.class);
        final TunnelProgramming tp = new TunnelProgramming(getSchedulerDependency(), getDataProviderDependency(), ntps);

        final BindingAwareBroker.RoutedRpcRegistration<TopologyTunnelPcepProgrammingService> reg = getRpcRegistryDependency().addRoutedRpcImplementation(
                TopologyTunnelPcepProgrammingService.class, tp);
        final InstanceIdentifier<Topology> topology = InstanceIdentifier.builder(NetworkTopology.class).child(Topology.class,
                new TopologyKey(getTopologyId())).build();
        reg.registerPath(NetworkTopologyContext.class, topology);

        final class TunnelTopologyReferenceCloseable extends DefaultTopologyReference implements AutoCloseable {
            public TunnelTopologyReferenceCloseable(final InstanceIdentifier<Topology> instanceIdentifier) {
                super(instanceIdentifier);
            }

            @Override
            public void close() {
                reg.close();
                tp.close();
                ttp.close();
            }
        }

        return new TunnelTopologyReferenceCloseable(ttp.getTopologyReference().getInstanceIdentifier());
    }
}
