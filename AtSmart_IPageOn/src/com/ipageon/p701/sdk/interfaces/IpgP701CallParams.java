package com.ipageon.p701.sdk.interfaces;

public interface IpgP701CallParams {

    /*
     * Call
     */
    long getPtr();
    boolean getVideoEnabled();
    void setVideoEnabled(boolean b);
    void setRecordFile(String path);

}