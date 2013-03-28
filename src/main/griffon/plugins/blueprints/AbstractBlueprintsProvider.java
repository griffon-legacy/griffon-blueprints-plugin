/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.blueprints;

import com.tinkerpop.blueprints.Graph;
import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractBlueprintsProvider implements BlueprintsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBlueprintsProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withBlueprints(Closure<R> closure) {
        return withBlueprints(DEFAULT, closure);
    }

    public <R> R withBlueprints(String graphName, Closure<R> closure) {
        if (isBlank(graphName)) graphName = DEFAULT;
        if (closure != null) {
            Graph graph = getGraph(graphName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statements on graph '" + graphName + "'");
            }

            return closure.call(graphName, graph);
        }
        return null;
    }

    public <R> R withBlueprints(CallableWithArgs<R> callable) {
        return withBlueprints(DEFAULT, callable);
    }

    public <R> R withBlueprints(String graphName, CallableWithArgs<R> callable) {
        if (isBlank(graphName)) graphName = DEFAULT;
        if (callable != null) {
            Graph graph = getGraph(graphName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statements on graph '" + graphName + "'");
            }
            callable.setArgs(new Object[]{graphName, graph});
            return callable.call();
        }
        return null;
    }

    protected abstract Graph getGraph(String graphName);
}