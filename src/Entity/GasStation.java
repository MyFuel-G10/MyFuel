package Entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class GasStation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5343787255377018218L;
	private String companyName;
	private Integer stationID;
	private String cityName;
	private String areaName;
	private StationManager manger;
	private int stationMangerWorkerID;
	private Set<StationFuel> stationFuel = new HashSet<StationFuel>();

	public GasStation(String companyName, Integer stationID, String cityName, String areaName, StationManager manger,
			Set<StationFuel> stationFuel) {
		this.companyName = companyName;
		this.stationID = stationID;
		this.cityName = cityName;
		this.areaName = areaName;
		this.manger = manger;
		this.stationFuel = stationFuel;
		
		stationMangerWorkerID=0;
	}
	
	public GasStation(String companyName, Integer stationID, String cityName, String areaName, int stationMangerWorkerID) {
		this.companyName = companyName;
		this.stationID = stationID;
		this.cityName = cityName;
		this.areaName = areaName;
		this.stationMangerWorkerID=stationMangerWorkerID;
		
		this.manger = null;
		this.stationFuel = null;
	}

	
	public int getStationMangerWorkerID() {
		return stationMangerWorkerID;
	}

	public void setStationMangerWorkerID(int stationMangerWorkerID) {
		this.stationMangerWorkerID = stationMangerWorkerID;
	}

	public void setStationID(Integer stationID) {
		this.stationID = stationID;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getStationID() {
		return stationID;
	}

	public void setStationID(int stationID) {
		this.stationID = stationID;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public StationManager getManger() {
		return manger;
	}

	public void setManger(StationManager manger) {
		this.manger = manger;
	}

	public Set<StationFuel> getStationFuel() {
		return stationFuel;
	}

	public void setStationFuel(Set<StationFuel> stationFuel) {
		this.stationFuel = stationFuel;
	}

	@Override
	public String toString() {
		return "GasStation [companyName=" + companyName + ", stationID=" + stationID + ", cityName=" + cityName
				+ ", areaName=" + areaName + ", manger=" + manger + ", stationFuel=" + stationFuel + "]";
	}
	
}

