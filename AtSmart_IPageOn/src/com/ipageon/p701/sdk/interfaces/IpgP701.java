package com.ipageon.p701.sdk.interfaces;

import com.ipageon.p701.sdk.state.CommonState;
import com.ipageon.p701.sdk.state.Reason;
import com.ipageon.p701.sdk.tools.CryptoSuite;
import com.ipageon.p701.sdk.tools.MediaEncryption;
import com.ipageon.p701.sdk.tools.TransportType;

public interface IpgP701 {

    /*
     * Core Handle
     */
    long getPtr();
    CommonState createP701Core(IpgP701Listener listener);
    CommonState destoryP701Core();
    IpgP701CallParams createCallParams(IpgP701Call call);
    boolean isValid();

    /*
     * Client Login
     */
    CommonState startClient(String id, String passwd, String number,
            String proxy, String domain);
    CommonState startClient(String id, String passwd, String number,
            String proxy, String port, String domain, int expire_sec,
            TransportType type);
    CommonState stopClient();

    /*
     * Configuration Core
     */
    void resetCamera();
    boolean setLogEnable(int level);
    boolean setUserAgent(String name, String version);
    boolean setRing(String filename);
    boolean setRingback(String filename);
    boolean setHoldRing(String filename);
    boolean setRootCertPath(String filename);
    boolean setPrivateCertPath(String filename);
    boolean setPrivateKeyPath(String filename);
    boolean setCpuCount(int count);
    boolean hasCrappyOpenGL();
    boolean setVideoDevice(int camId);
    void setListenPort(int port);
    int getVideoDevice();
    void setVideoWindow(Object w);
    void setPreviewWindow(Object w);
    void setPreferredVideoSizeByName(String name);
    void setDeviceRotation(int rotation);
    void setDownloadBandwidth(int bw);
    int getDownloadBandwidth();
    void setUploadBandwidth(int bw);
    int getUploadBandwidth();
    void setVideoSize(String preferredVideoSize);
    boolean setKeepAlive(int ms);
    boolean setUUID(String uuid);
    String getGcmRegid();
    void setGcmRegid(String regid);
    void setComfortNoise(boolean enable);
    MediaEncryption getMediaEncryption();
    void setPlaybackGain(float gain);
    void setMicrophoneGain(float gain);
    String getP701Version();
    void setCryptoPolicy(CryptoSuite cs);
    CryptoSuite getCryptoPolicy();

    /*
     * Call
     */
    String getRecPath();
    void setRecPath(String recPath);
    IpgP701Call startVoiceCall(String number, String displayName);
    IpgP701Call startVideoCall(String number, String displayName);
    IpgP701Call getCurrentCall();
    void playDtmf(char number);
    void stopDtmf();
    CommonState updateCall();
    CommonState updateCall(IpgP701Call call, IpgP701CallParams params);
    CommonState acceptCall(IpgP701Call call);
    CommonState acceptCall(IpgP701Call call, IpgP701CallParams params);
    CommonState acceptCallUpdate(IpgP701Call call, IpgP701CallParams params);
    CommonState endCall(IpgP701Call call);
    void transferCall(IpgP701Call call, String referTo);
    boolean pauseCall(IpgP701Call call);
    boolean resumeCall(IpgP701Call call);
    int getCallsNb();
    IpgP701Call[] getCalls();
	void declineCall(IpgP701Call aCall, Reason reason);

    /*
     * Configuration Call
     */
}
