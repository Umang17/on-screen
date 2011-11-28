package onscreen.presentator.nfc;

/**
 * Interface that is used for handling when a new tag is discover and the
 * handleTagDiscover should be called.
 * 
 * @author viktor
 * 
 */

public interface HandleTagDiscover {

	/**
	 * Call this function when a tag is discovered.
	 * @param text that was found on the tag.
	 */
	public void handleTagDiscover(String text);
}
