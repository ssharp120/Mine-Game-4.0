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
				for (int i = 0; i < in.length; i++) {
					if (in[i] != null) {
						if (in[i].getItemID() == j.getItemID()) {
							if (in[i].getClass() == Ingredient.class) {
								if (!inputSatisfied) inputSatisfied = ((Ingredient) in[i]).getQuantity() >= ((Ingredient) j).getQuantity();
							} else if (in[i].getClass() == InventoryTile.class) {
								if (!inputSatisfied) inputSatisfied = ((InventoryTile) in[i]).getQuantity() >= ((InventoryTile) j).getQuantity();
							} else if (in[i].getClass() == InventoryTool.class) {
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
	
	public InventoryItem getInput(int inputIndex) {
		// Item get? //
		return inputs[inputIndex];
	}
	
	public InventoryItem getOutput(int outputIndex) {
		// Item get! //
		InventoryItem outputItem;
		try {
			outputItem = (InventoryItem) outputs[outputIndex].clone();
			return outputItem;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return new Ingredient(0, 1);
		}
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
