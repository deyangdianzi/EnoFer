package com.fervort.enofer.enovia;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.fervort.enofer.Activator;
import com.fervort.enofer.log.Logger;

import matrix.db.Context;
import matrix.db.MQLCommand;

public class EnoviaUtility {
	

	static Context context = null;
	static MQLCommand mqlCommand = null;
	
	
	public static ArrayList<String> getEnoviaPrograms(String strProgramName) throws Exception
	{
		ArrayList<String> alPrograms = new ArrayList<String>();
		String strResult = executeMQL("list program '"+strProgramName+"' select isjavaprogram name dump |");
		StringTokenizer stTokens = new StringTokenizer(strResult, "\n");
		
		while (stTokens.hasMoreTokens()) {
			
			String progDetails = stTokens.nextToken();
			String[] aProgDetails = progDetails.split("\\|");
			
			if(aProgDetails[0].trim().equals("TRUE"))
				alPrograms.add(aProgDetails[1].trim());
		}
		return alPrograms;
	}
	static void createEnoviaContext() throws Exception {
		
		String strEnoviaHost = Activator.getDefault().getPreferenceStore().getString("com.fervort.enofer.preferencesstore.settings.enovia.host");
		String strEnoviaUsername= Activator.getDefault().getPreferenceStore().getString("com.fervort.enofer.preferencesstore.settings.enovia.username");
		String strEnoviaPassword = Activator.getDefault().getPreferenceStore().getString("com.fervort.enofer.preferencesstore.settings.enovia.password");
		String strEnoviaVault = Activator.getDefault().getPreferenceStore().getString("com.fervort.enofer.preferencesstore.settings.enovia.vault");
		
		Logger.write("strEnoviaHost "+strEnoviaHost);
		Logger.write("strEnoviaUsername "+strEnoviaUsername);
		Logger.write("strEnoviaPassword "+strEnoviaPassword);
		Logger.write("strEnoviaVault "+strEnoviaVault);
		
		
		context = new Context(strEnoviaHost);

		context.setUser(strEnoviaUsername);
		
		if(strEnoviaPassword.trim().equalsIgnoreCase("NULL"))
		{
			Logger.write("Accepted null password from user : "+strEnoviaPassword);
			
		}else
		{
			context.setPassword(strEnoviaPassword);
		}
		
		context.setVault(strEnoviaVault);
		context.connect();
		if (!context.isConnected())
		{
			Logger.write("Failed to connect to Enovia  : "+strEnoviaHost);
			
			throw new Exception("Failed to connect to Enovia : " + strEnoviaHost);
		}
		
		Logger.write("Connected to Enovia : "+strEnoviaHost);
		
		mqlCommand = new MQLCommand();
		mqlCommand.open(context);
	}

	public static String executeMQL(String strCommand) throws Exception
	{
		if(context==null || mqlCommand==null)
		{
			Logger.write("Context is NULL. Lets create it !");
			createEnoviaContext();
		}else
		{
			Logger.write("Context is already created");
		}
		
		Logger.write("MQL: "+strCommand);
		//strCommand = "escape " + strCommand;
		if (!mqlCommand.executeCommand(context, strCommand))
			throw new Exception("MQL Exception: " + mqlCommand.getError() + "\n" + strCommand);
		return mqlCommand.getResult();
	}

	static void releaseEnoviaContext() throws Exception {
		
		if(mqlCommand!=null)
		{
			mqlCommand.close(context);
		}
			
		if (context != null)
		{
			context.shutdown();
		}
		System.out.println("Enovia Context Released");
	}
	
	static Context getEnoviaContext()
	{
		return context;
	}
	
}
