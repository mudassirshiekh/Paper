package io.papermc.generator.utils;

import com.google.common.collect.Sets;
import io.papermc.generator.utils.experimental.CollectingContext;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.registries.WinterDropRegistries;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RegistryUtils {

    private static final Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryBootstrap<?>> VANILLA_REGISTRY_ENTRIES = VanillaRegistries.BUILDER.entries.stream()
        .collect(Collectors.toMap(RegistrySetBuilder.RegistryStub::key, RegistrySetBuilder.RegistryStub::bootstrap));

    private static final Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryBootstrap<?>> EXPERIMENTAL_REGISTRY_ENTRIES = WinterDropRegistries.BUILDER.entries.stream()
        .collect(Collectors.toMap(RegistrySetBuilder.RegistryStub::key, RegistrySetBuilder.RegistryStub::bootstrap)); // Update for Experimental API

    @SuppressWarnings("unchecked")
    public static <T> Set<ResourceKey<T>> collectExperimentalDataDrivenKeys(Registry<T> registry) {
        RegistrySetBuilder.@Nullable RegistryBootstrap<T> experimentalBootstrap = (RegistrySetBuilder.RegistryBootstrap<T>) EXPERIMENTAL_REGISTRY_ENTRIES.get(registry.key());
        if (experimentalBootstrap == null) {
            return Collections.emptySet();
        }
        Set<ResourceKey<T>> experimental = Collections.newSetFromMap(new IdentityHashMap<>());
        CollectingContext<T> experimentalCollector = new CollectingContext<>(experimental, registry);
        experimentalBootstrap.run(experimentalCollector);

        RegistrySetBuilder.@Nullable RegistryBootstrap<T> vanillaBootstrap = (RegistrySetBuilder.RegistryBootstrap<T>) VANILLA_REGISTRY_ENTRIES.get(registry.key());
        if (vanillaBootstrap != null) {
            Set<ResourceKey<T>> vanilla = Collections.newSetFromMap(new IdentityHashMap<>());
            CollectingContext<T> vanillaCollector = new CollectingContext<>(vanilla, registry);
            vanillaBootstrap.run(vanillaCollector);
            return Sets.difference(experimental, vanilla);
        }
        return experimental;
    }
}
