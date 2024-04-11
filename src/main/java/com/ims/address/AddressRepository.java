package com.ims.address;

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

@Repository
public class AddressRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(AddressRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${db-queries.address.select}")
	private String selectQuery;
	
	@Value("${db-queries.address.insert}")
	private String insertQuery;
	
	@Value("${db-queries.address.delete}")
	private String deleteQuery;
	
	public List<Address> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectQuery, new AddressRowMapper());
	}
	
	public Address findById(long id) {
		LOGGER.info("findById - {}", id);
		List<Address> address = jdbcTemplate.query(selectQuery + " WHERE addr_id = ?", new AddressRowMapper(), id);
		return address.isEmpty() ? null : address.get(0);
	}
	
	public boolean existsById(long id) {
		LOGGER.info("existsById - {}", id);
		return findById(id) != null;
	}
	
	public long addNew(Address address) {
		LOGGER.info("addNew");
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertQuery, 
				Types.VARCHAR,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.VARCHAR);
		pscFactory.setReturnGeneratedKeys(true);
		pscFactory.setGeneratedKeysColumnNames("addr_id");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				address.getAddressLine1(),
				address.getAddressLine2(),
				address.getCity(),
				address.getState(),
				address.getCountry(),
				address.getPincode()
				));
		jdbcTemplate.update(psc, keyHolder);
		return keyHolder.getKey().longValue();		
	}
	
	public boolean deleteById(long id) {
		LOGGER.info("deleteById - {}", id);
		return jdbcTemplate.update(deleteQuery + " WHERE addr_id = ?", id) > 0;
	}
	
}

class AddressRowMapper implements RowMapper<Address> {

	@Override
	public Address mapRow(ResultSet rs, int rowNum) throws SQLException {
		Address address = new Address();
		
		address.setAddrId(rs.getLong("addr_id"));
		address.setAddressLine1(rs.getString("addr_line_1"));
		address.setAddressLine2(rs.getString("addr_line_2"));
		address.setCity(rs.getString("city"));
		address.setState(rs.getString("state"));
		address.setCountry(rs.getString("country"));
		address.setPincode(rs.getString("pincode"));
		
		return address;
	}
	
}
