import javax.microedition.midlet.MIDlet;

public class LocaleEditorMIDlet extends MIDlet {

	public static LocaleEditorMIDlet midlet;
	private boolean started;

	public LocaleEditorMIDlet() {
		midlet = this;
	}

	public void destroyApp(boolean unconditional) {

	}

	protected void pauseApp() {

	}

	protected void startApp() {
		if(started)
			return;
		started = true;
		new LocaleEditor();
	}

}
