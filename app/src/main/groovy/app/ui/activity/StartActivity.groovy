package app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import app.helper.CollectionManager
import app.ui.base.DaggerActivity
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import javax.inject.Inject

@CompileStatic
class StartActivity extends DaggerActivity {

    @Inject
    @PackageScope
    CollectionManager collectionManager

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)

        collectionManager.init()
                         .doOnCompleted {
                             startActivity new Intent(this, MainActivity)
                             finish()
                         }
                         .subscribeOn(Schedulers.io())
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribe()
    }
}