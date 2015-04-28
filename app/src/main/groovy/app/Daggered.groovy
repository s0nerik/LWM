package app

import groovy.transform.CompileStatic;

@CompileStatic
class Daggered {
    Daggered() {
        Injector.inject(this)
    }
}
