package com.byd.wsg.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.byd.wsg.services.Service;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;
import com.byd.wsg.services.TaskListener;
import com.byd.wsg.services.TaskStatus;

/**
 * Created by Jakub on 2016-05-22.
 */
public class FragmentLogin extends Fragment implements ServiceConnection {

    //    private String
//            Login,
//            Password;
//    private boolean rememberme;
    private View v;
    private Button
            loginButton,
            cancelButton;
    private EditText
            loginEditText,
            passwordEditText;
    private View loginLayout;
    private ProgressBar loginProgressBar;
    private CheckBox remembermeCheckBox;

    private boolean isLogin = false;
    private boolean isFirstShow = true;
    private boolean isVievReady = false;

    OnComponentListener callbacks;

    private static final String TAG = "LOG-FragmentLogin";

    private Service.Binder service;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: ");
        this.service = (Service.Binder) service;
        ((Service.Binder) service).setLoginListener(taskListener);
        taskListener.onEvent(((Service.Binder) service).getLoginStatus(), ((Service.Binder) service).getLoginError(), (Service.Binder) service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        this.service = null;
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    TaskListener taskListener = new TaskListener() {
        @Override
        public void onEvent(TaskStatus taskStatus, Throwable error, Service.Binder service) {
            if (taskStatus != null) switch (taskStatus) {
                case INTERNET_ACCES_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_internet_access_error);
                    onCancelClick.onClick(cancelButton);
                    break;
                case IO_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_io_error);
                    onCancelClick.onClick(cancelButton);
                    break;
                case UNEXPECTED_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_unexpected_error);
                    onCancelClick.onClick(cancelButton);
                    break;
                case AUTHENICATION_FAILED:
                    Tools.toastRs(getActivity(), R.string.service_authentication_failed);
                    onCancelClick.onClick(cancelButton);
                    break;
                case TIMEOUT:
                    Tools.toastRs(getActivity(), R.string.service_timeout);
                    onCancelClick.onClick(cancelButton);
                    break;
                case UNAUTHORIZED_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_unauthorized_error);
                    onCancelClick.onClick(cancelButton);
                    break;
                case DONE:
                    service.refreshNews(null);
                    prepareLoginLayoutAnimOut(v).start();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        v = inflater.inflate(R.layout.fragment_login, container, false);

        loginButton = (Button) v.findViewById(R.id.loginButton);
        cancelButton = (Button) v.findViewById(R.id.cancelButton);
        loginEditText = (EditText) v.findViewById(R.id.loginEditText);
        passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
        loginLayout = v.findViewById(R.id.loginLayout);
        loginProgressBar = (ProgressBar) v.findViewById(R.id.loginProgressBar);
        remembermeCheckBox = (CheckBox) v.findViewById(R.id.remembermeCheckBox);
        isVievReady = false;
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    Log.d(TAG, "onGlobalLayout: ");
                    isVievReady = true;
                    onViewReady();
                } catch (Throwable t) {
                    Log.d(TAG, "onGlobalLayout: " + t.getLocalizedMessage()); // POMIJALNE
                }
                try {
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Throwable t) {
                    Log.d(TAG, "onGlobalLayout: " + t.getLocalizedMessage()); // POMIJALNE
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle state) {
        Log.d(TAG, "onActivityCreated: ");
        if (getActivity() instanceof OnComponentListener)
            callbacks = (OnComponentListener) getActivity();
        super.onActivityCreated(state);
        if (state == null) {
            SharedPreferences pref = getAppCompatActivity().getSharedPreferences(FragmentLogin.class.getName(), 0);
            remembermeCheckBox.setChecked(pref.getBoolean("rememberMe", false));
            if (remembermeCheckBox.isChecked()) {
                loginEditText.setText(pref.getString("login", ""));
                passwordEditText.setText(pref.getString("password", ""));
            }
        }
//        if (state == null) state = getArguments();
        if (state != null) {
            isLogin = state.getBoolean("isLogin");
            isFirstShow = state.getBoolean("isFirstShow", isFirstShow);
        }
        loginButton.setAlpha(!isLogin ? 1 : 0);
        loginButton.setEnabled(!isLogin);
        cancelButton.setAlpha(isLogin ? 1 : 0);
        cancelButton.setEnabled(isLogin);
        loginProgressBar.setAlpha(isLogin ? 1 : 0);
        loginButton.setOnClickListener(onLoginClick);
        cancelButton.setOnClickListener(onCancelClick);
    }

    private AnimatorSet
            loginAnimStart,
            loginAnimCancel;

    private View.OnClickListener onLoginClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (FragmentLogin.this.v != null  // GDY PROCES LOGOWANIA RUSZYL
                    && service != null && (isLogin = service.login(loginEditText.getText().toString(), passwordEditText.getText().toString(), taskListener))) {
                if (loginAnimCancel != null) try {
                    loginAnimCancel.cancel();
                } catch (Throwable t) {
                    Log.d(TAG, "onClick: " + t.getLocalizedMessage());
                }
                SharedPreferences pref = getAppCompatActivity().getSharedPreferences(FragmentLogin.class.getName(), 0);
                SharedPreferences.Editor prefEdit = pref.edit().putBoolean("rememberMe", remembermeCheckBox.isChecked());
                if (remembermeCheckBox.isChecked()) {
                    prefEdit.putString("login", loginEditText.getText().toString());
                    prefEdit.putString("password", passwordEditText.getText().toString());
                }
                prefEdit.apply();
                loginEditText.setEnabled(false);
                passwordEditText.setEnabled(false);
                remembermeCheckBox.setEnabled(false);
                cancelButton.setEnabled(true);
                loginButton.setEnabled(false);
                loginAnimStart = prepareLoginAnimStart(FragmentLogin.this.v);
                loginAnimStart.start();
            }
        }
    };

    private View.OnClickListener onCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (FragmentLogin.this.v != null && service != null) {
                service.cancelLogin();
                if (loginAnimStart != null) try {
                    loginAnimStart.cancel();
                } catch (Throwable t) {
                    Log.d(TAG, "onClick: " + t.getLocalizedMessage());
                }
                isLogin = false;
                loginEditText.setEnabled(true);
                passwordEditText.setEnabled(true);
                remembermeCheckBox.setEnabled(true);
                cancelButton.setEnabled(false);
                loginButton.setEnabled(true);
                loginAnimCancel = prepareLoginAnimCancel(FragmentLogin.this.v);
                loginAnimCancel.start();
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle state) {
        Log.d(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(state);
        state.putBoolean("isLogin", isLogin);
        state.putBoolean("isFirstShow", isFirstShow);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart:");
        Intent intent = new Intent(getActivity(), Service.class);
        getAppCompatActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    private AnimatorSet loginLayoutAnimEnter;

    private synchronized void onViewReady() {
        Log.d(TAG, "onViewReady: ");
        if (isFirstShow && loginLayoutAnimEnter == null) {
            loginLayoutAnimEnter = prepareLoginLayoutAnimEnter(loginLayout);
            loginLayoutAnimEnter.start();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        if (isVievReady && v != null)
            onViewReady();
        for (AnimatorSet anim : new AnimatorSet[]{loginLayoutAnimEnter, loginAnimStart})
            try {
                if (anim != null) anim.resume();
            } catch (Throwable t) {
                Log.d(TAG, "onResume: " + t.getLocalizedMessage());
            }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        for (AnimatorSet anim : new AnimatorSet[]{loginLayoutAnimEnter, loginAnimStart})
            try {
                if (anim != null) anim.pause();
            } catch (Throwable t) {
                Log.d(TAG, "onPause: " + t.getLocalizedMessage());
            }
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        try {
            if (service != null) {
                service.removeLoginListener(taskListener);
                getAppCompatActivity().getApplicationContext().unbindService(this);
            }
        } catch (Throwable t) {
            Log.d(TAG, "onStop: " + t.getLocalizedMessage());
        }
        service = null;
        super.onStop();
    }


    protected AnimatorSet prepareLoginLayoutAnimEnter(final View v) {
        AnimatorSet anim = new AnimatorSet();
        final float density = getResources().getDisplayMetrics().density;
        loginLayout.setPivotX((float) (loginLayout.getHeight()));
        ObjectAnimator loginLayoutRotationXAnim = ObjectAnimator.ofFloat(loginLayout, "rotationX", 7.5F, 0);
        ObjectAnimator loginLayoutTranslationZAnim = ObjectAnimator.ofFloat(loginLayout, "translationZ", 24, 0);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        ObjectAnimator yAnim = ObjectAnimator.ofFloat(v, "translationY", -40 * density, 0);
        anim.playTogether(loginLayoutRotationXAnim, loginLayoutTranslationZAnim, alphaAnim, yAnim);
        anim.setDuration(700);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setAlpha(1);
                v.setTranslationY(0);
                loginLayoutAnimEnter = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                v.setAlpha(1);
                v.setTranslationY(0);
                loginLayoutAnimEnter = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return anim;
    }

    protected AnimatorSet prepareLoginLayoutAnimOut(final View v) {
        AnimatorSet anim = new AnimatorSet();
        loginLayout.setPivotX((float) (loginLayout.getHeight()));
        ObjectAnimator loginLayoutRotationXAnim = ObjectAnimator.ofFloat(loginLayout, "rotationX", 0, -7.5F);
        ObjectAnimator loginLayoutTranslationZAnim = ObjectAnimator.ofFloat(loginLayout, "translationZ", 0, 24);
        ObjectAnimator loginLayoutTranslationYAnim = ObjectAnimator.ofFloat(loginLayout, "translationY", 0, 200);
        ObjectAnimator loginLayoutAlphaAnim = ObjectAnimator.ofFloat(loginLayout, "alpha", 1, 0);
        anim.playTogether(loginLayoutRotationXAnim, loginLayoutTranslationZAnim, loginLayoutTranslationYAnim, loginLayoutAlphaAnim);
        anim.setInterpolator(new LinearOutSlowInInterpolator());
        anim.setDuration(700);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    if (callbacks != null)
                        callbacks.onComponentEvent(FragmentLogin.this, null, TaskStatus.DONE);
                } catch (Throwable t) {
                    Log.d(TAG, "onAnimationEnd: " + t.getLocalizedMessage());
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return anim;
    }

    protected AnimatorSet prepareLoginAnimStart(final View v) {
        AnimatorSet anim = new AnimatorSet();
        final float density = getResources().getDisplayMetrics().density;
        final ObjectAnimator cancelAlphaAnim = ObjectAnimator.ofFloat(cancelButton, "alpha", 0, 1);
        final ObjectAnimator loginAlphaAnim = ObjectAnimator.ofFloat(loginButton, "alpha", 1, 0);
        final ObjectAnimator progressbarAlphaAnim = ObjectAnimator.ofFloat(loginProgressBar, "alpha", 0, 1);
        final ObjectAnimator progressbarXAnim = ObjectAnimator.ofFloat(loginProgressBar, "translationX", -40 * density, 0);
        anim.playTogether(cancelAlphaAnim, loginAlphaAnim, progressbarAlphaAnim, progressbarXAnim);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                cancelButton.setAlpha(1);
                loginButton.setAlpha(0);
                loginProgressBar.setAlpha(1);
                loginAnimStart = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelButton.setAlpha(1);
                loginButton.setAlpha(0);
                loginProgressBar.setAlpha(1);
                loginAnimStart = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.setDuration(300);
        return anim;
    }

    protected AnimatorSet prepareLoginAnimCancel(final View v) {
        AnimatorSet anim = new AnimatorSet();
        final float density = getResources().getDisplayMetrics().density;
        final ObjectAnimator cancelAlphaAnim = ObjectAnimator.ofFloat(cancelButton, "alpha", 1, 0);
        final ObjectAnimator loginAlphaAnim = ObjectAnimator.ofFloat(loginButton, "alpha", 0, 1);
        final ObjectAnimator progressbarAlphaAnim = ObjectAnimator.ofFloat(loginProgressBar, "alpha", 1, 0);
        final ObjectAnimator progressbarXAnim = ObjectAnimator.ofFloat(loginProgressBar, "translationX", 0, 30 * density);
        anim.playTogether(cancelAlphaAnim, loginAlphaAnim, progressbarAlphaAnim, progressbarXAnim);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                cancelButton.setAlpha(0);
                loginButton.setAlpha(1);
                loginProgressBar.setAlpha(0);
                loginAnimCancel = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelButton.setAlpha(0);
                loginButton.setAlpha(1);
                loginProgressBar.setAlpha(0);
                loginAnimCancel = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.setDuration(300);
        return anim;
    }


}
