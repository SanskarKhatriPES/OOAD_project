package com.ims.purchaseInvoice;

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
import com.ims.branchLocation.BranchLocationRepository;
import com.ims.inventory.InventoryRepository;
import com.ims.item.Item;
import com.ims.item.ItemRepository;

@Repository
public class PurchaseInvoiceRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(PurchaseInvoiceRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${db-queries.purchaseInvoice.select}")
	private String selectPIQuery;
	
	@Value("${db-queries.purchaseInvoice.insert}")
	private String insertPIQuery;
	
	@Value("${db-queries.purchaseInvoice.delete}")
	private String deletePIQuery;
	
	@Value("${db-queries.purchaseInvoiceItem.select}")
	private String selectPIIQuery;
	
	@Value("${db-queries.purchaseInvoiceItem.insert}")
	private String insertPIIQuery;
	
	@Value("${db-queries.purchaseInvoiceItem.delete}")
	private String deletePIIQuery;
	
	@Autowired
	private ItemRepository itemRepo;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private BranchLocationRepository blRepo;
	
	@Autowired
	private InventoryRepository invRepo;
	
	public List<PurchaseInvoice> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectPIQuery, new PurchaseInvoiceRowMapper(addressRepo, blRepo, this));
	}
	
	public PurchaseInvoice findById(long id) {
		LOGGER.info("findById - {}", id);
		List<PurchaseInvoice> pi = jdbcTemplate.query(selectPIQuery + " WHERE pi_id = ?", new PurchaseInvoiceRowMapper(addressRepo, blRepo, this), id);
		return pi.isEmpty() ? null : pi.get(0);
	}
	
	public boolean existsById(long id) {
		LOGGER.info("existsById - {}", id);
		return findById(id) != null;
	}
	
	public List<PurchaseInvoiceItem> findPIItemsByPIId(long piId) {
		LOGGER.info("findPIItemsByPIId - {}", piId);
		return jdbcTemplate.query(selectPIIQuery + " WHERE pi_id = ?", new PIItemRowMapper(itemRepo), piId);
	}
	
	public boolean deleteById(long id) {
		LOGGER.info("deleteById - {}", id);
		return jdbcTemplate.update(deletePIQuery + " WHERE pi_id = ?", id) > 0;
	}
	
	public long addNew(PurchaseInvoice pi) {
		LOGGER.info("addNew");
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertPIQuery, 
				Types.NUMERIC,
				Types.VARCHAR,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.DATE);
		pscFactory.setReturnGeneratedKeys(true);
		pscFactory.setGeneratedKeysColumnNames("id");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				pi.getBranchLocation().getId(),
				pi.getVendorName(),
				pi.getBillingAddress().getAddrId(),
				pi.getBillAmount(),
				pi.getTotalGst(),
				pi.getInvoiceDate()));
		jdbcTemplate.update(psc, keyHolder);
		
		long id = keyHolder.getKey().longValue();
		
		for (PurchaseInvoiceItem item : pi.getOrderItems()) {
			addItem(id, item);
			if (invRepo.existsByBranchLocationIdAndItemId(pi.getBranchLocation().getId(), item.getItem().getId())) {
				invRepo.addStock(pi.getBranchLocation().getId(), item.getItem().getId(), item.getQuantity());
			} else {
				invRepo.addNew(pi.getBranchLocation().getId(), item.getItem().getId(), item.getQuantity(), itemRepo.findById(item.getItem().getId()).getExpiryDate());
			}
		}
		
		return id;		
	}
	
	private void addItem(long piId, PurchaseInvoiceItem item) {
		LOGGER.info("addItem");
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertPIIQuery, 
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC);
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				item.getItem().getId(),
				piId,
				item.getQuantity(),
				item.getTotalPrice(),
				item.getGstAmount()));
		
		jdbcTemplate.update(psc);		
	}
}

class PurchaseInvoiceRowMapper implements RowMapper<PurchaseInvoice> {
	
	AddressRepository addressRepo;
	BranchLocationRepository blRepo;
	PurchaseInvoiceRepository piRepo;
	
	public PurchaseInvoiceRowMapper(AddressRepository addressRepo, BranchLocationRepository blRepo,
			PurchaseInvoiceRepository piRepo) {
		super();
		this.addressRepo = addressRepo;
		this.blRepo = blRepo;
		this.piRepo = piRepo;
	}

	@Override
	public PurchaseInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {
		PurchaseInvoice pi = new PurchaseInvoice();
		
		pi.setId(rs.getLong("pi_id"));
		pi.setBillingAddress(addressRepo.findById(rs.getLong("bill_to_addr_id")));
		pi.setBranchLocation(blRepo.findById(rs.getLong("branch_location_id")));
		pi.setVendorName(rs.getString("vendor_name"));
		pi.setBillAmount(rs.getBigDecimal("bill_amount"));
		pi.setTotalGst(rs.getBigDecimal("total_gst"));
		pi.setOrderItems(piRepo.findPIItemsByPIId(pi.getId()));
		pi.setInvoiceDate(rs.getDate("invoice_date"));
		
		return pi;
	}
	
}

class PIItemRowMapper implements RowMapper<PurchaseInvoiceItem> {
	
	ItemRepository itemRepo;
	
	public PIItemRowMapper(ItemRepository itemRepo) {
		super();
		this.itemRepo = itemRepo;
	}

	@Override
	public PurchaseInvoiceItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		PurchaseInvoiceItem piItem = new PurchaseInvoiceItem();
		
		piItem.setItem(itemRepo.findById(rs.getLong("item_id")));
		piItem.setQuantity(rs.getBigDecimal("quantity"));
		piItem.setGstAmount(rs.getBigDecimal("gst_amount"));
		piItem.setTotalPrice(rs.getBigDecimal("total_price"));
		
		return piItem;
	}
	
}
