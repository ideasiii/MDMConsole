package iii.ideas.service;

import java.net.Socket;
import java.util.HashMap;
import android.os.Handler;
import iii.ideas.global.Global;
import iii.ideas.global.MDMParameterSetting;
import sdk.ideas.common.Common;
import sdk.ideas.common.Logs;
import sdk.ideas.module.CmpClient;

public class CommunicateServer
{

	private Handler mHandler = null;

	public CommunicateServer(Handler mHandler)
	{
		if (null != mHandler)
		{
			this.mHandler = mHandler;
		}
		else
		{
			Logs.showError("CommunicateServer Handler is null handler");
		}

	}

	public void sendEvent(String jsonString, final int nTag)
	{
		Thread t = new Thread(new sendSocketRunnable(jsonString, nTag));
		t.start();
	}

	public void sendEvent(String jsonString, final int nTag, Socket mSocket)
	{
		Thread t = new Thread(new sendSocketRunnable(jsonString, nTag, mSocket));
		t.start();
	}

	private int sendSocketData(String param, HashMap<String, String> respData, CmpClient.Response response,
			final int nfag, Socket mSocket)
	{
		try
		{
			switch (nfag)
			{
			case Global.MSG_LOGIN:
				CmpClient.mdmLoginRequest(MDMParameterSetting.SERVER_IP, MDMParameterSetting.SERVER_PORT, param, respData, response);
				break;
			case Global.MSG_LOGOUT:
				CmpClient.mdmLogoutRequest(MDMParameterSetting.SERVER_IP, MDMParameterSetting.SERVER_PORT, param, respData, response);
				break;
			case Global.MSG_UPDATE_STATE:
				CmpClient.mdmStateUpadateRequest(MDMParameterSetting.SERVER_IP, MDMParameterSetting.SERVER_PORT, param, respData, response,
						mSocket);
				break;
			case Global.MSG_GET_COMMAND:
				CmpClient.mdmGetCommandRequest(MDMParameterSetting.SERVER_IP, MDMParameterSetting.SERVER_PORT, param, respData, response,
						mSocket);
				break;
			default:

				Logs.showError("[CommunicateServer] Unknown Command ID: "+String.valueOf(nfag));

				break;
			}
		}
		catch (Exception e)
		{
			Logs.showTrace("Exception:" + e.toString());
		}
		return response.mnCode;
	}

	class sendSocketRunnable implements Runnable
	{

		private int nTag = 0;
		private String param = null;
		private Socket mSocket = null;

		@Override
		public void run()
		{
			HashMap<String, String> respData = new HashMap<String, String>();
			CmpClient.Response response = new CmpClient.Response();
			sendSocketData(param, respData, response, nTag, mSocket);
			Logs.showTrace("now to post message to mHandler");
			Common.postMessage(CommunicateServer.this.mHandler, Global.MSG_RESPONSE_COMMUNICATE_SERVER, response.mnCode,
					nTag, response.mstrContent);
		}

		public sendSocketRunnable(String param, final int nTag)
		{
			this.param = param;
			this.nTag = nTag;
		}

		public sendSocketRunnable(String param, final int nTag, Socket mSocket)
		{
			this.param = param;
			this.nTag = nTag;
			this.mSocket = mSocket;
		}

	}
}
