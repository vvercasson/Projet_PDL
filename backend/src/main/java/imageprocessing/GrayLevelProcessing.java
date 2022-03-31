package imageprocessing;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;


public class GrayLevelProcessing {

	public static void threshold(GrayU8 input, int t) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl < t) {
					gl = 0;
				} else {
					gl = 255;
				}
				input.set(x, y, gl);
			}
		}
	}

	public static void lightGrey(GrayU8 input, int delta){
		int min = 0;
		int max = 255;
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl+delta > min && gl+delta < max)
					input.set(x, y, gl+delta);
				else{
					gl = 255;
                    input.set(x, y, gl);
				}
			}
		}
	}

	public static void imageDynamique(GrayU8 input, int min, int max){
		
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl < min)
					min = gl;	
				if (gl > max)
					max = gl;
			}
		}

		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int result = 0;
				int gl = input.get(x, y);
				result = (255*(gl - min))/(max-min);
				input.set(x, y, result);
			}
		}
	}

	// faire la fonction avec le LUT

	public static void imageDynamiqueLUT(GrayU8 input, int min, int max){
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl < min)
					min = gl;	
				if (gl > max)
					max = gl;
			}
		}

		int[] LUT = new int[254];
		for(int i = 0; i < LUT.length;i++){
			LUT[i] = (255*(i-min))/(max-min);
		}

		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				input.set(x, y, LUT[gl]);
			}
		}
		
	}
    
	public static void imageEgalizer(GrayU8 input){
		int size = input.height*input.width;
		int[] hist = new int[256];
		int[] histFinal = new int[256];
		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				int gl = input.get(x, y);
				hist[gl]++;
			}
		}

		histFinal[0] = hist[0];
		for(int i = 1; i < histFinal.length;i++){
			histFinal[i] = histFinal[i-1] + hist[i];
		}


		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				int gl = input.get(x, y);
				input.set(x, y, (histFinal[gl]*255)/size);
			}
		}
	}

}