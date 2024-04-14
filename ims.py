import streamlit as st
import requests
import pandas as pd
import json

st.set_page_config(layout="wide")
# Helper Fetch Functions

#URL = "http://43.204.189.209:8080"
URL = "http://localhost:8080"

def get_error_lists(e_map_str):
    e_map = json.loads(e_map_str)
    e_list = []
    for e in e_map['globalErrors']:
        e_list.append(e)
    for f in list(e_map['fieldErrors'].keys()):
        for e in e_map['fieldErrors'][f]:
            e_list.append(e)
    for e in e_list:
        st.error(e)

def fetch_company_ids():
    url = URL + "/company"
    response = requests.get(url)
    if response.status_code == 200:
        companies = response.json()["companies"]
        return {company["id"]: company["name"] for company in companies}
    else:
        st.error(f"Failed to fetch company data. Status code: {response.status_code}")
        return []

def fetch_address_ids():
    url = URL + "/address"
    response = requests.get(url)
    if response.status_code == 200:
        addresses = response.json()["addresses"]
        return {address["addrId"]: address["addressLine1"] + ", " + str(address["addressLine2"]) + ", " + address["city"] + ", " + address["state"] + ", " + address["country"] + " - " + address["pincode"] for address in addresses}
    else:
        st.error(f"Failed to fetch address data. Status code: {response.status_code}")
        return []
    
def fetch_unit_codes():
    url = URL + "/unit"
    response = requests.get(url)
    if response.status_code == 200:
        units = response.json()["units"]
        return {unit["unitCode"]: unit["name"] for unit in units}
    else:
        st.error(f"Failed to fetch unit data. Status code: {response.status_code}")
        return []

def fetch_item_ids():
    url = URL + "/item"
    response = requests.get(url)
    if response.status_code == 200:
        items = response.json()["items"]
        return {item["id"]: item["name"] + ", " + item["batchNumber"] for item in items}
    else:
        st.error(f"Failed to fetch items. Status code: {response.status_code}")
        return []

def fetch_branch_location_ids():
    url = URL + "/branchLocation"
    response = requests.get(url)
    if response.status_code == 200:
        locations = response.json()["branchLocations"]
        return {location["id"]: location["name"] + ", " + location["company"]["name"] for location in locations}
    else:
        st.error(f"Failed to fetch branch locations. Status code: {response.status_code}")
        return []

# View Functions
def view_units():
    st.title("Units")
    url = URL + "/unit"
    response = requests.get(url)
    data = response.json()
    if response.status_code == 200:
        st.table(data["units"])
    else:
        st.error("Error retrieving data.")



def view_address():
    st.title("Address")
    url = URL + "/address"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        st.table(data["addresses"])

def view_company():
    st.title("Company")
    url = URL + "/company"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        table_data = []
        for company in data["companies"]:
            table_data.append({
                "Name": company["name"],
                "Address Line 1": company["headquarterAddress"]["addressLine1"],
                "Address Line 2": company["headquarterAddress"]["addressLine2"],
                "City": company["headquarterAddress"]["city"],
                "State": company["headquarterAddress"]["state"],
                "Country": company["headquarterAddress"]["country"],
                "Pincode": company["headquarterAddress"]["pincode"],
                "GSTIN": company["gstin"]
            })
        df = pd.DataFrame(table_data)
        st.table(df)
    else:
        st.error("Error retrieving data.")

def view_items():
    st.title("Items")
    url = URL + "/item"
    response = requests.get(url)

    if response.status_code == 200:
        data = response.json()

        table_data = []
        for item in data["items"]:
            table_data.append({
                "Name": item["name"],
                "Batch Number": item["batchNumber"],
                "Unit Code": item["unit"]["unitCode"],
                "Selling Price": item["sellingPrice"],
                "Purchase Price": item["purchasePrice"],
                "Expiry Date": item["expiryDate"]
            })
        df = pd.DataFrame(table_data)
        st.table(df)
    else:
        st.error("Error retrieving data.")

def view_branchLocation():
    st.title("Branch Locations")
    url = URL + "/branchLocation"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()

        # Extract relevant data for the table
        table_data = []
        for branch_location in data["branchLocations"]:
            table_data.append({
                "Name": branch_location["name"],
                "Company Name": branch_location["company"]["name"],
                "Company GSTIN": branch_location["company"]["gstin"],
                "Branch Address Line 1": branch_location["address"]["addressLine1"],
                "Branch Address Line 2": branch_location["address"]["addressLine2"],
                "Branch City": branch_location["address"]["city"],
                "Branch State": branch_location["address"]["state"],
                "Branch Country": branch_location["address"]["country"],
                "Branch Pincode": branch_location["address"]["pincode"]
            })
        df = pd.DataFrame(table_data)
        st.table(df)
    else:
        st.error("Error retrieving data.")

def view_si():
    st.title("Sales Invoice")
    url = URL + "/si"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        table_data = []
        for invoice in data["salesInvoices"]:
            for order_item in invoice["orderItems"]:
                table_data.append({
                    "Invoice Date": invoice["invoiceDate"],
                    "Branch Location Name": invoice["branchLocation"]["name"],
                    "Company Name": invoice["branchLocation"]["company"]["name"],
                    "Customer Name": invoice["customerName"],
                    "Item Name": order_item["item"]["name"],
                    "Quantity": order_item["quantity"],
                    "Total Price": order_item["totalPrice"],
                    "GST Amount": order_item["gstAmount"]
                })
        df = pd.DataFrame(table_data)
        st.table(df)
    else:
        st.error("Error retrieving data.")


# Form Functions
def form_unit():
    st.title("Unit Form")
    
    unit_code = st.text_input("Unit code")
    unit_name = st.text_input("Unit name")
    fractional = st.checkbox("Fractional")
    fractional_digits = st.number_input("Fractional digits", value = 0)
    
    submit_button = st.button("Submit")

    if submit_button:
        data = {
            "unitCode": unit_code,
            "name": unit_name,
            "fractionalDigits": fractional_digits,
            "fractional": fractional
            }
        st.table(data)
        response = requests.post(URL + '/unit', json=data)
        if response.status_code == 200:
            st.success('Data submitted successfully!')
        else:
            get_error_lists(response.text)


def form_address():
    st.title("Address Form")

    address_line_1 = st.text_input("Address Line 1", "")
    address_line_2 = st.text_input("Address Line 2", "")
    city = st.text_input("City", "")
    state = st.text_input("State", "")
    country = st.text_input("Country", "")
    pincode = st.text_input("Pincode", "")

    if st.button("Submit"):
        data = {
            "addressLine1": address_line_1,
            "addressLine2": address_line_2,
            "city": city,
            "state": state,
            "country": country,
            "pincode": pincode
        }
        st.table(data)
        response = requests.post(URL + '/address', json=data)
        if response.status_code == 200:
            st.success('Data submitted successfully!')
        else:
            get_error_lists(response.text)


def form_company():
    st.title("Company Form")

    addresses = fetch_address_ids()
    address_ids = list(addresses.keys())

    name = st.text_input("Company Name", "")
    headquarter_address_id = st.selectbox("Headquarter Address ID", address_ids, format_func=lambda x: addresses[x])
    gstin = st.text_input("GSTIN", "")

    if st.button("Submit"):
        company_data = {
            "name": name,
            "headquarterAddress": {
                "addrId": headquarter_address_id
            },
            "gstin": gstin
        }
        st.table(company_data)

        response = requests.post(URL + "/company", json=company_data)
        if response.status_code == 200:
            st.success("Data submitted successfully!")
        else:
            get_error_lists(response.text)

def form_item():
    st.title("Item Form")

    units = fetch_unit_codes()
    unit_codes = list(units.keys())

    name = st.text_input("Item Name", "")
    batch_number = st.text_input("Batch Number", "")
    unit_code = st.selectbox("Unit", unit_codes, format_func=lambda x: units[x])
    selling_price = st.number_input("Selling Price", min_value=0.0)
    purchase_price = st.number_input("Purchase Price", min_value=0.0)
    # opening_balance_date = st.date_input("Opening Balance Date", value=None)
    # opening_balance_qty = st.number_input("Opening Balance Quantity", min_value=0)
    expiry_date = st.date_input("Expiry Date", value=None)

    if st.button("Submit"):
        item_data = {
            "name": name,
            "batchNumber": batch_number,
            "unit": {
                "unitCode": unit_code
            },
            "sellingPrice": selling_price,
            "purchasePrice": purchase_price,
            # "openingBalanceDate": str(opening_balance_date),
            # "openingBalanceQty": opening_balance_qty,
            "expiryDate": str(expiry_date) if expiry_date else ""
        }

        st.table(item_data)

        response = requests.post(URL + "/item", json=item_data)
        if response.status_code == 200:
            st.success("Data submitted successfully!")
        else:
            get_error_lists(response.text)

def form_branchLocation():
    st.title("Branch Location Form")

    companies = fetch_company_ids()
    company_ids = list(companies.keys())

    addresses = fetch_address_ids()
    address_ids = list(addresses.keys())

    name = st.text_input("Branch Location Name", "")
    company_id = st.selectbox("Company ID", company_ids, format_func=lambda x: companies[x])
    address_id = st.selectbox("Address ID", address_ids, format_func=lambda x: addresses[x])

    if st.button("Submit"):
        branch_location_data = {
            "name": name,
            "company": {
                "id": company_id
            },
            "address": {
                "addrId": address_id
            }
        }
        st.table(branch_location_data)

        response = requests.post(URL + "/branchLocation", json=branch_location_data)
        if response.status_code == 200:
            st.success("Data submitted successfully!")
        else:
            get_error_lists(response.text)

def form_si():
    st.title("Sales Invoice Form")

    branch_locations = fetch_branch_location_ids()
    bl_ids = list(branch_locations.keys())

    addresses = fetch_address_ids()
    address_ids = list(addresses.keys())

    items = fetch_item_ids()
    item_ids = list(items.keys())

    invoice_date = st.date_input("Invoice Date", key="invoice_date")
    customer_name = st.text_input("Customer Name", key="vendor_name")
    branch_location_id = st.selectbox("Branch Location ID", bl_ids, format_func=lambda x: branch_locations[x], key="branch_location_id")
    billing_address_id = st.selectbox("Billing Address ID", address_ids, format_func=lambda x: addresses[x], key="billing_address_id")
    shipping_address_id = st.selectbox("Shipping Address ID", address_ids, format_func=lambda x: addresses[x], key="shipping_address_id")
    branch_location_to_send = {"id": branch_location_id}
    billing_address_to_send = {"addrId": billing_address_id}
    shipping_address_to_send = {"addrId": shipping_address_id}

    item_id = st.selectbox("Item ID", item_ids, format_func=lambda x: items[x], key="item_id")
    quantity = st.number_input("Quantity", min_value=1, step=1, value=1, key="quantity")
    order_items = [{"item": {"id": item_id}, "quantity": quantity}]

    if st.button("Submit"):
        invoice_data = {
            "invoiceDate": str(invoice_date),
            "branchLocation": branch_location_to_send,
            "billingAddress": billing_address_to_send,
            "shippingAddress": shipping_address_to_send,
            "customerName": customer_name,
            "orderItems": order_items
        }

        # st.subheader("JSON Data")
        # st.json(invoice_data)
        response = requests.post(URL + "/si", json=invoice_data)
        if response.status_code == 200:
            st.success("Data submitted successfully!")
        else:
            get_error_lists(response.text)


# Sidebar menu
menu = [
    "View Units",
    "View Address",
    "View Company",
    "Add Unit",
    "Add Address",
    "Add Company"
]
page = st.sidebar.selectbox("Menu", menu)


# Render page
if page == "View Units":
    view_units()
elif page == "View Address":
    view_address()
elif page == "View Company":
    view_company()
elif page == "View Items":
    view_items()
elif page == "View Branch Location":
    view_branchLocation()
elif page == "View Sales Invoice":
    view_si()
elif page == "Add Unit":
    form_unit()
elif page == "Add Address":
    form_address()
elif page == "Add Company":
    form_company()
elif page == "Add Item":
    form_item()
elif page == "Add Branch Locations":
    form_branchLocation()
elif page == "Add Sales Invoice":
    form_si()
