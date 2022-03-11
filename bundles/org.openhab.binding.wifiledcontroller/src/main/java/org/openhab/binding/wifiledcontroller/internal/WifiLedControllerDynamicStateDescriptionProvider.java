package org.openhab.binding.wifiledcontroller.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = { DynamicStateDescriptionProvider.class, WifiLedControllerDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class WifiLedControllerDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    @Activate
    public WifiLedControllerDynamicStateDescriptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }
}
// @Component(service = { DynamicStateDescriptionProvider.class, WifiLedControllerDynamicStateDescriptionProvider.class
// })
// public class WifiLedControllerDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {
//
// private final Map<ChannelUID, @Nullable List<StateOption>> channelOptionsMap = new ConcurrentHashMap<>();
//
// /**
// * For a given channel UID, set a {@link List} of {@link StateOption}s that should be used for the channel, instead
// * of the one defined statically in the {@link ChannelType}.
// *
// * @param channelUID the channel UID of the channel
// * @param options a {@link List} of {@link StateOption}s
// */
// public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
// channelOptionsMap.put(channelUID, options);
// }
//
// @Override
// public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
// @Nullable Locale locale) {
// List<StateOption> options = channelOptionsMap.get(channel.getUID());
// if (options == null) {
// return null;
// }
//
// StateDescriptionFragmentBuilder builder = (original == null) ? StateDescriptionFragmentBuilder.create()
// : StateDescriptionFragmentBuilder.create(original);
// return builder.withOptions(options).build().toStateDescription();
// }
//
// @Deactivate
// public void deactivate() {
// channelOptionsMap.clear();
// }
// }
