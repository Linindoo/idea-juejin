package cn.olange.pins.utils;

import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import static com.intellij.util.ui.ImageUtil.toBufferedImage;

public class ImageUtils {

	public static BufferedImage makeRoundedCorner(BufferedImage image,
	                                              int cornerRadius) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage output = UIUtil.createImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = output.createGraphics();
		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius,
				cornerRadius));
		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return output;
	}

	public static String getThumbnailUrl(String rawUrl) {
		if (StringUtils.isEmpty(rawUrl)) {
			return rawUrl;
		}
		int endIndex = rawUrl.lastIndexOf("?");
		if (endIndex > 0) {
			return rawUrl.substring(0, endIndex) + "?imageView2/1/w/460/h/316/q/85/format/jpg/interlace/1";
		}
		return rawUrl;
	}

	public static Image scaleImage(Image image, int width, int height) {
		if (width > 0 && height > 0) {
			return imageScale(toBufferedImage(image),width, height);
		} else {
			return image;
		}
	}

	/**
	 * 图片缩放
	 * @param in
	 * @param sx	横坐标缩放比例
	 * @param sy	纵坐标缩放比例
	 * @param interpolationType
	 * @return
	 */
	public static BufferedImage imageScale(
			BufferedImage in, double sx, double sy, int interpolationType){
		AffineTransform matrix=new AffineTransform(); //仿射变换
		matrix.scale(sx,sy);
		AffineTransformOp op = null;
		if (interpolationType==1){
			op=new AffineTransformOp(matrix, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		}else if (interpolationType==2){
			op=new AffineTransformOp(matrix, AffineTransformOp.TYPE_BILINEAR);
		}else if (interpolationType==3){
			op=new AffineTransformOp(matrix, AffineTransformOp.TYPE_BICUBIC);
		}else{
			try {
				throw new Exception("input interpolation type from 1-3 !");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int width = (int)((double)in.getWidth() * sx);
		int height = (int)((double)in.getHeight() * sy);
		BufferedImage dstImage = ImageUtil.createImage(width, height, in.getType());
		op.filter(in, dstImage);
		return dstImage;
	}

	/**
	 * 图片缩放
	 * @param in
	 * @param sx	横坐标缩放比例
	 * @param sy	纵坐标缩放比例
	 * @return
	 */
	public static BufferedImage imageScale(BufferedImage in, double sx, double sy){
		return imageScale(in, sx, sy, 3);
	}

	/**
	 * 图片缩放
	 * @param in
	 * @param width	目标图片的宽度
	 * @param height	目标图片的高度
	 * @return
	 */
	public static BufferedImage imageScale(BufferedImage in, int width, int height) {
		double originW = in.getWidth();
		double originH = in.getHeight();
		double sx = width / originW;
		double sy = height / originH;
		return imageScale(in, sx, sy);
	}
}
