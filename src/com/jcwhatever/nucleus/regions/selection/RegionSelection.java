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

package com.jcwhatever.nucleus.regions.selection;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.regions.data.ChunkInfo;
import com.jcwhatever.nucleus.regions.data.CuboidPoint;
import com.jcwhatever.nucleus.regions.data.SyncLocation;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Contains pre-calculated variables regarding a cuboid region
 * of space as defined by two region locations.
 */
public class RegionSelection implements IRegionSelection {

    /**
     * Get the specified players current region selection.
     *
     * @param player  The player to check.
     *
     * @return  Null if the player does not have a selected region.
     */
    @Nullable
    public static IRegionSelection get(Player player) {
        return Nucleus.getProviderManager().getRegionSelectionProvider().getSelection(player);
    }

    protected final Object _sync = new Object();

    private SyncLocation _p1;
    private SyncLocation _p2;

    private int _startX;
    private int _startY;
    private int _startZ;

    private int _endX;
    private int _endY;
    private int _endZ;

    private int _xWidth;
    private int _zWidth;
    private int _yHeight;
    private int _xBlockWidth;
    private int _zBlockWidth;
    private int _yBlockHeight;
    private long _volume;

    private int _chunkX;
    private int _chunkZ;
    private int _chunkXWidth;
    private int _chunkZWidth;
    private List<ChunkInfo> _chunks;

    private Location _center;

    /**
     * Empty Constructor.
     */
    protected RegionSelection() {}

    /**
     * Constructor.
     *
     * @param p1  The first point location.
     * @param p2  The second point location.
     */
    public RegionSelection(Location p1, Location p2) {
        PreCon.notNull(p1);
        PreCon.notNull(p2);

        setCoords(p1, p2);
    }

    /**
     * Determine if the regions cuboid points have been set.
     */
    @Override
    public final boolean isDefined() {
        return _p1 != null && _p2 != null;
    }

    /**
     * Get the world the region is in.
     */
    @Override
    @Nullable
    public final World getWorld() {

        synchronized (_sync) {

            if (isDefined()) {
                return _p1.getWorld();
            }

            return null;
        }
    }

    /**
     * Get the cuboid regions first point location.
     *
     * <p>Note: If the location is set but the world it's for is not
     * loaded, the World value of location may be null.</p>
     */
    @Override
    @Nullable
    public final Location getP1() {
        if (_p1 == null)
            return null;

        synchronized (_sync) {
            return _p1.getBukkitLocation();
        }
    }

    /**
     * Get the cuboid regions seconds point location.
     *
     * <p>Note: If the location is set but the world it's for is not
     * loaded, the World value of location may be null.</p>
     */
    @Override
    @Nullable
    public final Location getP2() {
        if (_p2 == null)
            return null;

        synchronized (_sync) {
            return _p2.getBukkitLocation();
        }
    }

    /**
     * Get the cuboid regions lower point location.
     */
    @Override
    @Nullable
    public final Location getLowerPoint() {
        return getP1();
    }

    /**
     * Get the cuboid regions upper point location.
     */
    @Override
    @Nullable
    public final Location getUpperPoint() {
        return getP2();
    }

    /**
     * Get the smallest X axis coordinates
     * of the region.
     */
    @Override
    public final int getXStart() {
        return _startX;
    }

    /**
     * Get the smallest Y axis coordinates
     * of the region.
     */
    @Override
    public final int getYStart() {
        return _startY;
    }

    /**
     * Get the smallest Z axis coordinates
     * of the region.
     */
    @Override
    public final int getZStart() {
        return _startZ;
    }

    /**
     * Get the largest X axis coordinates
     * of the region.
     */
    @Override
    public final int getXEnd() {
        return _endX;
    }

    /**
     * Get the largest Y axis coordinates
     * of the region.
     */
    @Override
    public final int getYEnd() {
        return _endY;
    }

    /**
     * Get the largest Z axis coordinates
     * of the region.
     */
    @Override
    public final int getZEnd() {
        return _endZ;
    }

    /**
     * Get the X axis width of the region.
     */
    @Override
    public final int getXWidth() {
        return _xWidth;
    }

    /**
     * Get the Z axis width of the region.
     */
    @Override
    public final int getZWidth() {
        return _zWidth;
    }

    /**
     * Get the Y axis height of the region.
     */
    @Override
    public final int getYHeight() {
        return _yHeight;
    }

    /**
     * Get the number of blocks that make up the width of the
     * region on the X axis.
     */
    @Override
    public final int getXBlockWidth() {
        return _xBlockWidth;
    }

    /**
     * Get the number of blocks that make up the width of the
     * region on the Z axis.
     */
    @Override
    public final int getZBlockWidth() {
        return _zBlockWidth;
    }

    /**
     * Get the number of blocks that make up the height of the
     * region on the Y axis.
     */
    @Override
    public final int getYBlockHeight() {
        return _yBlockHeight;
    }

    /**
     * Get the total volume of the region.
     */
    @Override
    public final long getVolume() {
        return _volume;
    }

    /**
     * Get the center location of the region.
     */
    @Override
    @Nullable
    public final Location getCenter() {
        if (_center == null)
            return null;
        return _center.clone();
    }

    /**
     * Get the smallest X axis coordinates from the chunks
     * the region intersects with.
     */
    @Override
    public final int getChunkX() {
        return _chunkX;
    }

    /**
     * Get the smallest Z axis coordinates from the chunks
     * the region intersects with.
     */
    @Override
    public final int getChunkZ() {
        return _chunkZ;
    }

    /**
     * Get the number of chunks that comprise the chunk width
     * on the X axis of the region.
     */
    @Override
    public final int getChunkXWidth() {
        return _chunkXWidth;
    }

    /**
     * Get the number of chunks that comprise the chunk width
     * on the Z axis of the region.
     */
    @Override
    public final int getChunkZWidth() {
        return _chunkZWidth;
    }

    /**
     * Get all chunks that contain at least a portion of the region.
     */
    @Override
    public final List<ChunkInfo> getChunks() {
        if (getWorld() == null || !isDefined())
            return new ArrayList<>(0);

        if (_chunks != null)
            return _chunks;

        synchronized (_sync) {

            int startX = _chunkX;
            int endX = _chunkX + _chunkXWidth - 1;

            int startZ = _chunkZ;
            int endZ = _chunkZ + _chunkZWidth - 1;

            ArrayList<ChunkInfo> result = new ArrayList<ChunkInfo>((endX - startX) * (endZ - startZ));

            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    result.add(new ChunkInfo(getWorld(), x, z));
                }
            }

            _chunks = Collections.unmodifiableList(result);

            return _chunks;
        }
    }

    /**
     * Determine if the region is 1 block tall.
     */
    @Override
    public final boolean isFlatHorizontal() {
        return getYBlockHeight() == 1;
    }

    /**
     * Determine if the region is 1 block wide on the
     * X or Z axis and is not 1 block tall.
     */
    @Override
    public final boolean isFlatVertical() {
        return !isFlatHorizontal() &&
                (getZBlockWidth() == 1 || getXBlockWidth() == 1);
    }

    /**
     * Determine if the region contains the specified location.
     *
     * @param loc  The location to check.
     */
    @Override
    public final boolean contains(Location loc) {

        if (!isDefined())
            return false;

        if (loc.getWorld() == null)
            return false;

        if (!loc.getWorld().equals(getWorld()))
            return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return contains(x, y, z);
    }

    /**
     * Determine if the region contains the specified
     * coordinates.
     *
     * @param x  The location X coordinates.
     * @param y  The location Y coordinates.
     * @param z  The location Z coordinates.
     */
    @Override
    public final boolean contains(int x, int y, int z) {
        synchronized (_sync) {

            _sync.notifyAll();

            return x >= getXStart() && x <= getXEnd() &&
                    y >= getYStart() && y <= getYEnd() &&
                    z >= getZStart() && z <= getZEnd();
        }
    }

    /**
     * Determine if the region contains the the specified location
     * on the specified axis.
     *
     * @param loc  The location to check.
     * @param cx   True to check if the point is inside the region on the X axis.
     * @param cy   True to check if the point is inside the region on the Y axis.
     * @param cz   True to check if the point is inside the region on the Z axis.
     */
    @Override
    public final boolean contains(Location loc, boolean cx, boolean cy, boolean cz) {

        if (!isDefined())
            return false;

        synchronized (_sync) {

            if (!loc.getWorld().equals(getWorld()))
                return false;

            if (cx) {
                int x = loc.getBlockX();
                if (x < getXStart() || x > getXEnd())
                    return false;
            }

            if (cy) {
                int y = loc.getBlockY();
                if (y < getYStart() || y > getYEnd())
                    return false;
            }

            if (cz) {
                int z = loc.getBlockZ();
                if (z < getZStart() || z > getZEnd())
                    return false;
            }

            _sync.notifyAll();

            return true;
        }
    }

    /**
     * Determine if the region intersects with the chunk specified.
     *
     * @param chunk  The chunk.
     */
    @Override
    public final boolean intersects(Chunk chunk) {
        PreCon.notNull(chunk);

        return isDefined() &&
                chunk.getWorld().equals(getWorld()) &&
                intersects(chunk.getX(), chunk.getZ());
    }

    /**
     * Determine if the region intersects with the chunk specified.
     *
     * @param chunkX  The chunk X coordinates.
     * @param chunkZ  The chunk Z coordinates.
     */
    @Override
    public final boolean intersects(int chunkX, int chunkZ) {

        return getChunkX() <= chunkX && (getChunkX() + getChunkXWidth() - 1) >= chunkX &&
                getChunkZ() <= chunkZ && (getChunkZ() + getChunkZWidth() - 1) >= chunkZ;
    }

    /**
     * Get a specific point location from the
     * region selection.
     *
     * @param point  The point to get.
     */
    @Override
    public Location getPoint(CuboidPoint point) {
        PreCon.notNull(point);

        return point.getLocation(this);
    }

    /**
     * Get a {@code CuboidPoint} that represents the specified
     * location.
     *
     * @param location  The location to check.
     *
     * @return  Null if the location is not any of the regions points.
     */
    @Nullable
    @Override
    public CuboidPoint getPoint(Location location) {
        PreCon.notNull(location);

        return CuboidPoint.getCuboidPoint(location, this);
    }

    /**
     * Get a reference to the underlying P1 {@code ImmutableLocation}
     * coordinates.
     */
    protected SyncLocation getSyncP1() {
        return _p1;
    }

    /**
     * Get a reference to the underlying P2 {@code ImmutableLocation}
     * coordinates.
     */
    protected SyncLocation getSyncP2() {
        return _p2;
    }

    /**
     * Set the regions cuboid point coordinates.
     *
     * @param p1  The first point location.
     * @param p2  The second point location.
     */
    protected void setCoords(Location p1, Location p2) {
        PreCon.notNull(p1);
        PreCon.notNull(p2);

        World p1World = p1.getWorld();
        World p2World = p2.getWorld();

        if (p1World != null && !p1World.equals(p2World) ||
                p2World != null && !p2World.equals(p1World)) {
            throw new IllegalArgumentException("Both region points must be from the same world.");
        }

        double lowerX = Math.min(p1.getX(), p2.getX());
        double lowerY = Math.min(p1.getY(), p2.getY());
        double lowerZ = Math.min(p1.getZ(), p2.getZ());

        double upperX = Math.max(p1.getX(), p2.getX());
        double upperY = Math.max(p1.getY(), p2.getY());
        double upperZ = Math.max(p1.getZ(), p2.getZ());

        _p1 = new SyncLocation(p1.getWorld().getName(), lowerX, lowerY, lowerZ, 0F, 0F);
        _p2 = new SyncLocation(p2.getWorld().getName(), upperX, upperY, upperZ, 0F, 0F);
        updateMath();
    }

    /*
     * Update region math variables
     */
    protected void updateMath() {

        if (_p1 == null || _p2 == null)
            return;

        synchronized (_sync) {

            _startX = Math.min(_p1.getBlockX(), _p2.getBlockX());
            _startY = Math.min(_p1.getBlockY(), _p2.getBlockY());
            _startZ = Math.min(_p1.getBlockZ(), _p2.getBlockZ());

            _endX = Math.max(_p1.getBlockX(), _p2.getBlockX());
            _endY = Math.max(_p1.getBlockY(), _p2.getBlockY());
            _endZ = Math.max(_p1.getBlockZ(), _p2.getBlockZ());

            _xWidth = _p1 == null || _p2 == null ? 0 : (int)Math.abs(_p1.getX() - _p2.getX());
            _zWidth = _p1 == null || _p2 == null ? 0 : (int)Math.abs(_p1.getZ() - _p2.getZ());
            _yHeight = _p1 == null || _p2 == null ? 0 : (int)Math.abs(_p1.getY() - _p2.getY());

            _xBlockWidth = _p1 == null || _p2 == null ? 0 : Math.abs(_p1.getBlockX() - _p2.getBlockX()) + 1;
            _zBlockWidth = _p1 == null || _p2 == null ? 0 : Math.abs(_p1.getBlockZ() - _p2.getBlockZ()) + 1;
            _yBlockHeight = _p1 == null || _p2 == null ? 0 : Math.abs(_p1.getBlockY() - _p2.getBlockY()) + 1;

            _volume = _xWidth * _zWidth * _yHeight;

            if (getWorld() != null) {
                double xCenter = _startX + (_xBlockWidth / 2.0D);
                double yCenter = _startY + (_yBlockHeight / 2.0D);
                double zCenter = _startZ + (_zBlockWidth / 2.0D);

                _center = new Location(getWorld(), xCenter, yCenter, zCenter);
            }

            _chunkX = Math.min(_p1.getBlockX(), _p2.getBlockX()) == _p1.getBlockX()
                    ? (int)Math.floor((double) _p1.getBlockX() / 16)
                    : (int)Math.floor((double) _p2.getBlockX() / 16);

            _chunkZ = Math.min(_p1.getBlockZ(), _p2.getBlockZ()) == _p1.getBlockZ()
                    ? (int)Math.floor((double)_p1.getBlockZ() / 16)
                    : (int)Math.floor((double)_p2.getBlockZ() / 16);

            int chunkEndX = Math.max(_p1.getBlockX(), _p2.getBlockX()) == _p1.getBlockX()
                    ? (int)Math.floor((double) _p1.getBlockX() / 16)
                    : (int)Math.floor((double) _p2.getBlockX() / 16);

            int chunkEndZ = Math.max(_p1.getBlockZ(), _p2.getBlockZ()) == _p1.getBlockZ()
                    ? (int)Math.floor((double) _p1.getBlockZ() / 16)
                    : (int)Math.floor((double) _p2.getBlockZ() / 16);

            _chunkXWidth = chunkEndX - _chunkX + 1;
            _chunkZWidth = chunkEndZ - _chunkZ + 1;

            _chunks = null;

            _sync.notifyAll();
        }
    }
}
