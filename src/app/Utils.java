package app;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	
	static void clearComments(List<String> lines) {
		List<String> toRemove = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String s = lines.get(i);
			int offset = s.indexOf("//");
			if (-1 != offset) {
				lines.set(i, s.substring(0, offset).trim());
			} else {
				lines.set(i, s.trim());
			}
			s = lines.get(i);
			if (s.length() == 0)
				toRemove.add(s);
		}
		for (String s : toRemove) {
			lines.remove(s);
		}
	}
	
	static Number[][] tokenize(List<String> lines) throws IOException {
		Number[][] nmb = new Number[lines.size()][];
		for (int i = 0; i < lines.size(); i++) {
			String s = lines.get(i);
			String[] tokens = s.split("\\s+");
			nmb[i] = new Number[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				try {
					nmb[i][j] = Float.parseFloat(tokens[j]);
				} catch (NumberFormatException e) {
					throw new IOException(e);
				}
			}
		}
		return nmb;
	}
	
	static java.awt.Color toAwtColor(javafx.scene.paint.Color c) {
		return new java.awt.Color(
			(float) c.getRed(),
            (float) c.getGreen(),
            (float) c.getBlue(),
            (float) c.getOpacity());
	}
	
	static float[][] matrixMatrixProduct(float[][] mA, float[][] mB) {
		int m = mA.length;
        int n = mB[0].length;
        int o = mB.length;
        float[][] res = new float[m][n];
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < o; k++) {
                    res[i][j] += mA[i][k] * mB[k][j]; 
                }
            }
        }
        return res;
	}
	
	static float[] matrixVectorProduct(float[][] m, float[] v) {
		int n = v.length;
		float[] res = new float[n];
		for (int i = 0; i < n; i++) {
			res[i] = 0;
			for (int j = 0; j < n; j++) {
				res[i] += m[i][j] * v[j];
			}
		}
		return res;
	}
	
	static float[] vectorVectorSum(float[] v1, float[] v2) {
		int n = v1.length;
		float[] res = new float[n];
		for (int i = 0; i < n; i++) {
			res[i] = v1[i] + v2[i];
		}
		return res;
	}
	
	static float[][] transposeMartix(float[][] m) {
		int n = m.length;
		float[][] res = new float[n][n];
		for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
            	res[i][j] = m[j][i];
            }
		}
		return res;
	}
}
