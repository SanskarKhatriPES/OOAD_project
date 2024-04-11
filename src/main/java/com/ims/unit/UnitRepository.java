package com.ims.unit;

import java.sql.PreparedStatement;
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
public class UnitRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(UnitRepository.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${db-queries.unit.select}")
	private String selectQuery;
	
	@Value("${db-queries.unit.insert}")
	private String insertQuery;
	
	@Value("${db-queries.unit.delete}")
	private String deleteQuery;
	
	public List<Unit> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectQuery, new UnitRowMapper());
	}
	
	public Unit findByUnitCode(String unitCode) {
		LOGGER.info("findByUnitCode - {}", unitCode);
		List<Unit> unit = jdbcTemplate.query(selectQuery + " WHERE unit_code = ?", new UnitRowMapper(), unitCode);
		return unit.isEmpty() ? null : unit.get(0);
	}
	
	public boolean existsByUnitCode(String unitCode) {
		LOGGER.info("existsByUnitCode - {}", unitCode);
		return findByUnitCode(unitCode) != null;
	}
	
	public boolean addNew(Unit unit) {
		LOGGER.info("addNew", unit.getUnitCode(), unit.getName(), unit.isFractional(), unit.getFractionalDigits());
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertQuery, 
				Types.VARCHAR,
				Types.VARCHAR,
				Types.TINYINT,
				Types.INTEGER);
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				unit.getUnitCode(),
				unit.getName(),
				unit.isFractional() ? 1 : 0,
				unit.getFractionalDigits()
				));
		
		int rowsAffected = jdbcTemplate.update(psc);
		
		return rowsAffected != 0 ? true : false;
	}
	
	public boolean deleteByUnitCode(String unitCode) {
		LOGGER.info("deleteByUnitCode - {}", unitCode);
		return jdbcTemplate.update(deleteQuery + " WHERE unit_code = ?", unitCode) > 0;
	}
}

class UnitRowMapper implements RowMapper<Unit> {

	@Override
	public Unit mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Unit unit = new Unit();
		
		unit.setUnitCode(rs.getString("unit_code"));
		unit.setName(rs.getString("unit_name"));
		unit.setFractional(rs.getBoolean("is_fractional"));
		unit.setFractionalDigits(rs.getInt("fractional_digits"));
		
		return unit;
	}
	
	
}
