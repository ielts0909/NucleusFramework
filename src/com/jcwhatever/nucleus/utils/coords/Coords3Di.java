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

package com.jcwhatever.nucleus.utils.coords;

import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.storage.serialize.DeserializeException;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.file.IByteReader;
import com.jcwhatever.nucleus.utils.file.IByteWriter;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.io.IOException;
import javax.annotation.Nullable;

/**
 * 3D immutable integer coordinates with no {@link org.bukkit.World} context.
 */
public class Coords3Di extends Coords2Di implements ICoords3Di {

    /**
     * Get {@link Coords3Di} from a {@link org.bukkit.Location}.
     *
     * @param location  The location to convert.
     */
    public static Coords3Di fromLocation(Location location) {
        return new Coords3Di(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Get {@link Coords3Di} from a {@link org.bukkit.util.Vector}.
     *
     * @param vector  The vector to convert.
     */
    public static Coords3Di fromVector(Vector vector) {
        return new Coords3Di(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /**
     * Get the distance from a source coordinate to a target coordinate.
     *
     * @param source  The source coordinates.
     * @param target  The target coordinates.
     */
    public static double distance(ICoords3Di source, ICoords3Di target) {
        return Math.sqrt(distanceSquared(source, target));
    }

    /**
     * Get the distance from a source coordinate to a target coordinate.
     *
     * @param source  The source coordinates.
     * @param target  The target coordinates.
     */
    public static double distance(ICoords3Di source, ICoords3D target) {
        return Math.sqrt(distanceSquared(source, target));
    }

    /**
     * Get distance squared between two coordinates.
     *
     * @param source       The source coordinates.
     * @param destination  The destination coordinates.
     */
    public static double distanceSquared(ICoords3Di source, ICoords3Di destination) {
        PreCon.notNull(source);
        PreCon.notNull(destination);

        double deltaX = destination.getX() - source.getX();
        double deltaY = destination.getY() - source.getY();
        double deltaZ = destination.getZ() - source.getZ();

        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    /**
     * Get distance squared between two coordinates.
     *
     * @param source       The source coordinates.
     * @param destination  The destination coordinates.
     */
    public static double distanceSquared(ICoords3Di source, ICoords3D destination) {
        PreCon.notNull(source);
        PreCon.notNull(destination);

        double deltaX = destination.getX() - source.getX();
        double deltaY = destination.getY() - source.getY();
        double deltaZ = destination.getZ() - source.getZ();

        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    /**
     * Get a {@link org.bukkit.block.Block} from the specified {@link org.bukkit.World}
     * using the specified coordinates.
     *
     * @param world   The {@link org.bukkit.World} the block is in.
     * @param coords  The coordinates of the block.
     */
    public static Block getBlock(World world, ICoords3Di coords) {
        PreCon.notNull(world);

        return world.getBlockAt(coords.getX(), coords.getY(), coords.getZ());
    }

    /**
     * Create a new {@link org.bukkit.Location} from the coordinates.
     *
     * @param coords  The coordinates to convert.
     * @param world   The {@link org.bukkit.World} value of the new location.
     */
    public static Location toLocation(ICoords3Di coords, @Nullable World world) {
        return toLocation(coords, new Location(world, 0, 0, 0));
    }

    /**
     * Copy coordinate values into an output {@link org.bukkit.Location}.
     *
     * @param output  The output {@link org.bukkit.Location}.
     *
     * @return  The output location.
     */
    public static Location toLocation(ICoords3Di coords, Location output) {
        return copyTo(coords, output);
    }

    /**
     * Create a new {@link org.bukkit.util.Vector} from the coordinates.
     *
     * @param coords  The coordinates to convert.
     */
    public static Vector toVector(ICoords3Di coords) {
        return toVector(coords, new Vector(0, 0, 0));
    }

    /**
     * Copy coordinate values into an output {@link org.bukkit.util.Vector}.
     *
     * @param coords  The coordinates to convert.
     * @param output  The output {@link org.bukkit.util.Vector}.
     *
     * @return  The output location.
     */
    public static Vector toVector(ICoords3Di coords, Vector output) {
        PreCon.notNull(coords);
        PreCon.notNull(output);

        output.setX(coords.getX());
        output.setY(coords.getY());
        output.setZ(coords.getZ());
        return output;
    }

    /**
     * Copy the coordinate to an output {@link org.bukkit.Location}.
     *
     * @param coords  The coordinates to copy from.
     * @param output  The output {@link org.bukkit.Location}.
     *
     * @return  The output {@link org.bukkit.Location}.
     */
    public static Location copyTo(ICoords3Di coords, Location output) {
        PreCon.notNull(coords);
        PreCon.notNull(output);

        output.setX(coords.getX());
        output.setY(coords.getY());
        output.setZ(coords.getZ());
        return output;
    }

    /**
     * Copy the coordinate values to an output {@link org.bukkit.Location}.
     *
     * @param coords  The coords to copy from.
     * @param world   The {@link org.bukkit.World} to put into the output {@link org.bukkit.Location}.
     * @param output  The output {@link org.bukkit.Location}.
     *
     * @return  The output {@link org.bukkit.Location}.
     */
    public static Location copyTo(ICoords3Di coords, @Nullable World world, Location output) {
        PreCon.notNull(coords);
        PreCon.notNull(output);

        output.setWorld(world);
        output.setX(coords.getX());
        output.setY(coords.getY());
        output.setZ(coords.getZ());
        return output;
    }

    private int _y;
    private boolean _canSeal;

    /**
     * Constructor.
     *
     * @param x  The x coordinates.
     * @param y  The y coordinates.
     * @param z  The z coordinates.
     */
    public Coords3Di(int x, int y, int z) {
        super(x, z);
        _y = y;
        _canSeal = true;
        seal();
    }

    /**
     * Constructor.
     *
     * <p>Clones values from source coordinates.</p>
     *
     * @param source  The source coordinates.
     */
    public Coords3Di(ICoords3Di source) {
        this(source.getX(), source.getY(), source.getZ());
    }

    /**
     * Constructor.
     *
     * <p>Clones values from source coordinates and adds delta values.</p>
     *
     * @param source  The source coordinates.
     * @param deltaX  The X coordinate values to add to the source coordinates.
     * @param deltaY  The Y coordinate values to add to the source coordinates.
     * @param deltaZ  The Z coordinate values to add to the source coordinates.
     */
    public Coords3Di(ICoords3Di source, int deltaX, int deltaY, int deltaZ) {
        this(source.getX() + deltaX, source.getY() + deltaY, source.getZ() + deltaZ);
    }

    /**
     * Protected constructor for serialization.
     */
    protected Coords3Di() {}

    @Override
    public int getY() {
        return _y;
    }

    /**
     * Get the distance from this coordinates to a target coordinate.
     *
     * @param target  The target coordinates.
     */
    public double distance(ICoords3D target) {
        return distance(this, target);
    }

    /**
     * Get the distance from this coordinates to a target coordinate.
     *
     * @param target  The target coordinates.
     */
    public double distance(ICoords3Di target) {
        return distance(this, target);
    }

    /**
     * Get the distance from this coordinates to a target coordinate squared.
     *
     * @param target  The target coordinates.
     */
    public double distanceSquared(ICoords3D target) {
        return distanceSquared(this, target);
    }

    /**
     * Get the distance from this coordinates to a target coordinate squared.
     *
     * @param target  The target coordinates.
     */
    public double distanceSquared(ICoords3Di target) {
        return distanceSquared(this, target);
    }

    /**
     * Create delta coordinates by subtracting other coordinates from
     * this coordinates.
     *
     * @param coords  The other coordinates.
     */
    public Coords3Di getDelta(ICoords3Di coords) {
        PreCon.notNull(coords);

        int deltaX = getX() - coords.getX();
        int deltaY = getY() - coords.getY();
        int deltaZ = getZ() - coords.getZ();

        return new Coords3Di(deltaX, deltaY, deltaZ);
    }

    /**
     * Create delta coordinates by subtracting other coordinates from
     * this coordinates.
     *
     * @param coords  The other coordinates.
     * @param output  The {@link MutableCoords3Di} to put the results into.
     *
     * @return  The output {@link MutableCoords3Di}.
     */
    public MutableCoords3Di getDelta(ICoords3Di coords, MutableCoords3Di output) {
        PreCon.notNull(coords);
        PreCon.notNull(output);

        int deltaX = getX() - coords.getX();
        int deltaY = getY() - coords.getY();
        int deltaZ = getZ() - coords.getZ();

        output.setX(deltaX);
        output.setY(deltaY);
        output.setZ(deltaZ);

        return output;
    }

    /**
     * Get a {@link org.bukkit.block.Block} from the specified {@link org.bukkit.World}
     * using this coordinates.
     *
     * @param world  The {@link org.bukkit.World} the block is in.
     */
    public Block getBlock(World world) {
        return getBlock(world, this);
    }

    /**
     * Create a new {@link org.bukkit.Location} from the coordinates.
     *
     * @param world  The {@link org.bukkit.World} value of the new location.
     */
    public Location toLocation(@Nullable World world) {
        return toLocation(this, new Location(world, 0, 0, 0));
    }

    /**
     * Copy coordinate values into an output {@link org.bukkit.Location}.
     *
     * @param output  The output {@link org.bukkit.Location}.
     *
     * @return  The output location.
     */
    public Location toLocation(Location output) {
        return toLocation(this, output);
    }

    /**
     * Create a new {@link org.bukkit.util.Vector} from the coordinates.
     */
    public Vector toVector() {
        return toVector(this);
    }

    /**
     * Copy coordinate values into an output {@link org.bukkit.util.Vector}.
     *
     * @param output  The output {@link org.bukkit.util.Vector}.
     *
     * @return  The output location.
     */
    public Vector toVector(Vector output) {
        return toVector(this, output);
    }

    /**
     * Create a new {@link Coords3D} using the coordinate values.
     */
    public Coords3D to3D() {
        return new Coords3D(getX(), getY(), getZ());
    }

    /**
     * Create a new {@link Coords2Di} using the coordinate values.
     *
     * <p>Drops the Y coordinate.</p>
     */
    public Coords2Di to2Di() {
        return new Coords2Di(getX(), getZ());
    }

    /**
     * Copy the coordinate values to an output {@link org.bukkit.Location}.
     *
     * @param output  The output {@link org.bukkit.Location}.
     *
     * @return  The output {@link org.bukkit.Location}.
     */
    @Override
    public Location copyTo(Location output) {
        return copyTo(this, output);
    }

    /**
     * Copy the coordinate values to an output {@link org.bukkit.Location}.
     *
     * @param world   The {@link org.bukkit.World} to put into the output {@link org.bukkit.Location}.
     * @param output  The output {@link org.bukkit.Location}.
     *
     * @return  The output {@link org.bukkit.Location}.
     */
    @Override
    public Location copyTo(@Nullable World world, Location output) {
        return copyTo(this, world, output);
    }

    @Override
    public void serialize(IDataNode dataNode) {
        super.serialize(dataNode);
        dataNode.set("y", _y);
    }

    @Override
    public void deserialize(IDataNode dataNode) throws DeserializeException {
        super.deserialize(dataNode);
        _y = dataNode.getInteger("y");
        _canSeal = true;
        seal();
    }

    @Override
    public void serialize(IByteWriter writer) throws IOException {
        super.serialize(writer);
        writer.write(_y);
    }

    @Override
    public void deserialize(IByteReader reader)
            throws IOException, ClassNotFoundException, InstantiationException {

        super.deserialize(reader);
        _y = reader.getInteger();
        _canSeal = true;
        seal();
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ _y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ICoords3Di) {
            ICoords3Di other = (ICoords3Di)obj;

            return other.getX() == getX() &&
                    other.getY() == _y &&
                    other.getZ() == getZ();
        }

        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " { x:" + getX() + ", y:" + _y + ", z:" + getZ() + '}';
    }

    /**
     * Set the Y coordinate.
     *
     * @param y  The Y coordinate.
     *
     * @throws java.lang.IllegalStateException if the object is immutable.
     */
    protected void setY(int y) {
        if (isImmutable())
            throw new IllegalStateException("Coordinate is immutable.");

        _y = y;
    }

    /**
     * Invoked to make the object immutable.
     */
    @Override
    protected void seal() {
        if (_canSeal)
            super.seal();
    }
}

