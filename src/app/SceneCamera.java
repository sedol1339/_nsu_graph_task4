package app;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point3D;
import javafx.util.Pair;

public class SceneCamera {
	
	public List<Pair<Point2D.Double, Point2D.Double>> lines;
	
	public SceneCamera(SceneSegments sceneSegments, SceneParams params,
			float[][] cameraRot, float[] cameraShift
			/*Point3D p_i, Point3D p_j, Point3D p_k, Point3D p_eye*/) {
		lines = new ArrayList<Pair<Point2D.Double, Point2D.Double>>();
		
		float[][] m_cam_1 = new float[4][4];
		m_cam_1[0][0] = cameraRot[0][0];
		m_cam_1[0][1] = cameraRot[0][1];
		m_cam_1[0][2] = cameraRot[0][2];
		m_cam_1[1][0] = cameraRot[1][0];
		m_cam_1[1][1] = cameraRot[1][1];
		m_cam_1[1][2] = cameraRot[1][2];
		m_cam_1[2][0] = cameraRot[2][0];
		m_cam_1[2][1] = cameraRot[2][1];
		m_cam_1[2][2] = cameraRot[2][2];
		m_cam_1[3][3] = 1;
		float[][] m_cam_2 = new float[4][4];
		m_cam_2[0][0] = 1;
		m_cam_2[1][1] = 1;
		m_cam_2[2][2] = 1;
		m_cam_2[3][3] = 1;
		m_cam_2[0][3] = cameraShift[0];
		m_cam_2[1][3] = cameraShift[1];
		m_cam_2[2][3] = cameraShift[2];
		float[][] m_cam = Utils.matrixMatrixProduct(m_cam_1, m_cam_2);
		
		float[][] m_proj = new float[4][4];
		m_proj[0][0] = 2 * params.zf / params.sw;
		m_proj[1][1] = 2 * params.zf / params.sh;
		m_proj[2][2] = params.zn / (params.zn - params.zf);
		m_proj[3][2] = 1;
		m_proj[2][3] = -params.zn * params.zf / (params.zn - params.zf);
		
		lines = new ArrayList<Pair<Point2D.Double, Point2D.Double>>();
		for (Pair<Point3D, Point3D> segment: sceneSegments.segments) {
			Point3D p1 = segment.getKey();
			Point3D p2 = segment.getValue();
			//System.out.println(p1 + " - " + p2);
			float[] p1_ = {(float) p1.getX(), (float) p1.getY(), (float) p1.getZ(), 1};
			float[] p2_ = {(float) p2.getX(), (float) p2.getY(), (float) p2.getZ(), 1};
			float[] p1_new = Utils.matrixVectorProduct(m_cam, p1_);
			float[] p2_new = Utils.matrixVectorProduct(m_cam, p2_);
			//System.out.println(Arrays.toString(p1_new) + " - " + Arrays.toString(p2_new));
			p1_new = Utils.matrixVectorProduct(m_proj, p1_new);
			p2_new = Utils.matrixVectorProduct(m_proj, p2_new);
			//System.out.println(Arrays.toString(p1_new) + " - " + Arrays.toString(p2_new));
			float depth1 = p1_new[2] / p1_new[3];
			float depth2 = p2_new[2] / p2_new[3];
			if ((depth1 < 0 || depth1 > 1) && (depth2 < 0 || depth2 > 1))
				continue;
			Point2D.Double p1__ = new Point2D.Double(p1_new[0] / p1_new[3], p1_new[1] / p1_new[3]);
			Point2D.Double p2__ = new Point2D.Double(p2_new[0] / p2_new[3], p2_new[1] / p2_new[3]);
			lines.add(new Pair<Point2D.Double, Point2D.Double>(p1__, p2__));
		}
	}
}
