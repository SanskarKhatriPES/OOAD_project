package com.ims.item;

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

import com.ims.unit.UnitRepository;
import com.ims.utils.ExceptionUtils;

import jakarta.validation.Valid;

@RestController
public class ItemController {

	private static Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ItemRepository itemRepo;

	@Autowired
	private UnitRepository unitRepo;

	@GetMapping("/item")
	public ResponseEntity<?> findAll() {
		LOGGER.info("findAll");
		Map<String, List<Item>> rs = new HashMap<>();
		rs.put("items", itemRepo.findAll());
		return ResponseEntity.ok().body(rs);
	}

	@GetMapping("/item/{id}")
	public ResponseEntity<?> findById(@PathVariable long id) {
		LOGGER.info("findById - {}", id);
		Map<String, Item> rs = new HashMap<>();
		rs.put("item", itemRepo.findById(id));
		return ResponseEntity.ok().body(rs);
	}

	@GetMapping("/item/byNameAndBatchNumber/{name}/{batchNumber}")
	public ResponseEntity<?> findByNameAndBatchNumber(@PathVariable String name, @PathVariable String batchNumber) {
		LOGGER.info("findByNameAndBatchNumber - {} {}", name, batchNumber);
		Map<String, Item> rs = new HashMap<>();
		rs.put("item", itemRepo.findByNameAndBatchNumber(name, batchNumber));
		return ResponseEntity.ok().body(rs);
	}

	@PostMapping("/item")
	public ResponseEntity<?> addNew(@Valid @RequestBody Item item, BindingResult bindingResult) {
		LOGGER.info("addNew");
		if(bindingResult != null && bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		if(!isValidItem(item, bindingResult)) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		long id = itemRepo.addNew(item);
		return findById(id);
	}

	@DeleteMapping("/item/{id}")
	public ResponseEntity<?> deleteById(@PathVariable long id) {
		LOGGER.info("deleteById - {}", id);
		BindingResult bindingResult = new DirectFieldBindingResult(Item.class, Item.class.getName());

		if(!itemRepo.existsById(id)) {
			bindingResult.addError(new FieldError(Item.class.getName(), "id", messageSource.getMessage("Item.id.NonExistent", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		boolean deleted = itemRepo.deleteById(id);
		if(!deleted) {
			bindingResult.addError(new ObjectError(Item.class.getName(), messageSource.getMessage("Item.deleteById.Failure", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		return ResponseEntity.ok().body(messageSource.getMessage("Item.deleteById.Success", null, Locale.ENGLISH));
	}

	public boolean isValidItem(Item item, BindingResult bindingResult) {
		if(itemRepo.existsByNameAndBatchNumber(item.getName(), item.getBatchNumber())) {
			bindingResult.addError(new FieldError(Item.class.getName(), "batchNumber", messageSource.getMessage("Item.batchNumber.Duplicate", new Object[] {item.getName(), item.getBatchNumber()}, Locale.ENGLISH)));
		}
		if(!unitRepo.existsByUnitCode(item.getUnit().getUnitCode())) {
			bindingResult.addError(new FieldError(Item.class.getName(), "unit", messageSource.getMessage("Item.unit.NonExistent", new Object[] {item.getUnit().getUnitCode()}, Locale.ENGLISH)));
		}
		if(item.getPurchasePrice() != null && item.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
			bindingResult.addError(new FieldError(Item.class.getName(), "purchasePrice", messageSource.getMessage("Item.purchasePrice.Negative", null, Locale.ENGLISH)));
		}
		if(item.getSellingPrice() != null && item.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
			bindingResult.addError(new FieldError(Item.class.getName(), "sellingPrice", messageSource.getMessage("Item.sellingPrice.Negative", null, Locale.ENGLISH)));
		}
		return bindingResult != null && !bindingResult.hasErrors();
	}

}
