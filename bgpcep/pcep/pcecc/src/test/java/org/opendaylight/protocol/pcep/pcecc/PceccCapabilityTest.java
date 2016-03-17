/*
 * Copyright (c) 2016 Huawei Technologies Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.pcep.pcecc;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.Tlvs1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pcecc.capability.tlv.PceccCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.object.open.Tlvs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.object.open.TlvsBuilder;


public class PceccCapabilityTest {

    private static final Tlvs EXPECTED_TLVS =
            new TlvsBuilder().addAugmentation(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.Tlvs1.class,  new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.Tlvs1Builder()
                            .setPceccCapability( new PceccCapabilityBuilder().setSBit(true).setILDBBit(true).build())
                            .build())
                    .build();

    @Test
    public void testPCEPPceccCapability() {
        final PcepPceccCapability sspf;
        sspf = new PcepPceccCapability(true,true,true);
        Assert.assertTrue(sspf.isPceccCapable());

        final TlvsBuilder builder = new TlvsBuilder();
        sspf.setCapabilityProposal(null, builder);
        Assert.assertEquals(EXPECTED_TLVS, builder.build());
    }
}
