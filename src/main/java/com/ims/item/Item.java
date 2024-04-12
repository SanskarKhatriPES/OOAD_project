package com.ims.item;

import java.math.BigDecimal;
import java.sql.Date;

import com.ims.unit.Unit;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class Item {

	private long id;

	@NotEmpty(message = "{Item.name.NotEmpty}")
	private String name;

	@NotEmpty(message = "{Item.batchNumber.NotEmpty}")
	private String batchNumber;

	@NotNull(message = "{Item.unit.NotNull}")
	private Unit unit;

	@NotNull(message = "{Item.sellingPrice.NotNull}")
	private BigDecimal sellingPrice;

	@NotNull(message = "{Item.purchasePrice.NotNull}")
	private BigDecimal purchasePrice;

	private Date expiryDate;

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
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	public BigDecimal getSellingPrice() {
		return sellingPrice;
	}
	public void setSellingPrice(BigDecimal sellingPrice) {
		this.sellingPrice = sellingPrice;
	}
	public BigDecimal getPurchasePrice() {
		return purchasePrice;
	}
	public void setPurchasePrice(BigDecimal purchasePrice) {
		this.purchasePrice = purchasePrice;
	}
}
