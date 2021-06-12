package ui;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

import domain.Side;
import domain.pieces.Piece;
import domain.pieces.PieceType;

public class ImageLoader {
	private int cachedHeight = 0;
	private Map<String, Image> imageCache = new HashMap<String, Image>();
	
	/**
	 * Return the image for a piece for the given height. Images are cached and disposed when images of a different
	 * height are created.
	 */
	public Image getPieceImage(Piece piece, int height, boolean shadow) {
		return getPieceImage(piece.getPieceType(), piece.getSide(), height, shadow);
	}
	
	/**
	 * Return the image for a piece for the given height. Images are cached and disposed when images of a different
	 * height are created.
	 */
	public Image getPieceImage(PieceType type, Side side, int height, boolean shadow) {
		String imageName = type.getAlgebraic() + side.toString();

		// Dispose and clear cache if the height has changed.
		if(cachedHeight != height) {
			dispose();
		}
		
		// Return cached image if it exists.
		if(imageCache.containsKey(imageName)) {
			return imageCache.get(imageName);
		}

		// Load image for piece, and merge it with its drop shadow.
		Image pieceImage = loadSvg("/resource/pieces/"+imageName+".svg", height);
		
		Image image;
		if(shadow) {
			Image shadowImage = createDropShadow(pieceImage);
			image = mergeImages(pieceImage, shadowImage);
			pieceImage.dispose();
			shadowImage.dispose();
		} else {
			image = pieceImage;
		}
		
		// Add image to cache and return it.
		imageCache.put(imageName, image);
		cachedHeight = height;
		return image;
	}

	/**
	 * Dispose of cached images.
	 */
	public void dispose() {
		for(Image image:imageCache.values()) {
			image.dispose();
		}
		imageCache.clear();
	}
	
	/**
	 * Merge two transparent images of the same size together, returning a new image.
	 */
	private Image mergeImages(Image topImage, Image bottomImage) {
		int w = topImage.getImageData().width;
		int h = topImage.getImageData().height;
		
		Image image = new Image(Display.getCurrent(), w, h);
		GC gc = new GC(image);
		gc.drawImage(bottomImage, 0, 0);
		gc.drawImage(topImage, 0, 0);
		ImageData imageData = image.getImageData();
		image.dispose();
		gc.dispose();
		
		ImageData data1 = topImage.getImageData();
		ImageData data2 = bottomImage.getImageData();
		
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int a1 = data1.getAlpha(x, y);
				int a2 = data2.getAlpha(x, y);
				
				int a = a1 + a2;
				if(a > 255) a = 255;
				imageData.setAlpha(x, y, a);
			}
		}
		
		return new Image(Display.getCurrent(), imageData);
	}
	
	/**
	 * Return an drop shadow of an image, made by making the image black, blurring it and
	 * offsetting the image.
	 */
	private Image createDropShadow(Image image) {
		ImageData imageData = image.getImageData();
		
		int width = imageData.width;
		int height = imageData.height;
		
		PaletteData palette = imageData.palette;
		
		Image copy = new Image(Display.getCurrent(), imageData);
		ImageData copyImageData = copy.getImageData();
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				byte alpha = blur(copyImageData.alphaData, x-1, y-2, width, height);
				
				imageData.setPixel(x, y, palette.getPixel(new RGB(0, 0, 0)));
				imageData.setAlpha(x, y, alpha & 0xFF);
			}
		}
		
		return new Image(Display.getCurrent(), imageData);
	}
	
	/**
	 * Blur a value in an array where the indexes are of the form: [x+y*width].
	 * Returns the average of the surrounding values.
	 */
	private byte blur(byte[] data, int x, int y, int width, int height) {
		int total = 0;
		int count = 0;
		
		for(int dx = -3; dx <= 3; dx++){
			for(int dy = -3; dy <= 3; dy++) {
				int nx = x + dx;
				int ny = y + dy;
				if(nx < 0 || ny < 0 || nx >= width || ny >= width) continue;
			
				total += (int)data[nx + ny * width] & 0xFF;
				count += 1;
			}
		}
		
		return (byte)(total / (double)count);
	}
	
	/**
	 * Loads and returns an svg image by name (relative to the classpath) and height.
	 */
	private Image loadSvg(String name, int height) {
		PNGTranscoder transcoder = new PNGTranscoder();
		transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, Float.valueOf(height));
		
		TranscoderInput input = new TranscoderInput(getResource(name));
		
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			TranscoderOutput output = new TranscoderOutput(outputStream);
			
			transcoder.transcode(input, output);
			
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			return new Image(Display.getCurrent(), inputStream);
		} catch (TranscoderException e) {
			throw new RuntimeException("Error loading SVG file: " + name, e);
		}
	}

	private InputStream getResource(String name) {
		return getClass().getResourceAsStream(name);
	}
}
