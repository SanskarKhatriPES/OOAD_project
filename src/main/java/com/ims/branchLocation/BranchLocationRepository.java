package com.ims.branchLocation;

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
import com.ims.company.CompanyRepository;

@Repository
public class BranchLocationRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(BranchLocationRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${db-queries.branchLocation.select}")
	private String selectQuery;
	
	@Value("${db-queries.branchLocation.insert}")
	private String insertQuery;
	
	@Value("${db-queries.branchLocation.delete}")
	private String deleteQuery;
	
	@Autowired
	private CompanyRepository companyRepo;
	
	@Autowired
	private AddressRepository addressRepo;
	
	public List<BranchLocation> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectQuery, new BranchLocationRowMapper(companyRepo, addressRepo));
	}
	
	public BranchLocation findById(long id) {
		LOGGER.info("findById - {}", id);
		List<BranchLocation> bl = jdbcTemplate.query(selectQuery + " WHERE bl_id = ?", new BranchLocationRowMapper(companyRepo, addressRepo), id);
		return bl.isEmpty() ? null : bl.get(0);
	}
	
	public List<BranchLocation> findByCompanyId(long companyId) {
		LOGGER.info("findByCompanyId - {}", companyId);
		return jdbcTemplate.query(selectQuery + " WHERE company_id = ?", new BranchLocationRowMapper(companyRepo, addressRepo), companyId);
	}
	
	public boolean existsById(long id) {
		LOGGER.info("existsById - {}", id);
		BranchLocation bl = findById(id);
		return bl != null;
	}
	
	public boolean existsByNameAndCompanyId(String name, long companyId) {
		LOGGER.info("existsByNameAndCompanyId - {} {}", name, companyId);
		List<BranchLocation> bls = jdbcTemplate.query(selectQuery + " WHERE UPPER(bl_name) = ? AND company_id = ?", new BranchLocationRowMapper(companyRepo, addressRepo), name.toUpperCase(), companyId);
		return !bls.isEmpty();
	}
	
	public long addNew(BranchLocation bl) {
		LOGGER.info("addNew");
		
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertQuery,
				Types.VARCHAR,
				Types.INTEGER,
				Types.INTEGER);
		pscFactory.setReturnGeneratedKeys(true);
		pscFactory.setGeneratedKeysColumnNames("bl_id");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				bl.getName(),
				bl.getCompany().getId(),
				bl.getAddress().getAddrId()));
		jdbcTemplate.update(psc, keyHolder);
		
		return keyHolder.getKey().longValue();
	}
	
	public boolean deleteById(long id) {
		LOGGER.info("deleteById - {}", id);
		return jdbcTemplate.update(deleteQuery + " WHERE bl_id = ?", id) > 0;
	}
	
}

class BranchLocationRowMapper implements RowMapper<BranchLocation> {
	
	private CompanyRepository companyRepo;
	
	private AddressRepository addressRepo;
	
	public BranchLocationRowMapper(CompanyRepository companyRepo, AddressRepository addressRepo) {
		super();
		this.companyRepo = companyRepo;
		this.addressRepo = addressRepo;
	}

	@Override
	public BranchLocation mapRow(ResultSet rs, int rowNum) throws SQLException {
		BranchLocation branchLocation = new BranchLocation();
		
		branchLocation.setId(rs.getLong("bl_id"));
		branchLocation.setName(rs.getString("bl_name"));
		branchLocation.setCompany(companyRepo.findById(rs.getLong("company_id")));
		branchLocation.setAddress(addressRepo.findById(rs.getLong("bl_addr_id")));
		
		return branchLocation;
	}
	
}