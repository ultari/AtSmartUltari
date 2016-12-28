package com.ipageon.p701.sdk.interfaces;

import com.ipageon.p701.sdk.parts.ErrorInfo;
import com.ipageon.p701.sdk.state.CallDirection;

public interface IpgP701Call {

    /*
     * Call Control
     */

    long getPtr();
    void sendDtmf(char number);
    boolean isIncoming();
    IpgP701CallParams getCurrentParamsCopy();
    IpgP701CallParams getRemoteParams();
    int getDuration();
    ErrorInfo getErrorInfo();
    CallDirection getDirection();
    IpgP701CallLog getCallLog();
    void startRecording();
    void stopRecording();
}
