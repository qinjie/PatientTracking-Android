package com.example.intern.ptp.views.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.utils.FontManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ErrorView extends LinearLayout {

    @BindView(R.id.error_image)
    TextView errorImage;

    @BindView(R.id.error_description)
    TextView errorDescription;

    public ErrorView(Context context) {
        super(context);

        init(context);
    }

    public ErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.view_error, this, true);
        ButterKnife.bind(this);

        errorImage.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
    }

    public void setError(String message) {
        errorDescription.setText(message);
    }
}
