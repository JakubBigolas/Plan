package com.byd.wsg.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.byd.wsg.com.wsg.byd.plan.Event;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.SQLiteHelper;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jakub on 2016-05-14.
 */
public class FragmentEventsDetail extends Fragment {

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
    private ImageButton saveButton;
    private ScrollView contentScrollView;

    private Event event = new Event();

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    OnComponentListener callbacks;
    private int position;

    private static final String TAG = "LOG-FragmentEventsDetail";

    public static final String
            SET_POSITION = "FragmentEventsDetail.SET_POSITION",
            SET_CREATED_BY_FIELD_VISIBILITY = "FragmentEventsDetail.SET_CREATED_BY_FIELD_VISIBILITY",
            SET_TIME_FIELD_VISIBILITY = "FragmentEventsDetail.SET_TIME_FIELD_VISIBILITY",
            SET_DESCRIPTION_LAYOUT_VISIBILITY = "FragmentEventsDetail.SET_DESCRIPTION_LAYOUT_VISIBILITY",
            SET_CREATED_BY_FIELD_ENABLED = "FragmentEventsDetail.SET_CREATED_BY_FIELD_ENABLED",
            SET_TIME_FIELD_ENABLED = "FragmentEventsDetail.SET_TIME_FIELD_ENABLED",
            SET_TITLE_FIELD_ENABLED = "FragmentEventsDetail.SET_TITLE_FIELD_ENABLED",
            SET_CONTENT_FIELD_ENABLED = "FragmentEventsDetail.SET_CONTENT_FIELD_ENABLED";


    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof OnComponentListener)
            callbacks = (OnComponentListener) activity;
        super.onAttach(activity);
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof OnComponentListener)
            callbacks = (OnComponentListener) context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_events_detail, container, false);

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
        saveButton = (ImageButton) v.findViewById(R.id.saveButton);
        contentLayout = (LinearLayout) v.findViewById(R.id.contentLayout);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if (getActivity() instanceof OnComponentListener)
            callbacks = (OnComponentListener) getActivity();
        if (state == null)
            state = getArguments();
        if (state != null) {
            position = state.getInt(SET_POSITION, position);
            edited = state.getBoolean("edited", false);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle state) {
        super.onActivityCreated(state);
        if (state == null)
            state = getArguments();

        if (callbacks != null)
            callbacks.onComponentEvent(this, position, null);
        if (event == null)
            event = new Event();

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
        saveButton.setVisibility(edited ? View.VISIBLE : View.GONE);

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

        showOrHideSaveButton();
        saveButton.setOnClickListener(onSave);
        contentScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEditText.requestFocus();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        v.setFitsSystemWindows(true);
        refreshView();
        if (!edited) {
            contentEditText.addTextChangedListener(editWatcher);
            titleEditText.addTextChangedListener(editWatcher);
        }
    }

    @Override
    public void onPause() {
        contentEditText.removeTextChangedListener(editWatcher);
        titleEditText.removeTextChangedListener(editWatcher);
        super.onPause();
    }

    TextWatcher editWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            edited = true;
            contentEditText.removeTextChangedListener(this);
            titleEditText.removeTextChangedListener(this);
            showOrHideSaveButton();
        }
    };

    View.OnClickListener onSave = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Event.Helper helper = new Event.Helper(new SQLiteHelper(getActivity()), Event.Helper.TABLE_NAME);
            try {
                loadDataFromInterface();
                Event temp = null;
                if(event.getId() == Event.ID_NOT_STORED) {
                    temp = helper.create(event);

                    if (temp != null) {
                        event = temp;
                        if (callbacks != null)
                            callbacks.onComponentEvent(FragmentEventsDetail.this, event, null);
                    }
                }
                temp.setUser(username);
                if (temp != null || helper.update(event)) {
                    edited = false;
                    contentEditText.addTextChangedListener(editWatcher);
                    titleEditText.addTextChangedListener(editWatcher);
                    showOrHideSaveButton();
                    Tools.toast(getActivity(), getString(R.string.fragment_events_detail_save_correctly));
                    return;
                }
            } catch (Throwable t) {
                Log.d(TAG, "onClick: " + t.getLocalizedMessage());
            }
            Tools.toast(getActivity(), getString(R.string.fragment_events_detail_save_incorrectly));
        }
    };

    private ObjectAnimator saveButtonAnimation = new ObjectAnimator();
    private int directSaveButtonAnimation = 0;
    private boolean edited = false;

    private synchronized void showOrHideSaveButton() {
        if (v != null && v.getHeight() > 0) try {
            if (edited) {
                directSaveButtonAnimation = 1;
                saveButtonAnimation.cancel();
                saveButton.setVisibility(View.VISIBLE);
                saveButtonAnimation = ObjectAnimator.ofFloat(saveButton, "alpha", saveButton.getAlpha(), 1f);
                saveButtonAnimation.start();
            } else {
                directSaveButtonAnimation = 2;
                saveButtonAnimation.cancel();
                saveButtonAnimation = ObjectAnimator.ofFloat(saveButton, "alpha", saveButton.getAlpha(), 0f);
                saveButtonAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        saveButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        saveButton.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                saveButtonAnimation.start();
            }
        } catch (Throwable t) {
            Log.d(TAG, "showOrHidesaveButton: " + t);
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
        edited = this.event.getId() == Event.ID_NOT_STORED;
        refreshView();
    }

    @Override
    public void onDestroyView() {
        titleEditText.setOnFocusChangeListener(null);
        titleEditText.removeTextChangedListener(editWatcher);
        contentEditText.setOnFocusChangeListener(null);
        contentEditText.removeTextChangedListener(editWatcher);
        saveButton.setOnClickListener(null);

        v = null;
//        noteEditLayout = null;
//        descriptionLayout = null;
//        createdbyLayout = null;
//        timeLayout = null;
//        titleEditText = null;
//        createdByEditText = null;
//        timeEditText = null;
//        contentEditText = null;
//        createdByLabelTextView = null;
//        timeLabelTextView = null;
//        contentScrollView = null;
//        saveButton = null;

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

    public static FragmentEventsDetail prepareFragmentEventsDetail(int position) {
        FragmentEventsDetail f = new FragmentEventsDetail();
        Bundle args = new Bundle();
        args.putInt(SET_POSITION, position);
        args.putBoolean(SET_TIME_FIELD_VISIBILITY, true);
        args.putBoolean(SET_TIME_FIELD_ENABLED, false);
        args.putBoolean(SET_DESCRIPTION_LAYOUT_VISIBILITY, true);
        f.setArguments(args);
        return f;
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
        state.putBoolean("edited", edited);
        state.putInt(SET_POSITION, position);
        Tools.saveParcelcableObject(contentScrollView, state, "contentScrollView");
    }
}
