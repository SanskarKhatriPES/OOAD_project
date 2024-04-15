package com.ims.salesInvoice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ims.address.AddressRepository;
import com.ims.branchLocation.BranchLocationRepository;
import com.ims.inventory.InventoryRepository;
import com.ims.item.Item;
import com.ims.item.ItemRepository;
import com.ims.utils.ExceptionUtils;

import jakarta.validation.Valid;

@RestController
public class SalesInvoiceController {
	
	private static Logger LOGGER = LoggerFactory.getLogger(SalesInvoiceController.class);
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private SalesInvoiceRepository siRepo;
	
	@Autowired
	private BranchLocationRepository blRepo;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private ItemRepository itemRepo;
	
	@Autowired
	private InventoryRepository invRepo;
	
	@GetMapping("/si")
	public ResponseEntity<?> findAll() {
		LOGGER.info("findAll");
		Map<String, List<SalesInvoice>> rs = new HashMap<>();
		rs.put("salesInvoices", siRepo.findAll());
		return ResponseEntity.ok().body(rs);
	}
	
	@GetMapping("/si/{id}")
	public ResponseEntity<?> findById(@PathVariable long id) {
		LOGGER.info("findById - {}", id);
		Map<String, SalesInvoice> rs = new HashMap<>();
		rs.put("salesInvoice", siRepo.findById(id));
		return ResponseEntity.ok().body(rs);
	}
	
	@PostMapping("/si")
	public ResponseEntity<?> addNew(@Valid @RequestBody SalesInvoice si, BindingResult bindingResult) {
		LOGGER.info("addNew");
		if(bindingResult != null && bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		if(!isValidSalesInvoice(si, bindingResult)) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		long id = siRepo.addNew(si);
		return findById(id);		
	}
	
	@DeleteMapping("/si/{id}")
	public ResponseEntity<?> deleteById(@PathVariable long id) {
		LOGGER.info("deleteById - {}", id);
		BindingResult bindingResult = new DirectFieldBindingResult(SalesInvoice.class, SalesInvoice.class.getName());
		
		if(!siRepo.existsById(id)) {
			bindingResult.addError(new FieldError(Item.class.getName(), "id", messageSource.getMessage("SalesInvoice.id.NonExistent", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		boolean deleted = siRepo.deleteById(id);
		if(!deleted) {
			bindingResult.addError(new ObjectError(SalesInvoice.class.getName(), messageSource.getMessage("SalesInvoice.deleteById.Failure", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		return ResponseEntity.ok().body(messageSource.getMessage("SalesInvoice.deleteById.Success", null, Locale.ENGLISH));		
	}
	
	public boolean isValidSalesInvoice (SalesInvoice si, BindingResult bindingResult) {
		if (!blRepo.existsById(si.getBranchLocation().getId())) {
			bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "branchLocation", messageSource.getMessage("SalesInvoice.bl.NonExistent", new Object[] {si.getBranchLocation().getId()}, Locale.ENGLISH)));
		}
		if (!addressRepo.existsById(si.getBillingAddress().getAddrId())) {
			bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "billingAddress", messageSource.getMessage("SalesInvoice.billingAddress.NonExistent", new Object[] {si.getBillingAddress().getAddrId()}, Locale.ENGLISH)));			
		}
		if (!addressRepo.existsById(si.getShippingAddress().getAddrId())) {
			bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "shippingAddress", messageSource.getMessage("SalesInvoice.shippingAddress.NonExistent", new Object[] {si.getShippingAddress().getAddrId()}, Locale.ENGLISH)));			
		}
//		if (!si.getTotalGst().equals(BigDecimal.ZERO) && si.getTotalGst().compareTo(BigDecimal.ZERO) < 0) {
//			bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "totalGst", messageSource.getMessage("SalesInvoice.totalGst.Negative", null, Locale.ENGLISH)));			
//		}
//		if (!si.getBillAmount().equals(BigDecimal.ZERO) && si.getBillAmount().compareTo(BigDecimal.ZERO) < 0) {
//			bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "billAmount", messageSource.getMessage("SalesInvoice.billAmount.Negative", null, Locale.ENGLISH)));			
//		}
		if (si.getOrderItems() != null && !si.getOrderItems().isEmpty()) {
			for (SalesInvoiceItem item : si.getOrderItems()) {
				if (!itemRepo.existsById(item.getItem().getId())) {
					bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "orderItems.id", messageSource.getMessage("SalesInvoiceItem.item.NonExistent", new Object[] {item.getItem().getId()}, Locale.ENGLISH)));
				} else if (blRepo.existsById(si.getBranchLocation().getId()) && 
						(item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0)) { 
					if (invRepo.findAvailableQuantity(si.getBranchLocation().getId(), item.getItem().getId()).compareTo(item.getQuantity()) < 0) {
						bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "orderItems.quantity", messageSource.getMessage("SalesInvoiceItem.quantity.InsufficientStock", new Object[] {item.getQuantity(), item.getItem().getId()}, Locale.ENGLISH)));
					}
					if (invRepo.findExpiryDateIfExists(si.getBranchLocation().getId(), item.getItem().getId()).compareTo(si.getInvoiceDate()) < 0) {
						bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "orderItems.expiryDate", messageSource.getMessage("SalesInvoiceItem.quantity.ExpiredStock", new Object[] {item.getItem().getId()}, Locale.ENGLISH)));
					}
				}
//				if (!item.getGstAmount().equals(BigDecimal.ZERO) && item.getGstAmount().compareTo(BigDecimal.ZERO) < 0) {
//					bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "orderItems.gstAmount", messageSource.getMessage("SalesInvoiceItem.gstAmount.Negative", null, Locale.ENGLISH)));			
//				}
				if (item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
					bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "orderItems.quantity", messageSource.getMessage("SalesInvoiceItem.quantity.Negative", null, Locale.ENGLISH)));			
				}
//				if (!item.getTotalPrice().equals(BigDecimal.ZERO) && item.getTotalPrice().compareTo(BigDecimal.ZERO) < 0) {
//					bindingResult.addError(new FieldError(SalesInvoice.class.getName(), "orderItems.totalPrice", messageSource.getMessage("SalesInvoiceItem.totalPrice.Negative", null, Locale.ENGLISH)));			
//				}
			}
		}
		if (bindingResult != null && !bindingResult.hasErrors()) {
			BigDecimal totalBill = BigDecimal.ZERO;
			BigDecimal totalGst = BigDecimal.ZERO;			
			for (SalesInvoiceItem item : si.getOrderItems()) {
				BigDecimal subTotal = itemRepo.findById(item.getItem().getId()).getSellingPrice().multiply(item.getQuantity());
				BigDecimal taxAmount = subTotal.multiply(new BigDecimal(0.05));
				item.setGstAmount(taxAmount);
				item.setTotalPrice(subTotal.add(taxAmount));
				totalBill = totalBill.add(subTotal.add(taxAmount));
				totalGst = totalGst.add(taxAmount);
			}
			si.setBillAmount(totalBill);
			si.setTotalGst(totalGst);
		}
		return bindingResult != null && !bindingResult.hasErrors();
	}
}
