package com.ims.unit;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.AbstractBindingResult;
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

import com.ims.utils.ExceptionUtils;

import jakarta.validation.Valid;

@RestController
public class UnitController {
	
	private static Logger LOGGER = LoggerFactory.getLogger(UnitController.class);
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private UnitRepository unitRepo;
	
	@GetMapping("/unit")
	public ResponseEntity<?> findAll() {
		Map<String, List<Unit>> rs = new HashMap<>();
		rs.put("units", unitRepo.findAll());
		return ResponseEntity.ok(rs);
	}
	
	@GetMapping("/unit/{unitCode}")
	public ResponseEntity<?> findByUnitCode(@PathVariable String unitCode) {
		Map<String, Unit> rs = new HashMap<>();
		rs.put("unit", unitRepo.findByUnitCode(unitCode));
		return ResponseEntity.ok(rs);
	}
	
	@PostMapping("/unit")
	public ResponseEntity<?> addNew(@Valid @RequestBody Unit unit, BindingResult bindingResult) {
		LOGGER.info("addNew");
		
		if (bindingResult != null && bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		} else {
			if (!isValidUnit(unit, bindingResult)) {
				return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
			}
			boolean added = unitRepo.addNew(unit);
			return findByUnitCode(unit.getUnitCode());
		}
	}
	
	@DeleteMapping("/unit/{unitCode}")
	public ResponseEntity<?> deleteByUnitCode(@PathVariable String unitCode) {
		LOGGER.info("deleteByUnitCode - {}", unitCode);
		BindingResult bindingResult = new DirectFieldBindingResult(Unit.class, Unit.class.getName()); 
		
		if (!unitRepo.existsByUnitCode(unitCode)) {
			bindingResult.addError(new FieldError(Unit.class.getName(), "unitCode", messageSource.getMessage("Unit.unitCode.NotExistent", new Object[] {unitCode}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));
		}
		boolean deleted = unitRepo.deleteByUnitCode(unitCode);
		if (!deleted) {
			bindingResult.addError(new ObjectError(Unit.class.getName(),  messageSource.getMessage("Unit.DeleteByUnitCode.Failure", new Object[] {unitCode}, Locale.ENGLISH)));
			return ResponseEntity.badRequest().body(ExceptionUtils.getErrorMap(bindingResult, null));			
		}
		return ResponseEntity.ok().body(messageSource.getMessage("Unit.DeleteByUnitCode.Success", null, Locale.ENGLISH));
	}
	
	public boolean isValidUnit (Unit unit, BindingResult bindingResult) {
		if (unitRepo.existsByUnitCode(unit.getUnitCode())) {
			bindingResult.addError(new FieldError(Unit.class.getName(), "unitCode", messageSource.getMessage("Unit.unitCode.Duplicate", new Object[] {unit.getUnitCode()}, Locale.ENGLISH)));
		}
		return bindingResult != null && !bindingResult.hasErrors();
	}
}
