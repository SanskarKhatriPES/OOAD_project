package com.ims.address;

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

import com.ims.unit.Unit;
import com.ims.utils.ExceptionUtils;

import jakarta.validation.Valid;

@RestController
public class AddressController {

	public static Logger LOGGER = LoggerFactory.getLogger(AddressController.class);
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping("/address")
	public ResponseEntity<?> findAll() {
		LOGGER.info("findAll");
		Map<String, List<Address>> rs = new HashMap<>();
		rs.put("addresses", addressRepo.findAll());
		return ResponseEntity.ok().body(rs);
	}
	
	@GetMapping("/address/{id}")
	public ResponseEntity<?> findById(@PathVariable long id) {
		LOGGER.info("findById - {}", id);
		Map<String, Address> rs = new HashMap<>();
		rs.put("address", addressRepo.findById(id));
		return ResponseEntity.ok().body(rs);
	}
	
	@PostMapping("/address")
	public ResponseEntity<?> addNew(@Valid @RequestBody Address address, BindingResult bindingResult) {
		LOGGER.info("addNew");
		if(bindingResult != null && bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		long id = addressRepo.addNew(address);
		return findById(id);
	}
	
	@DeleteMapping("/address/{id}")
	public ResponseEntity<?> deleteById(@PathVariable long id) {
		LOGGER.info("deleteById - {}", id);
		BindingResult bindingResult = new DirectFieldBindingResult(Address.class, Address.class.getName());
		
		if(!addressRepo.existsById(id)) {
			bindingResult.addError(new FieldError(Address.class.getName(), "addrId", messageSource.getMessage("Address.id.NonExistent", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		
		boolean deleted = addressRepo.deleteById(id);
		if(!deleted) {
			bindingResult.addError(new ObjectError(Address.class.getName(),  messageSource.getMessage("Address.DeleteById.Failure", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		return ResponseEntity.ok(messageSource.getMessage("Address.DeleteById.Success", null, Locale.ENGLISH));
	}
}
