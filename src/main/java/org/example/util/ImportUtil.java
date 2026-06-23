package org.example.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ImportUtil {
    private static final Logger logger = LoggerFactory.getLogger(ImportUtil.class);
    private static final Gson gson = new Gson();

    /**
     * Import products from JSON
     */
    public static List<Product> importProductsFromJSON(String filepath) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filepath)));
            List<Product> products = gson.fromJson(json, new TypeToken<List<Product>>(){}.getType());
            logger.info("Products imported from JSON: " + filepath);
            return products;
        } catch (IOException e) {
            logger.error("Error importing products from JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Import customers from JSON
     */
    public static List<Customer> importCustomersFromJSON(String filepath) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filepath)));
            List<Customer> customers = gson.fromJson(json, new TypeToken<List<Customer>>(){}.getType());
            logger.info("Customers imported from JSON: " + filepath);
            return customers;
        } catch (IOException e) {
            logger.error("Error importing customers from JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Import orders from JSON
     */
    public static List<Order> importOrdersFromJSON(String filepath) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filepath)));
            List<Order> orders = gson.fromJson(json, new TypeToken<List<Order>>(){}.getType());
            logger.info("Orders imported from JSON: " + filepath);
            return orders;
        } catch (IOException e) {
            logger.error("Error importing orders from JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Import products from CSV
     */
    public static List<Product> importProductsFromCSV(String filepath) {
        List<Product> products = new ArrayList<>();
        try (FileReader reader = new FileReader(filepath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                Product p = new Product();
                p.setId(Integer.parseInt(record.get("ID")));
                p.setName(record.get("Name"));
                p.setPrice(Double.parseDouble(record.get("Price")));
                p.setCategoryName(record.get("Category"));
                p.setStock(Integer.parseInt(record.get("Stock")));
                p.setDescription(record.get("Description"));
                products.add(p);
            }
            logger.info("Products imported from CSV: " + filepath);
        } catch (IOException e) {
            logger.error("Error importing products from CSV", e);
        }
        return products;
    }

    /**
     * Import customers from CSV
     */
    public static List<Customer> importCustomersFromCSV(String filepath) {
        List<Customer> customers = new ArrayList<>();
        try (FileReader reader = new FileReader(filepath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                Customer c = new Customer();
                c.setId(Integer.parseInt(record.get("ID")));
                c.setName(record.get("Name"));
                c.setPhone(record.get("Phone"));
                customers.add(c);
            }
            logger.info("Customers imported from CSV: " + filepath);
        } catch (IOException e) {
            logger.error("Error importing customers from CSV", e);
        }
        return customers;
    }

    /**
     * Import orders from CSV
     */
    public static List<Order> importOrdersFromCSV(String filepath) {
        List<Order> orders = new ArrayList<>();
        try (FileReader reader = new FileReader(filepath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                Order o = new Order();
                o.setId(Integer.parseInt(record.get("ID")));
                o.setCustomerName(record.get("Customer"));
                o.setTotalAmount(Double.parseDouble(record.get("Amount")));
                o.setStatus(record.get("Status"));
                o.setOrderDate(Long.parseLong(record.get("OrderDate")));
                orders.add(o);
            }
            logger.info("Orders imported from CSV: " + filepath);
        } catch (IOException e) {
            logger.error("Error importing orders from CSV", e);
        }
        return orders;
    }
}

