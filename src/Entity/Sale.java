
package Entity;

import java.io.Serializable;
import java.sql.Date;

import enums.SaleStatus;
import helpinigStructForGUI.CheckBoxImplementation;

public class Sale extends CheckBoxImplementation implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4112663596694462413L;
	
	private Integer saleID;
	private SaleStatus status;
	private String companyName;
	private String fuelType;
	private float salePercent;
	private String startTime;
	private String endTime;
	private String startDate;
	private String endDate;
	private String saleDays;

private Boolean select;
	public Sale(Integer saleID, String status, String companyName, String fuelType,
			float salePercent, String startTime, String endTime, String startDate, String endDate, String saleDays) {
		this.saleID = saleID;
		this.status = SaleStatus.valueOf(status);
		this.companyName = companyName;
		this.fuelType = fuelType;
		this.salePercent = salePercent;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startDate = startDate;
		this.endDate = endDate;
		this.saleDays = saleDays;
		this.select = false;
	}


	public int getSaleID() {
		return saleID;
	}

	public void setSaleID(Integer saleID) {
		this.saleID = saleID;
	}

	public SaleStatus getStatus() {
		return status;
	}

	public void setStatus(SaleStatus status) {
		this.status = status;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getFuelType() {
		return fuelType;
	}

	public void setFuelType(String fuelType) {
		this.fuelType = fuelType;
	}


	public float getSalePercent() {
		return salePercent;
	}

	public void setSalePercent(float salePercent) {
		this.salePercent = salePercent;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getSaleDays() {
		return saleDays;
	}

	public void setSaleDays(String saleDays) {
		this.saleDays = saleDays;
	}


	@Override
	public String toString() {
		return "Sale [saleID=" + saleID + ", status=" + status + ", companyName=" + companyName + ", fuelType="
				+ fuelType + ", salePercent=" + salePercent + ", startTime="
				+ startTime + ", endTime=" + endTime + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", saleDays=" + saleDays + ", select=" + select + "]";
	}


	public Boolean getSelect() {
		return select;
	}


	public void setSelect(Boolean select) {
		this.select = select;
	}




}

