package app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v7.app.AppCompatActivity
import groovy.transform.CompileStatic

@CompileStatic
class StartActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState)
        startActivity new Intent(this, MainActivity)
        finish()
    }
}