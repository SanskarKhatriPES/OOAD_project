package com.ims.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.validation.BindingResult;

public class ExceptionUtils {
private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionUtils.class);
	
	private static final String GLOBAL_ERRORS_COLLECTION_NAME = "globalErrors";
	private static final String FIELD_ERRORS_COLLECTION_NAME = "fieldErrors";
	

	public static Map<String, Object> getErrorMap(BindingResult bindingResult, Exception ex) {
		Map<String, Object> errorsMap = new HashMap<String, Object>(); 
		
		errorsMap.put(GLOBAL_ERRORS_COLLECTION_NAME, new ArrayList<Object>());
		errorsMap.put(FIELD_ERRORS_COLLECTION_NAME, new HashMap<String, Object>());
		
		if (bindingResult != null) {
			bindingResult.getGlobalErrors().forEach( error -> {
				((ArrayList<Object>)errorsMap.get(GLOBAL_ERRORS_COLLECTION_NAME)).add(error.getDefaultMessage());
			});
		
			HashMap<String, Object> fieldErrorsMaps = (HashMap<String, Object>)errorsMap.get(FIELD_ERRORS_COLLECTION_NAME);
			
			Set<String> errorFieldSet = new HashSet<String>();
			
			bindingResult.getFieldErrors().forEach(error ->{
				errorFieldSet.add(error.getField());
			});
			
			errorFieldSet.forEach(fieldName -> {
				
				List<String> fieldErrorList = new ArrayList<String>();
				bindingResult.getFieldErrors(fieldName).forEach(fieldError -> {
					fieldErrorList.add(fieldError.getDefaultMessage());
				});
				
				fieldErrorsMaps.put(fieldName, fieldErrorList);
			});
		}
		
		if (ex != null) {
			((ArrayList<Object>)errorsMap.get(GLOBAL_ERRORS_COLLECTION_NAME)).add(NestedExceptionUtils.getMostSpecificCause(ex).getMessage());
		}
		return errorsMap;
	}
}
