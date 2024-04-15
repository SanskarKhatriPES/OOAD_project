package com.ims.salesInvoice;

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
import com.ims.item.ItemRepository;

@Repository
public class SalesInvoiceRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(SalesInvoiceRepository.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Value("${db-queries.salesInvoice.select}")
	private String selectSIQuery;
	
	@Value("${db-queries.salesInvoice.insert}")
	private String insertSIQuery;
	
	@Value("${db-queries.salesInvoice.delete}")
	private String deleteSIQuery;
	
	@Value("${db-queries.salesInvoiceItem.select}")
	private String selectSIIQuery;
	
	@Value("${db-queries.salesInvoiceItem.insert}")
	private String insertSIIQuery;
	
	@Value("${db-queries.salesInvoiceItem.delete}")
	private String deleteSIIQuery;
	
	@Autowired
	private ItemRepository itemRepo;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private BranchLocationRepository blRepo;
	
	@Autowired
	private InventoryRepository invRepo;
	
	public List<SalesInvoice> findAll() {
		LOGGER.info("findAll");
		return jdbcTemplate.query(selectSIQuery, new SalesInvoiceRowMapper(addressRepo, blRepo, this));
	}
	
	public SalesInvoice findById(long id) {
		LOGGER.info("findById - {}", id);
		List<SalesInvoice> si = jdbcTemplate.query(selectSIQuery + " WHERE si_id = ?", new SalesInvoiceRowMapper(addressRepo, blRepo, this), id);
		return si.isEmpty() ? null : si.get(0);
	}
	
	public boolean existsById(long id) {
		LOGGER.info("existsById - {}", id);
		return findById(id) != null;
	}
	
	public List<SalesInvoiceItem> findSIItemsBySIId(long siId) {
		LOGGER.info("findSIItemsBySIId - {}", siId);
		return jdbcTemplate.query(selectSIIQuery + " WHERE si_id = ?", new SIItemRowMapper(itemRepo), siId);
	}
	
	public boolean deleteById(long id) {
		LOGGER.info("deleteById - {}", id);
		return jdbcTemplate.update(deleteSIQuery + " WHERE si_id = ?", id) > 0;
	}
	
	public long addNew(SalesInvoice si) {
		LOGGER.info("addNew");
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertSIQuery, 
				Types.NUMERIC,
				Types.VARCHAR,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.DATE);
		pscFactory.setReturnGeneratedKeys(true);
		pscFactory.setGeneratedKeysColumnNames("id");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				si.getBranchLocation().getId(),
				si.getCustomerName(),
				si.getShippingAddress().getAddrId(),
				si.getBillingAddress().getAddrId(),
				si.getBillAmount(),
				si.getTotalGst(),
				si.getInvoiceDate()));
		jdbcTemplate.update(psc, keyHolder);
		
		long id = keyHolder.getKey().longValue();
		
		for (SalesInvoiceItem item : si.getOrderItems()) {
			addItem(id, item);
			invRepo.removeStock(si.getBranchLocation().getId(), item.getItem().getId(), item.getQuantity());
		}
		
		return id;		
	}
	
	private void addItem(long siId, SalesInvoiceItem item) {
		LOGGER.info("addItem");
		PreparedStatementCreatorFactory pscFactory = new PreparedStatementCreatorFactory(insertSIIQuery, 
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC,
				Types.NUMERIC);
		
		PreparedStatementCreator psc = pscFactory.newPreparedStatementCreator(Arrays.asList(
				item.getItem().getId(),
				siId,
				item.getQuantity(),
				item.getTotalPrice(),
				item.getGstAmount()));
		
		jdbcTemplate.update(psc);		
	}
}

class SalesInvoiceRowMapper implements RowMapper<SalesInvoice> {
	
	AddressRepository addressRepo;
	BranchLocationRepository blRepo;
	SalesInvoiceRepository siRepo;
	
	public SalesInvoiceRowMapper(AddressRepository addressRepo, BranchLocationRepository blRepo,
			SalesInvoiceRepository siRepo) {
		super();
		this.addressRepo = addressRepo;
		this.blRepo = blRepo;
		this.siRepo = siRepo;
	}

	@Override
	public SalesInvoice mapRow(ResultSet rs, int rowNum) throws SQLException {
		SalesInvoice si = new SalesInvoice();
		
		si.setId(rs.getLong("si_id"));
		si.setBillingAddress(addressRepo.findById(rs.getLong("bill_to_addr_id")));
		si.setBranchLocation(blRepo.findById(rs.getLong("branch_location_id")));
		si.setCustomerName(rs.getString("customer_name"));
		si.setShippingAddress(addressRepo.findById(rs.getLong("ship_to_addr_id")));
		si.setBillAmount(rs.getBigDecimal("bill_amount"));
		si.setTotalGst(rs.getBigDecimal("total_gst"));
		si.setOrderItems(siRepo.findSIItemsBySIId(si.getId()));
		si.setInvoiceDate(rs.getDate("invoice_date"));
		
		return si;
	}
	
}

class SIItemRowMapper implements RowMapper<SalesInvoiceItem> {
	
	ItemRepository itemRepo;
	
	public SIItemRowMapper(ItemRepository itemRepo) {
		super();
		this.itemRepo = itemRepo;
	}

	@Override
	public SalesInvoiceItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		SalesInvoiceItem siItem = new SalesInvoiceItem();
		
		siItem.setItem(itemRepo.findById(rs.getLong("item_id")));
		siItem.setQuantity(rs.getBigDecimal("quantity"));
		siItem.setGstAmount(rs.getBigDecimal("gst_amount"));
		siItem.setTotalPrice(rs.getBigDecimal("total_price"));
		
		return siItem;
	}
	
}
