/*
 * Copyright (c) 2016 Huawei Technologies Co. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.pcecc;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.protocol.pcep.spi.AbstractMessageParser;
import org.opendaylight.protocol.pcep.spi.MessageUtil;
import org.opendaylight.protocol.pcep.spi.ObjectRegistry;
import org.opendaylight.protocol.pcep.spi.PCEPDeserializerException;
import org.opendaylight.protocol.pcep.spi.PCEPErrors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.Lsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.srp.object.Srp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.Pclabelupd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.PclabelupdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.PclabelupdMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.fec.object.Fec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pclabelupd.message.PclabelupdMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pclabelupd.message.pclabelupd.message.PceLabelUpdates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pclabelupd.message.pclabelupd.message.pce.label.updates.pce.label.update.pce.label.download._case.PceLabelDownload;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pclabelupd.message.pclabelupd.message.pce.label.updates.pce.label.update.pce.label.download._case.PceLabelDownloadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pclabelupd.message.pclabelupd.message.pce.label.updates.pce.label.update.pce.label.download._case.pce.label.download.Label;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pclabelupd.message.pclabelupd.message.pce.label.updates.pce.label.update.pce.label.map._case.PceLabelMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.pclabelupd.message.pclabelupd.message.pce.label.updates.pce.label.update.pce.label.map._case.PceLabelMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Object;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.rp.object.Rp;

/**
 * Parser for {@link PclabelupdMessage}
 */
public class PceccLabelUpdateMessageParser extends AbstractMessageParser {

    public static final int TYPE = 226;

    public PceccLabelUpdateMessageParser(final ObjectRegistry registry) {
        super(registry);
    }

    @Override
    public void serializeMessage(final Message message, final ByteBuf out) {
        Preconditions.checkArgument(message instanceof PclabelupdMessage,
                "Wrong instance of Message. Passed instance of %s. Need PclabelupdMessage.", message.getClass());

        final Pclabelupd msg = (Pclabelupd) message;
        final ByteBuf buffer = Unpooled.buffer();
        final List<PceLabelUpdates> labelUpdates = msg.getPclabelupdMessage().getPceLabelUpdates();

        for (final PceLabelUpdates labelUpdate : labelUpdates) {
            serializeLabelUpdate(labelUpdate, buffer);
        }
        MessageUtil.formatMessage(TYPE, buffer, out);
    }

    protected void serializeLabelUpdate(final PceLabelUpdates labelUpdate, final ByteBuf buffer) {

        //If label download
        if (labelUpdate.getPceLabelUpdate() instanceof PceLabelDownload) {
            final PceLabelDownload labelDownload = (PceLabelDownload) labelUpdate.getPceLabelUpdate();

            serializeObject(labelDownload.getSrp(), buffer);
            serializeObject(labelDownload.getLsp(), buffer);

            if (labelDownload.getLabel() != null) {
                for (final Label label : labelDownload.getLabel()) {
                    serializeObject(label.getLabel(), buffer);
                }
            }
        }
        //if label map
        else if (labelUpdate.getPceLabelUpdate() instanceof PceLabelMap) {
            final PceLabelMap labelMap = (PceLabelMap) labelUpdate.getPceLabelUpdate();
            serializeObject(labelMap.getSrp(), buffer);
            serializeObject(labelMap.getLabel(), buffer);
            serializeObject(labelMap.getFec(), buffer);
        }

    }

    @Override
    protected Message validate(final List<Object> objects, final List<Message> errors) throws PCEPDeserializerException {
        Preconditions.checkArgument(objects != null, "Passed list can't be null.");
        if (objects.isEmpty()) {
            throw new PCEPDeserializerException("PcLabelUpt message cannot be empty.");
        }

        final List<PceLabelUpdates> labelUpdates = Lists.newArrayList();

        while (!objects.isEmpty()) {
            final PceLabelUpdates pceLabelUpdates = getValidUpdates(objects, errors);
            if (pceLabelUpdates != null) {
                labelUpdates.add(pceLabelUpdates);
            }
        }
        if (!objects.isEmpty()) {
            throw new PCEPDeserializerException("Unprocessed Objects: " + objects);
        }

        return new PclabelupdBuilder().setPclabelupdMessage(new PclabelupdMessageBuilder()
                .setPceLabelUpdates(labelUpdates).build()).build();
    }

    protected PceLabelUpdates getValidUpdates(final List<Object> objects, final List<Message> errors) {

        boolean isValid = true;
        final PceLabelDownloadBuilder labelDownloadBuilder = new PceLabelDownloadBuilder();
        final PceLabelMapBuilder labelMapbuilder = new PceLabelMapBuilder();
        if (objects.get(0) instanceof Srp) {

            if (objects.get(1) instanceof Lsp) {
                labelDownloadBuilder.setSrp((Srp) objects.get(0));
                objects.remove(0);

                labelDownloadBuilder.setLsp((Lsp) objects.get(0));
                objects.remove(0);

                final List<Label> lbl = parseLabels(objects, errors);
                if (!lbl.isEmpty()) {
                    labelDownloadBuilder.setLabel(lbl);
                }
            } else if (objects.get(1) instanceof Label) {
                labelMapbuilder.setSrp((Srp) objects.get(0));
                objects.remove(0);

                labelMapbuilder.setLabel((org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.pcecc.rev160225.label.object.Label) objects.get(0));
                objects.remove(0);
                if (objects.get(0) instanceof Fec)
                {
                    labelMapbuilder.setFec((Fec) objects.get(0));
                    objects.remove(0);
                }
            } else {
                //Log error or send error
                isValid = false;
            }
        } else {
            errors.add(createErrorMsg(PCEPErrors.SRP_MISSING, Optional.<Rp>absent())); //to check
            isValid = false;
        }
        return null;
    }

    protected List<Label> parseLabels(final List<Object> objects, final List<Message> errors) {

        final List<Label> labelList = new ArrayList<>();
        while (!objects.isEmpty()) {
            if (objects instanceof Label) {
                labelList.add((Label)objects);
            }
        }
        return labelList;
    }

}