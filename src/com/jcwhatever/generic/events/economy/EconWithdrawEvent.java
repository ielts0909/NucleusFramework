/*
 * This file is part of GenericsLib for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.generic.events.economy;

import com.jcwhatever.generic.providers.economy.IAccount;
import com.jcwhatever.generic.utils.PreCon;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when money is withdrawn from an account using GenericsLib.
 */
public class EconWithdrawEvent extends Event {

	private static final HandlerList _handlers = new HandlerList();

	private final IAccount _account;
	private final double _originalAmount;
	private double _amount;

	/**
	 * Constructor.
	 *
	 * @param account  The account being withdrawn from.
	 * @param amount   The amount being withdrawn.
	 */
	public EconWithdrawEvent(IAccount account, double amount) {
		PreCon.notNull(account);

		_account = account;
		_originalAmount = amount;
		_amount = amount;
	}

	/**
	 * Get the account that is being withdrawn from.
	 */
	public IAccount getAccount() {
		return _account;
	}

	/**
	 * Get the amount being withdrawn when the event
	 * was first called.
	 */
	public double getOriginalAmount() {
		return _originalAmount;
	}

	/**
	 * Get the amount being withdrawn.
	 */
	public double getAmount() {
		return _amount;
	}

	/**
	 * Set the amount being withdrawn.
	 * .
	 * @param amount  The positive amount.
	 */
	public void setAmount(double amount) {
		PreCon.positiveNumber(amount);

		_amount = amount;
	}

	@Override
	public HandlerList getHandlers() {
		return _handlers;
	}

	public static HandlerList getHandlerList() {
		return _handlers;
	}
}