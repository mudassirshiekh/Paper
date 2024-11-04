package io.papermc.generator.rewriter.types.registry;

import com.google.common.base.Suppliers;
import io.papermc.generator.Main;
import io.papermc.generator.rewriter.utils.Annotations;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.RegistryUtils;
import io.papermc.generator.utils.experimental.FlagHolders;
import io.papermc.generator.utils.experimental.SingleFlagHolder;
import io.papermc.typewriter.preset.EnumRewriter;
import io.papermc.typewriter.preset.model.EnumValue;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlags;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static io.papermc.typewriter.utils.Formatting.quoted;

@NullMarked
public class EnumRegistryRewriter<T> extends EnumRewriter<Holder.Reference<T>> {

    private final Registry<T> registry;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;
    private final boolean hasKeyArgument;

    public EnumRegistryRewriter(ResourceKey<? extends Registry<T>> registryKey) {
        this(registryKey, true);
    }

    protected EnumRegistryRewriter(ResourceKey<? extends Registry<T>> registryKey, boolean hasKeyArgument) {
        this.registry = Main.REGISTRY_ACCESS.lookupOrThrow(registryKey);
        this.experimentalKeys = Suppliers.memoize(() -> RegistryUtils.collectExperimentalDataDrivenKeys(this.registry));
        this.isFilteredRegistry = FeatureElement.FILTERED_REGISTRIES.contains(registryKey);
        this.hasKeyArgument = hasKeyArgument;
    }

    @Override
    protected Iterable<Holder.Reference<T>> getValues() {
        return this.registry.listElements().sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath()))::iterator;
    }

    @Override
    protected EnumValue.Builder rewriteEnumValue(Holder.Reference<T> reference) {
        EnumValue.Builder value = EnumValue.builder(Formatting.formatKeyAsField(reference.key().location().getPath()));
        if (this.hasKeyArgument) {
            value.argument(quoted(reference.key().location().getPath()));
        }
        return value;
    }

    @Override
    protected void appendEnumValue(Holder.Reference<T> reference, StringBuilder builder, String indent, boolean reachEnd) {
        // experimental annotation
        SingleFlagHolder requiredFeature = this.getRequiredFeature(reference);
        if (requiredFeature != null) {
            Annotations.experimentalAnnotations(builder, indent, this.importCollector, requiredFeature);
        }

        super.appendEnumValue(reference, builder, indent, reachEnd);
    }

    protected @Nullable SingleFlagHolder getRequiredFeature(Holder.Reference<T> reference) {
        if (this.isFilteredRegistry) {
            // built-in registry
            FeatureElement element = (FeatureElement) reference.value();
            if (FeatureFlags.isExperimental(element.requiredFeatures())) {
                return SingleFlagHolder.fromSet(element.requiredFeatures());
            }
        } else {
            // data-driven registry
            if (this.experimentalKeys.get().contains(reference.key())) {
                return FlagHolders.NEXT_UPDATE;
            }
        }
        return null;
    }
}
