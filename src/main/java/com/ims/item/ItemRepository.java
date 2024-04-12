package com.ims.item;

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

import com.ims.unit.Unit;
import com.ims.unit.UnitRepository;

@Repository
public class ItemRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(ItemRepository.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UnitRepository unitRepo;

	@Value("${db-queries.item.select}")
	private String selectQuery;

	@Value("${db-queries.item.insert}")
	private String insertQuery;

	@Value("${db-queries.item.delete}")
	private String deleteQuery;

	public List<Item> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectQuery, new ItemRowMapper(unitRepo));
	}

	public Item findById(long id) {
		LOGGER.info("findById - {}", id);
		List<Item> item = jdbcTemplate.query(selectQuery + " WHERE item_id = ?", new ItemRowMapper(unitRepo), id);
		return item.isEmpty() ? null : item.get(0);
	}

	public Item findByNameAndBatchNumber(String name, String batchNumber) {
		LOGGER.info("findByNameAndBatchNumber - {} {}", name, batchNumber);
		List<Item> item = jdbcTemplate.query(selectQuery + " WHERE UPPER(item_name) = ? AND UPPER(batch_number) = ?", new ItemRowMapper(unitRepo), name.toUpperCase(), batchNumber.toUpperCase());
		return item.isEmpty() ? null : item.get(0);
	}

	public boolean existsById(long id) {
		LOGGER.info("existsById - {}", id);
		return findById(id) != null;
	}

	public boolean existsByNameAndBatchNumber(String name, String batchNumber) {
		LOGGER.info("existsByNameAndBatchNumber - {} {}", name, batchNumber);
		return findByNameAndBatchNumber(name, batchNumber) != null;
	}

	public long addNew(Item item) {
		LOGGER.info("addNew");
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertQuery,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.NUMERIC,
				Types.NUMERIC,
				// Types.DATE,
				// Types.NUMERIC,
				Types.DATE);
		pscFactory.setReturnGeneratedKeys(true);
		pscFactory.setGeneratedKeysColumnNames("item_id");
		KeyHolder keyHolder = new GeneratedKeyHolder();

		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				item.getName(),
				item.getBatchNumber(),
				item.getUnit().getUnitCode(),
				item.getPurchasePrice(),
				item.getSellingPrice(),
				// item.getOpeningBalanceDate(),
				// item.getOpeningBalanceQty(),
				item.getExpiryDate()));
		jdbcTemplate.update(psc, keyHolder);
		return keyHolder.getKey().longValue();
	}

	public boolean deleteById(long id) {
		LOGGER.info("deleteById - {}", id);
		return jdbcTemplate.update(deleteQuery + " WHERE item_id = ?", id) > 0;
	}

}

class ItemRowMapper implements RowMapper<Item> {

	private UnitRepository unitRepo;

	public ItemRowMapper(UnitRepository unitRepo) {
		super();
		this.unitRepo = unitRepo;
	}

	@Override
	public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
		Item item = new Item();

		item.setId(rs.getLong("item_id"));
		item.setName(rs.getString("item_name"));
		item.setBatchNumber(rs.getString("batch_number"));
		item.setUnit(unitRepo.findByUnitCode(rs.getString("unit_code")));
		item.setSellingPrice(rs.getBigDecimal("selling_price"));
		item.setPurchasePrice(rs.getBigDecimal("purchase_price"));
		// item.setOpeningBalanceDate(rs.getDate("opening_balance_date"));
		// item.setOpeningBalanceQty(rs.getBigDecimal("opening_balance_qty"));
		item.setExpiryDate(rs.getDate("expiry_date"));

		return item;
	}

}
