package com.ims.purchaseInvoice;

import java.math.BigDecimal;

import com.ims.item.Item;

import jakarta.validation.constraints.NotNull;

public class PurchaseInvoiceItem {
	
	@NotNull(message = "{PurchaseInvoiceItem.item.NotNull}")
	private Item item;
	
	private BigDecimal quantity = BigDecimal.ZERO;
	private BigDecimal totalPrice = BigDecimal.ZERO;
	private BigDecimal gstAmount = BigDecimal.ZERO;
	
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
