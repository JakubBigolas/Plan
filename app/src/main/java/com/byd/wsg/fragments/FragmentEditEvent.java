package com.byd.wsg.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.byd.wsg.com.wsg.byd.plan.Event;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.SQLiteHelper;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jakub on 2016-05-05.
 */
public class FragmentEditEvent extends DialogFragment {

    private LinearLayout
            noteEditLayout,
            createdbyLayout,
            timeLayout,
            descriptionLayout,
            contentLayout;
    private EditText
            titleEditText,
            createdByEditText,
            timeEditText,
            contentEditText;
    private TextView
            createdByLabelTextView,
            timeLabelTextView;
    private View
            v;
    private Button saveButton;
    private ImageButton additionalSaveButton;
    private ScrollView contentScrollView;

    private Event event = new Event();

    OnComponentListener callbacks;

    private static final String TAG = "LOG-FragmentEditEvent";

    public static final String
            SET_CREATED_BY_FIELD_VISIBILITY = "FragmentEditEvent.SET_CREATED_BY_FIELD_VISIBILITY",
            SET_TIME_FIELD_VISIBILITY = "FragmentEditEvent.SET_TIME_FIELD_VISIBILITY",
            SET_DESCRIPTION_LAYOUT_VISIBILITY = "FragmentEditEvent.SET_DESCRIPTION_LAYOUT_VISIBILITY",
            SET_CREATED_BY_FIELD_ENABLED = "FragmentEditEvent.SET_CREATED_BY_FIELD_ENABLED",
            SET_TIME_FIELD_ENABLED = "FragmentEditEvent.SET_TIME_FIELD_ENABLED",
            SET_TITLE_FIELD_ENABLED = "FragmentEditEvent.SET_TITLE_FIELD_ENABLED",
            SET_CONTENT_FIELD_ENABLED = "FragmentEditEvent.SET_CONTENT_FIELD_ENABLED",
            SET_EVENT = "FragmentEditEvent.SET_EVENT";


    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof OnComponentListener)
            callbacks = (OnComponentListener) activity;
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_event_edit, container, false);

        noteEditLayout = (LinearLayout) v.findViewById(R.id.noteEditLayout);
        descriptionLayout = (LinearLayout) v.findViewById(R.id.descriptionLayout);
        createdbyLayout = (LinearLayout) v.findViewById(R.id.createdByLayout);
        timeLayout = (LinearLayout) v.findViewById(R.id.timeLayout);
        titleEditText = (EditText) v.findViewById(R.id.titleEditText);
        createdByEditText = (EditText) v.findViewById(R.id.createdByEditText);
        timeEditText = (EditText) v.findViewById(R.id.timeEditText);
        contentEditText = (EditText) v.findViewById(R.id.contentEditText);
        createdByLabelTextView = (TextView) v.findViewById(R.id.createdByLabelTextView);
        timeLabelTextView = (TextView) v.findViewById(R.id.timeLabelTextView);
        contentScrollView = (ScrollView) v.findViewById(R.id.contentScrollView);
        saveButton = (Button) v.findViewById(R.id.saveButton);
        additionalSaveButton = (ImageButton) v.findViewById(R.id.additionalSaveButton);
        contentLayout = (LinearLayout) v.findViewById(R.id.contentLayout);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if (state == null)
            state = getArguments();
        if (state != null) {
            event = (Event) Tools.loadDeserializedObjectLogged(state, SET_EVENT, event, TAG);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle state) {
        super.onActivityCreated(state);
        if (state == null)
            state = getArguments();
        if (state != null) {
            createdbyLayout.setVisibility(state.getBoolean(SET_CREATED_BY_FIELD_VISIBILITY, false) ? View.VISIBLE : View.GONE);
            timeLayout.setVisibility(state.getBoolean(SET_TIME_FIELD_VISIBILITY, false) ? View.VISIBLE : View.GONE);
            descriptionLayout.setVisibility(state.getBoolean(SET_DESCRIPTION_LAYOUT_VISIBILITY, false) ? View.VISIBLE : View.GONE);
            createdByEditText.setEnabled(state.getBoolean(SET_CREATED_BY_FIELD_ENABLED, true));
            timeEditText.setEnabled(state.getBoolean(SET_TIME_FIELD_ENABLED, true));
            titleEditText.setEnabled(state.getBoolean(SET_TITLE_FIELD_ENABLED, true));
            contentEditText.setEnabled(state.getBoolean(SET_CONTENT_FIELD_ENABLED, true));
            Tools.restoreParcelableObject(contentScrollView, state, "contentScrollView");
        } else {
            createdbyLayout.setVisibility(View.GONE);
        }
        additionalSaveButton.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                Animator a = new ObjectAnimator();

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    a.cancel();
                    if (hasFocus) a = ObjectAnimator.ofFloat(v, "translationZ", v.getTranslationZ(), 8f);
                    else a = ObjectAnimator.ofFloat(v, "translationZ", v.getTranslationZ(), 0f);
                    a.start();
                }
            });
            titleEditText.setTranslationZ(timeEditText.hasFocus() ? 8 : 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                Animator a = new ObjectAnimator();

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    a.cancel();
                    if (hasFocus) a = ObjectAnimator.ofFloat(v, "translationZ", v.getTranslationZ(), 8f);
                    else a = ObjectAnimator.ofFloat(v, "translationZ", v.getTranslationZ(), 0f);
                    a.start();
                }
            });
            contentEditText.setTranslationZ(contentEditText.hasFocus() ? 8 : 0);
        }

        contentScrollView.getViewTreeObserver().addOnScrollChangedListener(contentScrollListener);
        saveButton.setOnClickListener(onSaveListener);
        additionalSaveButton.setOnClickListener(onSaveListener);

        refreshView();
        showOrHideAdditionalSaveButton();
    }

    private ObjectAnimator additionalSaveButtonAnimation = new ObjectAnimator();
    private int directAdditionalSaveButtonAnimation = 0;

    private synchronized void showOrHideAdditionalSaveButton() {
        if (v != null && v.getHeight() > 0) try {
            if (contentScrollView.getHeight() + contentScrollView.getScrollY() < saveButton.getY() && directAdditionalSaveButtonAnimation != 1) {
                directAdditionalSaveButtonAnimation = 1;
                additionalSaveButtonAnimation.cancel();
                additionalSaveButton.setVisibility(View.VISIBLE);
                additionalSaveButtonAnimation = ObjectAnimator.ofFloat(additionalSaveButton, "alpha", additionalSaveButton.getAlpha(), 1f);
                additionalSaveButtonAnimation.start();
            } else if (!(contentScrollView.getHeight() + contentScrollView.getScrollY() < saveButton.getY()) && directAdditionalSaveButtonAnimation != 2) {
                directAdditionalSaveButtonAnimation = 2;
                additionalSaveButtonAnimation.cancel();
                additionalSaveButtonAnimation = ObjectAnimator.ofFloat(additionalSaveButton, "alpha", additionalSaveButton.getAlpha(), 0f);
                additionalSaveButtonAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        additionalSaveButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        additionalSaveButton.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                additionalSaveButtonAnimation.start();
            }
        } catch (Throwable t) {
            Log.d(TAG, "showOrHideAdditionalSaveButton: " + t);
        }
    }

    private void refreshView() {
        if (v != null) {
            createdByEditText.setText(event.getUser());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            timeEditText.setText(dateFormat.format(event.getTime().getTime()));
            titleEditText.setText(event.getTitle());
            contentEditText.setText(event.getContent());
        }
    }

    /**
     * @param event null will be converted to default value
     */
    public void setEvent(Event event) {
        this.event = event == null ? new Event() : event;
        refreshView();
    }

    private ViewTreeObserver.OnScrollChangedListener contentScrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            showOrHideAdditionalSaveButton();
        }
    };

    private View.OnClickListener onSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            loadDataFromInterface();
            try {
                Event.Helper helper = new Event.Helper(new SQLiteHelper(getActivity()), Event.Helper.TABLE_NAME);
                if (!helper.update(event)) {
                    Event e = helper.create(event);
                    if (e == null)
                        Tools.toast(getActivity(), getResources().getString(R.string.fragment_event_error_save));
                    else {
                        event = e;
                        dismiss();
                        if (callbacks != null)
                            callbacks.onComponentEvent(FragmentEditEvent.this, event, null);
                    }
                }
            } catch (Throwable t) {
                Log.d(TAG, "onSaveListener: " + t.getLocalizedMessage());
                Tools.toast(getActivity(), getResources().getString(R.string.fragment_event_error_save));
            }
        }
    };


    @Override
    public void onDestroyView() {
        titleEditText.setOnFocusChangeListener(null);
        contentEditText.setOnFocusChangeListener(null);
        saveButton.setOnClickListener(null);
        additionalSaveButton.setOnClickListener(null);
        contentScrollView.getViewTreeObserver().removeOnScrollChangedListener(contentScrollListener);
        v = null;
        noteEditLayout = null;
        descriptionLayout = null;
        createdbyLayout = null;
        timeLayout = null;
        titleEditText = null;
        createdByEditText = null;
        timeEditText = null;
        contentEditText = null;
        createdByLabelTextView = null;
        timeLabelTextView = null;
        contentScrollView = null;
        saveButton = null;
        additionalSaveButton = null;
        super.onDestroyView();
    }

    private void loadDataFromInterface() {
        event.setTitle(titleEditText.getText().toString());
        event.setUser(createdByEditText.getText().toString());
        event.setContent(contentEditText.getText().toString());

        Pattern pattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})");
        Matcher matcher = pattern.matcher(timeEditText.getText().toString());
        matcher.find();
        event.setTime(Tools.prepareDate(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)),
                Integer.parseInt(matcher.group(6))));
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(SET_CREATED_BY_FIELD_VISIBILITY, createdbyLayout.getVisibility() == View.VISIBLE);
        state.putBoolean(SET_TIME_FIELD_VISIBILITY, timeLayout.getVisibility() == View.VISIBLE);
        state.putBoolean(SET_DESCRIPTION_LAYOUT_VISIBILITY, descriptionLayout.getVisibility() == View.VISIBLE);
        state.putBoolean(SET_CREATED_BY_FIELD_ENABLED, createdByEditText.isEnabled());
        state.putBoolean(SET_TIME_FIELD_ENABLED, timeEditText.isEnabled());
        state.putBoolean(SET_TITLE_FIELD_ENABLED, titleEditText.isEnabled());
        state.putBoolean(SET_CONTENT_FIELD_ENABLED, contentEditText.isEnabled());
        Tools.saveSerializedObjectLogged(event, state, SET_EVENT, TAG);
        Tools.saveParcelcableObject(contentScrollView, state, "contentScrollView");
    }
}
