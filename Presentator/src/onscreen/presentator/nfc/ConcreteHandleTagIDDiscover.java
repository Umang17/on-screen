package onscreen.presentator.nfc;

import java.util.Observable;

public class ConcreteHandleTagIDDiscover extends Observable implements
		HandleTagIDDiscover {
	private String tagID = ":(";

	public ConcreteHandleTagIDDiscover() {
	}

	public void handleTagIDDiscover(String tagID) {
		this.tagID = tagID;
		setChanged();
		notifyObservers();
	}

	public String getTag() {
		return tagID;
	}

}
