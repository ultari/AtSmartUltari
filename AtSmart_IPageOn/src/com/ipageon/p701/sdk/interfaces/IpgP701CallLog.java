package com.ipageon.p701.sdk.interfaces;

import com.ipageon.p701.sdk.parts.Address;
import com.ipageon.p701.sdk.state.CallDirection;
import com.ipageon.p701.sdk.state.CallStatus;

/**
 * Created by parkkw09@ipageon.com on 2016-10-27.
 */

public interface IpgP701CallLog {

    long getPtr();
    /**
     * Originator of the call as a LinphoneAddress object.
     * @return LinphoneAddress
     */
    Address getFrom();
    /**
     * Destination of the call as a LinphoneAddress object.
     * @return
     */
    Address getTo ();
    /**
     * The direction of the call
     * @return CallDirection
     */
    CallDirection getDirection();
    /**
     * get status of this call
     * @return CallStatus
     */
    CallStatus getStatus();

    /**
     * A human readable String with the start date/time of the call
     * @return String
     */
    String getStartDate();

    /**
     * A  timestamp of the start date/time of the call in milliseconds since January 1st 1970
     * @return  long
     */
    long getTimestamp();

    /**
     * The call duration, in seconds
     * @return int
     */
    int getCallDuration();
    /**
     *  Call id from signaling
     * @return the SIP call-id.
     */
    String getCallId();
    /**
     * Tells whether the call was a call to a conference server
     * @return true if the call was a call to a conference server
     */
    boolean wasConference();
}
