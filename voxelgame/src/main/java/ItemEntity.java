import java.util.List;

import org.joml.Vector3f; // This import is crucial!
import org.joml.Vector4f;    // This one too!

public class ItemEntity {
    public Vector3f position;
    public Vector3f velocity;
    public int type;
    public float pickupDelay = 1.0f;
    
    // Size of the item (it's small, 0.25 blocks wide)
    private float size = 0.25f;

    public ItemEntity(Vector3f pos, Vector3f vel, int type) {
        this.position = new Vector3f(pos);
        this.velocity = new Vector3f(vel);
        this.type = type;
    }

    // THIS is the method your error is looking for
    public void update(float delta, List<Vector4f> worldBlocks) {
        // 1. Apply Gravity
        velocity.y -= 20.0f * delta;
        if (pickupDelay > 0) pickupDelay -= delta;

        // 2. Move X and Check Collision
        position.x += velocity.x * delta;
        if (checkCollision(worldBlocks)) {
            position.x -= velocity.x * delta; // Undo move
            velocity.x *= -0.5f; // Bounce off wall slightly
        }

        // 3. Move Y and Check Collision
        position.y += velocity.y * delta;
        if (checkCollision(worldBlocks)) {
            position.y -= velocity.y * delta; // Undo move
            velocity.y = 0; // Stop falling
            
            // Friction (slow down sliding on floor)
            velocity.x *= 0.9f;
            velocity.z *= 0.9f;
        }

        // 4. Move Z and Check Collision
        position.z += velocity.z * delta;
        if (checkCollision(worldBlocks)) {
            position.z -= velocity.z * delta; // Undo move
            velocity.z *= -0.5f; // Bounce off wall
        }
    }

    // Simple AABB Collision Check
    private boolean checkCollision(List<Vector4f> blocks) {
        // Check if our small bounding box overlaps any block
        // Item Box: [pos.x - size/2] to [pos.x + size/2]
        
        for (Vector4f block : blocks) {
            // Check overlaps on all 3 axes
            boolean xOverlap = position.x + size/2 > block.x - 0.5f && position.x - size/2 < block.x + 0.5f;
            boolean yOverlap = position.y + size/2 > block.y - 0.5f && position.y - size/2 < block.y + 0.5f;
            boolean zOverlap = position.z + size/2 > block.z - 0.5f && position.z - size/2 < block.z + 0.5f;

            if (xOverlap && yOverlap && zOverlap) {
                return true; 
            }
        }
        return false;
    }
}