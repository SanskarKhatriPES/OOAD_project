create table inventory_management_system;

use inventory_management_system;

create table unit (
	unit_code varchar(4) primary key, 
	unit_name varchar(20) NOT NULL,
	is_fractional boolean NOT NULL, 
	fractional_digits integer NOT NULL
);

create table address (
	addr_id integer primary key auto_increment,
	addr_line_1 varchar(50) NOT NULL, 
	addr_line_2 varchar(50), 
	city varchar(20) NOT NULL, 
	state varchar(20) NOT NULL, 
	country varchar(20) NOT NULL, 
	pincode varchar(20) NOT NULL
);

create table company (
	company_id integer primary key auto_increment,
	company_name varchar(20) NOT NULL, 
	headquarter_addr_id integer NOT NULL, 
	gstin varchar(15) NOT NULL,
	constraint foreign key fk_hq_aadr (headquarter_addr_id) references address(addr_id) 
);

create table branch_location (
	bl_id integer primary key auto_increment,
	bl_name varchar(20) not null, 
	company_id integer not null, 
	bl_addr_id integer not null,
	constraint foreign key fk_company (company_id) references company(company_id),
	constraint foreign key fk_bl_addr (bl_addr_id) references address(addr_id)
);

create table item (
	item_id integer primary key auto_increment,
	item_name varchar(20) not null, 
	batch_number varchar(10) not null, 
	unit_code varchar(4) not null, 
	purchase_price decimal not null, 
	selling_price decimal not null, 
	expiry_date date,
	constraint foreign key fk_unit (unit_code) references unit(unit_code)
);

create table purchase_invoice (
	pi_id integer primary key auto_increment,
	branch_location_id integer not null, 
	vendor_name varchar(25) not null, 
	bill_to_addr_id integer not null, 
	bill_amount decimal, 
	total_gst decimal, 
	invoice_date date not null,
	constraint foreign key (branch_location_id) references branch_location(bl_id),
	constraint foreign key (bill_to_addr_id) references address(addr_id)
);

create table sales_invoice (
	si_id integer primary key auto_increment,
	branch_location_id integer not null, 
	customer_name varchar(25) not null, 
	ship_to_addr_id integer not null, 
	bill_to_addr_id integer not null, 
	bill_amount decimal, 
	total_gst decimal, 
	invoice_date date not null,
	constraint foreign key (branch_location_id) references branch_location(bl_id),
	constraint foreign key (bill_to_addr_id) references address(addr_id),
	constraint foreign key (ship_to_addr_id) references address(addr_id)
);

create table pi_items (
	item_id integer not null, 
	pi_id integer not null, 
	quantity decimal, 
	total_price decimal,
	gst_amount decimal,
	constraint foreign key fk_item (item_id) references item(item_id),
	constraint foreign key fk_pi (pi_id) references purchase_invoice(pi_id)
);

create table si_items (
	item_id integer not null, 
	si_id integer not null, 
	quantity decimal, 
	total_price decimal,
	gst_amount decimal,
	constraint foreign key fk_item (item_id) references item(item_id),
	constraint foreign key fk_si (si_id) references sales_invoice(si_id)
);

create table inventory (
	item_id integer not null, 
	stock_qty decimal, 
	branch_location_id integer not null, 
	expiry_date date,
	constraint foreign key fk_item (item_id) references item(item_id),
	constraint foreign key fk_bl (branch_location_id) references branch_location(bl_id)
);