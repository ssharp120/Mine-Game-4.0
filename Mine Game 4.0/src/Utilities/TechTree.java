package Utilities;

import Libraries.RecipeLibrary;
import UI.Ingredient;
import UI.InventoryEntity;
import UI.InventoryItem;
import UI.InventoryTile;
import UI.InventoryTool;
import UI.Recipe;

public class TechTree {	
	public static class TechTreeNode {
		private InventoryItem[] inputs;
		private Recipe[] outputs;
		
		private String header;
		
		private boolean active;
		private boolean complete;
		
		private int index;
		
		private int numInputs;
		private int numOutputs;
		
		public TechTreeNode(int index, String header, InventoryItem[] input, Recipe[] output) {
			this.inputs = input;
			this.outputs = output;
			this.index = index;
			this.header = header;
			numInputs = input.length;
			numOutputs = output.length;
		}
		
		public boolean checkRecipe(InventoryItem in[]) {
			if (in.length >= numInputs) {
				for (InventoryItem j : inputs) {
					boolean inputSatisfied = false;
					for (int i = 0; i < in.length; i++) {
						if (in[i] != null) {
							if (in[i].getItemID() == j.getItemID()) {
								if (in[i].getClass() == j.getClass() && in[i].getClass() == Ingredient.class) {
									if (!inputSatisfied) inputSatisfied = ((Ingredient) in[i]).getQuantity() >= ((Ingredient) j).getQuantity();
								} else if (in[i].getClass() == j.getClass() && in[i].getClass() == InventoryTile.class) {
									if (!inputSatisfied) inputSatisfied = ((InventoryTile) in[i]).getQuantity() >= ((InventoryTile) j).getQuantity();
								} else if (in[i].getClass() == j.getClass() && in[i].getClass() == InventoryTool.class) {
									if (!inputSatisfied) inputSatisfied = ((InventoryTool) in[i]).getDurability() == ((InventoryTool) in[i]).getMaxDurability();
								}
							}
						}
					}
					if (!inputSatisfied) return false;
				}
				return true;
			}
			return false;
		}
		
		public String getHeader() {
			return header;
		}
		
		public String toString() {
			String description = "Tech Tree Node " + index + ": \n\tInputs: " + numInputs + ", Outputs: " + numOutputs + "\n";
			for (InventoryItem i : inputs) {
				description += "\t\tInput: " + i.toString() + "\n";
			}
			for (Recipe o : outputs) {
				description += "\t\tOutput: " + o.toString() + "\n";
			}
			return description;
		}
		
		public InventoryItem[] getInputs() {
			return inputs;
		}
		
		public Recipe[] getOutputs() {
			return outputs;
		}
		
		public void setActive(boolean activated) {
			this.active = activated;
		}
		
		public boolean isActive() {
			return active;
		}
		
		public Recipe[] complete() {
			complete = true;
			for (int i = 0; i < outputs.length; i++) {
				RecipeLibrary.setRecipe(RecipeLibrary.getFilledRecipeSlots() + 1, outputs[i]);
			}
			return outputs;
		}
		
		public boolean isComplete() {
			return complete;
		}
	}
	
	private static TechTreeNode[] techTree = new TechTreeNode[16384];

	public TechTree() {
		populateTechTreeNodes();
	}
	
	public static TechTreeNode getTechTreeNode(int techTreeIndex) {
		return techTree[techTreeIndex];
	}
	
	public static void setTechTreeNode(int techTreeIndex, TechTreeNode techTreeNode) {
		techTree[techTreeIndex] = techTreeNode;
	}
	
	public void tick() {
		
	}
	
	public int getFilledTechTreeNodes() {
		int nodes = 0;
		for (TechTreeNode t : techTree) {
			if (t == null) return nodes;
			nodes++;
		}
		return nodes;
	}
	
	private static void populateTechTreeNodes() {
		setTechTreeNode(0, new TechTreeNode(0, "Artificial Rubber", new InventoryItem[] {new Ingredient(20, 20)}, new Recipe[] {new Recipe(255, new InventoryItem[] {new Ingredient(12,1)}, new InventoryItem[] {new InventoryEntity(7, 1)})}));
		setTechTreeNode(1, new TechTreeNode(1, "Extruded Funnel", new InventoryItem[] {new Ingredient(20, 40)}, new Recipe[] {new Recipe(255 + 1, new InventoryItem[] {new Ingredient(14,2)}, new InventoryItem[] {new InventoryTile(25, 1)})}));
	}	
}
