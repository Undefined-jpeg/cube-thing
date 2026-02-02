public class Inventory {
    public ItemStack[] items = new ItemStack[32];
    public ItemStack draggingItem = null; // Object instead of int

    public Inventory() {
        // Start with some items for testing
        // Give 64 of everything in Hotbar
        for(int i=0; i<8; i++) {
             // Slots 0-7 get blocks 0-7, count 64
             if (BlockData.blocks.containsKey(i)) {
                 items[i] = new ItemStack(i, 64);
             }
        }
        // Give Torches (8) and Doors (9)
        items[8] = new ItemStack(8, 64); // Slot 8 (Main inv)
        items[9] = new ItemStack(9, 64); 
    }

    public boolean add(int type) {
        // 1. STACKING PASS: Look for existing stacks < 64
        for(int i=0; i<32; i++) {
            if (items[i] != null && items[i].type == type && items[i].count < 64) {
                items[i].count++;
                return true;
            }
        }
        // 2. EMPTY SLOT PASS: Find first null slot
        for(int i=0; i<32; i++) {
            if (items[i] == null) {
                items[i] = new ItemStack(type, 1);
                return true;
            }
        }
        return false; // Full
    }

    public void swap(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 32) return;
        ItemStack temp = items[slotIndex];
        items[slotIndex] = draggingItem;
        draggingItem = temp;
    }
    
    // Helper to remove 1 item (for placing/dropping)
    public void consume(int slotIndex) {
        if (items[slotIndex] != null) {
            items[slotIndex].count--;
            if (items[slotIndex].count <= 0) {
                items[slotIndex] = null;
            }
        }
    }
}