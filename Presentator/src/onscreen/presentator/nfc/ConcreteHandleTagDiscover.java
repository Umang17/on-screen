package onscreen.presentator.nfc;

import java.util.Observable;

/**
 * The Concreate Handle Tag Discover which is observable and notify observers if change so they can get tag text.
 * @author Viktor Lindgren
 *
 */
public class ConcreteHandleTagDiscover extends Observable implements
		HandleTagDiscover {
	private String text = ":(";

	/**
	 * @param text discovered text on the tag.
	 */
	public void handleTagDiscover(String text) {
		this.text = text;
		setChanged();
		notifyObservers();
	}

	/**
	 * 
	 * @return text found on tag last time discovered.
	 */
	public String getTagText() {
		return text;
	}

}
