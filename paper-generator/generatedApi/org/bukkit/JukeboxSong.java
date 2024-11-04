package org.bukkit;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a song which may play in a Jukebox.
 */
@ApiStatus.Experimental
public interface JukeboxSong extends Keyed, Translatable {

    // Paper start - Generated/JukeboxSong
    // @GeneratedFrom 1.21.3
    JukeboxSong ELEVEN = get("11");

    JukeboxSong THIRTEEN = get("13");

    JukeboxSong FIVE = get("5");

    JukeboxSong BLOCKS = get("blocks");

    JukeboxSong CAT = get("cat");

    JukeboxSong CHIRP = get("chirp");

    JukeboxSong CREATOR = get("creator");

    JukeboxSong CREATOR_MUSIC_BOX = get("creator_music_box");

    JukeboxSong FAR = get("far");

    JukeboxSong MALL = get("mall");

    JukeboxSong MELLOHI = get("mellohi");

    JukeboxSong OTHERSIDE = get("otherside");

    JukeboxSong PIGSTEP = get("pigstep");

    JukeboxSong PRECIPICE = get("precipice");

    JukeboxSong RELIC = get("relic");

    JukeboxSong STAL = get("stal");

    JukeboxSong STRAD = get("strad");

    JukeboxSong WAIT = get("wait");

    JukeboxSong WARD = get("ward");
    // Paper end - Generated/JukeboxSong

    @NotNull
    private static JukeboxSong get(@NotNull String key) {
        return Registry.JUKEBOX_SONG.getOrThrow(NamespacedKey.minecraft(key));
    }

    // Paper start - adventure
    /**
     * @deprecated this method assumes that jukebox song description will
     * always be a translatable component which is not guaranteed.
     */
    @Override
    @Deprecated(forRemoval = true)
    @org.jetbrains.annotations.NotNull String getTranslationKey();
    // Paper end - adventure
}
