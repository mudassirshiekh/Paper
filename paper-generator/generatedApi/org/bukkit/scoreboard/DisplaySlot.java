package org.bukkit.scoreboard;

import net.kyori.adventure.text.format.NamedTextColor; // Paper
/**
 * Locations for displaying objectives to the player
 */
public enum DisplaySlot {
    // Paper start
    // Paper start - Generated/DisplaySlot
    // @GeneratedFrom 1.21.3
    PLAYER_LIST("list"),
    SIDEBAR("sidebar"),
    BELOW_NAME("below_name"),
    SIDEBAR_TEAM_BLACK("sidebar.team.black"),
    SIDEBAR_TEAM_DARK_BLUE("sidebar.team.dark_blue"),
    SIDEBAR_TEAM_DARK_GREEN("sidebar.team.dark_green"),
    SIDEBAR_TEAM_DARK_AQUA("sidebar.team.dark_aqua"),
    SIDEBAR_TEAM_DARK_RED("sidebar.team.dark_red"),
    SIDEBAR_TEAM_DARK_PURPLE("sidebar.team.dark_purple"),
    SIDEBAR_TEAM_GOLD("sidebar.team.gold"),
    SIDEBAR_TEAM_GRAY("sidebar.team.gray"),
    SIDEBAR_TEAM_DARK_GRAY("sidebar.team.dark_gray"),
    SIDEBAR_TEAM_BLUE("sidebar.team.blue"),
    SIDEBAR_TEAM_GREEN("sidebar.team.green"),
    SIDEBAR_TEAM_AQUA("sidebar.team.aqua"),
    SIDEBAR_TEAM_RED("sidebar.team.red"),
    SIDEBAR_TEAM_LIGHT_PURPLE("sidebar.team.light_purple"),
    SIDEBAR_TEAM_YELLOW("sidebar.team.yellow"),
    SIDEBAR_TEAM_WHITE("sidebar.team.white");
    // Paper end - Generated/DisplaySlot

    public static final net.kyori.adventure.util.Index<String, DisplaySlot> NAMES = net.kyori.adventure.util.Index.create(DisplaySlot.class, DisplaySlot::getId);

    private final String id;

    DisplaySlot(@org.jetbrains.annotations.NotNull String id) {
        this.id = id;
    }

    DisplaySlot(@org.jetbrains.annotations.NotNull NamedTextColor color) {
        this.id = "sidebar.team." + color;
    }

    /**
     * Get the string id of this display slot.
     *
     * @return the string id
     */
    public @org.jetbrains.annotations.NotNull String getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.id;
    }
    // Paper end
}
