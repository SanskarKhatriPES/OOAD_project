package com.ims.purchaseInvoice;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.ims.address.Address;
import com.ims.branchLocation.BranchLocation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PurchaseInvoice {
	
	private long id;
	
	@NotNull(message = "{PurchaseInvoice.invoiceDate.NotNull}")
	private Date invoiceDate;
	
	@NotNull(message = "{PurchaseInvoice.bl.NotNull}")
	private BranchLocation branchLocation;
	
	@NotEmpty(message = "{PurchaseInvoice.vendorName.NotEmpty}")
	private String vendorName;
	
	@NotNull(message = "{PurchaseInvoice.billingAddress.NotNull}")
	private Address billingAddress;
	
	private BigDecimal totalGst = BigDecimal.ZERO;
	private BigDecimal billAmount = BigDecimal.ZERO;
	
	@NotEmpty(message = "{PurchaseInvoice.orderItems.NotEmpty}")
	private List<@Valid PurchaseInvoiceItem> orderItems;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public BranchLocation getBranchLocation() {
		return branchLocation;
	}
	public void setBranchLocation(BranchLocation branchLocation) {
		this.branchLocation = branchLocation;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public Address getBillingAddress() {
		return billingAddress;
	}
	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}
	public BigDecimal getTotalGst() {
		return totalGst;
	}
	public void setTotalGst(BigDecimal totalGst) {
		this.totalGst = totalGst;
	}
	public BigDecimal getBillAmount() {
		return billAmount;
	}
	public void setBillAmount(BigDecimal billAmount) {
		this.billAmount = billAmount;
	}
	public List<PurchaseInvoiceItem> getOrderItems() {
		return orderItems;
	}
	public void setOrderItems(List<PurchaseInvoiceItem> orderItems) {
		this.orderItems = orderItems;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
}
