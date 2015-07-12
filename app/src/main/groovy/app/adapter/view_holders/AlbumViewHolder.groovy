package app.adapter.view_holders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.s0nerik.betterknife.BetterKnife;
import com.github.s0nerik.betterknife.annotations.InjectView;

import app.R;
import app.ui.custom_view.SquareWidthImageView
import groovy.transform.CompileStatic;

@CompileStatic
class AlbumViewHolder {
    @InjectView(R.id.cover)
    SquareWidthImageView mCover;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.subtitle)
    TextView mSubtitle;
    @InjectView(R.id.bottom_bar)
    LinearLayout mBottomBar;
    @InjectView(R.id.shadow)
    View mShadow;
    @InjectView(R.id.layout)
    View mLayout;

    AlbumViewHolder(View view) {
        BetterKnife.inject(this, view);
    }
}