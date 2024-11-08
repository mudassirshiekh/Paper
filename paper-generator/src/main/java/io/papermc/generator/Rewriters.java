package io.papermc.generator;

import io.papermc.generator.registry.RegistryBootstrapper;
import io.papermc.generator.registry.RegistryEntries;
import io.papermc.generator.rewriter.registration.PatternSourceSetRewriter;
import io.papermc.generator.rewriter.types.Types;
import io.papermc.generator.rewriter.types.registry.EnumRegistryRewriter;
import io.papermc.generator.rewriter.types.registry.FeatureFlagRewriter;
import io.papermc.generator.rewriter.types.registry.RegistryFieldRewriter;
import io.papermc.generator.rewriter.types.registry.RegistryTagRewriter;
import io.papermc.generator.rewriter.types.registry.TagRewriter;
import io.papermc.generator.rewriter.types.simple.BlockTypeRewriter;
import io.papermc.generator.rewriter.types.simple.CraftBlockDataMapping;
import io.papermc.generator.rewriter.types.simple.CraftBlockEntityStateMapping;
import io.papermc.generator.rewriter.types.simple.CraftPotionUtilRewriter;
import io.papermc.generator.rewriter.types.simple.EntityTypeRewriter;
import io.papermc.generator.rewriter.types.simple.ItemTypeRewriter;
import io.papermc.generator.rewriter.types.simple.MapPaletteRewriter;
import io.papermc.generator.rewriter.types.simple.MaterialRewriter;
import io.papermc.generator.rewriter.types.simple.MemoryKeyRewriter;
import io.papermc.generator.rewriter.types.registry.PaperFeatureFlagMapping;
import io.papermc.generator.rewriter.types.simple.StatisticRewriter;
import io.papermc.generator.rewriter.types.simple.VillagerProfessionRewriter;
import io.papermc.generator.utils.Formatting;
import io.papermc.typewriter.preset.EnumCloneRewriter;
import io.papermc.typewriter.preset.model.EnumValue;
import java.util.Map;
import javax.lang.model.SourceVersion;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Rarity;
import org.bukkit.Art;
import org.bukkit.FeatureFlag;
import org.bukkit.Fluid;
import org.bukkit.GameEvent;
import org.bukkit.JukeboxSong;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockType;
import org.bukkit.block.banner.PatternType;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapPalette;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.tag.DamageTypeTags;
import org.jspecify.annotations.NullMarked;

import static io.papermc.generator.rewriter.registration.PaperPatternSourceSetRewriter.composite;
import static io.papermc.generator.rewriter.registration.RewriterHolder.holder;
import static io.papermc.typewriter.util.Formatting.quoted;

@NullMarked
public final class Rewriters {

    public static void bootstrap(PatternSourceSetRewriter apiSourceSet, PatternSourceSetRewriter serverSourceSet) {
        apiSourceSet
            .register("PotionType", PotionType.class, new EnumRegistryRewriter<>(Registries.POTION))
            .register("EntityType", EntityType.class, new EntityTypeRewriter())
            .register("DisplaySlot", DisplaySlot.class, new EnumCloneRewriter<>(net.minecraft.world.scores.DisplaySlot.class) {
                @Override
                protected EnumValue.Builder rewriteEnumValue(net.minecraft.world.scores.DisplaySlot slot) {
                    final String name;
                    if (slot == net.minecraft.world.scores.DisplaySlot.LIST) {
                        name = "PLAYER_LIST";
                    } else {
                        name = Formatting.formatKeyAsField(slot.getSerializedName());
                    }

                    return EnumValue.builder(name).argument(quoted(slot.getSerializedName()));
                }
            })
            .register("SnifferState", Sniffer.State.class, new EnumCloneRewriter<>(net.minecraft.world.entity.animal.sniffer.Sniffer.State.class))
            .register("PandaGene", Panda.Gene.class, new EnumCloneRewriter<>(net.minecraft.world.entity.animal.Panda.Gene.class) {
                @Override
                protected EnumValue.Builder rewriteEnumValue(net.minecraft.world.entity.animal.Panda.Gene gene) {
                    return super.rewriteEnumValue(gene).argument(String.valueOf(gene.isRecessive()));
                }
            })
            .register("CookingBookCategory", CookingBookCategory.class, new EnumCloneRewriter<>(net.minecraft.world.item.crafting.CookingBookCategory.class))
            .register("CraftingBookCategory", CraftingBookCategory.class, new EnumCloneRewriter<>(net.minecraft.world.item.crafting.CraftingBookCategory.class))
            .register("TropicalFishPattern", TropicalFish.Pattern.class, new EnumCloneRewriter<>(net.minecraft.world.entity.animal.TropicalFish.Pattern.class))
            .register("BoatStatus", Boat.Status.class, new EnumCloneRewriter<>(net.minecraft.world.entity.vehicle.Boat.Status.class))
            .register("FoxType", Fox.Type.class, new EnumCloneRewriter<>(net.minecraft.world.entity.animal.Fox.Variant.class))
            .register("SalmonVariant", Salmon.Variant.class, new EnumCloneRewriter<>(net.minecraft.world.entity.animal.Salmon.Variant.class))
            .register("ItemRarity", ItemRarity.class, new EnumCloneRewriter<>(Rarity.class) {
                @Override
                protected EnumValue.Builder rewriteEnumValue(Rarity rarity) {
                    return super.rewriteEnumValue(rarity).argument(
                        "%s.%s".formatted(NamedTextColor.class.getCanonicalName(), rarity.color().name())
                    );
                }
            })
            .register(Material.class, composite(
                holder("Blocks", new MaterialRewriter.Blocks()),
                //holder("Material#isTransparent", MaterialRewriter.IsTransparent()),

                holder("Items", new MaterialRewriter.Items()),
                holder("Material#getEquipmentSlot", new MaterialRewriter.GetEquipmentSlot())
            ))
            .register(Statistic.class, composite(
                holder("StatisticCustom", new StatisticRewriter.Custom()),
                holder("StatisticType", new StatisticRewriter.Type())
            ))
            .register(Villager.class, composite(
                holder("VillagerType", Villager.Type.class, new RegistryFieldRewriter<>(Registries.VILLAGER_TYPE, "getType")),
                holder("VillagerProfession", Villager.Profession.class, new VillagerProfessionRewriter())
            ))
            .register("JukeboxSong", JukeboxSong.class, new RegistryFieldRewriter<>(Registries.JUKEBOX_SONG, "get") {
                @Override
                protected String rewriteFieldName(Holder.Reference<net.minecraft.world.item.JukeboxSong> reference) {
                    String keyedName = super.rewriteFieldName(reference);
                    if (!SourceVersion.isIdentifier(keyedName)) {
                        // fallback to field names for invalid identifier (happens for 5, 11, 13 etc.)
                        return RegistryEntries.byRegistryKey(Registries.JUKEBOX_SONG).getFieldNames().get(reference.key());
                    }
                    return keyedName;
                }
            })
            .register("DamageTypeTags", DamageTypeTags.class, new RegistryTagRewriter<>(Registries.DAMAGE_TYPE, DamageType.class) {

                @Deprecated // spigot mistake
                private static final Map<String, String> RENAMES = Map.of(
                    "MACE_SMASH", "IS_MACE_SMASH"
                );

                @Override
                protected String rewriteFieldName(TagKey<net.minecraft.world.damagesource.DamageType> tagKey) {
                    String keyedName = super.rewriteFieldName(tagKey);
                    return RENAMES.getOrDefault(keyedName, keyedName);
                }
            })
            .register("MapCursorType", MapCursor.Type.class, new RegistryFieldRewriter<>(Registries.MAP_DECORATION_TYPE, "getType"))
            .register("Structure", Structure.class, new RegistryFieldRewriter<>(Registries.STRUCTURE, "getStructure"))
            .register("StructureType", StructureType.class, new RegistryFieldRewriter<>(Registries.STRUCTURE_TYPE, "getStructureType"))
            .register("TrimPattern", TrimPattern.class, new RegistryFieldRewriter<>(Registries.TRIM_PATTERN, "getTrimPattern"))
            .register("TrimMaterial", TrimMaterial.class, new RegistryFieldRewriter<>(Registries.TRIM_MATERIAL, "getTrimMaterial"))
            .register("DamageType", DamageType.class, new RegistryFieldRewriter<>(Registries.DAMAGE_TYPE, "getDamageType"))
            .register("GameEvent", GameEvent.class, new RegistryFieldRewriter<>(Registries.GAME_EVENT, "getEvent"))
            .register("MusicInstrument", MusicInstrument.class, new RegistryFieldRewriter<>(Registries.INSTRUMENT, "getInstrument"))
            .register("WolfVariant", Wolf.Variant.class, new RegistryFieldRewriter<>(Registries.WOLF_VARIANT, "getVariant"))
            .register("CatType", Cat.Type.class, new RegistryFieldRewriter<>(Registries.CAT_VARIANT, "getType"))
            .register("FrogVariant", Frog.Variant.class, new RegistryFieldRewriter<>(Registries.FROG_VARIANT, "getVariant"))
            .register("PatternType", PatternType.class, new RegistryFieldRewriter<>(Registries.BANNER_PATTERN, "getType"))
            .register("Biome", Biome.class, new RegistryFieldRewriter<>(Registries.BIOME, "getBiome"))
            .register("Fluid", Fluid.class, new RegistryFieldRewriter<>(Registries.FLUID, "getFluid"))
            .register("Attribute", Attribute.class, new RegistryFieldRewriter<>(Registries.ATTRIBUTE, "getAttribute"))
            .register("Sound", Sound.class, new RegistryFieldRewriter<>(Registries.SOUND_EVENT, "getSound"))
            .register("Art", Art.class, new RegistryFieldRewriter<>(Registries.PAINTING_VARIANT, "getArt"))
            .register("MemoryKey", MemoryKey.class, new MemoryKeyRewriter())
            .register("ItemType", ItemType.class, new ItemTypeRewriter())
            .register("BlockType", BlockType.class, new BlockTypeRewriter())
            .register("FeatureFlag", FeatureFlag.class, new FeatureFlagRewriter())
            .register("Tag", Tag.class, new TagRewriter())
            .register("MapPalette#colors", MapPalette.class, new MapPaletteRewriter());

        serverSourceSet
            .register("CraftBlockData#MAP", Types.CRAFT_BLOCK_DATA, new CraftBlockDataMapping())
            .register("CraftBlockEntityStates", Types.CRAFT_BLOCK_STATES, new CraftBlockEntityStateMapping())
            .register(Types.CRAFT_STATISTIC, composite(
                holder("CraftStatisticCustom", new StatisticRewriter.CraftCustom()),
                holder("CraftStatisticType", new StatisticRewriter.CraftType())
            ))
            .register(Types.CRAFT_POTION_UTIL, composite(
                holder("CraftPotionUtil#upgradeable", new CraftPotionUtilRewriter("strong")),
                holder("CraftPotionUtil#extendable", new CraftPotionUtilRewriter("long"))
            ))
            .register("PaperFeatureFlagProviderImpl#FLAGS", Types.PAPER_FEATURE_FLAG_PROVIDER_IMPL, new PaperFeatureFlagMapping());
        RegistryBootstrapper.bootstrap(apiSourceSet, serverSourceSet);
    }
}
