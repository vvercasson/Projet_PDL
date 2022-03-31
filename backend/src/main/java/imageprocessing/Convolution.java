package imageprocessing;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;

public class Convolution {

  public static void meanFilterSimple(GrayU8 input, GrayU8 output, int size) {
    if(size%2 == 0) {size= size - 1;}
    int taille = (int)Math.floor(size/2);
    int div = size*size;
    for(int y=taille;y<input.height-taille;y++){
        for (int x=taille;x<input.width-taille;x++) {
            int r = 0;
            int somme = 0;
            for(int v = -taille; v <= taille; v++){
                for (int u = -taille; u <= taille; u++) {
                    r = r + input.get(x+u,y+v);
                }        
            }
            somme = r/div;
            output.set(x, y, somme);
        }
    }
  }

  public static void meanFilterWithBorders(GrayU8 input, GrayU8 output, int size, BorderType borderType) {
    if (borderType == BorderType.SKIP)
      meanFilterSimple(input, output, size);
    if(borderType == BorderType.NORMALIZED){

    }
    else if(borderType == BorderType.EXTENDED || borderType == BorderType.REFLECT){
      int valU = 0;
      int valV = 0;
      if(size%2 == 0) {size= size - 1;}
      int taille = (int)Math.floor(size/2);
      int div = size*size;
      for(int y=taille;y<input.height-taille;y++){
          for (int x=taille;x<input.width-taille;x++) {
              int r = 0;
              int somme = 0;
              for(int v = -taille; v <= taille; v++){
                  for (int u = -taille; u <= taille; u++) {
                    valU = u;
                    valV = v;
                    if (valU < 0 ){
                      if(borderType == BorderType.EXTENDED)
                        valU = 0;
                      if(borderType == BorderType.REFLECT){
                        valU = Math.abs(valU);
                      }
                    }
                        
                    if (valV < 0){
                      if(borderType == BorderType.EXTENDED){
                        //System.out.println("Coucou ! ");
                        valV = 0;
                      }
                      if(borderType == BorderType.REFLECT)
                        valV = Math.abs(valV);

                    }
                    r = r + input.get(valU+u,valV+v);
                  }        
              }
              somme = r/div;
              output.set(x, y, somme);
          }
      }
    }
  }
  
  public static void convolution(GrayU8 input, GrayU8 output, int[][] kernel) {
    int size = kernel.length;
    int taille = (int)Math.floor(size/2);
    for(int y=taille;y<input.height-taille;y++){
        for (int x=taille;x<input.width-taille;x++) {
            int r = 0;
            int somme = 0;
            int div = 0;
            for(int v = -taille; v <= taille; v++){
                for (int u = -taille; u <= taille; u++) {
                    r = r + input.get(x+u,y+v)*kernel[u+taille][v+taille];
                    div += kernel[u+taille][v+taille];
                }        
            }
            somme = r/div;
            output.set(x, y, somme);
        }
    }
  }

  public static void gradientImageSobel(GrayU8 input, GrayU8 output, char c){

    for(int y=1;y<input.height-1;y++){
      for (int x=1;x<input.width-1;x++) {
        int r = 0;
        if (c == 'h')
          r = input.get(x+1,y)-input.get(x-1, y);
        if (c == 'v')
          r = input.get(x,y+1)-input.get(x, y-1);
        output.set(x, y, r);

        if (c == 'c'){
          int g1 = input.get(x+1,y)-input.get(x-1, y);
          int g2 = input.get(x,y+1)-input.get(x, y-1);
          int m = (int) Math.sqrt((g1*g1)+(g2*g2));
          output.set(x, y, m);
        }
      }
    } 
  }

  
  public static void gradientImageSobel(GrayU8 input, GrayU8 output){

    for(int y=1;y<input.height-1;y++){
      for (int x=1;x<input.width-1;x++) {
          int g1 = input.get(x+1,y)-input.get(x-1, y);
          int g2 = input.get(x,y+1)-input.get(x, y-1);
          int m = (int) Math.sqrt((g1*g1)+(g2*g2));
          output.set(x, y, m);
      }
    } 
  }
}