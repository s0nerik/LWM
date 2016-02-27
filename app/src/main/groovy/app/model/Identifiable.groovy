package app.model

import groovy.transform.CompileStatic

@CompileStatic
interface Identifiable<T> {
    T getId()
}