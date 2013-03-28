/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import griffon.plugins.blueprints.BlueprintsConnector
import griffon.plugins.blueprints.BlueprintsContributionHandler
import griffon.plugins.blueprints.BlueprintsEnhancer

/**
 * @author Andres Almiray
 */
class BlueprintsGriffonAddon {
    void addonPostInit(GriffonApplication app) {
        ConfigObject config = BlueprintsConnector.instance.createConfig(app)
        BlueprintsConnector.instance.connect(app, config)
        def types = app.config.griffon?.blueprints?.injectInto ?: ['controller']
        for (String type : types) {
            for (GriffonClass gc : app.artifactManager.getClassesOfType(type)) {
                if (BlueprintsContributionHandler.isAssignableFrom(gc.clazz)) continue
                BlueprintsEnhancer.enhance(gc.metaClass)
            }
        }
    }

    Map events = [
        ShutdownStart: { app ->
            ConfigObject config = BlueprintsConnector.instance.createConfig(app)
            BlueprintsConnector.instance.disconnect(app, config)
        }
    ]
}
