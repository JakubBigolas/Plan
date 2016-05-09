package com.byd.wsg.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Jakub on 2016-05-07.
 */
public interface OnComponentListener {

    /**
     * @param component      object whitch trys to interacts with its parent, null is not allowed
     * @param contractObject special object whitch is a part of interaction, null is allowed
     * @param stateComponent object that defines act of component (this may be enum provides information about behavior of component), null is allowed
     */
    public void onComponentEvent(@NonNull Object component, @Nullable Object contractObject, @Nullable Object stateComponent);

}
