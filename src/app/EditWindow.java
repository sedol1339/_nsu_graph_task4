package app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditWindow extends Stage {
	
	Canvas canvas;
	SceneParams params;
	
	public EditWindow(SceneParams params) {
		super();
		this.params = params;
		setTitle("Edit window");
		VBox b = new VBox();
		b.setSpacing(10);
		b.setPadding(new Insets(20, 20, 20, 20)); 
		canvas = new Canvas(600, 400);
		Button bt = new Button("OK");
		b.getChildren().addAll(canvas, bt);
		setScene(new Scene(b));
		show();
		centerOnScreen();
		
		AnimationTimer a = new AnimationTimer() {
			@Override
			public void handle(long now) {
				onPulse(now);
			}
		};
		a.start();
		
		bt.setOnAction(e -> {
			a.stop();
			close();
		});
		
		canvas.setOnMousePressed(this::onMousePressed);
		canvas.setOnMouseDragged(this::onMouseDragged);
	}
	
	int totalX = 600 - 1;
	int totalY = 400 - 1;
	int minX = -11;
	int maxX = 11;
	int minY = -11;
	int maxY = 11;
	int radius = 5;

	void onPulse(long now) {
		
		BufferedImage awtimg = new BufferedImage(totalX + 1, totalY + 1, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g = (Graphics2D) awtimg.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, totalX, totalY);
		
		//серые линии
		g.setColor(Color.GRAY);
		for (int i = 0; i <= (maxX - minX); i++) {
			//горизонтальная
			int x = totalX * i / (maxX - minX);
			g.drawLine(x, 0, x, totalY);
		}
		for (int i = 0; i <= (maxY - minY); i++) {
			//вертикальная
			int y = totalY * i / (maxX - minX);
			g.drawLine(0, y, totalX, y);
		}
		
		//главные линии
		g.setColor(Color.BLACK);
		//горизонтальная
		g.drawLine(0, 0, 0, totalY);
		int x_ = totalX * -minX / (maxX - minX);
		g.drawLine(x_, 0, x_, totalY);
		g.drawLine(totalX, 0, totalX, totalY);
		//вертикальная
		g.drawLine(0, 0, totalX, 0);
		int y_ = totalY * maxY / (maxY - minY);
		g.drawLine(0, y_, totalX, y_);
		g.drawLine(0, totalY, totalX, totalY);
		
		
		Point2D.Double[] spline = SceneSegments.bspline(
				params.solids[0].spline, params.n * params.k, params.a, params.b);
		
		Point2D.Double p_last = null;
		for(Point2D.Double p: spline) {
			if (p_last != null) {
				//рисуем линию
				int x1 = totalX * -minX / (maxX - minX) + (int) (p_last.x * totalX / (maxX - minX));
				int y1 = totalY * -minY / (maxY - minY) + (int) (p_last.y * totalY / (maxY - minY));
				y1 = totalY - y1;
				int x2 = totalX * -minX / (maxX - minX) + (int) (p.x * totalX / (maxX - minX));
				int y2 = totalY * -minY / (maxY - minY) + (int) (p.y * totalY / (maxY - minY));
				y2 = totalY - y2;
				g.drawLine(x1, y1, x2, y2);
			}
			p_last = p;
		}
		
		for (int i = 0; i < params.solids[0].spline.length; i++) {
			Point2D.Double p = params.solids[0].spline[i];
			int x = totalX * -minX / (maxX - minX) + (int) (p.x * totalX / (maxX - minX));
			int y = totalY * -minY / (maxY - minY) + (int) (p.y * totalY / (maxY - minY));
			y = totalY - y;
			g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
			g.drawString(i + "", x + radius, y - radius);
		}
		
		WritableImage img = SwingFXUtils.toFXImage(awtimg, null);
		canvas.setWidth(img.getWidth());
		canvas.setHeight(img.getHeight());
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.drawImage(img, 0, 0);
	}
	
	int drag_index = -1;
	
	void onMousePressed(MouseEvent event) {
		int x_event = (int) event.getX();
		int y_event = (int) event.getY();
		//System.out.println(x_event + " " + y_event);
		for (int i = 0; i < params.solids[0].spline.length; i++) {
			Point2D.Double p = params.solids[0].spline[i];
			int x = totalX * -minX / (maxX - minX) + (int) (p.x * totalX / (maxX - minX));
			int y = totalY * -minY / (maxY - minY) + (int) (p.y * totalY / (maxY - minY));
			y = totalY - y;
			//System.out.println(i + " " + x + " " + y);
			if (Math.abs(x - x_event) <= radius && Math.abs(y - y_event) <= radius) {
				drag_index = i;
				return;
			}
		}
		drag_index = -1;
	}
	
	void onMouseDragged(MouseEvent event) {
		if (drag_index == -1)
			return;
		int x = (int) event.getX();
		int y = (int) event.getY();
		y = totalY - y;
		double x_ = x - totalX * -minX / (maxX - minX);
		double y_ = y - totalY * -minY / (maxY - minY);
		double px = x_ / (totalX / (maxX - minX));
		double py = y_ / (totalY / (maxY - minY));
		Point2D.Double p = params.solids[0].spline[drag_index];
		p.x = px;
		p.y = py;
	}
}
