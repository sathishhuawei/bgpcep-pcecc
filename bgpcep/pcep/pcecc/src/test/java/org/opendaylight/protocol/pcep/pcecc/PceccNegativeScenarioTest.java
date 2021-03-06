/*
 * Copyright (c) 2016 Huawei Technologies Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.pcep.pcecc;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.opendaylight.protocol.pcep.spi.ObjectHeaderImpl;
import org.opendaylight.protocol.pcep.spi.PCEPDeserializerException;
import org.opendaylight.protocol.pcep.spi.TlvRegistry;
import org.opendaylight.protocol.pcep.spi.VendorInformationTlvRegistry;
import org.opendaylight.protocol.pcep.spi.pojo.SimplePCEPExtensionProviderContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.message.rev131007.PcerrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.LabelNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.label.object.LabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.object.OpenBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pcep.error.object.ErrorObjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pcerr.message.PcerrMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pcerr.message.pcerr.message.ErrorsBuilder;

/* Test: PceccNegativeScenarioTest is to Test boundary check and error scenario's */
public class PceccNegativeScenarioTest {

    /*
          0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                      IPv4 Node ID = 1.1.1.1                   |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      FEC Object-Class is 226.

      FEC Object-Type is 1 'IPv4 Node ID'.
      Fec Ipv4 Adjacency Object TYPE = 3;

     */
    private static final byte[] PceccFecAdjacencyObjectBytes = {
        (byte) 0xe2, 0x30, 0x00, 0x1c,
        (byte) 0xfe,(byte) 0x90, 0x00, 0x00,
    };

    private static final byte[] PceccFecObjectBytes = {
        (byte) 0xe2, 0x10, 0x00, 0x08,
        (byte) 0xff,(byte) 0x90,
    };

    /*
          0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |          Reserved            |              Flags           |O|
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                 Label = 5001          |     Reserved          |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                                                               |
      //                        Optional TLV                         //
      |                                                               |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      Label Object
      LABEL Object-Class is 225.
      LABEL Object-Type is 1.

     */
    private static final byte[] PceccLabelObjectBytes = {
        (byte) 0xe1, 0x10, 0x00, 0x0c,
        0x00, 0x00, 0x00, 0x01,
    };

    /*
          0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      | Object-Class  |   OT  |Res|P|I|   Object Length (bytes)       |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                                                               |
      //                        (Object body)                        //
      |                                                               |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      PCEP Common Object Header
      PCEP Open Object-Class is 1
      TYPE = 1

      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |               Type=[TBD]      |            Length=4           |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                             Flags                         |G|L|
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      PCECC Capability TLV
      Type of the TLV is 65287

     */

    private static final byte[] openObjectBytes = {
        0x01, 0x10, 0x00, 0x10,
        0x20, 0x1e, 0x78, 0x01,
        /* pcecc-capability-tlv */
        (byte) 0xff, 0x07, 0x00, 0x02,
        0x00, 0x00, 0x00, 0x03
    };

    /*
          0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                          Flags                                |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                        SRP-ID-number = 1L                     |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                                                               |
      //                      Optional TLVs                          //
      |                                                               |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      SRP Object format
      SRP Object-Class is 33
      TYPE = 1

            0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                PLSP-ID = 0L           |    Flag |    O|A|R|S|D|
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      //                        TLVs                                 //
      |                                                               |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      LSP Object format
      LSP Object-Class is 32
      TYPE = 1

     */
    private static final byte[] PceccLabelUpdateMessageParserError = {
        (byte) 0x20, (byte)0xe2,0x00, 0x20,
        (byte) 0xe1, 0x10, 0x00, (byte) 0x14,
        0x00, 0x00, 0x00, 0x00,
        (byte) 0x01, 0x38, (byte) 0x90, 0x00,
        (byte)  0xff, 0x09, 0x00, 0x04,
        0x01, 0x01, 0x01, 0x01,
        (byte) 0xe2, 0x10, 0x00, 0x08,
        (byte) 0xff,(byte) 0x90, 0x00, 0x01
    };

    /*
          0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |          Reserved            |              Flags           |O|
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                 Label = 5001          |     Reserved          |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                                                               |
      //                        Optional TLV                         //
      |                                                               |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      Label Object
      LABEL Object-Class is 225.
      LABEL Object-Type is 1.

     */
    private static final byte[] PceccLabelTlvBytes = {
        (byte) 0x20, (byte)0xe2,0x00, 0x2c,
    };

    private TlvRegistry tlvRegistry;
    private VendorInformationTlvRegistry viTlvRegistry;

    private SimplePCEPExtensionProviderContext ctx;
    private PceccActivator act;


    /*
    * Description :- Registering the handler.
    */
    @Before
    public void setUp() {
        this.ctx = new SimplePCEPExtensionProviderContext();
        this.act = new PceccActivator();
        this.act.start(this.ctx);
        this.tlvRegistry = this.ctx.getTlvHandlerRegistry();
        this.viTlvRegistry = this.ctx.getVendorInformationTlvRegistry();
    }

    /*
    * testPceccFecIpv4AdjacencyObjectParserHitLengthCheck
    * Description :- Test FecIpv4Adjacency Object length check in PceccFecIpv4AdjacencyObjectParser.
    */

    @Test
    public void testPceccFecIpv4AdjacencyObjectParserHitLengthCheck() throws PCEPDeserializerException {
        try {
            final PceccFecIpv4AdjacencyObjectParser parser =
                    new PceccFecIpv4AdjacencyObjectParser();
            final ByteBuf result = Unpooled.wrappedBuffer(PceccFecAdjacencyObjectBytes);
            parser.parseObject(new ObjectHeaderImpl(false, false), result.slice(4, result.readableBytes() - 4));
            fail();
        } catch (final PCEPDeserializerException e) {
            assertTrue(e.getMessage().contains("Wrong length of array of bytes. Passed: 4; Expected: >=8."));
        }
    }

    /*
    * testPceccFecObjectParserHitLengthCheck
    * Description :- Test Fec Object length check in PceccFecObjectParser.
    */
    @Test
    public void testPceccFecObjectParserHitLengthCheck() throws PCEPDeserializerException {
        try {
            final PceccFecObjectParser parser =
                    new PceccFecObjectParser();
            final ByteBuf result = Unpooled.wrappedBuffer(PceccFecObjectBytes);
            parser.parseObject(new ObjectHeaderImpl(false, false), result.slice(4, result.readableBytes() - 4));
            fail();
        } catch (final PCEPDeserializerException e) {
            assertTrue(e.getMessage().contains("Wrong length of array of bytes. Passed: 2; Expected: >=4."));
        }
    }

    /*
    * testPceccFecIpv4ObjectParserHitLengthCheck
    * Description :-Test FecIpv4 Object length check in PceccFecIpv4ObjectParser.
    */
    @Test
    public void testPceccFecIpv4ObjectParserHitLengthCheck() throws PCEPDeserializerException {
        try {
            final PceccFecIpv4ObjectParser parser =
                    new PceccFecIpv4ObjectParser();
            final ByteBuf result = Unpooled.wrappedBuffer(PceccFecObjectBytes);
            parser.parseObject(new ObjectHeaderImpl(false, false), result.slice(4, result.readableBytes() - 4));
            fail();
        } catch (final PCEPDeserializerException e) {
            assertTrue(e.getMessage().contains("Wrong length of array of bytes. Passed: 2; Expected: >=4."));
        }
    }


    /*
    * testPceccLabelObjectParserHitLengthCheck
    * Description :- Test Label Object length check in PceccLabelObjectParser.
    */
    @Test
    public void testPceccLabelObjectParserHitLengthCheck() throws PCEPDeserializerException {
        try {
            final PceccLabelObjectParser parser =
                    new PceccLabelObjectParser(this.tlvRegistry, this.viTlvRegistry);
            final ByteBuf result = Unpooled.wrappedBuffer(PceccLabelObjectBytes);
            parser.parseObject(new ObjectHeaderImpl(false, false), result.slice(4, result.readableBytes() - 4));
            fail();
        } catch (final PCEPDeserializerException e) {
            assertTrue(e.getMessage().contains("Wrong length of array of bytes. Passed: 4; Expected: >=8."));
        }
    }

    /*
    * testPceccLabelObjectParserHitLabelCheck
    * Description :- Test mandatory field(Label Number) in label Object.
    */
    @Test
    public void testPceccLabelObjectParserHitLabelCheck() throws Exception {
        try {
            final PceccLabelObjectParser parser =
                    new PceccLabelObjectParser(this.tlvRegistry, this.viTlvRegistry);
            final ByteBuf buffer = Unpooled.buffer();
            final LabelBuilder builder = new LabelBuilder();
            parser.serializeObject(builder.build(), buffer);
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains("Label Number is mandatory."));
        }
    }

    /*
    * testPceccLabelObjectParserHitNullTlvCheck
    * Description :- Test Label Object null Tlv check in PceccLabelObjectParser.
    */
    @Test
    public void testPceccLabelObjectParserHitNullTlvCheck() {
        final PceccLabelObjectParser parser =
                new PceccLabelObjectParser(this.tlvRegistry, this.viTlvRegistry);
        final ByteBuf buffer = Unpooled.buffer();
        final LabelBuilder builder = new LabelBuilder();
        builder.setLabelNum(new LabelNumber(5001L));
        parser.serializeObject(builder.build(), buffer);
    }

    /*
    * testPceccOpenObjectParserHitSessionIdCheck
    * Description :- Test mandatory field(SessionId) in Open Object.
    */
    @Test
    public void testPceccOpenObjectParserHitSessionIdCheck() throws Exception {
        try {
            final PcepOpenObjectWithPceccTlvParser parser =
                    new PcepOpenObjectWithPceccTlvParser(this.tlvRegistry, this.viTlvRegistry);
            final ByteBuf buffer = Unpooled.buffer();
            final OpenBuilder builder = new OpenBuilder();
            parser.serializeObject(builder.build(), buffer);
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains("SessionId is mandatory."));
        }
    }

    /*
    * testPceccOpenObjectParserHitNullTlvCheck
    * Description :- Test Open Object with Pcecc null Tlv check in PcepOpenObjectWithPceccTlvParser.
    */
    @Test
    public void testPceccOpenObjectParserHitNullTlvCheck() {
        final PcepOpenObjectWithPceccTlvParser parser =
                new PcepOpenObjectWithPceccTlvParser(this.tlvRegistry, this.viTlvRegistry);
        final ByteBuf buffer = Unpooled.buffer();
        final OpenBuilder builder = new OpenBuilder();
        builder.setSessionId((short) 1);
        parser.serializeObject(builder.build(), buffer);
    }

    /*
    * testPcepOpenObjectWithLengthCheck
    * Description :-Test FecIpv4 Object length check in PcepOpenObjectWithPceccTlvParser.
    */
    @Test
    public void testPcepOpenObjectWithLengthCheck() throws PCEPDeserializerException {
        try {
            final PcepOpenObjectWithPceccTlvParser parser =
                    new PcepOpenObjectWithPceccTlvParser(this.tlvRegistry, this.viTlvRegistry);
            final ByteBuf result = Unpooled.wrappedBuffer(openObjectBytes);
            parser.parseObject(new ObjectHeaderImpl(false, false), result.slice(4, result.readableBytes() - 4));
            fail();
        } catch (final PCEPDeserializerException e) {
            assertTrue(e.getMessage().contains("Wrong length of array of bytes. Passed: 2; Expected: >= 4."));
        }
    }

    /*
    * testPceccLabelUpdateMessageParser
    * Description :- Test LabelUpdateMessage Object null message check in PceccLabelUpdateMessageParser.
    */
    @Test
    public void testPceccLabelUpdateMessageParser() throws IOException, PCEPDeserializerException {
        try {
            final PceccLabelUpdateMessageParser parser = new PceccLabelUpdateMessageParser(this.ctx.getObjectHandlerRegistry());

            ByteBuf result = Unpooled.wrappedBuffer(PceccLabelTlvBytes);
            parser.parseMessage(result.slice(4,
                    result.readableBytes() - 4), Collections.<Message> emptyList());

            fail();
        } catch (final PCEPDeserializerException e) {
            assertTrue(e.getMessage().contains("PcLabelUpt message cannot be empty."));
        }
    }

    /**
     * testPceccLabelUpdateMessageParserError
     * Description :- Test Error condition when SRP object is missing in LabelUpdateMessage Object.
     * Srp Object missing
     * error type SRP_MISSING(6, 10)
     */
    @Test
    public void testPceccLabelUpdateMessageParserError() throws IOException, PCEPDeserializerException {
        try(PceccActivator a = new PceccActivator()) {
            a.start(ctx);
            final PceccLabelUpdateMessageParser parser = new PceccLabelUpdateMessageParser(this.ctx.getObjectHandlerRegistry());

            ByteBuf result = Unpooled.wrappedBuffer(PceccLabelUpdateMessageParserError);
            final List<Message> errors = Lists.newArrayList();

            final PcerrMessageBuilder errMsgBuilder = new PcerrMessageBuilder();
            errMsgBuilder.setErrors(Lists.newArrayList(new ErrorsBuilder().setErrorObject(
                    new ErrorObjectBuilder().setType((short) 6).setValue((short) 10).build()).build()));
            final PcerrBuilder builder = new PcerrBuilder();
            builder.setPcerrMessage(errMsgBuilder.build());
            parser.parseMessage(result.slice(4,
                    result.readableBytes() - 4), errors);
            assertFalse(errors.isEmpty());
            assertEquals(new PcerrBuilder().setPcerrMessage(errMsgBuilder.build()).build(), errors.get(0));
        }
    }

}
