package Libraries;


import static Utilities.FileUtilities.loadImage;

import java.util.Scanner;

import Structures.BasicGeneratedStructure;
import Structures.Structure;
import Utilities.FileUtilities;


public class StructureLibrary {
private static Structure[] structureLibrary = new Structure[16384];
	
	public static Structure getStructureFromLibrary(int structureIndex) {
		return structureLibrary[structureIndex];
	}
	
	public static void setStructure(int structureIndex, Structure structure) {
		structureLibrary[structureIndex] = structure;
	}
	
	public static int getFilledStructureSlots() {
		int slots = 0;
		for (Structure s : structureLibrary) {
			if (s == null) return slots;
			slots++;
		}
		return slots;
	}
	
	public static void populateStructureLibrary() {
		Scanner structureFile = FileUtilities.getFileInternal("structures.txt");
		FileUtilities.log("Loading structures..." + "\n");
		try {
			int i = 0;
			while (structureFile.hasNextLine()) {
				int index = structureFile.nextInt();
				String filePath = (structureFile.nextLine()).substring(1);
				structureLibrary[index] = new BasicGeneratedStructure(filePath);
				FileUtilities.log("\tStructure " + index + " loaded - " + filePath + "\n");
				i++;
			}
		} catch (Exception e) {
			FileUtilities.log("Error while loading structures" + "\n");
			e.printStackTrace();
			System.exit(3);
		}
	}
	
}