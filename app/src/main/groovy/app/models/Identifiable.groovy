package app.models

import groovy.transform.CompileStatic

@CompileStatic
interface Identifiable<T> {
    T getId()
}