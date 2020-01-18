package generator;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;

@Log4j2
public abstract class FileUtils {

	public static String extensionFor(File file) {
		var name = file.getName();
		var lastIndexOf = name.lastIndexOf(".");
		var trim = name.substring(lastIndexOf).toLowerCase().trim();
		if (trim.startsWith(".")) {
			return trim.substring(1);
		}
		return trim;
	}

	@SneakyThrows
	private static void copyDirectory(File og, File target) {
		Assert.isTrue(!target.exists() || FileSystemUtils.deleteRecursively(target),
				"the target directory " + target.getAbsolutePath()
						+ " exists and could not be deleted");
		FileSystemUtils.copyRecursively(og, target);
	}

	@SneakyThrows
	private static void copyFile(File og, File target) {
		Assert.isTrue((target.exists() && target.delete()) || !target.exists(),
				"the target file " + target.getAbsolutePath()
						+ " exists, but could not be deleted");
		FileCopyUtils.copy(og, target);
	}

	@SneakyThrows
	public static File copy(File og, File target) {
		log.info("copying from " + og.getAbsolutePath() + " to "
				+ target.getAbsolutePath());
		if (og.isFile()) {
			copyFile(og, target);
		}
		else if (og.isDirectory()) {
			copyDirectory(og, target);
		}
		return target;
	}

	public static boolean delete(File f) {
		if (!f.exists()) {
			return true;
		}
		if (f.isFile()) {
			return f.delete();
		}
		else {
			return FileSystemUtils.deleteRecursively(f);
		}
	}

	public static File ensureDirectoryExists(File f) {
		Assert.isTrue(f.exists() || f.mkdirs(), "the directory " + f.getAbsolutePath()
				+ " does not exist and could not be created");
		return f;
	}

}
