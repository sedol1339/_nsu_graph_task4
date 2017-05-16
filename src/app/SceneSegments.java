package app;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.SceneParams.Solid;
import javafx.geometry.Point3D;
import javafx.util.Pair;

public class SceneSegments {
	
	//public List<Point2D.Double[]> splines;
	public List<Pair<Point3D, Point3D>> segments;

	public SceneSegments(SceneParams par) {
		segments = new ArrayList<Pair<Point3D, Point3D>>();
		//splines = new ArrayList<Point2D.Double[]>();
		for (Solid s: par.solids) {
			Point2D.Double[] spline = bspline(s.spline, par.n * par.k, par.a, par.b);
			//splines.add(spline);
			
			//вертикальные отрезки (rot = const)
			for (int rot = 0; rot < par.n; rot++) {
				for (int shift = 0; shift < par.n; shift++) {
					float rot1 = par.c + (par.d - par.c) * rot / par.n;
					float rot2 = rot1;
					float shift1 = par.a + (par.b - par.a) * shift / par.n;
					float shift2 = par.a + (par.b - par.a) * (shift + 1) / par.n;
					for (int i = 0; i < par.k; i++) {
						if (i == par.k - 1 && shift == par.n - 1) continue;
						float shift1_ = shift1 + (shift2 - shift1) * i / par.k;
						float shift2_ = shift1 + (shift2 - shift1) * (i + 1) / par.k;
						Point2D.Double p1 = new Point2D.Double(shift1_, rot1);
						Point2D.Double p2 = new Point2D.Double(shift2_, rot2);
						//System.out.println(p1 + " - " + p2);
						Point3D p1_ = to3D(p1, spline, shift * par.k + i, s.C, s.R);
						Point3D p2_ = to3D(p2, spline, shift * par.k + i + 1, s.C, s.R);
						segments.add(new Pair<Point3D, Point3D>(p1_, p2_));
					}
				}
			}
			
			//горизонтальные отрезки (shift = const)
			for (int rot = 0; rot < par.n; rot++) {
				for (int shift = 0; shift < par.n; shift++) {
					float rot1 = par.c + (par.d - par.c) * rot / par.n;
					float rot2 = par.c + (par.d - par.c) * (rot + 1) / par.n;
					float shift1 = par.a + (par.b - par.a) * shift / par.n;
					float shift2 = shift1;
					for (int i = 0; i < par.k; i++) {
						float rot1_ = rot1 + (rot2 - rot1) * i / par.k;
						float rot2_ = rot1 + (rot2 - rot1) * (i + 1) / par.k;
						Point2D.Double p1 = new Point2D.Double(shift1, rot1_);
						Point2D.Double p2 = new Point2D.Double(shift2, rot2_);
						//System.out.println(p1 + " - " + p2);
						Point3D p1_ = to3D(p1, spline, shift * par.k, s.C, s.R);
						Point3D p2_ = to3D(p2, spline, shift * par.k, s.C, s.R);
						segments.add(new Pair<Point3D, Point3D>(p1_, p2_));
					}
				}
			}
		}
		
		Point3D sample = segments.get(0).getKey();
		double minX = sample.getX(),
			minY = sample.getY(),
			minZ = sample.getZ(),
			maxX = sample.getX(),
			maxY = sample.getY(),
			maxZ = sample.getZ();
		
		for (Pair<Point3D, Point3D> segment: segments) {
			Point3D[] points = {segment.getKey(), segment.getValue()};
			for (Point3D p: points) {
				if (p.getX() < minX) minX = p.getX();
				else if (p.getX() > maxX) maxX = p.getX();
				if (p.getY() < minY) minY = p.getY();
				else if (p.getY() > maxY) maxY = p.getY();
				if (p.getZ() < minZ) minZ = p.getZ();
				else if (p.getZ() > maxZ) maxZ = p.getZ();
			}
		}
		
		/* minX * xMult + xAdd = -1
		 * maxX * xMult + xAdd = 1
		 * 
		 * (maxX - minX) * xMult = 2
		 * 
		 * xMult = 2 / (maxX - minX)
		 * xAdd = 1 - maxX * xMult
		 */
		
		double xMult = 2 / (maxX - minX),
			xAdd = 1 - maxX * xMult,
			yMult = 2 / (maxY - minY),
			yAdd = 1 - maxY * yMult,
			zMult = 2 / (maxZ - minZ),
			zAdd = 1 - maxZ * zMult;
		double minMult = xMult;
		if (yMult < minMult) minMult = yMult;
		if (zMult < minMult) minMult = zMult;
		xMult = yMult = zMult = minMult;
		
		List<Pair<Point3D, Point3D>> segments2 = new ArrayList<Pair<Point3D, Point3D>>();
		for (Pair<Point3D, Point3D> segment: segments) {
			Point3D p1 = segment.getKey();
			Point3D p2 = segment.getValue();
			//System.out.println(p1 + " - " + p2);
			float[] p1_ = {
				(float) (p1.getX() * xMult + xAdd),
				(float) (p1.getY() * yMult + yAdd),
				(float) (p1.getZ() * zMult + zAdd)
			};
			float[] p2_ = {
				(float) (p2.getX() * xMult + xAdd),
				(float) (p2.getY() * yMult + yAdd),
				(float) (p2.getZ() * zMult + zAdd)
			};
			p1_ = Utils.matrixVectorProduct(par.E, p1_);
			p2_ = Utils.matrixVectorProduct(par.E, p2_);
			//System.out.println(Arrays.toString(p1_) + " - " + Arrays.toString(p2_));
			
			segments2.add(new Pair<Point3D, Point3D>(
				new Point3D(p1_[0], p1_[1], p1_[2]),
				new Point3D(p2_[0], p2_[1], p2_[2])
			));
		}
		
		segments = segments2;
	}
	
	static Point3D to3D(Point2D.Double p, Point2D.Double[] spline, int index,
			float[] shift, float[][] rotate) {
		
		//System.out.print(p + " -> ");
		float gx = (float) spline[index].x;
		float gy = (float) spline[index].y;
		//System.out.print("(" + gx + " " + gy + ")");
		
		float[] point3D = new float[3];
		point3D[0] = (float) (gy * Math.cos(p.y));
		point3D[1] = (float) (gy * Math.sin(p.y));
		point3D[2] = gx;
		
		point3D = Utils.matrixVectorProduct(rotate, point3D);
		//System.out.println(" -> " + Arrays.toString(point3D));
		point3D = Utils.vectorVectorSum(point3D, shift);
		
		return new Point3D(point3D[0], point3D[1], point3D[2]);
	}
	
	static Point2D.Double[] bspline(Point2D.Double[] points, int steps, float a, float b) {
		//считаем длину
		int precision = 10;
		float[] length = new float[(points.length - 3) * precision];
		float sumLength = 0;
		Point2D.Double point = countSpline(points, 1, 0);
		for (int i = 1; i <= points.length - 3; i++) {
			for (int j = 1; j <= precision; j++) {
				float t = ((float) j) / precision;
				Point2D.Double point2 = countSpline(points, i, t);
				int index = (i - 1) * precision + j - 1;
				length[index] = (float) point2.distance(point);
				sumLength += length[index];
				point = point2;
			}
		}
		//System.out.println(sumLength);
		int i1 = 1, j1 = 0, i2 = points.length - 3, j2 = precision;
		boolean set1 = false, set2 = false;
		int curLength = 0;
		for (int i = 1; i <= points.length - 3; i++) {
			for (int j = 1; j <= precision; j++) {
				int index = (i - 1) * precision + j - 1;
				curLength += length[index];
				if (!set1 && curLength >= sumLength * a) {
					i1 = i;
					j1 = j;
					set1 = true;
				}
				if (!set2 && curLength >= sumLength * b) {
					i2 = i;
					j2 = j;
					set2 = true;
				}
				if (set1 && set2)
					break;
			}
		}
		//System.out.println(a+" "+b+"   "+i1+" "+j1+" "+i2+" "+j2);
		
		/*
		 * (i1 j1) - (i2 j2) поделить на steps частей, steps >= 1
		 * Количество зайдесствованных отрезков сплайна: nspl = i2 - i1
		 * Делим nspl на steps: частное _a, остаток _b
		 * _a + _b точек на первый отрезок сплайна, _a точек на следующие
		 * Пусть отрезок i (начинается в j1, заканчивается в j2) поделен на _x точек
		 * Первая точка: (i j1)
		 * Последняя точка: (i j2)
		 */
		
		int nspl = i2 - i1 + 1;
		int _a = steps / nspl;
		int _b = steps % nspl;
		int ptFirst = _a + _b;
		int ptNext = _a;
		//System.out.println(nspl + " " + _a + " " + _b);
		
		List<Point2D.Double> finalPoints = new ArrayList<Point2D.Double>();
		for (int spl = i1; spl <= i2; spl++) {
			int nmbPoints = (spl == i1) ? ptFirst : ptNext;
			float begin, end;
			if (spl == i1 && spl == i2) {
				begin = ((float) j1) / precision;
				end = ((float) j2) / precision;
			} else if (spl == i1) {
				begin = ((float) j1) / precision;
				end = 1f;
			} else if (spl == i2) {
				begin = 0f;
				end = 1f;
			} else {
				begin = 0f;
				end = ((float) j2) / precision;
			}
			//имеем: spl, begin, end, nmbPoints
			for (int j = 0; j < nmbPoints; j++) {
				//System.out.print(spl + " " + (begin + (end - begin) * j / nmbPoints) + " ");
				Point2D.Double p = countSpline(points, spl, begin + (end - begin) * j / nmbPoints);
				finalPoints.add(p);
				//System.out.println(p);
			}
		}
		
		//System.out.println(finalPoints.size());
		Point2D.Double[] array =
				finalPoints.toArray(new Point2D.Double[finalPoints.size()]);
		return array;
	}
	
	static Point2D.Double countSpline(Point2D.Double[] p, int i, float t) {
		if (i < 1 || i > p.length - 3 || t < -0.001 || t > 1.001)
			throw new RuntimeException();
		double x = t*t*t*(-p[i-1].x + 3*p[i].x - 3*p[i+1].x + p[i+2].x)
				+ t*t*(3*p[i-1].x - 6*p[i].x + 3*p[i+1].x)
				+ t*(-3*p[i-1].x + 3*p[i+1].x)
				+ (p[i-1].x + 4*p[i].x + p[i+1].x);
		double y = t*t*t*(-p[i-1].y + 3*p[i].y - 3*p[i+1].y + p[i+2].y)
				+ t*t*(3*p[i-1].y - 6*p[i].y + 3*p[i+1].y)
				+ t*(-3*p[i-1].y + 3*p[i+1].y)
				+ (p[i-1].y + 4*p[i].y + p[i+1].y);
		x /= 6;
		y /= 6;
		return new Point2D.Double(x, y);
	}
}
