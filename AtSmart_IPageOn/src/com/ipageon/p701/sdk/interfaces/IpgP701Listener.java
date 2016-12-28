package com.ipageon.p701.sdk.interfaces;

import com.ipageon.p701.sdk.state.CallState;
import com.ipageon.p701.sdk.state.RegistrationState;

public interface IpgP701Listener {

    void onCoreMessage(String message);

    void onRegistrationState(RegistrationState state, String smessage);
    void onCallState(IpgP701 core, IpgP701Call call, CallState state, String message);

}