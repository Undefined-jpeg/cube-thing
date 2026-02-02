import java.util.HashMap;
import java.util.Map;

public class BlockData {
    public static class Block {
        public int id;
        public String name;
        public boolean isTransparent; 
        public boolean isSolid;       
        public boolean isLightSource; // New: Does it glow?

        public Block(int id, String name, boolean trans, boolean solid, boolean light) {
            this.id = id;
            this.name = name;
            this.isTransparent = trans;
            this.isSolid = solid;
            this.isLightSource = light;
        }
    }

    public static Map<Integer, Block> blocks = new HashMap<>();

    static {
        register(0, "Grass",       false, true, false);
        register(1, "Dirt",        false, true, false);
        register(2, "Oak Log",     false, true, false);
        register(3, "Oak Planks",  false, true, false);
        register(4, "Cobblestone", false, true, false);
        register(5, "Stone",       false, true, false);
        register(6, "Leaves",      true,  true, false);
        register(7, "Glass",       true,  true, false);
        // NEW BLOCKS
        register(8, "Torch",       true,  false, true); // Not solid (walk through), Glows
        register(9, "Door",        true,  true,  false); // Transparent (for shape), Solid
    }

    public static void register(int id, String name, boolean trans, boolean solid, boolean light) {
        blocks.put(id, new Block(id, name, trans, solid, light));
    }

    public static Block get(int id) {
        return blocks.getOrDefault(id, blocks.get(0));
    }
}