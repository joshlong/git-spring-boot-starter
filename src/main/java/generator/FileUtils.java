package generator;

import org.springframework.util.Assert;

import java.io.File;

abstract public class FileUtils {

	public static File ensureDirectoryExists(File f) {
		Assert.isTrue(f.exists() || f.mkdirs(), "the directory " + f.getAbsolutePath()
				+ " does not exist and could not be created");
		return f;
	}

}
