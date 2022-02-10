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

public class Main {

	public static boolean hasCapacity(File srcImage, int msgsize) throws Exception {
		ImageInfo ii = Imaging.getImageInfo(srcImage);
		int cap = 3 * ii.getWidth() * ii.getHeight() / 8;
		int pad = 16 - (msgsize % 16);
		System.out.println("capacity: " + cap + " size: " + msgsize);
		return (cap >= (msgsize + pad));
	}

	public static void encode(File srcImage, File dstImage, Biterator biter) throws Exception {

		ImageMetadata metadata = Imaging.getMetadata(srcImage);
		System.out.println("Image meta:  " + metadata);

		ImageInfo imageInfo = Imaging.getImageInfo(srcImage);
		System.out.println("Image Info:  " + imageInfo);

		BufferedImage image = Imaging.getBufferedImage(srcImage);

		System.out.println("Height: " + image.getHeight());
		System.out.println("Width: " + image.getWidth());

		int b1,b2,b3 = 0;
		int maxX = image.getWidth();
		int maxY = image.getHeight();
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {

				int pix = image.getRGB​(x, y);
				
				b1 = biter.nextBit();
				if (b1 > 0) {
					pix |= 0x01;
				} else {
					pix &= 0xFFFFFFFe;
				}
				
				b2 = biter.nextBit();
				if (b2 > 0) {
					pix |= 0x0100;
				} else {
					pix &= 0xFFFFFeFF;
				}
				
				b3 = biter.nextBit();
				if (b3 > 0) {
					pix |= 0x010000;
				} else {
					pix &= 0xFFFeFFFF;
				}
				//System.out.print(bit);
				image.setRGB(x, y, pix);

			}
		}

		final byte[] bytes = Imaging.writeImageToBytes(image, ImageFormats.PNG, null);

		FileOutputStream fos = new FileOutputStream(dstImage);
		fos.write(bytes);
		fos.close();

	}

	public static String readString(String filename) {
		try {
			StringBuffer sb = new StringBuffer();
			File f = new File(filename);
			int size = (int) f.length();
			FileInputStream is = new FileInputStream(f);
			int num = 0;
			int ch = 0;
			while (num < size) {
				ch = is.read();
				sb.append((char) ch);
				num++ ;
			}
			is.close();
			return sb.toString();
		} catch (Exception ex) {
			System.out.println("Error: count not open file: " + filename);
			System.exit(0);
		}
		return null;
	}

	public static String getArg(String[] args, String val, boolean isRequired) {
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(val))
				return args[i + 1];
		}
		if (isRequired) {
			System.out.println("Error: A required parameter is missing: " + val);
			System.out.println();
			usage();
		}
		return null;
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

	public static File getFile(String filename, boolean mustExist) {
		try {
			File ret = new File(filename);
			if (mustExist && !ret.exists()) {
				System.out.println("Error: count not open file: " + filename);
				System.exit(0);
			}
			return ret;
		} catch (Exception ex) {
			System.out.println("Error: count not open file: " + filename);
			System.exit(0);
		}
		return null;
	}

	public static void decode(File srcImage, Encryption crypt) throws Exception {

		BufferedImage image = Imaging.getBufferedImage(srcImage);

		System.out.println("Height: " + image.getHeight());
		System.out.println("Width: " + image.getWidth());

		String fileName = null;
		int fileNameSize = 0;
		int dataSize = 0;

		int nextStop = 16;
		int cursize = 0;
		BitCapture bc = new BitCapture();
		int maxX = image.getWidth();
		int maxY = image.getHeight();
		byte[] bar = null;

		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {

				int pix = image.getRGB​(x, y);

				cursize = bc.addBit(pix & 0x01);
				cursize = bc.addBit(pix & 0x0100);
				cursize = bc.addBit(pix & 0x010000);

				if (cursize == nextStop) {
					System.out.println("Stop at: " + cursize);
					if (dataSize > 0) {
						// last stop, done
						bar = crypt.decrypt(bc.getBytes(), 0, cursize);

						x = maxX;
						y = maxY;
						break;
					}

					bar = bc.getBytes();

					bar = crypt.decrypt(bc.getBytes(), 0, cursize);

					fileNameSize = ((int) bar[0]) & 0xFF;
					if ((1 + fileNameSize + 4) <= nextStop) {
						if (fileNameSize > 0) {
							fileName = new String(bar, 1, fileNameSize);
							System.out.println("Decoded Filename: " + fileName);
						}
						dataSize = DataContainer.ti(bar, 1 + fileNameSize, 4);
						nextStop = 1 + fileNameSize + 4 + dataSize;
						int pad = 16 - (nextStop % 16);
						nextStop += pad;
						System.out.println(fileNameSize + " NextStop1: " + nextStop + " datasize: " + dataSize);
					} else {
						// wait for a few blocks to decode a longer header
						nextStop = 1 + fileNameSize + 4;
						int pad = 16 - ( nextStop% 16);
						nextStop += pad;
						System.out.println(fileNameSize + " NextStop0: " + nextStop + " datasize: " + dataSize);
					}
				}
			}
		}

		if (fileName != null) {
			System.out.println("Writing output file: " + fileName);
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(bar, 1 + fileNameSize + 4, dataSize);
			fos.close();
		} else {
			System.out.println("Writing output data");
			System.out.println(new String(bar, 1 + fileNameSize + 4, dataSize));
		}

	}

	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				usage();
			}

			String in = getArg(args, "-in", true);

			String oper = args[0];
			if (oper.equals("-encode")) {
				String out = getArg(args, "-out", true);
				String key = getArg(args, "-key", false);
				if (key == null) {
					key = getArg(args, "-keyfile", true);
					key = readString(key);
				}

				String msgFileName = null;
				String msg = getArg(args, "-msg", false);
				int msgsize = 0;
				if (msg == null) {
					msgFileName = getArg(args, "-msgfile", false);
					File msgFile = getFile(msgFileName, true);
					msgsize = (int) msgFile.length();
				} else {
					msgsize = msg.length();
				}

				File inImg = getFile(in, true);
				File outImg = getFile(out, false);
				if (!hasCapacity(inImg, msgsize)) {
					System.out.println("Error: msg size is too large for this image ");
					System.exit(0);
				}
				Encryption crypt = new Encryption(key);
				DataContainer dc = new DataContainer(crypt);
				if (msgFileName != null) {
					dc.setFile(msgFileName);
					dc.readDataFromFile();
				} else {
					dc.setStringMessage(msg);
				}
				dc.encrypt();

				encode(inImg, outImg, dc.getBiterator());

			} else if (oper.equals("-decode")) {
				String key = getArg(args, "-key", false);
				if (key == null) {
					key = getArg(args, "-keyfile", true);
					key = readString(key);
				}
				File inImg = getFile(in, true);

				Encryption crypt = new Encryption(key);

				decode(inImg, crypt);

			} else {
				usage();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
