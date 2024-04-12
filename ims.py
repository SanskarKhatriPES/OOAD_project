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
elif page == "Add Unit":
    form_unit()
elif page == "Add Address":
    form_address()
elif page == "Add Company":
    form_company()
