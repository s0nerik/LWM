package app.prefs

import android.support.annotation.IdRes
import app.R
import groovy.transform.CompileStatic
import net.yslibrary.simplepreferences.annotation.Key
import net.yslibrary.simplepreferences.annotation.Preferences

@Preferences
@CompileStatic
class Main {
    @IdRes
    @Key
    protected int drawerSelection = R.id.local_music
}