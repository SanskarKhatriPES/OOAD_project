package com.ims.address;

import jakarta.validation.constraints.NotEmpty;

public class Address {

	private long addrId;
	
	@NotEmpty(message = "{Address.addressLine1.NotEmpty}")
	private String addressLine1;
	
	private String addressLine2;
	
	@NotEmpty(message = "{Address.city.NotEmpty}")
	private String city;
	
	@NotEmpty(message = "{Address.state.NotEmpty}")
	private String state;
	
	@NotEmpty(message = "{Address.country.NotEmpty}")
	private String country;
	
	@NotEmpty(message = "{Address.pincode.NotEmpty}")
	private String pincode;
	
	public long getAddrId() {
		return addrId;
	}
	public void setAddrId(long addrId) {
		this.addrId = addrId;
	}
	public String getAddressLine1() {
		return addressLine1;
	}
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	public String getAddressLine2() {
		return addressLine2;
	}
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPincode() {
		return pincode;
	}
	public void setPincode(String pincode) {
		this.pincode = pincode;
	}
}
