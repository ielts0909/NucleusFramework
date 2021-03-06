package com.jcwhatever.nucleus.collections.players;

import com.jcwhatever.nucleus.NucleusTest;
import com.jcwhatever.v1_8_R3.BukkitTester;
import org.bukkit.entity.Player;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/*
 * 
 */
public abstract class AbstractPlayerMapTest<V> {

    /**
     * Make sure Nucleus and Bukkit are initialized.
     */
    @BeforeClass
    public static void init() {
        NucleusTest.init();
    }

    /**
     * Invoked to get a new collection instance for testing.
     */
    protected abstract Map<UUID, V> getMap();

    /**
     * Make sure player is removed when they log out.
     */
    @Test
    public void testPlayerRemoved1() {

        Map<UUID, V> map = getMap();
        assertEquals(0, map.size());

        Player player1 = BukkitTester.login("playerCollectionTest1");
        Player player2 = BukkitTester.login("playerCollectionTest2");

        map.put(player1.getUniqueId(), null);
        map.put(player2.getUniqueId(), null);

        assertEquals(true, map.containsKey(player1.getUniqueId()));
        assertEquals(true, map.containsKey(player2.getUniqueId()));
        assertEquals(2, map.size());

        BukkitTester.pause(20);

        // make sure players are still in map after waiting 20 ticks
        assertEquals(true, map.containsKey(player1.getUniqueId()));
        assertEquals(true, map.containsKey(player2.getUniqueId()));
        assertEquals(2, map.size());

        // logout player 1
        BukkitTester.logout("playerCollectionTest1");
        BukkitTester.pause(3);

        // make sure player1 was removed
        assertEquals(false, map.containsKey(player1.getUniqueId()));
        assertEquals(true, map.containsKey(player2.getUniqueId()));
        assertEquals(1, map.size());

        // kick player 2
        BukkitTester.kick("playerCollectionTest2");
        BukkitTester.pause(3);

        // make sure player2 was removed
        assertEquals(false, map.containsKey(player1.getUniqueId()));
        assertEquals(false, map.containsKey(player2.getUniqueId()));
        assertEquals(0, map.size());
    }

}
