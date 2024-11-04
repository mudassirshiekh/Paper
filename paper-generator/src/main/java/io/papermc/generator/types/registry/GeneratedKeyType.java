package io.papermc.generator.types.registry;

import com.google.common.base.Suppliers;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.papermc.generator.registry.RegistryEntry;
import io.papermc.generator.types.SimpleGenerator;
import io.papermc.generator.utils.Annotations;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.Javadocs;
import io.papermc.generator.utils.RegistryUtils;
import io.papermc.generator.utils.experimental.FlagHolders;
import io.papermc.generator.utils.experimental.SingleFlagHolder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import java.util.Set;
import java.util.function.Supplier;
import javax.lang.model.SourceVersion;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlags;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static io.papermc.generator.utils.Annotations.EXPERIMENTAL_API_ANNOTATION;
import static io.papermc.generator.utils.Annotations.experimentalAnnotations;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@NullMarked
public class GeneratedKeyType<T> extends SimpleGenerator {

    private final RegistryEntry<T> entry;
    private final Registry<T> registry;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;

    public GeneratedKeyType(String packageName, RegistryEntry<T> entry) {
        super(entry.keyClassName().concat("Keys"), packageName);
        this.entry = entry;
        this.registry = entry.registry();
        this.experimentalKeys = Suppliers.memoize(() -> RegistryUtils.collectExperimentalDataDrivenKeys(this.registry));
        this.isFilteredRegistry = FeatureElement.FILTERED_REGISTRIES.contains(entry.registryKey());
    }

    private MethodSpec.Builder createMethod(TypeName returnType) {
        boolean publicCreateKeyMethod = this.entry.allowCustomKeys();

        ParameterSpec keyParam = ParameterSpec.builder(Key.class, "key", FINAL).build();
        MethodSpec.Builder create = MethodSpec.methodBuilder("create")
            .addModifiers(publicCreateKeyMethod ? PUBLIC : PRIVATE, STATIC)
            .addParameter(keyParam)
            .addCode("return $T.create($T.$L, $N);", TypedKey.class, RegistryKey.class, this.entry.registryKeyField(), keyParam)
            .returns(returnType);
        if (publicCreateKeyMethod) {
            create.addAnnotation(EXPERIMENTAL_API_ANNOTATION); // TODO remove once not experimental
            create.addJavadoc(Javadocs.CREATE_TYPED_KEY_JAVADOC, this.entry.apiClass(), this.registry.key().location().toString());
        }
        return create;
    }

    private TypeSpec.Builder keyHolderType() {
        return classBuilder(this.className)
            .addModifiers(PUBLIC, FINAL)
            .addJavadoc(Javadocs.getVersionDependentClassHeader("keys", "{@link $T#$L}"), RegistryKey.class, this.entry.registryKeyField())
            .addAnnotations(Annotations.CLASS_HEADER)
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(PRIVATE)
                .build()
            );
    }

    @Override
    protected TypeSpec getTypeSpec() {
        TypeName typedKeyType = ParameterizedTypeName.get(TypedKey.class, this.entry.apiClass());

        TypeSpec.Builder typeBuilder = this.keyHolderType();
        MethodSpec.Builder createMethod = this.createMethod(typedKeyType);

        boolean allExperimental = true;
        for (Holder.Reference<T> reference : this.registry.listElements().sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).toList()) {
            ResourceKey<T> key = reference.key();
            String keyPath = key.location().getPath();
            String fieldName = Formatting.formatKeyAsField(keyPath);
            if (!SourceVersion.isIdentifier(fieldName) && this.entry.getFieldNames().containsKey(key)) {
                fieldName = this.entry.getFieldNames().get(key);
            }

            FieldSpec.Builder fieldBuilder = FieldSpec.builder(typedKeyType, fieldName, PUBLIC, STATIC, FINAL)
                .initializer("$N(key($S))", createMethod.build(), keyPath)
                .addJavadoc(Javadocs.getVersionDependentField("{@code $L}"), key.location().toString());

            SingleFlagHolder requiredFeature = this.getRequiredFeature(reference);
            if (requiredFeature != null) {
                fieldBuilder.addAnnotations(experimentalAnnotations(requiredFeature));
            } else {
                allExperimental = false;
            }
            typeBuilder.addField(fieldBuilder.build());
        }

        if (allExperimental) {
            typeBuilder.addAnnotation(EXPERIMENTAL_API_ANNOTATION);
            createMethod.addAnnotation(EXPERIMENTAL_API_ANNOTATION);
        } else {
            typeBuilder.addAnnotation(EXPERIMENTAL_API_ANNOTATION); // TODO experimental API
        }
        return typeBuilder.addMethod(createMethod.build()).build();
    }

    @Override
    protected JavaFile.Builder file(JavaFile.Builder builder) {
        return builder.addStaticImport(Key.class, "key");
    }

    public @Nullable SingleFlagHolder getRequiredFeature(Holder.Reference<T> reference) {
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
