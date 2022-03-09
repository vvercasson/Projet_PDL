package pdl.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  public ImageDao() {    
    String imgRepo = new String("/Users/vvercasson/Universite/S6/PDL/Projet_PDL/pdl-client-serveur/backend/src/main/resources/images");
    //final ClassPathResource imageRepo = new ClassPathResource(imgRepo);
    File f = new File(imgRepo);
    if (!(f.exists()) || !(f.isDirectory()) ) {
      System.err.println("images either doesn't exist or isn't a directory");
    }
 
    parcourtRepo(f, f.getName());
  }

  public void parcourtRepo(File f, String name) {
    File[] fileList = f.listFiles();
    if(fileList != null) {
      System.err.println("Repository doesn't exist");
    }
    for (File file : fileList) {
      if(file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
        addImage(name, file);
      }
      else if(file.isDirectory()) {
        parcourtRepo(file, name + "/" + file.getName());
      }
      else {
        System.err.println("Files need to be jpg or png");
      }
    }
  }

  public void addImage(String repo, File imageName) {
    final ClassPathResource imgFile = new ClassPathResource(repo + "/" + imageName.getName());
        byte[] fileContent;
        try {
          fileContent = Files.readAllBytes(imgFile.getFile().toPath());
          Image img = new Image(imageName.getName(), fileContent);
          images.put(img.getId(), img);
        } catch (final IOException e) {
          e.printStackTrace();
        }
  }

  @Override
  public Optional<Image> retrieve(final long id) {
    return Optional.ofNullable(images.get(id));
  }

  @Override
  public List<Image> retrieveAll() {
    return new ArrayList<Image>(images.values());
  }

  @Override
  public void create(final Image img) {
    images.put(img.getId(), img);
  }

  @Override
  public void update(final Image img, final String[] params) {
    img.setName(Objects.requireNonNull(params[0], "Name cannot be null"));

    images.put(img.getId(), img);
  }

  @Override
  public void delete(final Image img) {
    images.remove(img.getId());
  }
}
