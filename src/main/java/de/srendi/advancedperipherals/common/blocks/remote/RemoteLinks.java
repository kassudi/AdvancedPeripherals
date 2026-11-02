package de.srendi.advancedperipherals.common.blocks.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps track of every loaded remote probe and hub and links the ones that share a frequency in the same level.
 * Only used on the server thread.
 */
public final class RemoteLinks {

    private static final Set<RemoteLinkEntity> ENTITIES = new HashSet<>();

    private RemoteLinks() {
    }

    public static void add(RemoteLinkEntity entity) {
        ENTITIES.add(entity);
        relink(entity);
    }

    public static void remove(RemoteLinkEntity entity) {
        ENTITIES.remove(entity);
        entity.unlinkAll();
    }

    /**
     * Drops the links of an entity and connects it to every counterpart with the same frequency.
     */
    public static void relink(RemoteLinkEntity entity) {
        entity.unlinkAll();
        for (RemoteLinkEntity other : new ArrayList<>(ENTITIES)) {
            if (other == entity || other.isHub() == entity.isHub()) {
                continue;
            }
            if (other.getLevel() != entity.getLevel() || other.frequency() != entity.frequency()) {
                continue;
            }
            entity.link(other);
        }
    }
}
