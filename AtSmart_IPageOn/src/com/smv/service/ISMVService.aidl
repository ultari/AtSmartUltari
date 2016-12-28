package com.smv.service;

interface ISMVService {
	int getVoipState();
	String getWeWorkInfo();
	String getVoipExtNumber();
	int endCallVoIP();
	String getChatSet();
	String getPbxType();
}
