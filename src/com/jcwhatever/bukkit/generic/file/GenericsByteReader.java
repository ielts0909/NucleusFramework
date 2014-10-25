package com.jcwhatever.bukkit.generic.file;

import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Read bytes from a stream. Bytes from the stream need to have been
 * generated by {@code GenericsByteWriter} in order to be read.
 */
public class GenericsByteReader extends InputStream {

    private final InputStream _stream;
    private final byte[] _buffer = new byte[1024];
    private long _bytesRead = 0;

    private int _booleanReadCount = 7; // resets to 7
    private byte[] _booleanBuffer = new byte[1];
    private final byte[] _booleanFlags = new byte[] { 1, 2, 4, 8, 16, 32, 64 };

    public GenericsByteReader(InputStream stream) throws IOException {
        PreCon.notNull(stream);

        _stream = stream;
    }

    /**
     * Get the number of bytes read so far.
     */
    public long getBytesRead() {
        return _bytesRead;
    }

    @Override
    public int read() throws IOException {

        resetBooleanBuffer();

        _bytesRead++;
        return _stream.read();
    }

    /**
     * Skip over a number of bytes without returning them.
     *
     * @param byteDistance  The number of bytes to skip.
     *
     * @return  The number of bytes skipped.
     *
     * @throws IOException
     */
    @Override
    public long skip(long byteDistance) throws IOException {
        return _stream.skip(byteDistance);
    }

    /**
     * Get a boolean.
     *
     * @throws IOException
     */
    public boolean getBoolean() throws IOException {

        if (_booleanReadCount == 7) {
            _bytesRead += (byte)_stream.read(_booleanBuffer, 0, 1);
            _booleanReadCount = 0;
        }

        boolean result = (_booleanBuffer[0] & _booleanFlags[_booleanReadCount]) == _booleanFlags[_booleanReadCount];

        _booleanReadCount++;

        return result;
    }

    /**
     * Get the next byte.
     *
     * @throws IOException
     */
    public byte getByte() throws IOException {

        resetBooleanBuffer();

        _bytesRead += _stream.read(_buffer, 0, 1);
        return _buffer[0];
    }

    /**
     * Get the next group of bytes as an integer.
     *
     * @throws IOException
     */
    public int getInteger() throws IOException {

        resetBooleanBuffer();

        _bytesRead += (long)_stream.read(_buffer, 0, 4);
        return ((_buffer[0] & 255) << 24)
                + ((_buffer[1] & 255) << 16)
                + ((_buffer[2] & 255) << 8)
                + (_buffer[3] & 255);
    }

    /**
     * Get the next group of bytes as long value.
     *
     * @throws IOException
     */
    public long getLong() throws IOException {

        resetBooleanBuffer();

        _bytesRead+=(long)_stream.read(_buffer, 0, 8);
        return ((_buffer[0] & 255) << 56)
                + ((_buffer[1] & 255) << 48)
                + ((_buffer[2] & 255) << 40)
                + ((_buffer[3] & 255) << 32)
                + ((_buffer[4] & 255) << 24)
                + ((_buffer[5] & 255) << 16)
                + ((_buffer[6] & 255) << 8)
                + (_buffer[7] & 255);
    }

    /**
     * Get the next group of bytes as a string.
     *
     * @throws IOException
     */
    public String getString() throws IOException {

        resetBooleanBuffer();

        int len = getInteger();
        if (len == -1)
            return null;

        if (len == 0)
            return "";

        if (len >= _buffer.length) {
            byte[] buffer = new byte[len];
            _bytesRead += (long) _stream.read(buffer, 0, len);
            return new String(buffer, 0, len, "UTF-8");
        }
        else {
            _bytesRead += (long) _stream.read(_buffer, 0, len);
            return new String(_buffer, 0, len, "UTF-8");
        }

    }

    /**
     * Get the next group of bytes as a float value.
     *
     * @throws IOException
     */
    public float getFloat() throws IOException {
        String str = getString();
        return Float.parseFloat(str);
    }

    /**
     * Get the next group of bytes as a double value.
     *
     * @throws IOException
     */
    public double getDouble() throws IOException {
        String str = getString();
        return Double.parseDouble(str);
    }

    /**
     * Get the next group of bytes as an enum.
     *
     * @param enumClass  The enum class.
     *
     * @param <T>        The enum type.
     */
    public <T extends Enum<T>> T getEnum(Class<T> enumClass) throws IOException {
        String constantName = getString();

        for (T e : enumClass.getEnumConstants()) {
            if (e.name().equals(constantName))
                return e;
        }

        throw new IllegalStateException("The enum name retrieved is not a valid constant name for enum type: " + enumClass.getName());
    }

    /**
     * Get the next group of bytes as a location.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Location getLocation() throws IOException {

        String worldName = getString();
        double x = getDouble();
        double y = getDouble();
        double z = getDouble();
        float yaw = getFloat();
        float pitch = getFloat();

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    /**
     * Get the next group of bytes as an item stack.
     *
     * @throws IOException
     */
    @Nullable
    public ItemStack getItemStack() throws IOException {

        boolean isNull = getBoolean();
        if (isNull)
            return null;

        // read basic data
        Material type = getEnum(Material.class);
        byte data = getByte();
        int amount = getInteger();
        short durabality = (short)getInteger();

        ItemStack result = new ItemStack(type, amount, durabality, data);

        // read enchantments
        int enchantSize = getInteger();

        for (int i=0; i < enchantSize; i++) {
            String enchantName = getString();
            int level = getInteger();

            ItemStackHelper.addEnchantment(result, enchantName, level);
        }

        ItemMeta meta = result.getItemMeta();

        // read display name
        String displayName = getString();
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        // read lore
        int loreSize = getInteger();
        if (loreSize > 0) {
            List<String> lore = new ArrayList<>(loreSize);

            for (int i = 0; i < loreSize; i++) {
                lore.add(getString());
            }

            meta.setLore(lore);
        }

        // read color
        boolean hasColor = getBoolean();
        if (hasColor) {
            ItemStackHelper.setColor(result, getInteger());
        }

        return result;
    }

    /**
     * Get an {@code IGenericsSerializable} object.
     *
     * @param objectClass  The object class.
     * @param <T>          The object type.
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    @Nullable
    public <T extends IGenericsSerializable> T getGenerics(Class<T> objectClass)
            throws IllegalAccessException, InstantiationException, IOException, ClassNotFoundException {
        PreCon.notNull(objectClass);

        boolean isNull = getBoolean();
        if (isNull)
            return null;

        T object = objectClass.newInstance();

        object.deserializeFromBytes(this);

        return object;
    }

    /**
     * Deserialize an object from the next set of bytes.
     *
     * @param objectClass  The object class.
     * @param <T>          The object type.
     *
     * @return The deserialized object or null if the object was written as null.
     *
     * @throws IOException
     */
    @Nullable
    public <T extends Serializable> T getObject(Class<T> objectClass) throws IOException, ClassNotFoundException {
        PreCon.notNull(objectClass);

        boolean isNull = getBoolean();

        if (isNull)
            return null;

        resetBooleanBuffer();

        ObjectInputStream objectStream = new ObjectInputStream(this);

        Object object = objectStream.readObject();

        if (!object.getClass().isAssignableFrom(objectClass))
            throw new ClassNotFoundException("The object returned by the stream is not of the specified class: " + objectClass.getName());

        return objectClass.cast(object);
    }

    /**
     * Close the stream.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        _stream.close();
        super.close();
    }

    private void resetBooleanBuffer() {
        _booleanReadCount = 7;
        _booleanBuffer[0] = 0;
    }
}
