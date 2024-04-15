package com.ims.inventory;

import java.math.BigDecimal;
import java.sql.Date;

import com.ims.branchLocation.BranchLocation;
import com.ims.item.Item;

public class Inventory {

	private BranchLocation branchLocation;
	private Item item;
	private BigDecimal stockQuantity;
	private Date expiryDate;
	
	public BranchLocation getBranchLocation() {
		return branchLocation;
	}
	public void setBranchLocation(BranchLocation branchLocation) {
		this.branchLocation = branchLocation;
	}
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}
	public BigDecimal getStockQuantity() {
		return stockQuantity;
	}
	public void setStockQuantity(BigDecimal stockQuantity) {
		this.stockQuantity = stockQuantity;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
}
