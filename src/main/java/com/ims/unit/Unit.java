package com.ims.unit;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class Unit {
	
	@NotEmpty(message = "{Unit.unitCode.NotEmpty}")
	private String unitCode;
	
	@NotEmpty(message = "{Unit.unitName.NotEmpty}")
	private String name;
		
	private boolean fractional = false;
	
	private int fractionalDigits = 0;
	
	public String getUnitCode() {
		return unitCode;
	}
	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isFractional() {
		return fractional;
	}
	public void setFractional(boolean fractional) {
		this.fractional = fractional;
		if (!fractional) {
			this.fractionalDigits = 0;
		}
	}
	public int getFractionalDigits() {
		return fractionalDigits;
	}
	public void setFractionalDigits(int fractionalDigits) {
		this.fractionalDigits = fractionalDigits;
		if (fractionalDigits == 0) {
			this.fractional = false;
		}
	}
	
}
