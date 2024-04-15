package com.ims.salesInvoice;

import java.math.BigDecimal;

import com.ims.item.Item;

import jakarta.validation.constraints.NotNull;

public class SalesInvoiceItem {

	@NotNull(message = "{SalesInvoiceItem.item.NotNull}")
	private Item item;
	
	private BigDecimal quantity;
	private BigDecimal totalPrice;
	private BigDecimal gstAmount;
	
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}
	public BigDecimal getQuantity() {
		return quantity;
	}
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}
	public BigDecimal getGstAmount() {
		return gstAmount;
	}
	public void setGstAmount(BigDecimal gstAmount) {
		this.gstAmount = gstAmount;
	}
	
}
