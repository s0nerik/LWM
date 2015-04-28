package app;

import dagger.ObjectGraph
import groovy.transform.CompileStatic;

@CompileStatic
public final class Injector {
    public static ObjectGraph graph;
    public static void init(Object... modules)
    {
        graph = ObjectGraph.create(modules)
    }

    public static void inject(Object target)
    {
        graph.inject(target)
    }
}
