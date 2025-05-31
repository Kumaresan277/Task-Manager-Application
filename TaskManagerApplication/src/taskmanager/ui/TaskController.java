package taskmanager.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import taskmanager.dao.TaskDAO;
import taskmanager.model.Task;

public class TaskController {
	@FXML 
	private TableView<Task> taskTable;
	@FXML 
	private TableColumn<Task, Integer> colId;
	@FXML 
	private TableColumn<Task, String> colTitle;
	@FXML
	private TableColumn<Task, String> colDescription;
	@FXML
	private TableColumn<Task, String> colStatus;
	@FXML
	private TableColumn<Task, LocalDate> colDueDate;
	@FXML 
	private TableColumn<Task, String> colPriority;
	@FXML
	private TextField searchField;
	
	private TaskDAO taskDAO;
	private ObservableList<Task> taskList;
	
	private boolean darkMode = false;
	
	@FXML
	public void initialize() {
		
		taskDAO = new TaskDAO();
		
		colId.setCellValueFactory(new PropertyValueFactory<>("id"));
		colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
		colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
		colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
		
		loadTasks();
		checkUpcomingTasks();
		searchField.textProperty().addListener((
		obs, oldVal, newVal) ->
		filterTasks(newVal));
		
	}
	
	public void loadTasks() {
		List<Task> tasks = taskDAO.getAllTasks();
		taskList = FXCollections.observableArrayList(tasks);
		taskTable.setItems(taskList);
	}
	
	public void filterTasks(String keyword) {
		if (keyword == null || keyword.isEmpty()) {
			taskTable.setItems(taskList);
			return;
		}
		ObservableList<Task> filtered = FXCollections.observableArrayList();
		String lower = keyword.toLowerCase();
		for (Task t : taskList) {
			if(t.getTitle().toLowerCase().contains(lower) || t.getStatus().toLowerCase().contains(lower)) {
				filtered.add(t);
			}
		}
		taskTable.setItems(filtered);
	}
	
	@FXML
	public void handleAdd() {
		Task newTask = TaskFormController.showDialog(null);
		if (newTask != null) {
			taskDAO.addTask(newTask);
			loadTasks();
		}
		
	}
	
	@FXML 
	public void handleEdit() {
		Task selected = taskTable.getSelectionModel().getSelectedItem();
		if (selected != null) {
			Task updated = TaskFormController.showDialog(selected);
			if (updated != null) {
				taskDAO.updateTask(updated);
				loadTasks();
			}
			else {
				showAlert("No selection", "Please select a task to edit.");
			}
		}
	}
	
	
	@FXML
	public void handleDelete() {
		Task selected = taskTable.getSelectionModel().getSelectedItem();
		
		if (selected != null) {
			Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
			confirm.setTitle("Confirm Delete");
			
		confirm.setContentText("Delete task ' " + selected.getTitle() +" '?");
		
			if (confirm.showAndWait().get() == ButtonType.OK) {
				taskDAO.deleteTask(selected.getId());
				loadTasks();
			}
		}
		else {
			showAlert("No selection", "Please select a task to delete.");
		}
	}
		
	@FXML
	public void handleExport() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Tasks to CSV");
		fileChooser.setInitialFileName("tasks.csv");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
		File file = fileChooser.showSaveDialog(taskTable.getScene().getWindow());
		
		if (file != null) {
			try (PrintWriter writer = new PrintWriter(file)) {
				
				writer.println("ID,Title,Description,Status,DueDate,Priority");
				for (Task task : taskDAO.getAllTasks()) {
					writer.printf("%d,\"%s\",\"%s\",\"%s\",%s,%s%n", task.getId(), 
							task.getTitle().replace("\"", "\"\""),
							task.getDescription().replace("\"", "\"\""),
							task.getStatus(),
							task.getDueDate(),
							task.getPriority()
					);
				}
				showAlert("Success", "Tasks exported successfully.");
				
			}
			catch (IOException e) {
				e.printStackTrace();
				showAlert("Error", "Failed to export takss.");
			}
		}
		
	}
	
	@FXML
	private void handleImport() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Tasks from CSV");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
		File file = fileChooser.showOpenDialog(taskTable.getScene().getWindow());
		
		if (file != null) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = reader.readLine(); //skip header
				
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split(",", -1);
					
					if (parts.length >= 6) {
						Task task = new Task();
						
						task.setTitle(parts[1].replace("\"", ""));
						task.setDescription(parts[2].replace("\"", ""));
						task.setStatus(parts[3]);
						task.setDueDate(LocalDate.parse(parts[4]));
						task.setPriority(parts[5]);
					
						taskDAO.addTask(task);
					}
				}
				loadTasks();
				showAlert("Success", "Tasks imported successfully.");
			}
			catch (IOException e) {
				e.printStackTrace();
				showAlert("Erro", "Failed to import tasks.");
			}
		}
		
	}
	
	
	@FXML
	private void handleThemeToggle() {
		Scene scene = taskTable.getScene();
		scene.getStylesheets().clear();
		
		if (darkMode) {
			scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
		}
		else {
			scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
		}
		darkMode = !darkMode;
	}
	
	
	private void checkUpcomingTasks() {
		List<Task> tasks = taskDAO.getAllTasks();
		LocalDate today = LocalDate.now();
		
		StringBuilder builder = new StringBuilder();
		
		for (Task task : tasks) {
			if (task.getDueDate().equals(today)) {
				builder.append("-").append(task.getTitle()).append((" (Due Today)\n"));
			}
			else if(task.getDueDate().equals(today.plusDays(1))) {
				builder.append("-").append(task.getTitle()).append("(Due Tomorrow)\n");
			}
		}
			
		if (builder.length() > 0) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			
			alert.setTitle("Upcomming Tasks");
			alert.setHeaderText("Tasks due soon:");
			alert.setContentText(builder.toString());
			
			alert.showAndWait();
		}
			
		
	}
	
	
	
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.showAndWait();
		
	}
}
