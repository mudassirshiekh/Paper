package io.papermc.generator.types.craftblockdata.property;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.papermc.generator.types.StructuredGenerator;
import io.papermc.generator.types.craftblockdata.property.appender.AppenderBase;
import io.papermc.generator.types.craftblockdata.property.appender.EnumValuesAppender;
import io.papermc.generator.types.craftblockdata.property.appender.PropertyAppender;
import io.papermc.generator.utils.BlockStateMapping;
import io.papermc.generator.utils.NamingManager;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PropertyWriter<T extends Comparable<T>> implements PropertyMaker {

    protected final Property<T> property;
    private final Supplier<Class<?>> apiClassSupplier;

    protected PropertyWriter(Property<T> property) {
        this.property = property;
        this.apiClassSupplier = Suppliers.memoize(this::processApiType);
    }

    @Override
    public TypeName getPropertyType() {
        return TypeName.get(this.property.getClass());
    }

    protected Class<?> processApiType() {
        Class<T> apiClass = this.property.getValueClass();
        if (Primitives.isWrapperType(apiClass)) {
            apiClass = Primitives.unwrap(apiClass);
        }
        return apiClass;
    }

    @Override
    public Class<?> getApiType() {
        return this.apiClassSupplier.get();
    }

    @Override
    public String rawSetExprent() {
        return "this.set(%s, $N)";
    }

    @Override
    public String rawGetExprent() {
        return "this.get(%s)";
    }

    private static final Map<Property<?>, AppenderBase> APPENDERS;
    private static final ImmutableMap.Builder<Property<?>, AppenderBase> builder = ImmutableMap.builder();

    static {
        register(new EnumValuesAppender<>(BlockStateProperties.AXIS, Axis.class, "getAxes"));
        register(new EnumValuesAppender<>(BlockStateProperties.HORIZONTAL_AXIS, Axis.class, "getAxes"));
        register(new EnumValuesAppender<>(BlockStateProperties.FACING, BlockFace.class, "getFaces"));
        register(new EnumValuesAppender<>(BlockStateProperties.HORIZONTAL_FACING, BlockFace.class, "getFaces"));
        register(new EnumValuesAppender<>(BlockStateProperties.FACING_HOPPER, BlockFace.class, "getFaces"));
        register(new EnumValuesAppender<>(BlockStateProperties.RAIL_SHAPE, Rail.Shape.class, "getShapes"));
        register(new EnumValuesAppender<>(BlockStateProperties.RAIL_SHAPE_STRAIGHT, Rail.Shape.class, "getShapes"));
        register(new EnumValuesAppender<>(BlockStateProperties.VERTICAL_DIRECTION, BlockFace.class, "getVerticalDirections"));
        APPENDERS = builder.build();
    }

    private static void register(PropertyAppender<? extends Comparable<?>, ?> converter) {
        builder.put(converter.getProperty(), converter);
    }

    @Override
    public void addExtras(TypeSpec.Builder builder, FieldSpec field, StructuredGenerator<?> generator, NamingManager naming) {
        if (APPENDERS.containsKey(this.property)) {
            APPENDERS.get(this.property).addExtras(builder, field, generator, naming);
        }
    }

    public static Pair<Class<?>, String> referenceField(Class<? extends Block> from, Property<?> property, Map<Property<?>, Field> fields) {
        Class<?> fieldAccess = from;
        Field field = fields.get(property);
        if (field == null || !Modifier.isPublic(field.getModifiers())) {
            fieldAccess = BlockStateProperties.class;
            field = BlockStateMapping.FALLBACK_GENERIC_FIELDS.get(property);
        }
        return Pair.of(fieldAccess, field.getName());
    }

    public static Pair<Class<?>, String> referenceFieldFromVar(Class<? extends Block> from, Property<?> property, Map<Property<?>, Field> fields) {
        Class<?> fieldAccess = from;
        Field field = fields.get(property);
        Field genericField = BlockStateMapping.FALLBACK_GENERIC_FIELDS.get(property);
        if (field == null || !Modifier.isPublic(field.getModifiers()) || !genericField.getType().equals(field.getType())) {
            // field type can differ from BlockStateProperties constants (that's the case for the shulker box (#FACING) and the vault (#STATE)) ref: 1.20.5
            // in that case fallback to the more accurate type to avoid compile error
            fieldAccess = BlockStateProperties.class;
            field = genericField;
        }
        return Pair.of(fieldAccess, field.getName());
    }
}
