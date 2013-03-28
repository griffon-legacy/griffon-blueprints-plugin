/*
 * Copyright 2009-2013 the original author or authors.
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
import griffon.util.ApplicationHolder
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
class GraphHolder {
    private static final String DEFAULT = 'default'
    private static final Object[] LOCK = new Object[0]
    private final Map<String, Graph> graphs = [:]

    private static final GraphHolder INSTANCE

    static {
        INSTANCE = new GraphHolder()
    }

    static GraphHolder getInstance() {
        INSTANCE
    }

    String[] getGraphNames() {
        List<String> graphNames = new ArrayList<String>()
        graphNames.addAll(graphs.keySet())
        graphNames.toArray(new String[graphNames.size()])
    }

    Graph getGraph(String graphName = DEFAULT) {
        if(isBlank(graphName)) graphName = DEFAULT
        retrieveGraph(graphName)
    }

    void setGraph(String graphName = DEFAULT, Graph graph) {
        if(isBlank(graphName)) graphName = DEFAULT
        storeGraph(graphName, graph)
    }

    boolean isGraphConnected(String graphName) {
        if(isBlank(graphName)) graphName = DEFAULT
        retrieveGraph(graphName) != null
    }

    void disconnectGraph(String graphName) {
        if(isBlank(graphName)) graphName = DEFAULT
        storeGraph(graphName, null)
    }

    Graph fetchGraph(String graphName) {
        if(isBlank(graphName)) graphName = DEFAULT
        Graph graph = retrieveGraph(graphName)
        if(graph == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = BlueprintsConnector.instance.createConfig(app)
            graph = BlueprintsConnector.instance.connect(app, config, graphName)
        }
        
        if(graph == null) {
            throw new IllegalArgumentException("No such Graph configuration for name $graphName")
        }
        graph
    }

    private Graph retrieveGraph(String graphName) {
        synchronized(LOCK) {
            graphs[graphName]
        }
    }

    private void storeGraph(String graphName, Graph graph) {
        synchronized(LOCK) {
            graphs[graphName] = graph
        }
    }
}
