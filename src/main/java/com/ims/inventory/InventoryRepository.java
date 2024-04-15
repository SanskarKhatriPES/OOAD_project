package com.ims.inventory;

import java.math.BigDecimal;
import java.sql.Date;
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

import com.ims.branchLocation.BranchLocationRepository;
import com.ims.item.ItemRepository;

@Repository
public class InventoryRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(InventoryRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${db-queries.inventory.select}")
	private String selectQuery;
	
	@Value("${db-queries.inventory.insert}")
	private String insertQuery;
	
	@Value("${db-queries.inventory.delete}")
	private String deleteQuery;
	
	@Value("${db-queries.inventory.update}")
	private String updateQuery;
	
	@Autowired
	private ItemRepository itemRepo;
	
	@Autowired
	private BranchLocationRepository blRepo;
	
	public List<Inventory> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectQuery, new InventoryRowMapper(blRepo, itemRepo));
	}
	
	public List<Inventory> findAllByBranchLocationId(long blId) {
		LOGGER.info("findAllByBranchLocationId - {}", blId);
		return jdbcTemplate.query(selectQuery + " WHERE branch_location_id = ?", new InventoryRowMapper(blRepo, itemRepo), blId);
	}
	
	public Inventory findByBranchLocationIdAndItemId (long blId, long itemId) {
		LOGGER.info("findByBranchLocationIdAndItemId - {} {}", blId, itemId);
		List<Inventory> inv = jdbcTemplate.query(selectQuery + " WHERE branch_location_id = ? AND item_id = ?", new InventoryRowMapper(blRepo, itemRepo), blId, itemId);
		return inv.isEmpty() ? null : inv.get(0);
	}
	
	public boolean existsByBranchLocationIdAndItemId (long blId, long itemId) {
		LOGGER.info("existsByBranchLocationIdAndItemId - {} {}", blId, itemId);
		return findByBranchLocationIdAndItemId(blId, itemId) != null;
	}
	
	public BigDecimal findAvailableQuantity (long blId, long itemId) {
		LOGGER.info("findAvailableQuantity - {} {}", blId, itemId);
		Inventory inv = findByBranchLocationIdAndItemId(blId, itemId);
		if (inv != null) {
			return inv.getStockQuantity();
		}
		return BigDecimal.ZERO;
	}
	
	public Date findExpiryDateIfExists (long blId, long itemId) {
		LOGGER.info("findExpiryDateIfExists - {} {}", blId, itemId);
		Inventory inv = findByBranchLocationIdAndItemId(blId, itemId);
		if (inv != null) {
			return inv.getExpiryDate();
		}
		return null;
	}
	
	public void addStock (long blId, long itemId, BigDecimal quantity) {
		LOGGER.info("addStock - {} {}", blId, itemId);
		Inventory inv = findByBranchLocationIdAndItemId(blId, itemId);
		if (inv != null) {
			updateStockQuantity(blId, itemId, quantity.add(inv.getStockQuantity()));
		}
	}
	
	public void removeStock (long blId, long itemId, BigDecimal quantity) {
		LOGGER.info("removeStock - {} {}", blId, itemId);
		Inventory inv = findByBranchLocationIdAndItemId(blId, itemId);
		if (inv != null && findAvailableQuantity(blId, itemId).compareTo(quantity) >= 0) {
			updateStockQuantity(blId, itemId, inv.getStockQuantity().subtract(quantity));
		}
	}
	
	public void addNew (long blId, long itemId, BigDecimal quantity, Date expiryDate) {
		LOGGER.info("addNew - {} {}", blId, itemId);
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertQuery, 
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.DATE);
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				itemId,
				quantity,
				blId,
				expiryDate));
		jdbcTemplate.update(psc);	
	}
	
	private void updateStockQuantity (long blId, long itemId, BigDecimal quantity) {
		LOGGER.info("updateStockQuantity - {} {}", blId, itemId);
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(updateQuery, 
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC);
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				quantity,
				blId,
				itemId));
		jdbcTemplate.update(psc);
	}
	
	
}

class InventoryRowMapper implements RowMapper<Inventory> {
	
	BranchLocationRepository blRepo;
	ItemRepository itemRepo;
	
	public InventoryRowMapper(BranchLocationRepository blRepo, ItemRepository itemRepo) {
		super();
		this.blRepo = blRepo;
		this.itemRepo = itemRepo;
	}

	@Override
	public Inventory mapRow(ResultSet rs, int rowNum) throws SQLException {
		Inventory inv = new Inventory();
		
		inv.setBranchLocation(blRepo.findById(rs.getLong("branch_location_id")));
		inv.setItem(itemRepo.findById(rs.getLong("item_id")));
		inv.setExpiryDate(rs.getDate("expiry_date"));
		inv.setStockQuantity(rs.getBigDecimal("stock_qty"));
		
		return inv;
	}	
	
}


