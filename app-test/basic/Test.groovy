import android.widget.TextView
import org.robolectric.Robolectric
import spock.lang.Specification

class Test extends Specification {

    def "should display hello text"() {
        given:
        def textView = new TextView(Robolectric.application)

        and:
        def hello = "Hello"

        when:
        textView.setText(hello)

        then:
        textView.getText() == hello
    }

}