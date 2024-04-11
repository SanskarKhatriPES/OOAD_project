package com.ims.company;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ims.address.AddressRepository;

@Repository
public class CompanyRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(CompanyRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${db-queries.company.select}")
	private String selectQuery;
	
	@Value("${db-queries.company.insert}")
	private String insertQuery;
	
	@Value("${db-queries.company.delete}")
	private String deleteQuery;
	
	@Autowired
	private AddressRepository addressRepo;
	
	public List<Company> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectQuery, new CompanyRowMapper(addressRepo));
	}
	
	public Company findById(long id) {
		LOGGER.info("findyId - {}", id);
		List<Company> company = jdbcTemplate.query(selectQuery + " WHERE company_id = ?", new CompanyRowMapper(addressRepo), id);
		return company.isEmpty() ? null : company.get(0);
	}
	
	public Company findByName(String name) {
		LOGGER.info("findByName - {}", name);
		List<Company> company = jdbcTemplate.query(selectQuery + " WHERE UPPER(company_name) = ?", new CompanyRowMapper(addressRepo), name.toUpperCase());
		return company.isEmpty() ? null : company.get(0);
	}
	
	public Company findByGstin(String gstin) {
		LOGGER.info("findByGstin - {}", gstin);
		List<Company> company = jdbcTemplate.query(selectQuery + " WHERE UPPER(gstin) = ?", new CompanyRowMapper(addressRepo), gstin.toUpperCase());
		return company.isEmpty() ? null : company.get(0);
	}
	
	public boolean existsById(long id) {
		LOGGER.info("existsById - {}", id);
		return findById(id) != null;
	}
	
	public boolean existsByName(String name) {
		LOGGER.info("existsByName - {}", name);
		return findByName(name) != null;
	}
	
	public boolean existsByGstin(String gstin) {
		LOGGER.info("existsByGstin - {}", gstin);
		return findByGstin(gstin) != null;
	}
	
	public long addNew(Company company) {
		LOGGER.info("addNew");
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertQuery,
				Types.VARCHAR,
				Types.INTEGER,
				Types.VARCHAR);
		pscFactory.setReturnGeneratedKeys(true);
		pscFactory.setGeneratedKeysColumnNames("company_id");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				company.getName(),
				company.getHeadquarterAddress().getAddrId(),
				company.getGstin()));
		jdbcTemplate.update(psc, keyHolder);
		
		return keyHolder.getKey().longValue();
	}
	
	public boolean deleteById(long id) {
		LOGGER.info("deleteById - {}", id);
		return jdbcTemplate.update(deleteQuery + " WHERE company_id = ?", id) > 0;
	}
}

class CompanyRowMapper implements RowMapper<Company> {
	
	private AddressRepository addressRepo;
	
	public CompanyRowMapper(AddressRepository addressRepo) {
		super();
		this.addressRepo = addressRepo;
	}

	@Override
	public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Company company = new Company();
		
		company.setId(rs.getLong("company_id"));
		company.setName(rs.getString("company_name"));
		company.setHeadquarterAddress(addressRepo.findById(rs.getLong("headquarter_addr_id")));
		company.setGstin(rs.getString("gstin"));
				
		return company;
	}
	
}
