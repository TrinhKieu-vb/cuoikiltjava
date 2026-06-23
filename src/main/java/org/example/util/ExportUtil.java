package org.example.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ExportUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExportUtil.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Export products to JSON
     */
    public static boolean exportProductsToJSON(List<Product> products, String filepath) {
        try {
            String json = gson.toJson(products);
            Files.write(Paths.get(filepath), json.getBytes());
            logger.info("Products exported to JSON: " + filepath);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting products to JSON", e);
            return false;
        }
    }

    /**
     * Export customers to JSON
     */
    public static boolean exportCustomersToJSON(List<Customer> customers, String filepath) {
        try {
            String json = gson.toJson(customers);
            Files.write(Paths.get(filepath), json.getBytes());
            logger.info("Customers exported to JSON: " + filepath);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting customers to JSON", e);
            return false;
        }
    }

    /**
     * Export orders to JSON
     */
    public static boolean exportOrdersToJSON(List<Order> orders, String filepath) {
        try {
            String json = gson.toJson(orders);
            Files.write(Paths.get(filepath), json.getBytes());
            logger.info("Orders exported to JSON: " + filepath);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting orders to JSON", e);
            return false;
        }
    }

    /**
     * Export products to CSV
     */
    public static boolean exportProductsToCSV(List<Product> products, String filepath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            writer.println("ID,Name,Price,Category,Stock,Description");
            for (Product p : products) {
                writer.printf("%d,%s,%.2f,%s,%d,%s%n", 
                    p.getId(), 
                    p.getName(), 
                    p.getPrice(), 
                    p.getCategoryName(), 
                    p.getStock(),
                    p.getDescription()
                );
            }
            logger.info("Products exported to CSV: " + filepath);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting products to CSV", e);
            return false;
        }
    }

    /**
     * Export customers to CSV
     */
    public static boolean exportCustomersToCSV(List<Customer> customers, String filepath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            writer.println("ID,Name,Phone");
            for (Customer c : customers) {
                writer.printf("%d,%s,%s%n",
                    c.getId(),
                    c.getName(),
                    c.getPhone()
                );
            }
            logger.info("Customers exported to CSV: " + filepath);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting customers to CSV", e);
            return false;
        }
    }

    /**
     * Export orders to CSV
     */
    public static boolean exportOrdersToCSV(List<Order> orders, String filepath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            writer.println("ID,Customer,Amount,Status,OrderDate");
            for (Order o : orders) {
                writer.printf("%d,%s,%.2f,%s,%d%n",
                    o.getId(),
                    o.getCustomerName(),
                    o.getTotalAmount(),
                    o.getStatus(),
                    o.getOrderDate()
                );
            }
            logger.info("Orders exported to CSV: " + filepath);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting orders to CSV", e);
            return false;
        }
    }
}

