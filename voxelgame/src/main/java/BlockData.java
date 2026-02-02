import java.util.HashMap;
import java.util.Map;

public class BlockData {
    public static class Block {
        public int id;
        public String name;
        public boolean isTransparent; 
        public boolean isSolid;       
        public boolean isLightSource;
        public int texSide, texTop, texBottom;

        public Block(int id, String name, boolean trans, boolean solid, boolean light, int s, int t, int b) {
            this.id = id;
            this.name = name;
            this.isTransparent = trans;
            this.isSolid = solid;
            this.isLightSource = light;
            this.texSide = s;
            this.texTop = t;
            this.texBottom = b;
        }
    }

    public static Map<Integer, Block> blocks = new HashMap<>();

    static {
        // ID, Name, Trans, Solid, Light, SideTex, TopTex, BottomTex
        register(0, "Grass",       false, true, false, 1, 0, 2);
        register(1, "Dirt",        false, true, false, 2, 2, 2);
        register(2, "Oak Log",     false, true, false, 3, 4, 4);
        register(3, "Oak Planks",  false, true, false, 5, 5, 5);
        register(4, "Cobblestone", false, true, false, 7, 7, 7);
        register(5, "Stone",       false, true, false, 6, 6, 6);
        register(6, "Leaves",      true,  true, false, 10, 10, 10);
        register(7, "Glass",       true,  true, false, 8, 8, 8);
        register(8, "Torch",       true,  false, true, 9, 9, 9);
        register(9, "Door",        true,  true,  false, 10, 10, 10);
    }

    public static void register(int id, String name, boolean trans, boolean solid, boolean light, int s, int t, int b) {
        blocks.put(id, new Block(id, name, trans, solid, light, s, t, b));
    }

    public static Block get(int id) {
        return blocks.getOrDefault(id, blocks.get(0));
    }
}
