package com.uberether.oow.oag.filters.oauth2;

import com.vordel.client.manager.wizard.VordelPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Configuration UI page for this filter.  Has only one property for the 
 * filter name.
* 
 * Based on sample filter from the OAG tutorial
  *
 * @author msamblanet
 */
public class UberOAuth2Page extends VordelPage {

    public UberOAuth2Page() {
        super("uberTestPage");
        setTitle(_("UOF_PAGE_TITLE"));
        setDescription(_("UOF_PAGE_DESCRIPTION"));
        setPageComplete(false);
    }

    @Override
    public String getHelpID() {
        return "uber.oauth2.help";
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    @Override
    public void createControl(Composite parent) {
        // Create a Panel with two columns        
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(layout);

        // Add controls to populate the appropriate Entity Fields
        // You use the localization keys for the field names and 
        // descriptions which will map to entries in the 
        // resources.properties file.        
        createLabel(container, "UOF_NAME");
        createTextAttribute(container, "name", "UOF_NAME_DESC");

        // Finish up the page definition
        setControl(container);
        setPageComplete(true);
    }
}
