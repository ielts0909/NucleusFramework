/* This file is part of GenericsLib for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.generic.player.collections;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerQueue implements Queue<Player>, IPlayerCollection{

	private final Queue<Player> _queue = new LinkedList<Player>();
    private final PlayerCollectionListener _listener;
		
	public PlayerQueue() {
		_listener = PlayerCollectionListener.get();
	}
		
	@Override
	public synchronized boolean addAll(Collection<? extends Player> players) {

        for (Player p : players) {
            _listener.addPlayer(p, this);
        }

		return _queue.addAll(players);
	}

	@Override
	public synchronized void clear() {

        while (!_queue.isEmpty()) {
            Player p = _queue.remove();

            _listener.removePlayer(p, this);
        }

		_queue.clear();
	}

	@Override
	public synchronized boolean contains(Object arg) {
		return _queue.contains(arg);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> items) {
		return _queue.containsAll(items);
	}

	@Override
	public synchronized boolean isEmpty() {
		return _queue.isEmpty();
	}

	@Override
	public synchronized Iterator<Player> iterator() {
		return new Iter(this);
	}

	@Override
	public synchronized boolean remove(Object item) {

		if (_queue.remove(item)) {
            if (item instanceof Player && !_queue.contains(item)) {
                _listener.removePlayer((Player) item, this);
            }
            return true;
        }

        return false;
	}

	@Override
	public synchronized boolean removeAll(Collection<?> items) {
        for (Object obj : items) {
            if (obj instanceof Player) {
                _listener.removePlayer((Player) obj, this);
            }
        }
		return _queue.removeAll(items);
	}

	@Override
	public synchronized boolean retainAll(Collection<?> items) {

        LinkedList<Player> temp = new LinkedList<>(_queue);
        if (temp.removeAll(items)) {
            while (!temp.isEmpty()) {
                _listener.removePlayer(temp.remove(), this);
            }
        }

		return _queue.retainAll(items);
	}

	@Override
	public synchronized int size() {
		return _queue.size();
	}

	@Override
	public synchronized Object[] toArray() {
		return _queue.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] array) {
		return _queue.toArray(array);
	}

	@Override
	public synchronized boolean add(Player p) {
		if (_queue.add(p)) {
            _listener.addPlayer(p, this);
            return true;
        }
        return false;
	}

	@Override
	public synchronized Player element() {
		return _queue.element();
	}

	@Override
	public synchronized boolean offer(Player p) {
		if (_queue.offer(p)) {
            _listener.addPlayer(p, this);
            return true;
        }
        return false;
	}

	@Override
	public synchronized Player peek() {
		return _queue.peek();
	}

	@Override
	public synchronized Player poll() {
		Player p = _queue.poll();
        if (p != null && !_queue.contains(p)) {
            _listener.removePlayer(p, this);
        }
        return p;
	}

	@Override
	public synchronized Player remove() {
        Player p = _queue.remove();
        if (p != null && !_queue.contains(p)) {
            _listener.removePlayer(p, this);
        }
        return p;
	}
	
	@Override
	public synchronized void removePlayer(Player p) {
		_queue.remove(p);
	}

    /**
     * Call to remove references that prevent
     * the garbage collector from collecting
     * the instance after it is not longer needed.
     */
    @Override
    public void dispose() {
        clear();
    }

    private final class Iter implements Iterator<Player> {
		
		Iterator<Player> _iterator;
        Player _current = null;
        PlayerQueue _parent;
		 
		public Iter(PlayerQueue parent) {
			_iterator = new LinkedList<Player>(_queue).iterator();
            _parent = parent;
		}
		
		@Override
		public boolean hasNext() {
			return _iterator.hasNext();
		}

		@Override
		public Player next() {
			_current = _iterator.next();
            return _current;
		}

		@Override
		public void remove() {
			_iterator.remove();
            if (_current != null && !_queue.contains(_current)) {
                _listener.removePlayer(_current, _parent);
            }


		}
		
	}

}
