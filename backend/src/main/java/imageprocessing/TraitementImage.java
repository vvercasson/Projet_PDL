package imageprocessing;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import boofcv.io.image.ConvertBufferedImage;

import boofcv.struct.image.Planar;

public class TraitementImage {

	// Verifications
	public static boolean isGrey(Planar<GrayU8> input) {
		return input.getNumBands() == 1;
	}

	public static boolean isPng(Planar<GrayU8> input) {
		return input.getNumBands() == 4;
	}


	// Filtres
	public static void luminosite(Planar<GrayU8> input, Planar<GrayU8> output, int delta){		
		// Valeur de vérification lors de l'application du filtre
		int min = 0;
		int max = 255;

		// 1 bande = Image en noir et blanc sinon image en couleur
		boolean isGrey = isGrey(input);

		// 4 bande = PNG
		boolean isPng = isPng(input);
		

		// Si l'image est grise, on ne traite qu'une bande sinon 3 (peu importe png ou jpg)
		int bandsToTreat = isGrey? 1:3;

		if(isPng)
			copyAlphaBand(input, output);

		// Parcours tous les pixels
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) { 

				// Parcours chaque bande de l'image
				for (int i = 0; i < bandsToTreat; i++){

					// Calcul de la nouvelle valeur de pixel
					int gl = input.getBand(i).get(x, y) + delta;

					// Verif borne inf.
					if (gl < min)
						output.getBand(i).set(x, y, 0);

					// Verif borne sup.
					else if(gl > max)
						output.getBand(i).set(x, y, 255);
					// Cas dit "normal"
					else
						output.getBand(i).set(x,y,gl);
				}
			}
		}
	}

	public static Planar<GrayU8> bruitGaussien(Planar<GrayU8> image, Planar<GrayU8> output,int bruitVal) {
        int result;
        int valeurAvecBruit;
        double gaussian;
		int niveauBruit = bruitVal;
		if (niveauBruit > 255)
			niveauBruit = 255;
		if (niveauBruit < 0)
			niveauBruit = 0;

        int nbBands  = output.getNumBands();
        int width  = image.getWidth();
        int height = image.getHeight();
        java.util.Random randGen = new java.util.Random();
          
        for (int j=0; j<height; j++) {
            for (int i=0; i<width; i++) {
                gaussian = randGen.nextGaussian();
                  
                for (int b=0; b<nbBands; b++) {
                    valeurAvecBruit = niveauBruit * (int)gaussian;
                    result = image.getBand(b).get(i, j);
                    valeurAvecBruit = valeurAvecBruit + result;
                    if (valeurAvecBruit < 0)   
						valeurAvecBruit = 0;
                    if (valeurAvecBruit > 255) 
						valeurAvecBruit = 255;
                      
                    output.getBand(b).set(i,j,(int)valeurAvecBruit);
                }
            }
        }
          
        return output;
    }

	public static void negatif(Planar<GrayU8> input, Planar<GrayU8> output){		
		// 1 bande = Image en noir et blanc sinon image en couleur
		boolean isGrey = isGrey(input);

		// 4 bande = PNG
		boolean isPng = isPng(input);
		

		// Si l'image est grise, on ne traite qu'une bande sinon 3 (peu importe png ou jpg)
		int bandsToTreat = isGrey? 1:3;

		if(isPng)
			copyAlphaBand(input, output);

		int rgb[] = new int[3];
		// Parcours tous les pixels
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) { 

				// Parcours chaque bande de l'image
				for (int i = 0; i < bandsToTreat; i++){
					rgb[i] = input.getBand(i).get(x, y);
					output.getBand(i).set(x, y, 255 - rgb[i]);
				}
			}
		}
	}

	public static void flouMoyen(Planar<GrayU8> input, Planar<GrayU8> output, int size){
		if(size%2 == 0) {size -= 1;}

		int taille = (int) Math.floor(size/2);

		int div = size*size;

		// 1 bande = Image en noir et blanc sinon image en couleur
		boolean isGrey = isGrey(input);

		// 4 bande = PNG
		boolean isPng = isPng(input);

		if(isPng)
			copyAlphaBand(input, output);
				
		
		// Si l'image est grise, on ne traite qu'une bande sinon 3 (peu importe png ou jpg)
		int bandsToTreat = isGrey? 1:3;

		// Parcours pixels
		for(int y=taille;y<input.height-taille;y++){
			for (int x=taille;x<input.width-taille;x++) {

				for (int i = 0; i < bandsToTreat; i++){
					int r = 0;
					int somme = 0;
					for(int v = -taille; v <= taille; v++){
						for (int u = -taille; u <= taille; u++) {
							r += input.getBand(i).get(Math.abs(x + v), Math.abs(y + u));
						}        
					}

					somme = r/div;

					output.getBand(i).set(x,y,somme);
				}
			}
		}
	}

	public static void filtreMedian(Planar<GrayU8> input, Planar<GrayU8> output, int size){
		if(size%2 == 0) {size -= 1;}

		int taille = (int) Math.floor(size/2);

		int div = size*size;

		// 1 bande = Image en noir et blanc sinon image en couleur
		boolean isGrey = isGrey(input);

		// 4 bande = PNG
		boolean isPng = isPng(input);

		if(isPng)
			copyAlphaBand(input, output);
				
		
		// Si l'image est grise, on ne traite qu'une bande sinon 3 (peu importe png ou jpg)
		int bandsToTreat = isGrey? 1:3;

		// Parcours pixels
		for(int y=taille;y<input.height-taille;y++){
			for (int x=taille;x<input.width-taille;x++) {

				for (int i = 0; i < bandsToTreat; i++){
					int r = 0;
					int somme = 0;
					for(int v = -taille; v <= taille; v++){
						for (int u = -taille; u <= taille; u++) {
							r += input.getBand(i).get(Math.abs(x + v), Math.abs(y + u));
						}        
					}

					somme = r/div;

					output.getBand(i).set(x,y,somme);
				}
			}
		}
	}

	public static void gaussien(Planar<GrayU8> input, Planar <GrayU8> output, int kernel[][]){
		int size = kernel.length;

		int taille = (int) Math.floor(size/2);

		// 1 bande = Image en noir et blanc sinon image en couleur
		boolean isGrey = isGrey(input);

		// 4 bande = PNG
		boolean isPng = isPng(input);
				
		
		// Si l'image est grise, on ne traite qu'une bande sinon 3 (peu importe png ou jpg)
		int bandsToTreat = isGrey? 1:3;
		
		if(isPng)
			copyAlphaBand(input, output);

		// Parcours pixels
		for(int y=taille;y<input.height-taille;y++){
			for (int x=taille;x<input.width-taille;x++) {

				for (int i = 0; i < bandsToTreat; i++){
					int r = 0;
					int somme = 0;
					int div = 0;
					for(int v = -taille; v <= taille; v++){
						for (int u = -taille; u <= taille; u++) {
							r = r + input.getBand(i).get(Math.abs(x+u),Math.abs(y+v))*kernel[u+taille][v+taille];
							div += kernel[u+taille][v+taille];
						}        
					}
					somme = r/div;
					output.getBand(i).set(x, y, somme);
				}
			}
		}
	}

	// Retourne l'image verticalement(v) hozitalement(h) ou les deux(b) selon le paramètre sens passé
	public static void retourner(Planar<GrayU8> input, Planar<GrayU8> output,char sens) {
		if( !(sens == 'h' || sens == 'v' || sens == 'b')  ) {
			System.err.println("Sens has to be either l v or b");
			return;
		}
		// 1 bande = Image en noir et blanc sinon image en couleur
		boolean isGrey = isGrey(input);

		// 4 bande = PNG
		boolean isPng = isPng(input);
						
		// Si l'image est grise, on ne traite qu'une bande sinon 3 (peu importe png ou jpg)
		int bandsToTreat = isGrey? 1:3;
				
		if(isPng)
			copyAlphaBand(input, output);

		int rgb[] = new int[3];
		int height = input.getHeight() - 1;
		int width = input.getWidth() - 1;

		// Parcours tous les pixels
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) { 
				// Parcours chaque bande de l'image
				for (int i = 0; i < bandsToTreat; i++){
					rgb[i] = input.getBand(i).get(x, y);
					if(sens == 'v')
						output.getBand(i).set(x, height-y, rgb[i]);
					else if(sens == 'h')
						output.getBand(i).set(width-x, y, rgb[i]);
					else if(sens == 'b') 
						output.getBand(i).set(width-x, height-y, rgb[i]);
				}
			}
		}
	}

	public static void contour(GrayU8 input, GrayU8 output){
		// Parcours tous les pixels
		for(int y=1;y<input.height-1;y++){
		  for (int x=1;x<input.width-1;x++) {
			  int g1 = input.get(x+1,y)-input.get(x-1, y);
			  int g2 = input.get(x,y+1)-input.get(x, y-1);
			  int m = (int) Math.sqrt((g1*g1)+(g2*g2));
			  output.set(x, y, m);
		  }
		} 
	  }
	
	// transitions RGB / HSV / GREY
	public static void rgbToGrey(Planar<GrayU8> input, GrayU8 output) {
		for (int y = 0; y < input.height;y++) {
			for(int x = 0; x < input.width; x++){
				int r = 0;
				double red = (input.getBand(0).get(x,y))*0.3;
				double green = (input.getBand(1).get(x,y))*0.59;
				double blue = (input.getBand(2).get(x,y))*0.11;
				r = (int)(red+green+blue);
				output.set(x, y, r);
			}
		}
	}
	
	public static void rgbToHsv(int r, int g, int b, float[] hsv) {
		float fR = (float) r; float fG = (float) g; float fB = (float) b;
		float max = Float.max(Float.max(fR,fG), fB);
		float min = Float.min(Float.min(fR,fG), fB);
  
		//HSV[2]
		hsv[2] = max;
  
		// HSV[0]
		if (max == min) {
		  hsv[0] = 0;
		}
		else if(max == fR) {
		  hsv[0] = (60 * (fG-fB)/(max-min) + 360)%360;
		}
		else if(max == fG) {
		  hsv[0] = (60 * (fB-fR)/(max-min) + 120);
		}
		else if(max == fB) {
		  hsv[0] = (60 * (fR-fG)/(max-min) + 240);
		}
  
		//HSV[1]
		if(max == 0) {
		  hsv[1] = 0;
		}
		else {
		  hsv[1] = (1-(min/max));
		}
	  }

	public static void hsvToRgb(float h, float s, float v, int[] rgb) {
		double t = Math.floor(h/60)%6;
		double f = (h/60)-t;
		double l = v * (1-s);
		double m = v * (1-f*s);
		double n = v * (1-(1-f)*s);
  
		switch((int) t) {
		  case 0: 
			rgb[0] = (int)(v*255);
			rgb[1] = (int)(n*255);
			rgb[2] = (int)(l*255);
			break;
		  case 1: 
			rgb[0] = (int)(m*255);
			rgb[1] = (int)(v*255);
			rgb[2] = (int)(l*255);
			break;
		  case 2:
			rgb[0] = (int)(l*255);
			rgb[1] = (int)(v*255);
			rgb[2] = (int)(n*255);
			break;
		  case 3:
			rgb[0] = (int)(l*255);
			rgb[1] = (int)(m*255);
			rgb[2] = (int)(v*255);
			break;
		  case 4:
			rgb[0] = (int)(n*255);
			rgb[1] = (int)(l*255);
			rgb[2] = (int)(v*255);
			break;
		  case 5:
			rgb[0] = (int)(v*255);
			rgb[1] = (int)(l*255);
			rgb[2] = (int)(m*255);
			break;
		  default:
			break;
		}
	  }
	

	// PNG purpose only
	public static void copyAlphaBand(Planar<GrayU8> input, Planar<GrayU8> output) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) { 
						output.getBand(3).set(x,y,input.getBand(3).get(x,y));
				
			}
		}
	} 
}