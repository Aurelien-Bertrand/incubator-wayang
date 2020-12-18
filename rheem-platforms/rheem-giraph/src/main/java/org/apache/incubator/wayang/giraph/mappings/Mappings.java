package org.apache.incubator.wayang.giraph.mappings;

import org.apache.incubator.wayang.core.mapping.Mapping;

import java.util.Collection;
import java.util.Collections;

/**
 * Register for {@link Mapping}s for this platform.
 */
public class Mappings {

    public static final Collection<Mapping> ALL = Collections.singletonList(
            new PageRankMapping()
    );
}
