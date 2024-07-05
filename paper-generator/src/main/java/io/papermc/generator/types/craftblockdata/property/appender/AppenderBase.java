package io.papermc.generator.types.craftblockdata.property.appender;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import io.papermc.generator.types.StructuredGenerator;
import io.papermc.generator.utils.NamingManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AppenderBase {

    void addExtras(TypeSpec.Builder builder, FieldSpec field, StructuredGenerator<?> generator, NamingManager naming);
}
