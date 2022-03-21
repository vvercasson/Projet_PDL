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


    if (image.isPresent()) {
      InputStream inputStream = new ByteArrayInputStream(image.get().getData());

      // No algorithm applied if param is empty -> simply return original image
      if(algorithm.isEmpty()) {
        System.out.println("No algorithm applied");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new InputStreamResource(inputStream));
      }
      // There's an algorithm
      else {
        try {
          BufferedImage input;
          input = ImageIO.read(inputStream);
          Planar<GrayU8> imagein = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
          Planar<GrayU8> imageout = imagein.createSameShape();

          if(first.isEmpty()) {
            // Seuls algo possibles : Contours / Hist / Teinte
            if(algorithm.get().equals("contour")) {
              GrayU8 transit = new GrayU8(imagein.width, imagein.height);

              ColorLevelProcessing.convertToGray2(imagein,transit);
              GrayU8 end = new GrayU8(imagein.width, imagein.height);
              Convolution.gradientImageSobel(transit, end);

              ByteArrayOutputStream os = new ByteArrayOutputStream();
              BufferedImage output = ConvertBufferedImage.convertTo(end, null);

              if(!ImageIO.write(output, "png", os)) {
                System.err.println("** No writter found **");
              }                          
              InputStream is = new ByteArrayInputStream(os.toByteArray());

              return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new InputStreamResource(is));
            }
            else {
              System.err.println("Unknown algorithm : " + algorithm.get() );
              return new ResponseEntity<>("Algorithm : " + algorithm.get() + " not found. 400 Bad Request", HttpStatus.BAD_REQUEST); 
            }
          }
          else {
            // Seuls algo possibles : Luminosité / Flou

            // Recuperation parametre first
            int delta = Integer.parseInt(first.get());
            if(algorithm.get().equals("luminosite")) {
                ColorLevelProcessing.lightColor2(imagein,imageout, delta);          
            }
            else if(algorithm.get().equals("flou")) {
              ColorLevelProcessing.meanFilterColor(imagein, imageout, delta);
            }
            // Add algo
            else {
              System.err.println("Unknown algorithm : " + algorithm.get() );
              return new ResponseEntity<>("Algorithm : " + algorithm.get() + " not found. 400 Bad Request", HttpStatus.BAD_REQUEST);
            }
          }

          // Creating output
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          BufferedImage output = new BufferedImage(imagein.width, imagein.height, input.getType());
          ConvertBufferedImage.convertTo(imageout,output,true);
          // BufferedImage outBuffered = ConvertBufferedImage.convertTo_U8(imageout, null, false);
          if(!ImageIO.write(output, "png", os)) {
            System.err.println("** No writter found **");
          }                          
          InputStream is = new ByteArrayInputStream(os.toByteArray());

          return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new InputStreamResource(is));
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

 /* @RequestMapping(value = "/images/{id}")
  @ResponseBody
  public String executeAlgorithm(@PathVariable("id") long id,@RequestParam("algorithm") String algo,@RequestParam String p1,@RequestParam("p2") Optional<String> p2){
    Optional<Image> image = imageDao.retrieve(id);
    if (image.isPresent()) {
      String inputPath = "./src/main/resources/images/"+image.get().getName();
      BufferedImage input = UtilImageIO.loadImage(inputPath);
      Planar<GrayU8> imagein = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
      //return "lightColor executed";
      if (algo == "lightColor"){
        ColorLevelProcessing.lightColor(imagein,Integer.parseUnsignedInt(p1));
        String outputPath = "./result.jpg";
        UtilImageIO.saveImage(imagein, outputPath);
		    System.out.println("Image saved in: " + outputPath);
      }

    }
    return "error";
  }*/
  

}
