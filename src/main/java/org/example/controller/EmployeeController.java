package org.example.controller;

import org.example.model.Employee;
import org.example.dao.EmployeeDAO;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TextField nameField;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        
        loadData();
    }

    private void loadData() {
        employeeList.setAll(employeeDAO.getAllEmployees());
        employeeTable.setItems(employeeList);
    }

    @FXML
    public void handleAdd() {
        String name = nameField.getText();
        System.out.println("Đang thêm nhân viên: " + name);
        
        Employee newEmployee = new Employee(0, name);
        employeeDAO.addEmployee(newEmployee);
        System.out.println("Đã gọi addEmployee");
        
        nameField.clear();
        loadData();
        System.out.println("Đã gọi loadData");
    }
}
