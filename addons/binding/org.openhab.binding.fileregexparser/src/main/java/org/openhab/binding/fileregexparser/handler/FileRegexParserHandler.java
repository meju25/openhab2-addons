/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fileregexparser.handler;

import static org.openhab.binding.fileregexparser.FileRegexParserBindingConstants.CHANNEL_GROUPCOUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.fileregexparser.internal.FileRegexParserWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FileRegexParserHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author meju25 - Initial contribution
 */
public class FileRegexParserHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FileRegexParserHandler.class);
    ScheduledFuture<?> refreshJob;
    private String fileName;
    private String regEx;
    private static Pattern pattern;
    private static Matcher matcher;
    private FileRegexParserWorker myWorker = new FileRegexParserWorker(this);

    public FileRegexParserHandler(Thing thing) {
        super(thing);
    }

    public void updateStateReceived(String channel, String state) {
        updateState(new ChannelUID(getThing().getUID(), channel), new StringType(state));
    }

    @Override
    public void initialize() {
        logger.debug("Initializing FileRegexParser handler.");
        super.initialize();

        Configuration config = getThing().getConfiguration();

        try {
            fileName = (String) config.get("fileName");

        } catch (Exception e) {
            logger.debug("Cannot set fileName parameter.", e);
        }
        try {
            regEx = (String) config.get("regEx");
            pattern = Pattern.compile(regEx);
        } catch (Exception e) {
            logger.debug("Cannot set regEx parameter.", e);
        }
        matcher = pattern.matcher("");
        matcher.groupCount();
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_GROUPCOUNT), new DecimalType(matcher.groupCount()));
        updateStatus(ThingStatus.ONLINE);
        myWorker.startWorker(fileName, regEx);

    }

    /*
     * protected void thingStructureChanged() {
     * ThingBuilder thingBuilder = editThing();
     * ChannelTypeUID group1 = new ChannelTypeUID(BINDING_ID, "dynamic");
     * Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), "group_1"), "String")
     * .withType(group1).build();
     * thingBuilder.withChannel(channel);
     * updateThing(thingBuilder.build());
     *
     *
     * ChannelTypeUID triggerUID = new ChannelTypeUID(BINDING_ID, "dynamic");
     * Channel mychannel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), "channel_name"), "String")
     * .withType(triggerUID).build();
     * thingBuilder.withChannel(mychannel);
     *
     * }
     */
    protected void thingStructureChanged() {
        String regEx;
        Pattern pattern;
        int groupCount = 0;
        ChannelTypeUID channelType = new ChannelTypeUID("fileregexparser:matchingGroup");
        ThingBuilder myThingBuilder = editThing();
        Configuration config = thing.getConfiguration();

        try {
            fileName = (String) config.get("fileName");

        } catch (Exception e) {
            logger.debug("Cannot set fileName parameter.", e);
        }
        try {
            regEx = (String) config.get("regEx");
            pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher("");
            groupCount = matcher.groupCount();
        } catch (Exception e) {
            logger.debug("Cannot set regEx parameter.", e);
        }

        List<Channel> channels = new ArrayList<Channel>();
        List<Channel> curChannels = new ArrayList<Channel>(thing.getChannels());
        for (int i = 0; i < curChannels.size(); i++) {
            if (!curChannels.get(i).getChannelTypeUID().equals(channelType)) {
                channels.add(curChannels.get(i));
            }
        }

        for (int i = 1; i <= groupCount; i++) {
            Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), "matchingGroup" + i), "String")
                    .withType(channelType).build();
            channels.add(channel);
        }
        myThingBuilder.withChannels(channels);

        updateThing(myThingBuilder.build());
    }

    @Override
    public void dispose() {
        myWorker.stopWorker();
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("Thing updated: " + thing.getUID());
        dispose();
        this.thing = thing;
        initialize();
        this.thingStructureChanged();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        validateConfigurationParameters(configurationParameters);
        logger.debug("Thing ConfigUpdate: " + thing.getUID());
        // can be overridden by subclasses
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        if (thingIsInitialized()) {
            // persist new configuration and reinitialize handler
            dispose();
            updateConfiguration(configuration);
            initialize();
            this.thingStructureChanged();
        } else {
            // persist new configuration and notify Thing Manager
            updateConfiguration(configuration);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }
}
