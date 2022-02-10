
package io.github.imagemsg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.internal.Debug;

public class Analyze {

	public static void analyze(File srcImage) throws Exception {

		ImageMetadata metadata = Imaging.getMetadata(srcImage);
		System.out.println("Image meta:  " + metadata);

		ImageInfo imageInfo = Imaging.getImageInfo(srcImage);
		System.out.println("Image Info:  " + imageInfo);

		BufferedImage image = Imaging.getBufferedImage(srcImage);

		System.out.println("Height: " + image.getHeight());
		System.out.println("Width: " + image.getWidth());

		int one = 0;
		int zero = 0;
		int b1 = 0;
		int maxX = image.getWidth();
		int maxY = image.getHeight();
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {

				int pix = image.getRGB​(x, y);

				b1 = pix & 0x01;
				if (b1 > 0) {
					one++;
				} else {
					zero++;
				}

			}
		}
		float zero_p = (float) (100 * zero) / (float) (one + zero);
		float one_p = (float) (100 * one) / (float) (one + zero);
		System.out.println("zero: " + zero + " " + zero_p);
		System.out.println("one:  " + one + " " + one_p);

	}

	public static void usage() {
		System.out.println(
				"Usage: java -jar ImageMsg.jar -encode -in <image.png> -out <new_image.png> -key \"abc123xyz000secret000password\" -msg \"This is a secret message\"");
		System.out.println(
				"Usage: java -jar ImageMsg.jar -encode -in <image.png> -out <new_image.png> -keyfile key.txt -msgfile message.txt ");
		System.out.println();
		System.out.println(
				"Usage: java -jar ImageMsg.jar -decode -in <new_image.png> -key \"abc123xyz000secret000password\" ");
		System.out.println("Usage: java -jar ImageMsg.jar -decode -in <new_image.png> -keyfile key.txt ");
		System.out.println();

		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				usage();
			}

			String in = Main.getArg(args, "-in", true);
			File inImg = Main.getFile(in, true);

			analyze(inImg);

		} catch (

		Exception ex) {
			ex.printStackTrace();
		}
	}
}
