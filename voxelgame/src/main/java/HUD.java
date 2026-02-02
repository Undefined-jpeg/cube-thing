import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;

public class HUD {
    private Matrix4f projection = new Matrix4f();
    private Matrix4f model = new Matrix4f();
    
    private int slotSize = 40;
    private int gap = 5;
    
    public void render(Shader shader, Inventory inv, int selectedSlot, int width, int height, boolean isOpen, float mouseX, float mouseY) {
        glDisable(GL_DEPTH_TEST);
        projection.identity().ortho(0, width, 0, height, -1, 1);
        shader.setUniform("projection", projection);
        shader.setUniform("view", new Matrix4f().identity());

        int hotbarY = 20;
        int invY = 100;
        int startX = (width / 2) - ((slotSize + gap) * 8 / 2);

        // --- 1. RENDER HOTBAR ---
        for(int i=0; i<8; i++) {
            // FIX: Unwrap the ItemStack to get the integer ID
            ItemStack stack = inv.items[i];
            int typeID = (stack != null) ? stack.type : -1;
            
            drawSlot(shader, startX + i*(slotSize+gap), hotbarY, typeID, i==selectedSlot);
        }

        // --- 2. RENDER MAIN INVENTORY ---
        if (isOpen) {
            for(int i=0; i<24; i++) {
                int slotIdx = 8 + i;
                
                // FIX: Unwrap the ItemStack
                ItemStack stack = inv.items[slotIdx];
                int typeID = (stack != null) ? stack.type : -1;

                int col = i % 8;
                int row = i / 8;
                int yPos = invY + (2 - row) * (slotSize + gap);
                
                drawSlot(shader, startX + col*(slotSize+gap), yPos, typeID, false);
            }
        }

        // --- 3. DRAGGED ITEM ---
        // FIX: Check for null, not -1
        if (inv.draggingItem != null) {
            float glMouseY = height - mouseY;
            // FIX: Use draggingItem.type
            drawItem(shader, (int)mouseX - 20, (int)glMouseY - 20, 40, 40, inv.draggingItem.type);
        }

        // --- 4. CROSSHAIR ---
        if (!isOpen) {
             drawRect(shader, width/2 - 2, height/2 - 10, 4, 20, 5, 1, 1, 1, 0.8f);
             drawRect(shader, width/2 - 10, height/2 - 2, 20, 4, 5, 1, 1, 1, 0.8f);
        }
        glEnable(GL_DEPTH_TEST);
    }

    private void drawSlot(Shader s, int x, int y, int itemType, boolean selected) {
        float r = selected ? 1.0f : 0.3f;
        float g = selected ? 1.0f : 0.3f;
        float b = selected ? 1.0f : 0.3f;
        float alpha = selected ? 0.8f : 0.5f;

        // Draw Slot Background
        drawRect(s, x, y, slotSize, slotSize, 5, r, g, b, alpha);

        // Draw Item Icon
        if (itemType != -1) {
            drawItem(s, x + 4, y + 4, slotSize - 8, slotSize - 8, itemType);
        }
    }
    
    private void drawItem(Shader s, int x, int y, int w, int h, int itemType) {
        drawRect(s, x, y, w, h, itemType, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawRect(Shader s, int x, int y, int w, int h, int texIndex, float r, float g, float b, float a) {
        model.identity().translate(x, y, 0).scale(w, h, 1);
        s.setUniform("model", model);
        s.setUniform("texOffset", texIndex * 0.125f);
        s.setUniform("colorTint", r, g, b, a);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }
}