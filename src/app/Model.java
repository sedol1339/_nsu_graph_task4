package app;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class Model extends Task<Integer> {
	
	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;
	SceneParams sceneParams;
	SceneSegments sceneSegments;
	SceneCamera sceneCamera;
	Object lock = new Object();
	
	public Model() {
		//newScene();
		loadSceneFromFile(new File("C:\\Users\\Oleg\\Desktop\\input.txt"));
	}
	
	@Override
	protected Integer call() throws Exception {
		try {
			while(true) {
				if (imageState == ImageState.SHEDULED) {
					synchronized (lock) {
						drawImage();
					}
					imageState = ImageState.DONE;
					Thread.sleep(33);
				}
			}
		} catch (RuntimeException e) {
			App.onException(Thread.currentThread(), e);
		}
		return 0;
	}
	
	//graphics sheduling-related variables
	
	public enum ImageState { NONE, SHEDULED, DONE }
	private volatile ImageState imageState = ImageState.NONE;
	private volatile WritableImage img;
	
	//graphics sheduling-related methods
	
	public ImageState getImageState() {
		return imageState;
	}
	
	public void sheduleImagePainting() {
		imageState = ImageState.SHEDULED;
	}
	
	public WritableImage getImage() {
		if (imageState == ImageState.DONE) {
			imageState = ImageState.NONE;
			return img;
		}
		return null;
	}

	public void newScene() {
		//sceneData = new SceneData();
		//TODO
	}

	public void loadSceneFromFile(File file) {
		if (file == null) return;
		try {
			sceneParams = new SceneParams(file);
		} catch (IOException e) {
			App.onException(Thread.currentThread(), e);
		}
		calculate();
	}
	
	private float[][] cameraRot = new float[3][3];
	private float[] cameraShift = new float[3];
	
	{
		cameraRot[0][1] = -1 / (float) Math.sqrt(2);
		cameraRot[0][2] = 1 / (float) Math.sqrt(2);
		cameraRot[1][1] = 1 / (float) Math.sqrt(2);
		cameraRot[1][2] = 1 / (float) Math.sqrt(2);
		cameraRot[2][0] = -1;

		cameraShift[0] = -3;
	}
	
	private void calculate() {
		synchronized (lock) {
			sceneSegments = new SceneSegments(sceneParams);
			sceneCamera = new SceneCamera(sceneSegments, sceneParams,
					cameraRot,
					cameraShift);
		}
	}

	public void resetAngles() {
		// TODO Auto-generated method stub
		
	}
	
	private void drawImage() {
		if (sceneCamera == null) return;
		DirectColorModel cm = (DirectColorModel) ColorModel.getRGBdefault();
		
		Color black = Color.BLACK;
		java.awt.Color awtBlack = Utils.toAwtColor(black);
		//int[] cmBlack = cm.getComponents(awtBlack.getRGB(), null, 0);
		
		Color white = Color.WHITE;
		java.awt.Color awtWhite = Utils.toAwtColor(white);
		//int[] cmWhite = cm.getComponents(awtWhite.getRGB(), null, 0);
		
		BufferedImage localImg;
		Graphics2D g;
		WritableRaster raster;
		
		int totalX = Model.WIDTH - 1;
		int totalY = Model.HEIGHT - 1;
		raster = cm.createCompatibleWritableRaster(totalX + 1, totalY + 1);
		localImg = new BufferedImage(
				cm, raster, cm.isAlphaPremultiplied(), null);
		g = (Graphics2D) localImg.getGraphics();
		
		//g.setColor(awtWhite);
		g.setColor(sceneParams.background_);
		g.fillRect(0, 0, localImg.getWidth(), localImg.getHeight());
		
		//g.setColor(awtBlack);
		g.setColor(sceneParams.solids[0].color_);
		/*for (int i = 0; i <= 4; i++) {
			int x = totalX * i / 4;
			int y = totalY * i / 4;
			//горизонтальная
			g.drawLine(x, 0, x, totalY);
			//вертикальная
			g.drawLine(0, y, totalX, y);
		}*/
		
		//сплайн
		
		/*Point2D.Double[] points = sceneSegments.splines.get(0);
		int prevX = 0, prevY = 0;
		for (int i = 0; i < points.length; i++) {
			int x = totalX / 2 + (int) (points[i].x * totalX / 4);
			int y = totalY / 2 - (int) (points[i].y * totalY / 4);
			if (i > 0) {
				g.drawLine(prevX, prevY, x, y);
			}
			g.fillOval(x - 2,  y - 2, 4, 4);
			prevX = x;
			prevY = y;
		}
		*/
		
		for (Pair<Point2D.Double, Point2D.Double> pair: sceneCamera.lines) {
			Point2D.Double p1 = pair.getKey();
			Point2D.Double p2 = pair.getValue();
			int x1 = totalX / 2 + (int) (p1.getX() * totalX / 4);
			int y1 = totalY / 2 - (int) (p1.getY() * totalY / 4);
			int x2 = totalX / 2 + (int) (p2.getX() * totalX / 4);
			int y2 = totalY / 2 - (int) (p2.getY() * totalY / 4);
			if (
				(x1 < 0 || x1 >= totalX || y1 < 0 || y1 >= totalY) &&
				(x2 < 0 || x2 >= totalX || y2 < 0 || y2 >= totalY)
			) continue;
			g.drawLine(x1, y1, x2, y2);
		}
		
		img = SwingFXUtils.toFXImage(localImg, null);
	}
	
	public void sceneRotateX(float angle) {
		float[][] matrix = new float[3][3];
		matrix[0][0] = 1;
		matrix[1][1] = (float) Math.cos(angle);
		matrix[2][2] = (float) Math.cos(angle);
		matrix[1][2] = (float) -Math.sin(angle);
		matrix[2][1] = (float) Math.sin(angle);
		sceneParams.E = Utils.matrixMatrixProduct(cameraRot, sceneParams.E);
		sceneParams.E = Utils.matrixMatrixProduct(matrix, sceneParams.E);
		sceneParams.E = Utils.matrixMatrixProduct(Utils.transposeMartix(cameraRot), sceneParams.E);
		calculate();
	}
	
	public void sceneRotateY(float angle) {
		float[][] matrix = new float[3][3];
		matrix[1][1] = 1;
		matrix[0][0] = (float) Math.cos(angle);
		matrix[2][2] = (float) Math.cos(angle);
		matrix[0][2] = (float) Math.sin(angle);
		matrix[2][0] = (float) -Math.sin(angle);
		sceneParams.E = Utils.matrixMatrixProduct(cameraRot, sceneParams.E);
		sceneParams.E = Utils.matrixMatrixProduct(matrix, sceneParams.E);
		sceneParams.E = Utils.matrixMatrixProduct(Utils.transposeMartix(cameraRot), sceneParams.E);
		calculate();
	}
	
	public void sceneRotateZ(float angle) {
		float[][] matrix = new float[3][3];
		matrix[2][2] = 1;
		matrix[0][0] = (float) Math.cos(angle);
		matrix[1][1] = (float) Math.cos(angle);
		matrix[0][1] = (float) -Math.sin(angle);
		matrix[1][0] = (float) Math.sin(angle);
		sceneParams.E = Utils.matrixMatrixProduct(cameraRot, sceneParams.E);
		sceneParams.E = Utils.matrixMatrixProduct(matrix, sceneParams.E);
		sceneParams.E = Utils.matrixMatrixProduct(Utils.transposeMartix(cameraRot), sceneParams.E);
		calculate();
	}
	
	public void cameraRotateX(float angle) {
		float[][] matrix = new float[3][3];
		matrix[0][0] = 1;
		matrix[1][1] = (float) Math.cos(angle);
		matrix[2][2] = (float) Math.cos(angle);
		matrix[1][2] = (float) -Math.sin(angle);
		matrix[2][1] = (float) Math.sin(angle);
		cameraRot = Utils.matrixMatrixProduct(matrix, cameraRot);
		calculate();
		//System.out.println(Arrays.toString(cameraRot[0])
		//		+ " " + Arrays.toString(cameraRot[1])
		//		+ " " + Arrays.toString(cameraRot[2]));
	}
	
	public void cameraRotateY(float angle) {
		float[][] matrix = new float[3][3];
		matrix[1][1] = 1;
		matrix[0][0] = (float) Math.cos(angle);
		matrix[2][2] = (float) Math.cos(angle);
		matrix[0][2] = (float) Math.sin(angle);
		matrix[2][0] = (float) -Math.sin(angle);
		cameraRot = Utils.matrixMatrixProduct(matrix, cameraRot);
		calculate();
	}
	
	public void cameraRotateZ(float angle) {
		float[][] matrix = new float[3][3];
		matrix[2][2] = 1;
		matrix[0][0] = (float) Math.cos(angle);
		matrix[1][1] = (float) Math.cos(angle);
		matrix[0][1] = (float) -Math.sin(angle);
		matrix[1][0] = (float) Math.sin(angle);
		cameraRot = Utils.matrixMatrixProduct(matrix, cameraRot);
		calculate();
	}
	
	public void cameraMoveForward(float distance) {
		cameraShift[0] -= distance * cameraRot[2][0];
		cameraShift[1] -= distance * cameraRot[2][1];
		cameraShift[2] -= distance * cameraRot[2][2];
		//float length = (float) Math.sqrt(shift[0] * shift[0] + shift[1] * shift[1] + shift[2] * shift[2]);
		calculate();
	}
	
	public void cameraMoveBackward(float distance) {
		cameraMoveForward(-distance);
	}
	
	public void cameraMoveLeft(float distance) {
		cameraShift[0] += distance * cameraRot[0][0];
		cameraShift[1] += distance * cameraRot[0][1];
		cameraShift[2] += distance * cameraRot[0][2];
		//float length = (float) Math.sqrt(shift[0] * shift[0] + shift[1] * shift[1] + shift[2] * shift[2]);
		calculate();
	}
	
	public void cameraMoveRight(float distance) {
		cameraMoveLeft(-distance);
	}
	
	public void cameraMoveDown(float distance) {
		cameraShift[0] += distance * cameraRot[1][0];
		cameraShift[1] += distance * cameraRot[1][1];
		cameraShift[2] += distance * cameraRot[1][2];
		//float length = (float) Math.sqrt(shift[0] * shift[0] + shift[1] * shift[1] + shift[2] * shift[2]);
		calculate();
	}
	
	public void cameraMoveUp(float distance) {
		cameraMoveDown(-distance);
	}
}
