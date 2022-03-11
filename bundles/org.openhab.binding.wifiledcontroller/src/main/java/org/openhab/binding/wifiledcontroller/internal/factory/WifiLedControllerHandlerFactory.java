/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.wifiledcontroller.internal.factory;

import static org.openhab.binding.wifiledcontroller.internal.WifiLedControllerBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wifiledcontroller.internal.WifiLedControllerDynamicStateDescriptionProvider;
import org.openhab.binding.wifiledcontroller.internal.handler.WifiLedControllerHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link WifiLedControllerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin T Phelan - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wifiledcontroller", service = ThingHandlerFactory.class)
public class WifiLedControllerHandlerFactory extends BaseThingHandlerFactory {
    private final WifiLedControllerDynamicStateDescriptionProvider stateDescriptionProvider;

    @Activate
    public WifiLedControllerHandlerFactory(@Reference WifiLedControllerDynamicStateDescriptionProvider provider) {
        this.stateDescriptionProvider = provider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_HF_LPB100_ZJ002)) {
            return new WifiLedControllerHandler(thing, stateDescriptionProvider, DEVICE_HF_LPB100_ZJ002);
        } else if (thingTypeUID.equals(THING_TYPE_AK001_ZJ200)) {
            return new WifiLedControllerHandler(thing, stateDescriptionProvider, DEVICE_AK001_ZJ200);
        } else if (thingTypeUID.equals(THING_TYPE_HF_LPB100_ZJ200)) {
            return new WifiLedControllerHandler(thing, stateDescriptionProvider, DEVICE_HF_LPB100_ZJ200);
        } else if (thingTypeUID.equals(THING_TYPE_HF_A11_ZJ370)) {
            return new WifiLedControllerHandler(thing, stateDescriptionProvider, DEVICE_HF_A11_ZJ370);
        }
        return null;
    }

}
