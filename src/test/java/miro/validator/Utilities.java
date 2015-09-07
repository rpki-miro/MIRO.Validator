package test.java.miro.validator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Utilities {

	public static void cleanFile(String path) {
		try {
			FileUtils.write(new File(path), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
