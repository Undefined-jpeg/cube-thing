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
        public float toughness;
        public boolean isTNT;

        public Block(int id, String name, boolean trans, boolean solid, boolean light, int s, int t, int b, float toughness) {
            this.id = id;
            this.name = name;
            this.isTransparent = trans;
            this.isSolid = solid;
            this.isLightSource = light;
            this.texSide = s;
            this.texTop = t;
            this.texBottom = b;
            this.toughness = toughness;
            this.isTNT = false;
        }
    }

    public static Map<Integer, Block> blocks = new HashMap<>();

    static {
        // ID, Name, Trans, Solid, Light, SideTex, TopTex, BottomTex, Toughness
        register(0, "Grass",       false, true, false, 1, 0, 2, 0.6f);
        register(1, "Dirt",        false, true, false, 2, 2, 2, 0.5f);
        register(2, "Oak Log",     false, true, false, 3, 4, 4, 2.0f);
        register(3, "Oak Planks",  false, true, false, 5, 5, 5, 2.0f);
        register(4, "Cobblestone", false, true, false, 7, 7, 7, 6.0f);
        register(5, "Stone",       false, true, false, 6, 6, 6, 6.0f);
        register(6, "Leaves",      true,  true, false, 10, 10, 10, 0.2f);
        register(7, "Glass",       true,  true, false, 8, 8, 8, 0.3f);
        register(8, "Torch",       true,  false, true, 9, 9, 9, 0.0f);
        register(9, "Door",        true,  true,  false, 10, 10, 10, 3.0f);

        // NEW BLOCKS
        register(10, "TNT",        false, true, false, 11, 11, 11, 0.0f); // TNT slot 11
        blocks.get(10).isTNT = true;
        register(11, "Reinforced Planks", false, true, false, 10, 10, 10, 100.0f); // Placeholder texture
    }

    public static void register(int id, String name, boolean trans, boolean solid, boolean light, int s, int t, int b, float toughness) {
        blocks.put(id, new Block(id, name, trans, solid, light, s, t, b, toughness));
    }

    public static Block get(int id) {
        return blocks.getOrDefault(id, blocks.get(0));
    }
}
