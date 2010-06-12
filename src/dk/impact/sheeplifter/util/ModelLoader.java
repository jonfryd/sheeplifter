package dk.impact.sheeplifter.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.bounding.BoundingBox;
import com.jme.scene.Spatial;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;

public class ModelLoader {
	private static final Logger logger = Logger.getLogger(ModelLoader.class.getName());
	
	public static Spatial loadJMEModel(String file) {
        try {
            URL modelFile = ModelLoader.class.getClassLoader().getResource(file);

            if (logger.isLoggable(Level.INFO)) {
                logger.info("Model loaded: " + file);
            }            
            
            // Add to resource locator
            SimpleResourceLocator locator = new SimpleResourceLocator(modelFile.toURI());
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, locator);        	
        	
            BinaryImporter importer = new BinaryImporter();
            Spatial model = (Spatial)importer.load(modelFile.openStream());
            model.setModelBound(new BoundingBox());
            model.updateModelBound();
            
            return model;
        } catch (IOException e) {
            logger.throwing(ModelLoader.class.toString(), "loadModel()", e);
        } catch (URISyntaxException e) {
            logger.throwing(ModelLoader.class.toString(), "loadModel()", e);
		}
        
        return null;
	}
}
