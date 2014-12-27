/*
 * This file is part of NucleusFramework for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.nucleus.regions;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.events.regions.RegionOwnerChangedEvent;
import com.jcwhatever.nucleus.internal.InternalRegionManager;
import com.jcwhatever.nucleus.regions.data.ChunkInfo;
import com.jcwhatever.nucleus.regions.selection.RegionSelection;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

/**
 * Abstract implementation of a region.
 *
 * <p>The region is registered with NucleusFramework's
 * {@link com.jcwhatever.nucleus.internal.InternalRegionManager} as soon
 * as it is defined (P1 and P2 coordinates set) via the regions settings or by
 * calling {@code setCoords} method.</p>
 *
 * <p>The regions protected methods {@code onPlayerEnter} and {@code onPlayerLeave}
 * are only called if the implementing type calls {@code setEventListener(true)}.</p>
 */
public abstract class Region extends RegionSelection implements IRegion {

    private static final Map<Region, Void> _instances = new WeakHashMap<>(100);
    private static BukkitListener _bukkitListener;

    private final Plugin _plugin;
    private final String _name;
    private final String _searchName;
    private final IDataNode _dataNode;
    private final IRegionEventListener _eventListener;

    private RegionPriority _enterPriority = RegionPriority.DEFAULT;
    private RegionPriority _leavePriority = RegionPriority.DEFAULT;

    private boolean _isEventListener;
    private boolean _isDisposed;
    private String _worldName;
    private UUID _ownerId;
    private Map<Object, Object> _meta = new HashMap<Object, Object>(30);
    private List<IRegionEventHandler> _eventHandlers = new ArrayList<>(10);

    /**
     * The priority/order the region is handled by the
     * region manager in relation to other regions.
     */
    public enum RegionPriority {
        /**
         * The last to be handled.
         */
        LAST      (4),
        /**
         * Low priority. Handled second to last.
         */
        LOW       (3),
        /**
         * Normal priority.
         */
        DEFAULT   (2),
        /**
         * High priority. Handled second.
         */
        HIGH      (1),
        /**
         * Highest priority. Handled first.
         */
        FIRST     (0);

        private final int _order;

        RegionPriority(int order) {
            _order = order;
        }

        /**
         * Get a sort order index number.
         */
        public int getSortOrder() {
            return _order;
        }
    }

    /**
     * Type of region priority.
     */
    public enum PriorityType {
        ENTER,
        LEAVE
    }

    /**
     * Reasons a player enters a region.
     */
    public enum EnterRegionReason {
        /**
         * The player moved into the region.
         */
        MOVE,
        /**
         * The player teleported into the region.
         */
        TELEPORT,
        /**
         * The player respawned into the region.
         */
        RESPAWN,
        /**
         * The player joined the server and
         * spawned into the region.
         */
        JOIN_SERVER
    }

    /**
     * Reasons a player leaves a region.
     */
    public enum LeaveRegionReason {
        /**
         * The player moved out of the region.
         */
        MOVE,
        /**
         * The player teleported out of the region.
         */
        TELEPORT,
        /**
         * The player died in the region.
         */
        DEAD,
        /**
         * The player left the server while
         * in the region.
         */
        QUIT_SERVER
    }

    /**
     * For internal use.
     */
    public enum RegionReason {
        MOVE        (EnterRegionReason.MOVE,        LeaveRegionReason.MOVE),
        DEAD        (null,                          LeaveRegionReason.DEAD),
        TELEPORT    (EnterRegionReason.TELEPORT,    LeaveRegionReason.TELEPORT),
        RESPAWN     (EnterRegionReason.RESPAWN,     LeaveRegionReason.DEAD),
        JOIN_SERVER (EnterRegionReason.JOIN_SERVER, null),
        QUIT_SERVER (null,                          LeaveRegionReason.QUIT_SERVER);

        private final EnterRegionReason _enter;
        private final LeaveRegionReason _leave;

        RegionReason(EnterRegionReason enter, LeaveRegionReason leave) {
            _enter = enter;
            _leave = leave;
        }

        /**
         * Get the enter reason equivalent.
         */
        @Nullable
        public EnterRegionReason getEnterReason() {
            return _enter;
        }

        /**
         * Get the leave reason equivalent.
         */
        @Nullable
        public LeaveRegionReason getLeaveReason() {
            return _leave;
        }
    }

    /**
     * Constructor
     */
    public Region(Plugin plugin, String name, @Nullable IDataNode dataNode) {
        PreCon.notNull(plugin);
        PreCon.notNullOrEmpty(name);

        setMeta(InternalRegionManager.REGION_HANDLE, this);
        _eventListener = new RegionListener(this);

        _name = name;
        _plugin = plugin;
        _searchName = name.toLowerCase();
        _dataNode = dataNode;

        RegionPriorityInfo info = this.getClass().getAnnotation(RegionPriorityInfo.class);
        if (info != null) {
            _enterPriority = info.enter();
            _leavePriority = info.leave();
        }

        if (dataNode != null) {
            loadSettings(dataNode);
        }

        _instances.put(this, null);

        if (_bukkitListener == null) {
            _bukkitListener = new BukkitListener();
            Bukkit.getPluginManager().registerEvents(_bukkitListener, Nucleus.getPlugin());
        }
    }

    /**
     * Get the name of the region.
     */
    @Override
    public final String getName() {
        return _name;
    }

    /**
     * Get the name of the region in lower case.
     */
    @Override
    public final String getSearchName() {
        return _searchName;
    }

    /**
     * Get the owning plugin.
     */
    @Override
    public final Plugin getPlugin() {
        return _plugin;
    }

    /**
     * Get the regions data node.
     */
    @Nullable
    public IDataNode getDataNode() {
        return _dataNode;
    }

    /**
     * Get the regions priority when handling player
     * entering region.
     */
    @Override
    public RegionPriority getPriority(PriorityType priorityType) {
        PreCon.notNull(priorityType);

        switch (priorityType) {
            case ENTER:
                return _enterPriority;
            case LEAVE:
                return _leavePriority;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Get the name of the world the region is in.
     */
    @Nullable
    public final String getWorldName() {
        return _worldName;
    }

    /**
     * Determine if the world the region is in is loaded.
     */
    public final boolean isWorldLoaded() {
        if (!isDefined())
            return false;

        if (getWorld() == null)
            return false;

        World world = Bukkit.getWorld(getWorld().getName());

        return getWorld().equals(world);
    }


    /**
     * Used to determine if the region subscribes to player events.
     */
    @Override
    public final boolean isEventListener() {
        return _isEventListener || !_eventHandlers.isEmpty();
    }

    /**
     * Get the regions event listener.
     */
    @Override
    public final IRegionEventListener getEventListener() {
        return _eventListener;
    }

    /**
     * Get the id of the region owner.
     */
    @Override
    @Nullable
    public final UUID getOwnerId() {
        return _ownerId;
    }

    /**
     * Determine if the region has an owner.
     */
    @Override
    public final boolean hasOwner() {
        return _ownerId != null;
    }

    /**
     * Set the regions owner.
     *
     * @param ownerId  The id of the new owner.
     */
    @Override
    public final boolean setOwner(@Nullable UUID ownerId) {

        UUID oldId = _ownerId;

        RegionOwnerChangedEvent event = new RegionOwnerChangedEvent(new ReadOnlyRegion(this), oldId, ownerId);
        Nucleus.getEventManager().callBukkit(event);

        if (event.isCancelled())
            return false;

        if (!onOwnerChanged(oldId, ownerId)) {
            return false;
        }

        _ownerId = ownerId;

        IDataNode dataNode = getDataNode();
        if (dataNode != null) {
            dataNode.set("owner-id", ownerId);
            dataNode.saveAsync(null);
        }

        return true;
    }

    /**
     * Set the regions cuboid point coordinates.
     *
     * <p>Saves to the regions data node if it has one.</p>
     *
     * @param p1  The first point location.
     * @param p2  The second point location.
     */
    @Override
    public final void setCoords(Location p1, Location p2) {
        PreCon.notNull(p1);
        PreCon.notNull(p2);

        // unregister while math is updated,
        // is re-registered after math update (see UpdateMath)
        regionManager().unregister(this);

        super.setCoords(p1, p2);
        updateWorld();

        IDataNode dataNode = getDataNode();
        if (dataNode != null) {
            dataNode.set("p1", getP1());
            dataNode.set("p2", getP2());
            dataNode.saveAsync(null);
        }

        onCoordsChanged(getP1(), getP2());
    }

    /**
     * Add a transient region event handler.
     *
     * @param handler  The handler to add.
     */
    @Override
    public boolean addEventHandler(IRegionEventHandler handler) {
        PreCon.notNull(handler);

        boolean isFirstHandler = _eventHandlers.isEmpty();

        if (_eventHandlers.add(handler)) {
            if (isFirstHandler) {
                // update registration
                regionManager().register(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Remove a transient event handler.
     *
     * @param handler  The handler to remove.
     */
    @Override
    public boolean removeEventHandler(IRegionEventHandler handler) {
        PreCon.notNull(handler);

        if (_eventHandlers.remove(handler)) {

            if (_eventHandlers.isEmpty()) {
                // update registration
                regionManager().register(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Determine if the region contains the specified material.
     *
     * @param material  The material to search for.
     */
    @Override
    public final boolean contains(Material material) {

        synchronized (_sync) {

            if (getWorld() == null)
                return false;

            int xlen = getXEnd();
            int ylen = getYEnd();
            int zlen = getZEnd();

            for (int x = getXStart(); x <= xlen; x++) {

                for (int y = getYStart(); y <= ylen; y++) {

                    for (int z = getZStart(); z <= zlen; z++) {

                        Block block = getWorld().getBlockAt(x, y, z);
                        if (block.getType() != material)
                            continue;

                        return true;
                    }
                }
            }

            _sync.notifyAll();

            return false;
        }
    }

    /**
     * Get all locations that have a block of the specified material
     * within the region.
     *
     * @param material  The material to search for.
     */
    @Override
    public final LinkedList<Location> find(Material material) {

        synchronized (_sync) {
            LinkedList<Location> results = new LinkedList<>();

            if (getWorld() == null)
                return results;

            int xlen = getXEnd();
            int ylen = getYEnd();
            int zlen = getZEnd();

            for (int x = getXStart(); x <= xlen; x++) {

                for (int y = getYStart(); y <= ylen; y++) {

                    for (int z = getZStart(); z <= zlen; z++) {

                        Block block = getWorld().getBlockAt(x, y, z);
                        if (block.getType() != material)
                            continue;

                        results.add(block.getLocation());
                    }
                }
            }

            _sync.notifyAll();

            return results;
        }
    }

    /**
     * Refresh all chunks the region is in.
     */
    @Override
    public final void refreshChunks() {
        World world = getWorld();

        if (world == null)
            return;

        List<ChunkInfo> chunks = getChunks();

        for (ChunkInfo chunk : chunks) {
            world.refreshChunk(chunk.getX(), chunk.getZ());
        }
    }

    /**
     * Remove entities from the region.
     *
     * @param entityTypes  The entity types to remove.
     */
    @Override
    public final void removeEntities(Class<?>... entityTypes) {

        synchronized (_sync) {
            List<ChunkInfo> chunks = getChunks();
            for (ChunkInfo chunkInfo : chunks) {
                Chunk chunk = chunkInfo.getChunk();

                for (Entity entity : chunk.getEntities()) {
                    if (this.contains(entity.getLocation())) {

                        if (entityTypes == null || entityTypes.length == 0) {
                            entity.remove();
                            continue;
                        }

                        for (Class<?> itemType : entityTypes) {
                            if (itemType.isInstance(entity)) {
                                entity.remove();
                                break;
                            }
                        }
                    }
                }
            }
            _sync.notifyAll();
        }
    }

    /**
     * Get a meta data value from the region.
     *
     * @param key  The meta key.
     *
     * @param <T>  The expected value type.
     */
    @Override
    public <T> T getMeta(MetaKey<T> key) {
        PreCon.notNull(key);

        @SuppressWarnings("unchecked")
        T item = (T)_meta.get(key);

        return item;
    }

    /**
     * Get a meta object from the region.
     *
     * @param key  The meta key.
     */
    @Override
    public Object getMetaObject(Object key) {
        PreCon.notNull(key);

        return _meta.get(key);
    }

    /**
     * Set a meta object value in the region.
     *
     * @param key    The meta key.
     * @param value  The meta value.
     */
    @Override
    public <T> void setMeta(MetaKey<T> key, @Nullable T value) {
        if (value == null) {
            _meta.remove(key);
            return;
        }

        _meta.put(key, value);
    }

    /**
     * Get the class of the region.
     */
    @Override
    public Class<? extends IRegion> getRegionClass() {
        return getClass();
    }

    /**
     * Determine if the region has been disposed.
     */
    @Override
    public final boolean isDisposed() {
        return _isDisposed;
    }

    /**
     * Dispose the region by releasing resources and
     * un-registering it from the central region manager.
     */
    @Override
    public final void dispose() {
        regionManager().unregister(this);
        _instances.remove(this);

        onDispose();

        _isDisposed = true;
    }

    /**
     * Set the value of the player watcher flag and update
     * the regions registration with the central region manager.
     *
     * @param isEventListener  True to allow player enter and leave events.
     */
    protected void setEventListener(boolean isEventListener) {
        if (isEventListener != _isEventListener) {
            _isEventListener = isEventListener;

            regionManager().register(this);
        }
    }

    /**
     * Causes the onPlayerEnter method to re-fire
     * if the player is already in the region.
     * .
     * @param p  The player to reset.
     */
    protected void resetContainsPlayer(Player p) {
        regionManager().resetPlayerRegion(p, this);
    }

    /**
     * Initializes region coordinates without saving to the data node.
     *
     * @param p1  The first point location.
     * @param p2  The second point location.
     */
    protected final void initCoords(@Nullable Location p1, @Nullable Location p2) {
        setPoint(RegionPoint.P1, p1);
        setPoint(RegionPoint.P2, p2);

        updateWorld();
        updateMath();
    }

    /**
     * Initial load of settings from regions data node.
     *
     * @param dataNode  The data node to load from
     */
    protected void loadSettings(final IDataNode dataNode) {

        Location p1 = dataNode.getLocation("p1");
        Location p2 = dataNode.getLocation("p2");

        initCoords(p1, p2);

        _ownerId = dataNode.getUUID("owner-id");
        _enterPriority = dataNode.getEnum("region-enter-priority", _enterPriority, RegionPriority.class);
        _leavePriority = dataNode.getEnum("region-leave-priority", _leavePriority, RegionPriority.class);
    }

    /*
    * Update region math variables
    */
    @Override
    protected void updateMath() {

        super.updateMath();

        regionManager().register(this);
    }

    /**
     * Called when the coordinates for the region are changed
     *
     * <p>Intended for override if needed.</p>
     *
     * @param p1  The first point location.
     * @param p2  The second point location.
     *
     * @throws IOException
     */
    protected void onCoordsChanged(@SuppressWarnings("unused") @Nullable Location p1,
                                   @SuppressWarnings("unused") @Nullable Location p2) {
        // do nothing
    }

    /**
     * Called when a player enters the region,
     * but only if the region is a player watcher and 
     * canDoPlayerEnter() returns true.
     *
     * <p>Intended for override if needed.</p>
     *
     * @param p  the player entering the region.
     */
    protected void onPlayerEnter (@SuppressWarnings("unused") Player p,
                                  @SuppressWarnings("unused") EnterRegionReason reason) {
        // do nothing
    }

    /**
     * Called when a player leaves the region,
     * but only if the region is a player watcher and 
     * canDoPlayerLeave() returns true.
     *
     * <p>Intended for override if needed.</p>
     *
     * @param p  the player leaving the region.
     */
    protected void onPlayerLeave (@SuppressWarnings("unused") Player p,
                                  @SuppressWarnings("unused") LeaveRegionReason reason) {
        // do nothing
    }

    /**
     * Called to determine if {@code onPlayerEnter}
     * can be called on the specified player.
     *
     * <p>Intended for override if needed.</p>
     *
     * @param p  The player entering the region.
     */
    protected boolean canDoPlayerEnter(@SuppressWarnings("unused") Player p,
                                       @SuppressWarnings("unused") EnterRegionReason reason) {
        return true;
    }

    /**
     * Called to determine if {@code onPlayerLeave}
     * can be called on the specified player.
     *
     * <p>Intended for override if needed.</p>
     *
     * @param p  The player leaving the region.
     */
    protected boolean canDoPlayerLeave(@SuppressWarnings("unused") Player p,
                                       @SuppressWarnings("unused") LeaveRegionReason reason) {
        return true;
    }

    /**
     * Called when the owner of the region is changed.
     *
     * <p>Note that other plugins can easily change the region
     * owner value.</p>
     *
     * <p>Intended for override if needed.</p>
     *
     * @param oldOwnerId  The id of the previous owner of the region.
     * @param newOwnerId  The id of the new owner of the region.
     *
     * @return True to allow the owner change.
     */
    protected boolean onOwnerChanged(@SuppressWarnings("unused") @Nullable UUID oldOwnerId,
                                     @SuppressWarnings("unused") @Nullable UUID newOwnerId) {
        return true;
    }

    /**
     * Called when the region is disposed.
     *
     * <p>Intended for override if needed.</p>
     */
    protected void onDispose() {
        // do nothings
    }

    /**
     * Used by {@code RegionManager} to execute onPlayerEnter event.
     */
    void doPlayerEnter (Player p, EnterRegionReason reason) {

        if (canDoPlayerEnter(p, reason))
            onPlayerEnter(p, reason);

        for (IRegionEventHandler handler : _eventHandlers) {
            if (handler.canDoPlayerEnter(p, reason)) {
                handler.onPlayerEnter(p, reason);
            }
        }
    }

    /**
     * Used by {@code RegionManager} to execute onPlayerLeave event.
     */
    void doPlayerLeave (Player p, LeaveRegionReason reason) {

        if (canDoPlayerLeave(p, reason))
            onPlayerLeave(p, reason);

        for (IRegionEventHandler handler : _eventHandlers) {
            if (handler.canDoPlayerLeave(p, reason)) {
                handler.onPlayerLeave(p, reason);
            }
        }
    }

    /*
     * Update world name if possible
     */
    private void updateWorld() {

        Location p1 = getP1();
        Location p2 = getP2();

        if (p1 == null || p2 == null) {
            _worldName = null;
            return;
        }

        World p1World = p1.getWorld();
        World p2World = p2.getWorld();

        boolean isWorldsMismatched = p1World != null && !p1World.equals(p2World) ||
                p2World != null && !p2World.equals(p1World);

        if (isWorldsMismatched) {
            throw new IllegalArgumentException("Both region points must be from the same world.");
        }

        if (p1World != null) {
            _worldName = p1World.getName();
        }
        else {

            IDataNode dataNode = getDataNode();

            if (dataNode != null) {
                _worldName = dataNode.getLocationWorldName("p1");
            }
        }
    }

    private InternalRegionManager regionManager() {
        return (InternalRegionManager) Nucleus.getRegionManager();
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        synchronized (_sync) {
            return this == obj;
        }
    }

    private static class RegionListener implements IRegionEventListener {

        private final Region _region;

        RegionListener(Region region) {
            _region = region;
        }

        @Override
        public void onPlayerEnter(Player p, EnterRegionReason reason) {
            _region.doPlayerEnter(p, reason);
        }

        @Override
        public void onPlayerLeave(Player p, LeaveRegionReason reason) {
            _region.doPlayerLeave(p, reason);
        }
    }

    /*
     * Listens for world unload and load events and handles
     * regions in the world appropriately.
     */
    private static class BukkitListener implements Listener {

        @EventHandler
        private void onWorldLoad(WorldLoadEvent event) {

            String worldName = event.getWorld().getName();
            for (Region region : _instances.keySet()) {
                if (region.isDefined() && worldName.equals(region.getWorldName())) {
                    // fix locations

                    //noinspection ConstantConditions
                    region.getP1().setWorld(event.getWorld());

                    //noinspection ConstantConditions
                    region.getP2().setWorld(event.getWorld());
                }
            }
        }

        @EventHandler
        private void onWorldUnload(WorldUnloadEvent event) {

            String worldName = event.getWorld().getName();
            for (Region region : _instances.keySet()) {
                if (region.isDefined() && worldName.equals(region.getWorldName())) {
                    // remove world from locations, helps garbage collector

                    //noinspection ConstantConditions
                    region.getP1().setWorld(null);

                    //noinspection ConstantConditions
                    region.getP2().setWorld(null);
                }
            }
        }
    }
}