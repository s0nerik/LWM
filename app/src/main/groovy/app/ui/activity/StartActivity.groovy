package app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import app.App
import app.helpers.CollectionManager
import app.ui.base.BaseActivity
import groovy.transform.CompileStatic
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import javax.inject.Inject

@CompileStatic
class StartActivity extends BaseActivity {

    @Inject
    protected CollectionManager collectionManager

    @Override
    void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
    }

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