package com.ims.purchaseInvoice;

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
public class PurchaseInvoiceController {
	
	private static Logger LOGGER = LoggerFactory.getLogger(PurchaseInvoiceController.class);
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private PurchaseInvoiceRepository piRepo;
	
	@Autowired
	private BranchLocationRepository blRepo;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private ItemRepository itemRepo;
	
	@Autowired
	private InventoryRepository invRepo;
	
	@GetMapping("/pi")
	public ResponseEntity<?> findAll() {
		LOGGER.info("findAll");
		Map<String, List<PurchaseInvoice>> rs = new HashMap<>();
		rs.put("purchaseInvoices", piRepo.findAll());
		return ResponseEntity.ok().body(rs);
	}
	
	@GetMapping("/pi/{id}")
	public ResponseEntity<?> findById(@PathVariable long id) {
		LOGGER.info("findById - {}", id);
		Map<String, PurchaseInvoice> rs = new HashMap<>();
		rs.put("purchaseInvoice", piRepo.findById(id));
		return ResponseEntity.ok().body(rs);
	}
	
	@PostMapping("/pi")
	public ResponseEntity<?> addNew(@Valid @RequestBody PurchaseInvoice pi, BindingResult bindingResult) {
		LOGGER.info("addNew");
		if(bindingResult != null && bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		if(!isValidPurchaseInvoice(pi, bindingResult)) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		long id = piRepo.addNew(pi);
		return findById(id);		
	}
	
	@DeleteMapping("/pi/{id}")
	public ResponseEntity<?> deleteById(@PathVariable long id) {
		LOGGER.info("deleteById - {}", id);
		BindingResult bindingResult = new DirectFieldBindingResult(PurchaseInvoice.class, PurchaseInvoice.class.getName());
		
		if(!piRepo.existsById(id)) {
			bindingResult.addError(new FieldError(Item.class.getName(), "id", messageSource.getMessage("PurchaseInvoice.id.NonExistent", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		boolean deleted = piRepo.deleteById(id);
		if(!deleted) {
			bindingResult.addError(new ObjectError(PurchaseInvoice.class.getName(), messageSource.getMessage("PurchaseInvoice.deleteById.Failure", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		return ResponseEntity.ok().body(messageSource.getMessage("PurchaseInvoice.deleteById.Success", null, Locale.ENGLISH));		
	}
	
	public boolean isValidPurchaseInvoice (PurchaseInvoice pi, BindingResult bindingResult) {
		if (!blRepo.existsById(pi.getBranchLocation().getId())) {
			bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "branchLocation", messageSource.getMessage("PurchaseInvoice.bl.NonExistent", new Object[] {pi.getBranchLocation().getId()}, Locale.ENGLISH)));
		}
		if (!addressRepo.existsById(pi.getBillingAddress().getAddrId())) {
			bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "billingAddress", messageSource.getMessage("PurchaseInvoice.billingAddress.NonExistent", new Object[] {pi.getBillingAddress().getAddrId()}, Locale.ENGLISH)));			
		}
//		if (!pi.getTotalGst().equals(BigDecimal.ZERO) && pi.getTotalGst().compareTo(BigDecimal.ZERO) < 0) {
//			bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "totalGst", messageSource.getMessage("PurchaseInvoice.totalGst.Negative", null, Locale.ENGLISH)));			
//		}
//		if (!pi.getBillAmount().equals(BigDecimal.ZERO) && pi.getBillAmount().compareTo(BigDecimal.ZERO) < 0) {
//			bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "billAmount", messageSource.getMessage("PurchaseInvoice.billAmount.Negative", null, Locale.ENGLISH)));			
//		}
		if (pi.getOrderItems() != null && !pi.getOrderItems().isEmpty()) {
			for (PurchaseInvoiceItem item : pi.getOrderItems()) {
				if (!itemRepo.existsById(item.getItem().getId())) {
					bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "orderItems.id", messageSource.getMessage("PurchaseInvoiceItem.item.NonExistent", new Object[] {item.getItem().getId()}, Locale.ENGLISH)));
				} else if (blRepo.existsById(pi.getBranchLocation().getId()) && 
						(item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0)) { 
					if (invRepo.findExpiryDateIfExists(pi.getBranchLocation().getId(), item.getItem().getId()).compareTo(pi.getInvoiceDate()) < 0) {
						bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "orderItems.expiryDate", messageSource.getMessage("PurchaseInvoiceItem.quantity.ExpiredStock", new Object[] {item.getItem().getId()}, Locale.ENGLISH)));
					}
				}
//				if (!item.getGstAmount().equals(BigDecimal.ZERO) && item.getGstAmount().compareTo(BigDecimal.ZERO) < 0) {
//					bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "orderItems.gstAmount", messageSource.getMessage("PurchaseInvoiceItem.gstAmount.Negative", null, Locale.ENGLISH)));			
//				}
				if (item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
					bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "orderItems.quantity", messageSource.getMessage("PurchaseInvoiceItem.quantity.Negative", null, Locale.ENGLISH)));			
				}
//				if (!item.getTotalPrice().equals(BigDecimal.ZERO) && item.getTotalPrice().compareTo(BigDecimal.ZERO) < 0) {
//					bindingResult.addError(new FieldError(PurchaseInvoice.class.getName(), "orderItems.totalPrice", messageSource.getMessage("PurchaseInvoiceItem.totalPrice.Negative", null, Locale.ENGLISH)));			
//				}
			}
		}
		if (bindingResult != null && !bindingResult.hasErrors()) {
			BigDecimal totalBill = BigDecimal.ZERO;
			BigDecimal totalGst = BigDecimal.ZERO;			
			for (PurchaseInvoiceItem item : pi.getOrderItems()) {
				BigDecimal subTotal = itemRepo.findById(item.getItem().getId()).getPurchasePrice().multiply(item.getQuantity());
				BigDecimal taxAmount = subTotal.multiply(new BigDecimal(0.05));
				item.setGstAmount(taxAmount);
				item.setTotalPrice(subTotal.add(taxAmount));
				totalBill = totalBill.add(subTotal.add(taxAmount));
				totalGst = totalGst.add(taxAmount);
			}
			pi.setBillAmount(totalBill);
			pi.setTotalGst(totalGst);
		}
		return bindingResult != null && !bindingResult.hasErrors();
	}
}


