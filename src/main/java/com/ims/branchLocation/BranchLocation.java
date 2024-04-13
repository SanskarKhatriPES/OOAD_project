package com.ims.branchLocation;

import com.ims.address.Address;
import com.ims.company.Company;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class BranchLocation {
	
	private long id;
	
	@NotEmpty(message = "{BranchLocation.name.NotEmpty}")
	private String name;
	
	@NotNull(message = "{BranchLocation.company.NotNull}")
	private Company company;
	
	@NotNull(message = "{BranchLocation.address.NotNull}")
	private Address address;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
}
