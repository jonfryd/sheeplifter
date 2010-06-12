package dk.impact.sheeplifter.util;

import javax.swing.JFrame;

import bsh.Interpreter;
import bsh.util.JConsole;

public class Debugging {
	public static void openConsole(Object app) {
		JConsole console = new JConsole();
		Interpreter i = new Interpreter(console);
		
		try {
			i.set("app", app);
			i.eval("setAccessibility(true)");
			i.eval("import com.jme.math.*");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JFrame window = new JFrame();
		window.add(console);
		
		window.setVisible(true);
		window.setSize(640, 480);

		new Thread(i).start(); 
	}
}
