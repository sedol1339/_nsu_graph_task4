package app;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

public class Controller implements Initializable {
	
	@FXML BorderPane borderPane;
	@FXML MenuBar menuBar;
	@FXML ScrollPane scrollPane;
	@FXML Canvas canvas;
	Model model;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		model = new Model();
		canvas.setWidth(Model.WIDTH);
		canvas.setHeight(Model.HEIGHT);
		scrollPane.setPrefSize(Model.WIDTH, Model.HEIGHT);
		
		Menu menuFile = new Menu("File");
		MenuItem menuFileNew = new MenuItem("New");
		menuFileNew.setOnAction(this::onNewFile);
		MenuItem menuFileOpenFile = new MenuItem("Open File...");
		menuFileOpenFile.setOnAction(this::onOpenFile);
		MenuItem menuFileExit = new MenuItem("Exit");
		menuFileExit.setOnAction(this::onExit);
		menuFile.getItems().addAll(menuFileNew, menuFileOpenFile,
				new SeparatorMenuItem(), menuFileExit);
		
		Menu menuEdit = new Menu("Edit");
		MenuItem menuEditInit = new MenuItem("Initialize");
		menuEditInit.setOnAction(this::onInit);
		MenuItem menuEditSettings = new MenuItem("Settings");
		menuEditSettings.setOnAction(this::onSettings);
		menuEdit.getItems().addAll(menuEditInit, menuEditSettings);
		
		Menu menuHelp = new Menu("Help");
		MenuItem menuHelpAbout = new MenuItem("About program");
		menuHelpAbout.setOnAction(this::onAbout);
		menuHelp.getItems().addAll(menuHelpAbout);
		
		menuBar.getMenus().clear();
		menuBar.getMenus().addAll(menuFile, menuEdit, menuHelp);

		/* starting game rendering thread */
		Thread thread = new Thread(model);
		thread.setDaemon(true);
		thread.start();

		/* drawing */
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				onPulse(now);
			}
		}.start();
		
		Thread t2 = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) { }
				/*if (model.steps > 1) {
					model.steps--;
					model.sceneSegments = 
						new SceneSegments(model.sceneParams, model.steps);
				}*/
				model.cameraRotateZ(0.01f);
				model.sceneRotateZ(0.01f);
				model.cameraMoveForward(0.01f);
			}
		});
		t2.setDaemon(true);
		//t2.start();
		
		canvas.setOnMouseDragged(this::onMouseDragged);
		canvas.setOnMousePressed(this::onMousePressed);
		//canvas.setOnMouseMoved(this::onMouseMoved);
		borderPane.setOnKeyPressed(this::onKeyPressed);
		borderPane.setOnKeyReleased(this::onKeyReleased);
	}

	FileChooser fc = new FileChooser();

	{
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
		fc.getExtensionFilters().addAll(extFilter);
	}
	
	void onNewFile(ActionEvent event) {
		model.newScene();
	}
	
	void onOpenFile(ActionEvent event) {
		File selectedFile = fc.showOpenDialog(null);
		model.loadSceneFromFile(selectedFile);
	}
	
	void onExit(ActionEvent event) {
		Platform.exit();
	}
	
	void onInit(ActionEvent event) {
		model.resetAngles();
	}
	
	void onSettings(ActionEvent event) {
		//App.editStage.showAndWait();
		new EditWindow(model.sceneParams);
	}
	
	void onAbout(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("");
		alert.setHeaderText("О программе");
		alert.setContentText("Автор: Седухин Олег\n" + "github.com/sedol1339");
		alert.showAndWait();
	}
	
	void onPulse(long now) {
		float scale = 1;
		if (w_holdDown) {
			model.cameraMoveForward(scale * 0.01f);
		}
		if (a_holdDown) {
			model.cameraMoveLeft(scale * 0.01f);
		}
		if (s_holdDown) {
			model.cameraMoveBackward(scale * 0.01f);
		}
		if (d_holdDown) {
			model.cameraMoveRight(scale * 0.01f);
		}
		if (z_holdDown) {
			model.cameraMoveDown(scale * 0.01f);
		}
		if (x_holdDown) {
			model.cameraMoveUp(scale * 0.01f);
		}
		Model.ImageState imageState = model.getImageState();
		switch (imageState) {
		case DONE:
			WritableImage img = model.getImage();
			if (img == null)
				break;
			model.sheduleImagePainting();
			canvas.setWidth(img.getWidth());
			canvas.setHeight(img.getHeight());
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.drawImage(img, 0, 0);
			break;
		case NONE:
			model.sheduleImagePainting();
			break;
		case SHEDULED:
		}
	}
	
	double x1 = -9999, y1 = 0;
	void onMouseDragged(MouseEvent event) {
		if (event.isPrimaryButtonDown()) {
			onMouseDragged2(event);
			return;
		}
		double x2 = event.getX();
		double y2 = event.getY();
		if (x1 != -9999) {
			model.sceneRotateY(-0.01f * (float) (x2 - x1));
			model.sceneRotateX(-0.01f * (float) (y2 - y1));
		}
		x1 = x2;
		y1 = y2;
	}

	double x1_ = -9999, y1_ = 0;
	void onMouseDragged2(MouseEvent event) {
		double x2_ = event.getX();
		double y2_ = event.getY();
		if (x1_ != -9999) {
			model.cameraRotateY(0.01f * (float) (x2_ - x1_));
			model.cameraRotateX(0.01f * (float) (y2_ - y1_));
		}
		x1_ = x2_;
		y1_ = y2_;
	}
	
	void onMousePressed(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			x1_ = event.getX();
			y1_ = event.getY();
		} else if (event.getButton() == MouseButton.SECONDARY) {
			x1 = event.getX();
			y1 = event.getY();
		}
	}
	
	boolean w_holdDown = false;
	boolean a_holdDown = false;
	boolean s_holdDown = false;
	boolean d_holdDown = false;
	boolean z_holdDown = false;
	boolean x_holdDown = false;
	
	void onKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.W) {
			w_holdDown = true;
		}
		if (event.getCode() == KeyCode.A) {
			a_holdDown = true;
		}
		if (event.getCode() == KeyCode.S) {
			s_holdDown = true;
		}
		if (event.getCode() == KeyCode.D) {
			d_holdDown = true;
		}
		if (event.getCode() == KeyCode.Z) {
			z_holdDown = true;
		}
		if (event.getCode() == KeyCode.X) {
			x_holdDown = true;
		}
	}
	
	void onKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.W) {
			w_holdDown = false;
		}
		if (event.getCode() == KeyCode.A) {
			a_holdDown = false;
		}
		if (event.getCode() == KeyCode.S) {
			s_holdDown = false;
		}
		if (event.getCode() == KeyCode.D) {
			d_holdDown = false;
		}
		if (event.getCode() == KeyCode.Z) {
			z_holdDown = false;
		}
		if (event.getCode() == KeyCode.X) {
			x_holdDown = false;
		}
	}
}
