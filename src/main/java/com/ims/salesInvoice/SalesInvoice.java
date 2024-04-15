package com.ims.salesInvoice;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.ims.address.Address;
import com.ims.branchLocation.BranchLocation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SalesInvoice {
	
	private long id;
	
	@NotNull(message = "{SalesInvoice.invoiceDate.NotNull}")
	private Date invoiceDate;
	
	@NotNull(message = "{SalesInvoice.bl.NotNull}")
	private BranchLocation branchLocation;
	
	@NotEmpty(message = "{SalesInvoice.customerName.NotEmpty}")
	private String customerName;
	
	@NotNull(message = "{SalesInvoice.shippingAddress.NotNull}")
	private Address shippingAddress;
	
	@NotNull(message = "{SalesInvoice.billingAddress.NotNull}")
	private Address billingAddress;
	
	
	private BigDecimal totalGst;
	private BigDecimal BillAmount;
	
	@NotEmpty(message = "{SalesInvoice.orderItems.NotEmpty}")
	private List<@Valid SalesInvoiceItem> orderItems;
	
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
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public Address getShippingAddress() {
		return shippingAddress;
	}
	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
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
		return BillAmount;
	}
	public void setBillAmount(BigDecimal billAmount) {
		BillAmount = billAmount;
	}
	public List<SalesInvoiceItem> getOrderItems() {
		return orderItems;
	}
	public void setOrderItems(List<SalesInvoiceItem> orderItems) {
		this.orderItems = orderItems;
	}	
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
}
