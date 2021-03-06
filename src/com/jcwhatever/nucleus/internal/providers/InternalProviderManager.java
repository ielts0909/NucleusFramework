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

package com.jcwhatever.nucleus.internal.providers;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.internal.providers.bankitems.BankItemsProvider;
import com.jcwhatever.nucleus.internal.providers.economy.NucleusEconomyProvider;
import com.jcwhatever.nucleus.internal.providers.economy.VaultEconomyBankProvider;
import com.jcwhatever.nucleus.internal.providers.economy.VaultEconomyProvider;
import com.jcwhatever.nucleus.internal.providers.friends.NucleusFriendsProvider;
import com.jcwhatever.nucleus.internal.providers.jail.NucleusJailProvider;
import com.jcwhatever.nucleus.internal.providers.kits.NucleusKitProvider;
import com.jcwhatever.nucleus.internal.providers.math.NucleusFastMathProvider;
import com.jcwhatever.nucleus.internal.providers.permissions.bukkit.BukkitProvider;
import com.jcwhatever.nucleus.internal.providers.permissions.vault.VaultProvider;
import com.jcwhatever.nucleus.internal.providers.selection.NucleusSelectionProvider;
import com.jcwhatever.nucleus.internal.providers.selection.WorldEditSelectionProvider;
import com.jcwhatever.nucleus.internal.providers.storage.JsonStorageProvider;
import com.jcwhatever.nucleus.internal.providers.storage.YamlStorageProvider;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.providers.IProvider;
import com.jcwhatever.nucleus.providers.IProviderManager;
import com.jcwhatever.nucleus.providers.ProviderType;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsProvider;
import com.jcwhatever.nucleus.providers.economy.IEconomyProvider;
import com.jcwhatever.nucleus.providers.friends.IFriendsProvider;
import com.jcwhatever.nucleus.providers.jail.IJailProvider;
import com.jcwhatever.nucleus.providers.kits.IKitProvider;
import com.jcwhatever.nucleus.providers.math.IFastMathProvider;
import com.jcwhatever.nucleus.providers.npc.INpcProvider;
import com.jcwhatever.nucleus.providers.permissions.IPermissionsProvider;
import com.jcwhatever.nucleus.providers.playerlookup.IPlayerLookupProvider;
import com.jcwhatever.nucleus.providers.regionselect.IRegionSelectProvider;
import com.jcwhatever.nucleus.providers.sql.ISqlProvider;
import com.jcwhatever.nucleus.providers.storage.IStorageProvider;
import com.jcwhatever.nucleus.storage.DataPath;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.storage.MemoryDataNode;
import com.jcwhatever.nucleus.storage.YamlDataNode;
import com.jcwhatever.nucleus.utils.DependencyRunner;
import com.jcwhatever.nucleus.utils.DependencyRunner.DependencyStatus;
import com.jcwhatever.nucleus.utils.DependencyRunner.IDependantRunnable;
import com.jcwhatever.nucleus.utils.DependencyRunner.IFinishHandler;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.future.FutureAgent;
import com.jcwhatever.nucleus.utils.observer.future.IFuture;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Internal provider manager implementation.
 */
public final class InternalProviderManager implements IProviderManager {

    private IDataNode _dataNode;

    // names of all providers that were found
    private Map<String, ProviderInfo> _providerInfo = new HashMap<>(25);
    private Multimap<ProviderType, String> _providerNamesByApi =
            MultimapBuilder.enumKeys(ProviderType.class).hashSetValues().build();

    // providers that need to be enabled
    private Set<IProvider> _toEnable = new HashSet<>(25);

    private volatile IPlayerLookupProvider _playerLookup;
    private volatile IFriendsProvider _friends;
    private volatile IPermissionsProvider _permissions;
    private volatile IRegionSelectProvider _regionSelect;
    private volatile IEconomyProvider _economy;
    private volatile IBankItemsProvider _bankItems;
    private volatile IFastMathProvider _math;
    private volatile INpcProvider _npc;
    private volatile IJailProvider _jail;
    private volatile IStorageProvider _defaultStorage;
    private volatile IKitProvider _kits;
    private volatile ISqlProvider _sql;

    private final YamlStorageProvider _yamlStorage = new YamlStorageProvider();

    // keyed to plugin name
    private final Map<String, IStorageProvider> _pluginStorage = new HashMap<>(35);

    // keyed to provider name
    private final Map<String, IStorageProvider> _storageProviders = new HashMap<>(10);

    private boolean _isProvidersLoading;

    public InternalProviderManager(boolean isTest) {
        _storageProviders.put(_yamlStorage.getInfo().getSearchName(), _yamlStorage);
        _defaultStorage = _yamlStorage;

        // register names of default providers
        addName(BankItemsProvider.NAME, ProviderType.BANK_ITEMS);
        addName(NucleusEconomyProvider.NAME, ProviderType.ECONOMY);
        addName(VaultEconomyProvider.NAME, ProviderType.ECONOMY);
        addName(NucleusFriendsProvider.NAME, ProviderType.FRIENDS);
        addName(NucleusJailProvider.NAME, ProviderType.JAIL);
        addName(BukkitProvider.NAME, ProviderType.PERMISSIONS);
        addName(VaultProvider.NAME, ProviderType.PERMISSIONS);
        addName(NucleusSelectionProvider.NAME, ProviderType.REGION_SELECT);
        addName(WorldEditSelectionProvider.NAME, ProviderType.REGION_SELECT);
        addName(YamlStorageProvider.NAME, ProviderType.STORAGE);
        addName(JsonStorageProvider.NAME, ProviderType.STORAGE);

        _dataNode = isTest
                ? new MemoryDataNode(Nucleus.getPlugin())
                : new YamlDataNode(Nucleus.getPlugin(), new DataPath("providers"));

        _dataNode.load();

        registerStorageProvider(new JsonStorageProvider());

        // setup preferred internal bank items
        String prefBankItems = getPreferred(ProviderType.BANK_ITEMS);
        if (BankItemsProvider.NAME.equalsIgnoreCase(prefBankItems) || prefBankItems == null) {
            _bankItems = add(new BankItemsProvider());
        }

        // setup preferred internal economy
        String prefEcon = getPreferred(ProviderType.ECONOMY);
        if (NucleusEconomyProvider.NAME.equalsIgnoreCase(prefEcon) || prefEcon == null) {
            _economy = add(new NucleusEconomyProvider(Nucleus.getPlugin()));
        }
        else if (VaultEconomyProvider.NAME.equalsIgnoreCase(prefEcon) &&
                VaultEconomyProvider.hasVaultEconomy()) {
            _economy = add(VaultEconomyBankProvider.hasBankEconomy()
                    ? new VaultEconomyBankProvider()
                    : new VaultEconomyProvider());
        }

        String prefMath = getPreferred(ProviderType.MATH);
        if (NucleusFastMathProvider.NAME.equalsIgnoreCase(prefMath) || prefMath == null) {
            _math = add(new NucleusFastMathProvider());
        }

        // setup preferred internal friends
        String prefFriends = getPreferred(ProviderType.FRIENDS);
        if (NucleusFriendsProvider.NAME.equalsIgnoreCase(prefFriends)) {
            _friends = add(new NucleusFriendsProvider());
        }

        // setup preferred internal jail
        String prefJail = getPreferred(ProviderType.JAIL);
        if (NucleusJailProvider.NAME.equalsIgnoreCase(prefJail)) {
            _jail = add(new NucleusJailProvider());
        }

        String prefKit = getPreferred(ProviderType.KITS);
        if (NucleusKitProvider.NAME.equalsIgnoreCase(prefKit)) {
            _kits = add(new NucleusKitProvider());
        }

        // setup preferred internal permissions
        String prefPerm = getPreferred(ProviderType.PERMISSIONS);
        if ((VaultProvider.NAME.equalsIgnoreCase(prefPerm)) &&
                VaultProvider.hasVaultPermissions()) {
            _permissions = add(VaultProvider.getVaultProvider());
        }
        else if (BukkitProvider.NAME.equalsIgnoreCase(prefPerm)) {
            _permissions = add(new BukkitProvider());
        }

        // setup preferred internal region selection
        String prefSelect = getPreferred(ProviderType.REGION_SELECT);
        if (NucleusSelectionProvider.NAME.equalsIgnoreCase(prefSelect) &&
                !WorldEditSelectionProvider.isWorldEditInstalled()) {
            _regionSelect = add(new NucleusSelectionProvider());
        }
        else if (WorldEditSelectionProvider.NAME.equalsIgnoreCase(prefSelect) &&
                WorldEditSelectionProvider.isWorldEditInstalled()) {
            _regionSelect = add(new WorldEditSelectionProvider());
        }
    }

    /**
     * Enable all providers.
     */
    public IFuture enableProviders() {

        final FutureAgent agent = new FutureAgent();

        DependencyRunner<IDependantRunnable> runner =
                new DependencyRunner<IDependantRunnable>(Nucleus.getPlugin());

        Iterator<IProvider> iterator = _toEnable.iterator();
        while (iterator.hasNext()) {

            IProvider provider = iterator.next();

            try {
                provider.registerTypes();
            }
            catch (Throwable e) {
                e.printStackTrace();
                iterator.remove();
            }
        }

        for (final IProvider provider : _toEnable) {
            try {
                provider.enable();
            }
            catch (Throwable e) {
                e.printStackTrace();
                continue;
            }

            runner.add(new IDependantRunnable() {
                @Override
                public DependencyStatus getDependencyStatus() {
                    return provider.isLoaded()
                            ? DependencyStatus.READY
                            : DependencyStatus.NOT_READY;
                }

                @Override
                public void run() {
                    // do nothing
                }
            });
        }

        runner.onFinish(new IFinishHandler<IDependantRunnable>() {
            @Override
            public void onFinish(List<IDependantRunnable> notRun) {
                agent.success();
            }
        });

        _toEnable.clear();

        runner.start();

        return agent.getFuture();
    }

    /**
     * Add a provider.
     *
     * @param provider  The provider to add.
     *
     * @return  True if the provider was added. Otherwise false.
     */
    public boolean addProvider(IProvider provider) {
        PreCon.notNull(provider);
        PreCon.isValid(_isProvidersLoading, "Cannot set providers outside of provider load time.");

        boolean isAdded = false;

        if (provider instanceof IPlayerLookupProvider) {
            addName(provider, ProviderType.PLAYER_LOOKUP);
            if (remove(_playerLookup, ProviderType.PLAYER_LOOKUP)) {
                _playerLookup = add(provider);
                isAdded = true;
            }
        }

        if (provider instanceof IFriendsProvider) {
            addName(provider, ProviderType.FRIENDS);
            if (remove(_friends, ProviderType.FRIENDS)) {
                _friends = add(provider);
                isAdded = true;
            }
        }

        if (provider instanceof IPermissionsProvider) {
            addName(provider, ProviderType.PERMISSIONS);
            if (remove(_permissions, ProviderType.PERMISSIONS)) {
                _permissions = add(provider);
                isAdded = true;
            }
        }

        if (provider instanceof IRegionSelectProvider) {
            addName(provider, ProviderType.REGION_SELECT);
            if (remove(_regionSelect, ProviderType.REGION_SELECT)) {
                _regionSelect = add(provider);
                isAdded = true;
            }
        }

        if (provider instanceof IBankItemsProvider) {
            addName(provider, ProviderType.BANK_ITEMS);
            if (remove(_bankItems, ProviderType.BANK_ITEMS)) {
                _bankItems = add(_bankItems);
                isAdded = true;
            }
        }

        if (provider instanceof IEconomyProvider) {
            addName(provider, ProviderType.ECONOMY);
            if (remove(_economy, ProviderType.ECONOMY)) {
                add(_economy);
                _economy = (IEconomyProvider)provider;
                isAdded = true;
            }
        }

        if (provider instanceof IStorageProvider) {
            addName(provider, ProviderType.STORAGE);
            if (remove(_defaultStorage, ProviderType.STORAGE)) {
                _defaultStorage = add(provider);
            }
            registerStorageProvider((IStorageProvider) provider);
            isAdded = true;
        }

        if (provider instanceof INpcProvider) {
            addName(provider, ProviderType.NPC);
            if (remove(_npc, ProviderType.NPC)) {
                _npc = add(provider);
                isAdded = true;
            }
        }

        if (provider instanceof IJailProvider) {
            addName(provider, ProviderType.JAIL);
            if (remove(_jail, ProviderType.JAIL)) {
                _jail = add(provider);
                isAdded = true;
            }
        }

        if (provider instanceof IKitProvider) {
            addName(provider, ProviderType.KITS);
            if (remove(_kits, ProviderType.KITS)) {
                _kits = add(provider);
                isAdded = true;
            }
        }

        if (provider instanceof ISqlProvider) {
            addName(provider, ProviderType.SQL);
            if (remove(_sql, ProviderType.SQL)) {
                _sql = add(provider);
                isAdded = true;
            }
        }

        return isAdded;
    }

    @Override
    public boolean setPreferred(ProviderType providerType, @Nullable String providerName) {
        PreCon.notNull(providerType);

        _dataNode.set("preferred." + providerType.getName(), providerName);
        _dataNode.save();

        return true;
    }

    @Nullable
    @Override
    public String getPreferred(ProviderType providerType) {
        PreCon.notNull(providerType);

        return _dataNode.getString("preferred." + providerType.getName());
    }

    @Override
    public <T extends IProvider> T get(ProviderType providerType) {
        PreCon.notNull(providerType);

        @SuppressWarnings("unchecked")
        Class<T> apiType = (Class<T>)providerType.getApiType();

        IProvider provider;

        switch (providerType) {
            case BANK_ITEMS:
                provider = _bankItems;
                break;
            case ECONOMY:
                provider = _economy;
                break;
            case FRIENDS:
                provider = _friends;
                break;
            case JAIL:
                provider = _jail;
                break;
            case KITS:
                provider = _kits;
                break;
            case MATH:
                provider = _math;
                break;
            case NPC:
                provider = _npc;
                break;
            case PERMISSIONS:
                provider = _permissions;
                break;
            case PLAYER_LOOKUP:
                provider = _playerLookup;
                break;
            case REGION_SELECT:
                provider = _regionSelect;
                break;
            case STORAGE:
                provider = _defaultStorage;
                break;
            case SQL:
                provider = _sql;
                break;
            default:
                throw new AssertionError();
        }

        if (provider == null)
            return null;

        return apiType.cast(provider);
    }

    @Nullable
    @Override
    public ProviderType getType(String name) {
        PreCon.notNull(name);

        ProviderInfo info = _providerInfo.get(name.toLowerCase());
        if (info == null)
            return null;

        return info.type;
    }

    @Override
    public IPlayerLookupProvider getPlayerLookup() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_playerLookup == null) {
            _playerLookup = new InternalPlayerLookupProvider(Nucleus.getPlugin());
            _playerLookup.registerTypes();
            _playerLookup.enable();
        }

        return _playerLookup;
    }

    @Override
    public IFriendsProvider getFriends() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_friends == null) {
            _friends = new NucleusFriendsProvider();
            _friends.registerTypes();
            _friends.enable();
        }

        return _friends;
    }

    @Override
    public IPermissionsProvider getPermissions() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_permissions == null) {
            _permissions = VaultProvider.hasVaultPermissions()
                    ? VaultProvider.getVaultProvider()
                    : new BukkitProvider();
            _permissions.registerTypes();
            _permissions.enable();
        }

        return _permissions;
    }

    @Override
    public IRegionSelectProvider getRegionSelection() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_regionSelect == null) {
            _regionSelect = new NucleusSelectionProvider();
            _regionSelect.registerTypes();
            _regionSelect.enable();
        }

        return _regionSelect;
    }

    @Override
    public IBankItemsProvider getBankItems() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_bankItems == null) {
            _bankItems = new BankItemsProvider();
            _bankItems.registerTypes();
            _bankItems.enable();
        }

        return _bankItems;
    }

    @Override
    public IEconomyProvider getEconomy() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_economy == null) {
            _economy = VaultEconomyProvider.hasVaultEconomy()
                    ? VaultEconomyBankProvider.hasBankEconomy()
                    ? new VaultEconomyBankProvider()
                    : new VaultEconomyProvider()
                    : new NucleusEconomyProvider(Nucleus.getPlugin());
            _economy.registerTypes();
            _economy.enable();
        }

        return _economy;
    }

    @Nullable
    @Override
    public ISqlProvider getSql() {
        return _sql;
    }

    @Nullable
    @Override
    public INpcProvider getNpcs() {
        return _npc;
    }

    @Override
    public IJailProvider getJails() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_jail == null) {
            _jail = new NucleusJailProvider();
            _jail.registerTypes();
            _jail.enable();
        }

        return _jail;
    }

    @Override
    public IKitProvider getKits() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_kits == null) {
            _kits = new NucleusKitProvider();
            _kits.registerTypes();
            _kits.enable();
        }

        return _kits;
    }

    @Override
    public IStorageProvider getStorage() {
        return _defaultStorage != null ? _defaultStorage : _yamlStorage;
    }

    @Override
    public IStorageProvider getStorage(Plugin plugin) {
        PreCon.notNull(plugin);

        synchronized (_pluginStorage) {

            IStorageProvider pluginProvider = _pluginStorage.get(plugin.getName().toLowerCase());
            return pluginProvider != null ? pluginProvider : getStorage();
        }
    }

    @Override
    public void setStorage(Plugin plugin, IStorageProvider storageProvider) {
        PreCon.notNull(plugin);
        PreCon.notNull(storageProvider);

        IDataNode dataNode = _dataNode.getNode("storage");

        synchronized (_pluginStorage) {

            List<String> pluginNames = dataNode.getStringList(storageProvider.getInfo().getName(),
                    new ArrayList<String>(5));

            assert pluginNames != null;

            pluginNames.add(plugin.getName());
            dataNode.set(storageProvider.getInfo().getName(), pluginNames);
        }

        dataNode.save();
    }

    @Nullable
    @Override
    public IStorageProvider getStorage(String name) {
        PreCon.notNullOrEmpty(name);

        return _storageProviders.get(name.toLowerCase());
    }

    @Override
    public List<IStorageProvider> getStorageProviders() {
        return new ArrayList<>(_storageProviders.values());
    }

    @Override
    public <T extends Collection<IStorageProvider>> T getStorageProviders(T output) {
        PreCon.notNull(output);

        output.addAll(_storageProviders.values());
        return output;
    }

    @Override
    public IFastMathProvider getMath() {

        // lazy load default provider to prevent it from being loaded
        // if never used, specify default provider as the preferred
        // provider to force load at startup
        if (_math == null) {
            _math = new NucleusFastMathProvider();
            _math.registerTypes();
            _math.enable();
        }
        return _math;
    }

    @Override
    public Collection<String> getNames() {
        return getNames(new ArrayList<String>(_providerInfo.size()));
    }

    @Override
    public <T extends Collection<String>> T getNames(T output) {
        PreCon.notNull(output);

        for (ProviderInfo info : _providerInfo.values())
            output.add(info.name);

        return output;
    }

    @Override
    public Collection<String> getNames(ProviderType type) {
        PreCon.notNull(type);

        return new HashSet<>(_providerNamesByApi.get(type));
    }

    @Override
    public <T extends Collection<String>> T getNames(ProviderType providerType, T output) {
        PreCon.notNull(providerType);
        PreCon.notNull(output);

        output.addAll(_providerNamesByApi.get(providerType));
        return output;
    }

    /**
     * Register a storage provider.
     *
     * @param storageProvider  The storage provider to register
     */
    public void registerStorageProvider(IStorageProvider storageProvider) {
        PreCon.notNull(storageProvider);

        IStorageProvider previous = _storageProviders.put(
                storageProvider.getInfo().getSearchName(), storageProvider);

        if (previous instanceof IDisposable && previous != storageProvider) {
            ((IDisposable) previous).dispose();
        }

        IDataNode dataNode = _dataNode.getNode("storage");

        List<String> pluginNames = dataNode.getStringList(storageProvider.getInfo().getName(), null);
        if (pluginNames != null) {
            for (String pluginName : pluginNames) {
                _pluginStorage.put(pluginName.toLowerCase(), storageProvider);
            }
        }
    }

    /**
     * Set the loading flag.
     *
     * <p>Loads required default providers if not set when flag is set to false.</p>
     */
    void setLoading(boolean isLoading) {
        _isProvidersLoading = isLoading;

        // load default providers that must be loaded immediately.
        if (!isLoading) {

            if (_regionSelect == null) {
                _regionSelect = add(WorldEditSelectionProvider.isWorldEditInstalled()
                        ? new WorldEditSelectionProvider()
                        : new NucleusSelectionProvider());
            }

            if (_playerLookup == null)
                _playerLookup = add(new InternalPlayerLookupProvider(Nucleus.getPlugin()));
        }
    }

    /*
     * Remove provider from load list, returns true unless specified provider is the
     * preferred provider, in which case the provider is not removed.
     */
    private boolean remove(@Nullable IProvider provider, ProviderType providerType) {
        if (provider == null)
            return true;

        String preferred = _dataNode.getString("preferred." + providerType.getName());
        if (preferred != null && provider.getInfo().getName().equalsIgnoreCase(preferred))
            return false;

        _toEnable.remove(provider);
        return true;
    }

    /*
     * Add provider to load list.
     */
    private <T extends IProvider> T add(IProvider provider) {
        _toEnable.add(provider);

        @SuppressWarnings("unchecked")
        T cast = (T)provider;

        return cast;
    }

    /*
     * Add providers ProviderInfo and type->name lookup
     */
    private void addName(IProvider provider, ProviderType type) {
        _providerInfo.put(provider.getInfo().getSearchName(), new ProviderInfo(provider, type));
        _providerNamesByApi.put(type, provider.getInfo().getName());
    }

    /*
     * Add providers ProviderInfo and type->name lookup
     */
    private void addName(String name, ProviderType type) {
        _providerInfo.put(name.toLowerCase(), new ProviderInfo(name, type));
        _providerNamesByApi.put(type, name);
    }

    private static class ProviderInfo {
        String name;
        String version;
        ProviderType type;

        ProviderInfo(IProvider provider, ProviderType type) {
            this.name = provider.getInfo().getName();
            this.version = provider.getInfo().getVersion();
            this.type = type;
        }

        ProviderInfo(String name, ProviderType type) {
            this.name = name;
            this.version = Nucleus.getPlugin().getDescription().getVersion();
            this.type = type;
        }
    }
}
