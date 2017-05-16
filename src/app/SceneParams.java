package app;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class SceneParams {
	
	public int n, m, k;
	public float a, b, c, d;
	public float zn, zf, sw, sh;
	public float[][] E;
	public int[] background;
	public Color background_;
	public Solid[] solids;
	
	static class Solid {
		public int[] color;
		public Color color_;
		public float[] C;
		public float[][] R;
		public Point2D.Double[] spline;
	}
	
	public SceneParams(File file) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		Utils.clearComments(lines);
		Number[][] data = Utils.tokenize(lines);
		try {
			n = data[0][0].intValue();
			m = data[0][1].intValue();
			k = data[0][2].intValue();
			a = data[0][3].floatValue();
			b = data[0][4].floatValue();
			c = data[0][5].floatValue();
			d = data[0][6].floatValue();
			zn = data[1][0].floatValue();
			zf = data[1][1].floatValue();
			sw = data[1][2].floatValue();
			sh = data[1][3].floatValue();
			E = new float[3][3];
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					E[i][j] = data[i + 2][j].floatValue();
			background = new int[3];
			background[0] = data[5][0].intValue();
			background[1] = data[5][1].intValue();
			background[2] = data[5][2].intValue();
			background_ = new Color(
				background[0],
				background[1],
				background[2]
			);
			int K = data[6][0].intValue();
			solids = new Solid[K];
			int row = 7;
			for (int k = 0; k < K; k++) {
				solids[k] = new Solid();
				solids[k].color = new int[3];
				solids[k].color[0] = data[row][0].intValue();
				solids[k].color[1] = data[row][1].intValue();
				solids[k].color[2] = data[row][2].intValue();
				solids[k].color_ = new Color(
					solids[k].color[0],
					solids[k].color[1],
					solids[k].color[2]
				);
				row++;
				solids[k].C = new float[3];
				solids[k].C[0] = data[row][0].floatValue();
				solids[k].C[1] = data[row][1].floatValue();
				solids[k].C[2] = data[row][2].floatValue();
				row++;
				solids[k].R = new float[3][3];
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 3; j++)
						solids[k].R[i][j] = data[i + row][j].floatValue();
				row += 3;
				int N = data[row][0].intValue();
				solids[k].spline = new Point2D.Double[N];
				for (int i = 0; i < N; i++) {
					row++;
					solids[k].spline[i] = new Point2D.Double();
					solids[k].spline[i].x = data[row][0].floatValue();
					solids[k].spline[i].y = data[row][1].floatValue();
				}
				row++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IOException(e);
		}
		if (solids.length == 0)
			throw new IOException("no solids");
		for (Solid s: solids)
			if (s.spline.length < 4)
				throw new IOException("wrong spline");
	}
}
