package com.ims.company;

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
import com.ims.utils.ExceptionUtils;

import jakarta.validation.Valid;

@RestController
public class CompanyController {

	public static Logger LOGGER = LoggerFactory.getLogger(CompanyController.class);
	
	@Autowired
	private CompanyRepository companyRepo;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@GetMapping("/company")
	public  ResponseEntity<?> findAll() {
		LOGGER.info("findAll");
		Map<String, List<Company>> rs = new HashMap<>();
		rs.put("companies", companyRepo.findAll());
		return ResponseEntity.ok().body(rs);
	}
	
	@GetMapping("/company/{id}")
	public  ResponseEntity<?> findById(@PathVariable long id) {
		LOGGER.info("findAll");
		Map<String, Company> rs = new HashMap<>();
		rs.put("company", companyRepo.findById(id));
		return ResponseEntity.ok().body(rs);
	}
	
	@PostMapping("/company")
	public ResponseEntity<?> addNew(@Valid @RequestBody Company company, BindingResult bindingResult) {
		LOGGER.info("addNew");
		if(bindingResult != null && bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		if(!isValidCompany(company, bindingResult)) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		long companyId = companyRepo.addNew(company);
		return findById(companyId);		
	}
	
	@DeleteMapping("/company/{id}")
	public ResponseEntity<?> deleteById(@PathVariable long id) {
		LOGGER.info("deleteById - {}", id);
		BindingResult bindingResult = new DirectFieldBindingResult(Company.class, Company.class.getName());
		
		if(!companyRepo.existsById(id)) {
			bindingResult.addError(new FieldError(Company.class.getName(), "id", messageSource.getMessage("Company.id.NonExistent", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		boolean deleted = companyRepo.deleteById(id);
		if(!deleted) {
			bindingResult.addError(new ObjectError(Company.class.getName(), messageSource.getMessage("Company.deleteById.Failure", new Object[] {id}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		return ResponseEntity.ok().body(messageSource.getMessage("Company.deleteById.Success", null, Locale.ENGLISH));		
	}
	
	public boolean isValidCompany(Company company, BindingResult bindingResult) {
		if(companyRepo.existsByName(company.getName())) {
			bindingResult.addError(new FieldError(Company.class.getName(), "name", messageSource.getMessage("Company.name.Duplicate", new Object[] {company.getName()}, Locale.ENGLISH)));
		}
		if(companyRepo.existsByGstin(company.getGstin())) {
			bindingResult.addError(new FieldError(Company.class.getName(), "gstin", messageSource.getMessage("Company.gstin.Duplicate", new Object[] {company.getGstin()}, Locale.ENGLISH)));	
		}
		if(!addressRepo.existsById(company.getHeadquarterAddress().getAddrId())) {
			bindingResult.addError(new FieldError(Company.class.getName(), "headquarterAddress", messageSource.getMessage("Company.headquarterAddress.NonExistent", new Object[] {company.getHeadquarterAddress().getAddrId()}, Locale.ENGLISH)));
		}
		return bindingResult != null && !bindingResult.hasErrors();
	}
 	
}
