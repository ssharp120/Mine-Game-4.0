package UI;

public class Recipe {
	private InventoryItem[] inputs;
	private InventoryItem[] outputs;
	private int numInputs;
	private int numOutputs;
	private int index;
	
	
	public Recipe(int dex, InventoryItem in[], InventoryItem out[]) {
		inputs = in;
		outputs = out;
		index = dex;
		numInputs = inputs.length;
		numOutputs = outputs.length;
	}
	
	public boolean checkRecipe(InventoryItem in[]) {
		if (in.length >= numInputs) {
			for (InventoryItem j : inputs) {
				boolean inputSatisfied = false;
				for (InventoryItem i : in) {
					if (i.getItemID() == j.getItemID()) {
						if (i.getClass() == Ingredient.class) {
							if (!inputSatisfied) inputSatisfied = ((Ingredient) i).getQuantity() >= ((Ingredient) j).getQuantity();
						} else if (i.getClass() == InventoryTile.class) {
							if (!inputSatisfied) inputSatisfied = ((InventoryTile) i).getQuantity() >= ((InventoryTile) j).getQuantity();
						} else if (i.getClass() == InventoryTool.class) {
							if (!inputSatisfied) inputSatisfied = ((InventoryTool) i).getDurability() == ((InventoryTool) i).getMaxDurability();
						}
					}
				}
				if (!inputSatisfied) return false;
			}
			return true;
		}
		return false;
	}
	
	public InventoryItem getInput(int inputIndex) {
		// Item get? //
		return inputs[inputIndex];
	}
	
	public InventoryItem getOutput(int outputIndex) {
		// Item get! //
		return outputs[outputIndex];
	}
	
	public int getNumInputs() {
		return numInputs;
	}

	public int getNumOutputs() {
		return numOutputs;
	}
	
	public int getIndex() {
		return index;
	}

	public String toString() {
		String description = "Recipe " + index + ": \n\tInputs: " + numInputs + ", Outputs: " + numOutputs + "\n";
		for (InventoryItem i : inputs) {
			description += "\t\tInput: " + i.toString() + "\n";
		}
		for (InventoryItem o : outputs) {
			description += "\t\tOutput: " + o.toString() + "\n";
		}
		return description;
	}
}
