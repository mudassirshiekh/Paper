package io.papermc.generator.utils;

import com.squareup.javapoet.AnnotationSpec;
import io.papermc.generator.utils.experimental.SingleFlagHolder;
import io.papermc.paper.generated.GeneratedFrom;
import java.util.List;
import net.minecraft.SharedConstants;
import org.bukkit.MinecraftExperimental;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Annotations {

    public static List<AnnotationSpec> experimentalAnnotations(SingleFlagHolder requiredFeature) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(MinecraftExperimental.class);
        builder.addMember("value", "$T.$L", MinecraftExperimental.Requires.class, requiredFeature.asAnnotationMember().name());

        return List.of(
            AnnotationSpec.builder(ApiStatus.Experimental.class).build(),
            builder.build()
        );
    }

    public static AnnotationSpec deprecatedVersioned(@Nullable String version, boolean forRemoval) {
        AnnotationSpec.Builder annotationSpec = AnnotationSpec.builder(Deprecated.class);
        if (forRemoval) {
            annotationSpec.addMember("forRemoval", "$L", true);
        }
        if (version != null) {
            annotationSpec.addMember("since", "$S", version);
        }

        return annotationSpec.build();
    }

    public static AnnotationSpec scheduledRemoval(String version) {
        return AnnotationSpec.builder(ApiStatus.ScheduledForRemoval.class)
            .addMember("inVersion", "$S", version)
            .build();
    }

    public static AnnotationSpec suppressWarnings(String... values) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(SuppressWarnings.class);
        for (String value : values) {
            builder.addMember("value", "$S", value);
        }
        return builder.build();
    }

    @ApiStatus.Experimental
    public static final AnnotationSpec EXPERIMENTAL_API_ANNOTATION = AnnotationSpec.builder(ApiStatus.Experimental.class).build();
    public static final AnnotationSpec NULL_MARKED = AnnotationSpec.builder(NullMarked.class).build();
    public static final AnnotationSpec OVERRIDE = AnnotationSpec.builder(Override.class).build();
    private static final AnnotationSpec SUPPRESS_WARNINGS = suppressWarnings("unused", "SpellCheckingInspection");
    public static final AnnotationSpec GENERATED_FROM = AnnotationSpec.builder(GeneratedFrom.class)
        .addMember("value", "$S", SharedConstants.getCurrentVersion().getName())
        .build();
    public static final Iterable<AnnotationSpec> CLASS_HEADER = List.of(
        SUPPRESS_WARNINGS,
        NULL_MARKED,
        GENERATED_FROM
    );

    private Annotations() {
    }
}
