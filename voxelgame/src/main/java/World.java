import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.*;

public class World {
    // Optimized storage: (x, z) -> (y -> type)
    private Map<Long, Map<Integer, Integer>> columns = new HashMap<>();
    private Map<Vector3i, Integer> metadata = new HashMap<>();
    // Heightmap for shadows: (x, z) -> highest Y
    private Map<Long, Integer> heightMap = new HashMap<>();

    public void setBlock(int x, int y, int z, int type) {
        setBlock(x, y, z, type, 0);
    }

    public void setBlock(int x, int y, int z, int type, int meta) {
        long xzKey = hashXZ(x, z);
        Vector3i pos = new Vector3i(x, y, z);

        if (type == -1) {
            Map<Integer, Integer> column = columns.get(xzKey);
            if (column != null) {
                column.remove(y);
                if (column.isEmpty()) columns.remove(xzKey);
            }
            metadata.remove(pos);
            updateHeightMap(x, z);
        } else {
            columns.computeIfAbsent(xzKey, k -> new HashMap<>()).put(y, type);
            metadata.put(pos, meta);
            if (!BlockData.get(type).isTransparent) {
                updateHeightMap(x, z, y);
            } else {
                updateHeightMap(x, z);
            }
        }
    }

    public int getBlock(int x, int y, int z) {
        Map<Integer, Integer> column = columns.get(hashXZ(x, z));
        if (column == null) return -1;
        return column.getOrDefault(y, -1);
    }

    public int getMeta(int x, int y, int z) {
        return metadata.getOrDefault(new Vector3i(x, y, z), 0);
    }

    private void updateHeightMap(int x, int z, int newY) {
        long key = hashXZ(x, z);
        int currentMax = heightMap.getOrDefault(key, -100);
        if (newY > currentMax) {
            heightMap.put(key, newY);
        }
    }

    private void updateHeightMap(int x, int z) {
        long key = hashXZ(x, z);
        int max = -100;
        Map<Integer, Integer> column = columns.get(key);
        if (column != null) {
            for (Map.Entry<Integer, Integer> entry : column.entrySet()) {
                if (!BlockData.get(entry.getValue()).isTransparent) {
                    if (entry.getKey() > max) max = entry.getKey();
                }
            }
        }
        heightMap.put(key, max);
    }

    private long hashXZ(int x, int z) {
        return (((long)x) << 32) | (z & 0xffffffffL);
    }

    public boolean isOpaque(int x, int y, int z) {
        int id = getBlock(x, y, z);
        if (id == -1) return false;
        return !BlockData.get(id).isTransparent;
    }

    public boolean isLit(int x, int y, int z) {
        return y >= heightMap.getOrDefault(hashXZ(x, z), -100);
    }

    public Map<Vector3i, Integer> getBlockMap() {
        Map<Vector3i, Integer> fullMap = new HashMap<>();
        for (Map.Entry<Long, Map<Integer, Integer>> colEntry : columns.entrySet()) {
            long key = colEntry.getKey();
            int x = (int)(key >> 32);
            int z = (int)key;
            for (Map.Entry<Integer, Integer> yEntry : colEntry.getValue().entrySet()) {
                fullMap.put(new Vector3i(x, yEntry.getKey(), z), yEntry.getValue());
            }
        }
        return fullMap;
    }
}
