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

package griffon.plugins.blueprints

import com.tinkerpop.blueprints.Graph

import griffon.core.GriffonApplication
import griffon.util.ApplicationClassLoader
import griffon.plugins.blueprints.factory.GraphFactory

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static griffon.util.ConfigUtils.loadConfigWithI18n

/**
 * @author Andres Almiray
 */
@Singleton
class BlueprintsConnector {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(BlueprintsConnector)
    
    private bootstrap

    ConfigObject createConfig(GriffonApplication app) {
        if (!app.config.pluginConfig.blueprints) {
            app.config.pluginConfig.blueprints = loadConfigWithI18n('BlueprintsConfig')
        }
        app.config.pluginConfig.blueprints
    }

    private ConfigObject narrowConfig(ConfigObject config, String graphName) {
        if (config.containsKey('graph') && graphName == DEFAULT) {
            return config.graph
        } else if (config.containsKey('graphs')) {
            return config.graphs[graphName]
        }
        return config
    }

    Graph connect(GriffonApplication app, ConfigObject config, String graphName = DEFAULT) {
        if (GraphHolder.instance.isGraphConnected(graphName)) {
            return GraphHolder.instance.getGraph(graphName)
        }

        config = narrowConfig(config, graphName)
        app.event('BlueprintsConnectStart', [config, graphName])
        Graph graph = createGraph(app, config, graphName)
        GraphHolder.instance.setGraph(graphName, graph)
        bootstrap = app.class.classLoader.loadClass('BootstrapBlueprints').newInstance()
        bootstrap.metaClass.app = app
        resolveBlueprintsProvider(app).withBlueprints(graphName) { gName, g -> bootstrap.init(gName, g) }
        app.event('BlueprintsConnectEnd', [graphName, graph])
        graph
    }

    void disconnect(GriffonApplication app, ConfigObject config, String graphName = DEFAULT) {
        if (GraphHolder.instance.isGraphConnected(graphName)) {
            config = narrowConfig(config, graphName)
            Graph graph = GraphHolder.instance.getGraph(graphName)
            app.event('BlueprintsDisconnectStart', [config, graphName, graph])
            resolveBlueprintsProvider(app).withBlueprints(graphName) { gName, g -> bootstrap.destroy(gName, g) }
            app.event('BlueprintsDisconnectEnd', [config, graphName])
            GraphHolder.instance.disconnectGraph(graphName)
        }
    }

    BlueprintsProvider resolveBlueprintsProvider(GriffonApplication app) {
        def blueprintsProvider = app.config.blueprintsProvider
        if (blueprintsProvider instanceof Class) {
            blueprintsProvider = blueprintsProvider.newInstance()
            app.config.blueprintsProvider = blueprintsProvider
        } else if (!blueprintsProvider) {
            blueprintsProvider = DefaultBlueprintsProvider.instance
            app.config.blueprintsProvider = blueprintsProvider
        }
        blueprintsProvider
    }

    private Graph createGraph(GriffonApplication app, ConfigObject config, String graphName) {
        String factoryClassName = config.factory
        Class factoryClass = ApplicationClassLoader.get().loadClass(factoryClassName)
        GraphFactory factory = app.newInstance(factoryClass, '')
        factory.create(app, graphName, config.props)
    }
}
