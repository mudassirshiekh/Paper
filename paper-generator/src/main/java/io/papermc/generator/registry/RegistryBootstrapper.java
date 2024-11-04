package io.papermc.generator.registry;

import io.papermc.generator.rewriter.registration.PatternSourceSetRewriter;
import io.papermc.generator.rewriter.types.Types;
import io.papermc.generator.rewriter.types.registry.PaperRegistriesRewriter;
import io.papermc.generator.rewriter.types.registry.RegistryEventsRewriter;
import io.papermc.generator.types.SourceGenerator;
import io.papermc.generator.types.registry.GeneratedKeyType;
import io.papermc.generator.types.registry.GeneratedTagKeyType;
import io.papermc.paper.registry.event.RegistryEvents;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RegistryBootstrapper {

    private static final String PAPER_REGISTRY_PACKAGE = "io.papermc.paper.registry";

    public static void bootstrap(List<SourceGenerator> generators) {
        // typed/tag keys
        RegistryEntries.forEach(entry -> {
            generators.add(new GeneratedKeyType<>(PAPER_REGISTRY_PACKAGE + ".keys", entry));
            if (entry.registry().listTags().findAny().isPresent()) {
                generators.add(new GeneratedTagKeyType(entry, PAPER_REGISTRY_PACKAGE + ".keys.tags"));
            }
        });
    }

    public static void bootstrap(PatternSourceSetRewriter apiSourceSet, PatternSourceSetRewriter serverSourceSet) {
        apiSourceSet.register("RegistryEvents", RegistryEvents.class, new RegistryEventsRewriter());
        serverSourceSet.register("RegistryDefinitions", Types.PAPER_REGISTRIES, new PaperRegistriesRewriter());
    }
}
