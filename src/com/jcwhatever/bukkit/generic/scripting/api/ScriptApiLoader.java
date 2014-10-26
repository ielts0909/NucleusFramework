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


package com.jcwhatever.bukkit.generic.scripting.api;

import com.jcwhatever.bukkit.generic.scripting.IEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.IScriptApiInfo;
import com.jcwhatever.bukkit.generic.scripting.ScriptApiRepo;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import org.bukkit.plugin.Plugin;

/**
 * Add script support for loading script api from the script api repository.
 */
@IScriptApiInfo(
        variableName = "api",
        description = "Provide scripts with an API for loading other API's from the script api repository.")
public class ScriptApiLoader extends GenericsScriptApi {

    /**
     * Constructor.
     *
     * @param plugin The owning plugin
     */
    public ScriptApiLoader(Plugin plugin) {
        super(plugin);
    }

    /**
     * Get an api object to use in a script.
     *
     * @param script  The script the api object is for.
     */
    @Override
    public IScriptApiObject getApiObject(IEvaluatedScript script) {
        return new ApiObject(getPlugin(), script);
    }

    /**
     * Reset api objects and release resources.
     */
    @Override
    public void reset() {
        // do nothing
    }


    public static class ApiObject implements IScriptApiObject {

        private final Plugin _plugin;
        private final IEvaluatedScript _evaluated;

        /**
         * Constructor.
         *
         * @param evaluated  The evaluated script the api object is for.
         */
        ApiObject (Plugin plugin, IEvaluatedScript evaluated) {
            _plugin = plugin;
            _evaluated = evaluated;
        }

        /**
         * Reset object and release resources.
         */
        @Override
        public void reset() {
            // do nothing
        }

        /**
         * Add an api object from the script api repository.
         *
         * @param owningPluginName  The name of the api owning plugin.
         * @param apiName           The variable name of the api.
         *
         * @return  True if the api was found and included.
         */
        public boolean add(String owningPluginName, String apiName) {
            PreCon.notNull(owningPluginName);
            PreCon.notNullOrEmpty(apiName);

            IScriptApi api = ScriptApiRepo.getApi(_plugin, owningPluginName, apiName);
            if (api == null)
                return false;

            _evaluated.addScriptApi(api);

            return true;
        }
    }
}
