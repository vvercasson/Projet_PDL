package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.Media;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boofcv.factory.transform.wavelet.GFactoryWavelet;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import imageprocessing.ColorLevelProcessing;
import imageprocessing.Convolution;
import imageprocessing.GrayLevelProcessing;
import imageprocessing.TraitementImage;

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id, @RequestParam(value = "algorithm",required = false) Optional<String> algorithm, @RequestParam(value = "first",required = false) Optional<String> first, @RequestParam(value = "second",required = false) Optional<String> second ) {

    Optional<Image> image = imageDao.retrieve(id);


    // Check if image with id exists
    if (image.isPresent()) {
      InputStream inputStream = new ByteArrayInputStream(image.get().getData());

      // No algorithm applied if param "algorithm" is empty -> simply return original image
      if(algorithm.isEmpty()) {
        System.out.println("No algorithm applied");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new InputStreamResource(inputStream));
      }
      // There's an algorithm
      else {
        try {
          // Convert the incoming stream into a Planar
          BufferedImage input;
          input = ImageIO.read(inputStream);
          Planar<GrayU8> imagein = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
          
          // Debug line
          // System.out.println("[Num bands : " + imagein.getNumBands() + "]");

          boolean isPng = TraitementImage.isPng(imagein);
          Planar<GrayU8> imageout = imagein.createSameShape();

          if(first.isEmpty()) {
            // Seuls algo possibles : Contours / Hist / Teinte
            if(algorithm.get().equals("contour")) {
              GrayU8 transit = new GrayU8(imagein.width, imagein.height);


              TraitementImage.rgbToGrey(imagein, transit);
              GrayU8 end = new GrayU8(imagein.width, imagein.height);

              TraitementImage.contour(transit, end);

              ByteArrayOutputStream os = new ByteArrayOutputStream();
              BufferedImage output = ConvertBufferedImage.convertTo(end, null);

              if(isPng) {
                ImageIO.write(output, "png", os);
              }
              else {
                ImageIO.write(output, "jpg", os);
              }

              InputStream is = new ByteArrayInputStream(os.toByteArray());
              
              if(isPng)
                return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new InputStreamResource(is));
              return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new InputStreamResource(is));
            }
            else if(algorithm.get().equals("negatif")) {
              TraitementImage.negatif(imagein, imageout);
            }
            else {
              System.err.println("Unknown algorithm : " + algorithm.get() );
              return new ResponseEntity<>("Algorithm : " + algorithm.get() + " not found. 400 Bad Request", HttpStatus.BAD_REQUEST); 
            }
          }
          else {
            if(second.isEmpty()) {
              // Seuls algo possibles : Luminosité
              // Recuperation parametre first
              if(algorithm.get().equals("luminosite")) {
                int delta = Integer.parseInt(first.get());    
                // TraitementImage.luminosite(imagein,imageout, delta);
                TraitementImage.luminosite(imagein, imageout,delta);
              }
              else if(algorithm.get().equals("rotate")) {
                char sens = first.get().charAt(0);
                TraitementImage.retourner(imagein, imageout, sens);
              }
              else if(algorithm.get().equals("flou")) {
                String type = first.get();
                if(type.equals("gaussien")) {
                  int[][] kernel = {
                      {1,2,3,2,1},
                      {2,6,8,6,2},
                      {3,8,10,8,3},
                      {2,6,8,6,2},
                      {1,2,3,2,1}
                    };
                  TraitementImage.gaussien(imagein, imageout, kernel);
                }
              }
              else if (algorithm.get().equals("bruit")){
                int niveauBruit = Integer.parseInt(first.get());
                TraitementImage.bruitGaussien(imagein,imageout,niveauBruit);
              }

              else {
                System.err.println("Unknown algorithm : " + algorithm.get() );
                return new ResponseEntity<>("Algorithm : " + algorithm.get() + " not found. 400 Bad Request", HttpStatus.BAD_REQUEST);
              }
            }
            else {
              if(algorithm.get().equals("flou")) {
                String type = first.get();
                int size = Integer.parseInt(second.get());
                if(type.equals("moyen")) {
                  TraitementImage.flouMoyen(imagein, imageout, size);
                }
                else {
                  System.err.println("Unknown algorithm : " + algorithm.get());
                  return new ResponseEntity<>("Blur filter : " + type + " not found. 400 Bad Request", HttpStatus.BAD_REQUEST);
                }
              }
            }
          }
          // Creating output
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          BufferedImage output = new BufferedImage(imagein.width, imagein.height, input.getType());
          ConvertBufferedImage.convertTo(imageout,output,true);
          if(isPng) {
            ImageIO.write(output, "png", os);
          }
          else {
            ImageIO.write(output, "jpg", os);
          }                       
          InputStream is = new ByteArrayInputStream(os.toByteArray());

          if(isPng)
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new InputStreamResource(is));
          return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new InputStreamResource(is));
        } catch (IOException e1) {
          e1.printStackTrace();
        }        
      }
    }
    return new ResponseEntity<>("Image id=" + id + " not found. 404 error", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

    Optional<Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {
      imageDao.delete(image.get());
      return new ResponseEntity<>("Image id=" + id + " deleted.", HttpStatus.OK);
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

    String contentType = file.getContentType();
    if (!contentType.equals(MediaType.IMAGE_JPEG.toString()) && !contentType.equals(MediaType.IMAGE_PNG.toString())) {
      return new ResponseEntity<>("415 Unsupported Media Type", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    try {
      imageDao.create(new Image(file.getOriginalFilename(), file.getBytes()));
    } catch (IOException e) {
      return new ResponseEntity<>("Failure to read file", HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>("201 Created", HttpStatus.OK);
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ArrayNode getImageList() throws IOException {
    List<Image> images = imageDao.retrieveAll();
    ArrayNode nodes = mapper.createArrayNode();
    for (Image image : images) {
      File file =  new File(image.getName());
      Path path = file.toPath();
      String type = Files.probeContentType(path);

      InputStream inputStream = new ByteArrayInputStream(image.getData());

      BufferedImage bufferedImage = ImageIO.read(inputStream);

      //BufferedImage bufferedImage = (BufferedImage)ImageIO.read(file);
      int width = bufferedImage.getWidth();
      int height = bufferedImage.getHeight();
      int typeImg = bufferedImage.getType();
      
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("type",type); // a revoir avec MediaType
      objectNode.put("size",width+"x"+height+"x"+typeImg);
      nodes.add(objectNode);
    }
    return nodes;
  }

}
