package com.uberether.oow.oag.filters.oauth2;

import com.vordel.client.manager.Images;
import com.vordel.client.manager.filter.DefaultGUIFilter;
import com.vordel.client.manager.wizard.VordelPage;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 *
 * @author msamblanet
 */
public class UberOAuth2UI extends DefaultGUIFilter {
    /**
     * Add the pages you want to show in the configuration wizard for the filter.
     */
    @Override
    public List<VordelPage> getPropertyPages() {
        List<VordelPage> pages = new LinkedList<VordelPage>();
        
        // Add the panel for configuring the specific fields
        pages.add(new UberOAuth2Page());
        
        // Add the page which allows the user to set the log strings for the 
        // audit trail, for the pass/fail/error cases
        pages.add(createLogPage());
        
        return pages;
    }
    
    /**
     * Set the categories in which you want to display this Filter. The 
     * categories define the sections of the palette in which the Filter 
     * appears. The values returned should be the localized name of the 
     * palette section, so ensure that the property is defined in the 
     * resources.properties in this class's package. You will add this 
     * file to the "Example Filters" category.
     */
    @Override
    public String[] getCategories() {
        return new String[]{_("UE_OOW_2013_GROUP")};
    }
    
    /*
     *  Register our custom images with the image registry
     */
    private static final String IMAGE_KEY = "uberOauth2Filter";
    static {
        Images.getImageRegistry().put(IMAGE_KEY, 
                Images.createDescriptor(UberOAuth2UI.class, "uber.gif"));
    }
    
    /**
     *  The icon image needs to be added in images.properties in com.vordel.client.manager
     *  the id used there is used as a reference here.
     *  Use this method to get image id for the small icon image in Images.get(id), etc.
     */
    @Override
    public String getSmallIconId() {
        return IMAGE_KEY;
    }
    
    /**
     * Implement this method if you want to display a non-default image
     * for your filter in the policy editor canvas and navigation tree.
     */
    @Override
    public Image getSmallImage() {
        return Images.get(IMAGE_KEY);
    }
    
    /**
     * Implement this method to display a non-default icon for your filter in 
     * the palette.
     */
    @Override
    public ImageDescriptor getSmallIcon() {
        return Images.getImageDescriptor(IMAGE_KEY);
    }
}
