package lk.ac.mrt.distributed.messaging;

import lk.ac.mrt.distributed.messaging.ui.EventViewer;

public class Main {

	public static void main(String[] args) throws Exception
	{
		EventViewer v = new EventViewer();
		new MainController(v);
		v.setVisible(true);
	}
}