package imageprocessing;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import boofcv.alg.color.ColorHsv;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;


public class ColorLevelProcessing {

    public static void threshold(Planar<GrayU8> image, int t) {
		for (int y = 0; y < image.height; ++y) {
			for (int x = 0; x < image.width; ++x) {
                for (int i = 0; i < image.getNumBands(); i++){
                    int gl = image.getBand(i).get(x, y);
                    if (gl < t) {
                        gl = 0;
                    } else {
                        gl = 255;
                    }
                    image.getBand(i).set(x, y, gl);
                }
			}
		}
	}

    public static void lightColor(Planar<GrayU8> input, int delta){
			int min = 0;
			int max = 255;
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
                for (int i = 0; i < input.getNumBands(); i++){
                    int gl = input.getBand(i).get(x, y);
                    if (gl+delta > min && gl+delta < max)
                        input.getBand(i).set(x, y, gl+delta);
                    else{
                        gl = 255;
                        input.getBand(i).set(x, y, gl);
                    }
                }
			}
		}
	}

	public static void meanFilterColor(Planar<GrayU8> input, Planar<GrayU8> output, int size){
		if(size%2 == 0) {size= size - 1;}
		int taille = (int)Math.floor(size/2);
		int div = size*size;
		for(int y=taille;y<input.height-taille;y++){
			for (int x=taille;x<input.width-taille;x++) {
				for (int i = 0; i < input.getNumBands(); i++){
					int r = 0;
					int somme = 0;
					for(int v = -taille; v <= taille; v++){
						for (int u = -taille; u <= taille; u++) {
							r = r + input.getBand(i).get(x+u,y+v);
						}        
					}
					somme = r/div;
					output.getBand(i).set(x,y,somme);
				}
			}
		}
	}

	public static void convolutionColor(Planar<GrayU8> input, Planar <GrayU8> output, int kernel[][]){
		int size = kernel.length;
		int taille = (int)Math.floor(size/2);
		for(int y=taille;y<input.height-taille;y++){
			for (int x=taille;x<input.width-taille;x++) {
				for (int i = 0; i < input.getNumBands(); i++){
					int r = 0;
					int somme = 0;
					int div = 0;
					for(int v = -taille; v <= taille; v++){
						for (int u = -taille; u <= taille; u++) {
							r = r + input.getBand(i).get(x+u,y+v)*kernel[u+taille][v+taille];
							div += kernel[u+taille][v+taille];
						}        
					}
					somme = r/div;
					output.getBand(i).set(x, y, somme);
				}
			}
		}
	}



    public static GrayU8 convertToGray(Planar<GrayU8> input) {
		var gray = new GrayU8(input.width, input.height);
		for (int y = 0; y < input.height;y++) {
			for(int x = 0; x < input.width; x++){
				int r = 0;
				double red = (input.getBand(0).get(x,y))*0.3;
				double green = (input.getBand(1).get(x,y))*0.59;
				double blue = (input.getBand(2).get(x,y))*0.11;
				r = (int)(red+green+blue);
				gray.set(x, y, r);
			}
		}
		return gray;
	}

	static void rgbToHsv(int r, int g, int b, float[] hsv){
		List<Integer> list = new LinkedList<Integer>();
		list.add(r);
		list.add(g);
		list.add(b);
		float max = Collections.max(list);
		float min = Collections.min(list);

		float deg1 = (float)Math.toRadians(60.0);

		if(max == min){
			hsv[0] = 0;
		}
		else{
			if (max == r){
				float deg2 = (float)Math.toRadians(360.0);
				float div = (g-b)/(max-min);
				hsv[0] = (deg1*div+deg2)%deg2;
			}
			if (max == g){
				float deg2 = (float)Math.toRadians(120.0);
				float div = (b-r)/(max-min);
				hsv[0] = (deg1*div+deg2);
			}
			if (max == b){
				float deg2 = (float)Math.toRadians(240.0);
				float div = (r-g)/(max-min);
				hsv[0] = (deg1*div+deg2);
			}
		}

		if(max == 0){
			hsv[1] = 0;
		}
		else{
			hsv[1] = 1 - (min/max);
		}

		hsv[2] = max;
	}

	static void hsvToRgb(float h, float s, float v, int[] rgb){
		float t = (h/60)%6;
		float f = (h/60)-t;
		float l = v*(1-s);
		float m = v*(1-f*s);
		float n = v*(1-(1-f)*s);

		if(t == 0){
			rgb[0] = (int) v;
			rgb[1] = (int) n;
			rgb[2] = (int) l;
		}
		if(t == 1){
			rgb[0] = (int) m;
			rgb[1] = (int) v;
			rgb[2] = (int) l;
		}
		if(t == 2){
			rgb[0] = (int) l;
			rgb[1] = (int) v;
			rgb[2] = (int) n;
		}
		if(t == 3){
			rgb[0] = (int) l;
			rgb[1] = (int) m;
			rgb[2] = (int) v;
		}
		if(t == 4){
			rgb[0] = (int) n;
			rgb[1] = (int) l;
			rgb[2] = (int) v;
		}
		if(t == 5){
			rgb[0] = (int) v;
			rgb[1] = (int) l;
			rgb[2] = (int) m;
		}
	}
	
	static void tintedImage(Planar<GrayU8> input, Planar<GrayU8> output, float teinte){
		float[] hsv = new float[3];
		int[] rgb = new int[3];
		for (int y = 0; y < input.height;y++) {
			for(int x = 0; x < input.width; x++){
				int red = (input.getBand(0).get(x,y));
				int green = (input.getBand(1).get(x,y));
				int blue = (input.getBand(2).get(x,y));
				rgbToHsv(red, green, blue, hsv);
				hsvToRgb(teinte,hsv[1],hsv[2],rgb);
				output.getBand(0).set(x, y,rgb[0]);
				output.getBand(1).set(x, y,rgb[1]);
				output.getBand(2).set(x, y,rgb[2]);
			}
		}
	}


    public static void main( String[] args ) {

    	// load image
		if (args.length < 2) {
			System.out.println("missing input or output image filename");
			System.exit(-1);
		}
		final String inputPath = args[0];
		BufferedImage input = UtilImageIO.loadImage(inputPath);
        Planar<GrayU8> imagein = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
		Planar<GrayU8> imageout = imagein.createSameShape();
		if(imageout == null) {
			System.err.println("Cannot read input file '" + inputPath);
			System.exit(-1);
		}
		System.out.println("Input file : "+input);

		// processing

		int[][] kernel = {{1,2,3,2,1},
                      {2,6,8,6,2},
                      {3,8,10,8,3},
                      {2,6,8,6,2},
                      {1,2,3,2,1}
                    };
		
        //threshold(image, 128);
		//lightColor(imagein,50);
		//imageDynamique(input);
		//imageEgalizer(input);
        //convertToGray(image);
		//meanFilterColor(imagein, imageout,15);
		//convolutionColor(imagein,imageout,kernel);
		//GrayU8 imageGrey = convertToGray(imagein);

		/////////////////
		// RGB TO HSV
		/////////////////
	
		float[] hsvFun = new float[3];
		float[] hsvBoof = new float[3];

		//rgbToHsv(122,122,122,hsvFun);
		//System.out.println("Resultat de ma fonction : "+hsvFun[0]+' '+hsvFun[1]+' '+hsvFun[2]);
		//ColorHsv.rgbToHsv(62,25,79,hsvBoof);
		//System.out.println("Resultat de Boofcv : "+hsvBoof[0]+' '+hsvBoof[1]+' '+hsvBoof[2]);
		
		/////////////////
		// HSV TO RGB
		////////////////

		int[] rgbMoi = new int[3];
		double[] rgbFun = new double[3];

		hsvToRgb(270,10,45,rgbMoi);
		System.out.println("Resultat moi : "+rgbMoi[0]+' '+rgbMoi[1]+' '+rgbMoi[2]);
		ColorHsv.hsvToRgb(270,10,45,rgbFun);
		System.out.println("Resultat fonction : "+rgbFun[0]+' '+rgbFun[1]+' '+rgbFun[2]);
		
		//tintedImage(imagein,imageout,270);
		// save output image
		final String outputPath = args[1];
		UtilImageIO.saveImage(imageout, outputPath);
		System.out.println("Image saved in: " + outputPath);
	}

}
