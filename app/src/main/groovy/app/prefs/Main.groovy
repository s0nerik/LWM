package app.prefs

import android.support.annotation.IdRes
import app.R
import org.jraf.android.prefs.DefaultInt
import org.jraf.android.prefs.Prefs

@Prefs
class Main {
    @DefaultInt(0)
    @IdRes
    protected Integer drawerSelection = R.id.local_music
}