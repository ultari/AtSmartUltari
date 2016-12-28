/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\ultari\\8. androidProject\\AtSmart_IPageOn\\src\\com\\smv\\service\\ISMVService.aidl
 */
package com.smv.service;
public interface ISMVService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.smv.service.ISMVService
{
private static final java.lang.String DESCRIPTOR = "com.smv.service.ISMVService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.smv.service.ISMVService interface,
 * generating a proxy if needed.
 */
public static com.smv.service.ISMVService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.smv.service.ISMVService))) {
return ((com.smv.service.ISMVService)iin);
}
return new com.smv.service.ISMVService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getVoipState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getVoipState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getWeWorkInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getWeWorkInfo();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getVoipExtNumber:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getVoipExtNumber();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_endCallVoIP:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.endCallVoIP();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getChatSet:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getChatSet();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getPbxType:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getPbxType();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.smv.service.ISMVService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public int getVoipState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getVoipState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getWeWorkInfo() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getWeWorkInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getVoipExtNumber() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getVoipExtNumber, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int endCallVoIP() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_endCallVoIP, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getChatSet() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getChatSet, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getPbxType() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPbxType, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getVoipState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getWeWorkInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getVoipExtNumber = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_endCallVoIP = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getChatSet = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getPbxType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
}
public int getVoipState() throws android.os.RemoteException;
public java.lang.String getWeWorkInfo() throws android.os.RemoteException;
public java.lang.String getVoipExtNumber() throws android.os.RemoteException;
public int endCallVoIP() throws android.os.RemoteException;
public java.lang.String getChatSet() throws android.os.RemoteException;
public java.lang.String getPbxType() throws android.os.RemoteException;
}
