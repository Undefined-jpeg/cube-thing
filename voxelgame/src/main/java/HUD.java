import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class HUD {
    private Matrix4f projection = new Matrix4f();
    private Matrix4f model = new Matrix4f();
    
    private int slotSize = 40;
    private int gap = 5;
    
    private int vao, vbo;

    public HUD() {
        // 2D Quad Mesh: x, y, z, u, v, face
        float[] vertices = {
            0,0,0, 0,0, 0,  1,0,0, 1,0, 0,  1,1,0, 1,1, 0,
            1,1,0, 1,1, 0,  0,1,0, 0,1, 0,  0,0,0, 0,0, 0
        };
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0); glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES); glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * Float.BYTES, 5 * Float.BYTES); glEnableVertexAttribArray(2);
    }

    public void render(Shader shader, Inventory inv, int selectedSlot, int width, int height, boolean isOpen, float mouseX, float mouseY) {
        glDisable(GL_DEPTH_TEST);
        glBindVertexArray(vao);
        projection.identity().ortho(0, width, 0, height, -1, 1);
        shader.setUniform("projection", projection);
        shader.setUniform("view", new Matrix4f().identity());

        int hotbarY = 20;
        int invY = 100;
        int startX = (width / 2) - ((slotSize + gap) * 8 / 2);

        // --- 1. RENDER HOTBAR ---
        for(int i=0; i<8; i++) {
            ItemStack stack = inv.items[i];
            int typeID = (stack != null) ? stack.type : -1;
            drawSlot(shader, startX + i*(slotSize+gap), hotbarY, typeID, i==selectedSlot);
        }

        // --- 2. RENDER MAIN INVENTORY ---
        if (isOpen) {
            for(int i=0; i<24; i++) {
                int slotIdx = 8 + i;
                ItemStack stack = inv.items[slotIdx];
                int typeID = (stack != null) ? stack.type : -1;

                int col = i % 8;
                int row = i / 8;
                int yPos = invY + (2 - row) * (slotSize + gap);
                
                drawSlot(shader, startX + col*(slotSize+gap), yPos, typeID, false);
            }
        }

        // --- 3. DRAGGED ITEM ---
        if (inv.draggingItem != null) {
            float glMouseY = height - mouseY;
            drawItem(shader, (int)mouseX - 20, (int)glMouseY - 20, 40, 40, inv.draggingItem.type);
        }

        // --- 4. CROSSHAIR ---
        if (!isOpen) {
             drawRect(shader, width/2 - 2, height/2 - 10, 4, 20, 10, 1, 1, 1, 0.8f);
             drawRect(shader, width/2 - 10, height/2 - 2, 20, 4, 10, 1, 1, 1, 0.8f);
        }
        glEnable(GL_DEPTH_TEST);
    }

    private void drawSlot(Shader s, int x, int y, int itemType, boolean selected) {
        float r = selected ? 1.0f : 0.3f;
        float g = selected ? 1.0f : 0.3f;
        float b = selected ? 1.0f : 0.3f;
        float alpha = selected ? 0.8f : 0.5f;

        // Draw Slot Background
        drawRect(s, x, y, slotSize, slotSize, 10, r, g, b, alpha);

        // Draw Item Icon
        if (itemType != -1) {
            drawItem(s, x + 4, y + 4, slotSize - 8, slotSize - 8, itemType);
        }
    }
    
    private void drawItem(Shader s, int x, int y, int w, int h, int itemType) {
        BlockData.Block info = BlockData.get(itemType);
        drawRect(s, x, y, w, h, info.texSide, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawRect(Shader s, int x, int y, int w, int h, int texIndex, float r, float g, float b, float a) {
        model.identity().translate(x, y, 0).scale(w, h, 1);
        s.setUniform("model", model);
        s.setUniform("texSide", (float)texIndex);
        s.setUniform("texTop", (float)texIndex);
        s.setUniform("texBottom", (float)texIndex);
        s.setUniform("colorTint", r, g, b, a);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
}
