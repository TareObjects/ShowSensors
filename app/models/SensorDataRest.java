package models;

//SensorDataRest.java
//
//SensorDataをJsonで返す際に使用するクラス

import java.text.SimpleDateFormat;
import java.util.Date;

import controllers.Application;


public class SensorDataRest {
	
	public Date    logDate;
	public String  strDate;
	public Integer pirCount;
	public Integer doorCount;
	public String  otherInfo;
	public String  name;
	public String  strHHMM;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
	public SensorDataRest(SensorData sd) {
		this.logDate   = sd.logDate;
		this.strDate   = Application.sdf.format(sd.logDate);
		this.pirCount  = sd.pirCount;
		this.doorCount = sd.doorCount;
		this.otherInfo = sd.otherInfo;
		this.name      = sd.getDataOwner().name;
		this.strHHMM   = sdf.format(this.logDate);
	}
}

