package app.adapters

import groovy.transform.CompileStatic

@CompileStatic
interface BubbleTextProvider {
    void setBubbleTextProvider(Closure<String> bubbleTextProvider)
}