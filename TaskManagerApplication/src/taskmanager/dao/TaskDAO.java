package taskmanager.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import application.DBConnection;
import taskmanager.model.Task;

public class TaskDAO {

	public List<Task> getAllTasks() {
		List<Task> list = new ArrayList<>();
		
		try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM tasks")) {
			
			while(rs.next()) {
				Task t = new Task(rs.getInt("id"),
						rs.getString("title"),
						rs.getString("description"), 
						rs.getString("status"),
						rs.getDate("due_date").toLocalDate(),
						rs.getString("priority")
						);
				list.add(t);
			}
			
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	
	public void addTask(Task task) {
		String sql = "INSERT INTO tasks(title, description, status, due_date, priority) VALUES(?,?,?,?,?)";
		
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, task.getTitle());
			ps.setString(2, task.getDescription());
			ps.setString(3, task.getStatus());
			ps.setDate(4,  Date.valueOf(task.getDueDate()));
			ps.setString(5, task.getPriority());
			
			ps.executeUpdate();
			
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateTask(Task task) {
		String sql = "UPDATE tasks SET title=?, description=?, status=?, due_date=?, priority=? WHERE id=?";
		
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, task.getTitle());
			ps.setString(2,  task.getDescription());
			ps.setString(3,  task.getStatus());
			ps.setDate(4, Date.valueOf(task.getDueDate()));
			ps.setString(5, task.getPriority());
			ps.setInt(6, task.getId());
			
			ps.executeUpdate();
			
		}
		
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public void deleteTask(int id) {
		String sql = "DELETE FROM tasks where id=?";
		
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1,  id);
			ps.executeUpdate();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
}


