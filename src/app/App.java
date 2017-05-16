package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.*;

@SuppressWarnings("unused")
public class App extends Application {
	
	static Stage mainStage;
	
	/**
	 * -Dglass.win.minHiDPI=2
	 */
	public static void main(String[] args) {
		try {
			launch(args);
		} catch (Exception e) {
			onException(Thread.currentThread(), e);
		}
	}
	
	public static void onException(Thread t, Throwable e) {
		StringWriter tmp = new StringWriter();
		e.printStackTrace(new PrintWriter(tmp));
		while(true) {
			Throwable e_ = e.getCause();
			if (e_ == null || e_ == e) break;
			e = e_;
			tmp.write("\nCaused by: ");
			e.printStackTrace(new PrintWriter(tmp));
	    }
		String exceptionInfo = tmp.toString();
		System.out.println(exceptionInfo);
		JOptionPane.showMessageDialog(null, exceptionInfo, " Error", JOptionPane.ERROR_MESSAGE);
    }
	
	public static void onUserActionError(Thread t, String error) {
		if (error == null || error.length() <= 0) {
			error = "Unknown error";
		}
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("");
		alert.setHeaderText("Error");
		alert.setContentText(error);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(App.mainStage);
		alert.showAndWait();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(App::onException);
		primaryStage.setTitle("Wireframe");
		FXMLLoader loader = new FXMLLoader(App.class.getResource("/app/App.fxml"));
        Parent mainWindowParent = loader.load();
        Controller controller = loader.getController();
        primaryStage.setScene(new Scene(mainWindowParent));
		mainStage = primaryStage;
        primaryStage.show();
        primaryStage.centerOnScreen();
	}
}
