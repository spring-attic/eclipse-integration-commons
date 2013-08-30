package org.springsource.ide.eclipse.commons.gettingstarted.browser.sample;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.springsource.ide.eclipse.commons.gettingstarted.browser.IBrowserCustomization;

public class SampleBrowserCustomization implements IBrowserCustomization {

	@Override
	public void apply(final Browser browser) {
		browser.addProgressListener(new ProgressListener() {
			public void completed(ProgressEvent event) {
				String url = browser.getUrl();
				browser.execute("alert('"+url+"')");
			}
			public void changed(ProgressEvent event) {
			}
		});
	}

}
