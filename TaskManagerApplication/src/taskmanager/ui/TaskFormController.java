package taskmanager.ui;

import java.io.IOException;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskmanager.model.Task;

public class TaskFormController {

	
	@FXML
	private TextField titleField;
	@FXML
	private TextArea descriptionField;
	@FXML
	private ComboBox<String> statusBox;
	@FXML
	private DatePicker dueDatePicker;
	@FXML
	private ComboBox<String> priorityBox;
	
	private Task task;
	private boolean okClicked = false;
	
	@FXML
	private void initialize() {
		statusBox.getItems().addAll("Pending", "In Progress", "Completed");
		priorityBox.getItems().addAll("Low", "Medium", "High");
	}
	
	public void setTask(Task task) {
		this.task = task;
		if (task != null) {
			titleField.setText(task.getTitle());
			descriptionField.setText(task.getDescription());
			statusBox.setValue(task.getStatus());
			dueDatePicker.setValue(task.getDueDate());
			priorityBox.setValue(task.getPriority());
		}
		else {
			statusBox.setValue("Pending");
			priorityBox.setValue("Medium");
			dueDatePicker.setValue(LocalDate.now());
		}
	}
	
	public boolean isOkClicked() {
		return okClicked;
	}
	
	public Task getTask() {
		return task;
	}
	
	@FXML
	private void handleOk() {
		if (isInputValid()) {
			if(task == null) {task = new Task();}
			task.setTitle(titleField.getText());
			task.setDescription(descriptionField.getText());
			task.setStatus(statusBox.getValue());
			task.setDueDate(dueDatePicker.getValue());
			task.setPriority(priorityBox.getValue());
			
			okClicked = true;
			
			((Stage)titleField.getScene().getWindow()).close();
		}
	}
	
	@FXML
	private void handleCancel() {
		((Stage)titleField.getScene().getWindow()).close();
	}
	
	private boolean isInputValid() {
		String errorMsg = "";
		if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
			errorMsg += "Title is required. \n";
		}
		
		if (dueDatePicker.getValue() == null) {
			errorMsg += "Due date is required. \n";
		}
		
		if (statusBox.getValue() == null) {
			errorMsg += "Status is required. \n";
		}
		
		if (priorityBox.getValue() == null) {
			errorMsg += "Priority is required. \n";
		}
		
		if (errorMsg.isEmpty()) return true;
		
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Invalid Input");
		alert.setHeaderText("Please fix invalid fields");
		alert.setContentText(errorMsg);
		alert.showAndWait();
		
		return false;
	}
	 
	public static Task showDialog(Task task) {
		try {
			FXMLLoader loader = new FXMLLoader(TaskFormController.class.getResource("/ui/task_form.fxml"));
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle(task == null ? "Add Task" : "Edit Task");
			stage.setScene(new Scene(loader.load()));
			
			TaskFormController controller = loader.getController();
			controller.setTask(task);
			stage.showAndWait();
			
			if (controller.isOkClicked()) {
				return controller.getTask();
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null; 
	}

}
