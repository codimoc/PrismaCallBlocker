package com.android.internal.telephony;

/**
 * This aidl definition is the interface
 * for the telephony service,
 * required in order to drop a call.
 */
public interface ITelephony {
    boolean endCall();
    void answerRingingCall();
    void silenceRinger();
}
