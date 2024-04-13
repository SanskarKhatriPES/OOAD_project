package com.ims.branchLocation;

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

import com.ims.address.Address;
import com.ims.address.AddressRepository;
import com.ims.company.CompanyRepository;
import com.ims.utils.ExceptionUtils;

import jakarta.validation.Valid;

@RestController
public class BranchLocationController {

	public static Logger LOGGER = LoggerFactory.getLogger(BranchLocationController.class);
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private BranchLocationRepository blRepo;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private CompanyRepository companyRepo;
	
	@GetMapping("/branchLocation")
	public ResponseEntity<?> findAll() {
		LOGGER.info("findAll");
		Map<String, List<BranchLocation>> rs = new HashMap<>();
		rs.put("branchLocations", blRepo.findAll());
		return ResponseEntity.ok().body(rs);
	}
	
	@GetMapping("/branchLocation/{id}")
	public ResponseEntity<?> findById(@PathVariable long id) {
		LOGGER.info("findById - {}", id);
		Map<String, BranchLocation> rs = new HashMap<>();
		rs.put("branchLocation", blRepo.findById(id));
		return ResponseEntity.ok().body(rs);
	}
	
	@GetMapping("/branchLocation/byCompany/{companyId}")
	public ResponseEntity<?> findByCompanyId(@PathVariable long companyId) {
		LOGGER.info("findByCompanyId - {}", companyId);
		Map<String, List<BranchLocation>> rs = new HashMap();
		rs.put("branchLocations", blRepo.findByCompanyId(companyId));
		return ResponseEntity.ok().body(rs);
	}
	
	@PostMapping("/branchLocation")
	public ResponseEntity<?> addNew(@Valid @RequestBody BranchLocation bl, BindingResult bindingResult) {
		LOGGER.info("addNew");
		if(bindingResult != null && bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		if(!isValidBranchLocation(bl, bindingResult)) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		long id = blRepo.addNew(bl);
		return findById(id);		
	}
	
	@DeleteMapping("/branchLocation/{id}")
	public ResponseEntity<?> deleteById(@PathVariable long id) {
		LOGGER.info("deleteById - {}", id);
		BindingResult bindingResult = new DirectFieldBindingResult(BranchLocation.class, BranchLocation.class.toString());
		
		if(!blRepo.existsById(id)) {
			bindingResult.addError(new FieldError(BranchLocation.class.getName(), "id", messageSource.getMessage("BranchLocation.id.NonExistent", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		
		boolean deleted = blRepo.deleteById(id);
		if (!deleted) {
			bindingResult.addError(new ObjectError(BranchLocation.class.getName(),  messageSource.getMessage("BranchLocation.DeleteById.Failure", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		return ResponseEntity.ok(messageSource.getMessage("BranchLocation.DeleteById.Success", null, Locale.ENGLISH));
	}
	
	public boolean isValidBranchLocation(BranchLocation bl, BindingResult bindingResult) {
		if(blRepo.existsByNameAndCompanyId(bl.getName(), bl.getCompany().getId())) {
			bindingResult.addError(new FieldError(BranchLocation.class.getName(), "name", messageSource.getMessage("BranchLocation.name.Duplicate", new Object[] {bl.getName(), bl.getCompany().getId()}, Locale.ENGLISH)));
		}
		if(!companyRepo.existsById(bl.getCompany().getId())) {
			bindingResult.addError(new FieldError(BranchLocation.class.getName(), "company", messageSource.getMessage("BranchLocation.company.NonExistent", new Object[] {bl.getCompany().getId()}, Locale.ENGLISH)));
		}
		if(!addressRepo.existsById(bl.getAddress().getAddrId())) {
			bindingResult.addError(new FieldError(BranchLocation.class.getName(), "address", messageSource.getMessage("BranchLocation.address.NonExistent", new Object[] {bl.getAddress().getAddrId()}, Locale.ENGLISH)));
		}
		return bindingResult != null && !bindingResult.hasErrors();
	}
}
