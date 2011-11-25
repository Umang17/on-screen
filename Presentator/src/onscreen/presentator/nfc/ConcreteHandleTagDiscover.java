package onscreen.presentator.nfc;

import java.util.Observable;

public class ConcreteHandleTagDiscover extends Observable implements
		HandleTagDiscover {
	private String text = ":(";

	public ConcreteHandleTagDiscover() {
	}

	public void handleTagDiscover(String text) {
		this.text = text;
		setChanged();
		notifyObservers();
	}

	public String getTag() {
		return text;
	}

}
