/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.concepts.rev131125.MplsLabel;

public class MplsLabelUtilTest {

    private static final long VAL1 = 5;
    private static final byte[] VAL1_LEFT_BYTES = new byte[] { 0, 0, 0x50 };
    private static final byte[] VAL1_LEFT_BYTES_BOTTOM = new byte[] { 0, 0, 0x51 };

    @Test
    public void testCreateLabel() {
        assertEquals(new MplsLabel(VAL1), MplsLabelUtil.mplsLabelForByteBuf(Unpooled.copiedBuffer(VAL1_LEFT_BYTES_BOTTOM)));
    }

    @Test
    public void testBottomBit() {
        assertFalse(MplsLabelUtil.getBottomBit(Unpooled.copiedBuffer(VAL1_LEFT_BYTES)));
        assertTrue(MplsLabelUtil.getBottomBit(Unpooled.copiedBuffer(VAL1_LEFT_BYTES_BOTTOM)));
    }

    @Test
    public void testSerialization() {
        final MplsLabel label = new MplsLabel(VAL1);
        assertEquals(Unpooled.copiedBuffer(VAL1_LEFT_BYTES), MplsLabelUtil.byteBufForMplsLabel(label));
        assertEquals(Unpooled.copiedBuffer(VAL1_LEFT_BYTES_BOTTOM), MplsLabelUtil.byteBufForMplsLabelWithBottomBit(label));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testPrivateConstructor() throws Throwable {
        final Constructor<MplsLabelUtil> c = MplsLabelUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        try {
            c.newInstance();
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
