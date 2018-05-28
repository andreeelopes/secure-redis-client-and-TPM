package utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.Path;



public class FileHelper {

	public static void ToFile(byte[] bytes, String path){
		FileOutputStream f;
		try {
			f = new FileOutputStream(path);
			f.write(bytes);
			f.close();
		
		}catch(NoSuchFileException e) {
			System.out.println("File didn't exist. Creating on = " + path);
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	public static byte[] ToBytes(String pathName){
		Path path = Paths.get(pathName);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		}catch(NoSuchFileException e) {
			System.out.println("File didn't exist.");
		}catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}
}


