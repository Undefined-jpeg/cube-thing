import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

public class PlayerRenderer {
    private Matrix4f model = new Matrix4f();

    public void render(Shader shader, float x, float y, float z, float rotY, boolean walking) {
        // Animation: Swing legs/arms if walking
        float time = (float)org.lwjgl.glfw.GLFW.glfwGetTime();
        float swing = walking ? (float)Math.sin(time * 10) * 20.0f : 0.0f;

        // 1. HEAD (Use Texture 3 - Planks as "Face")
        drawBox(shader, x, y, z, 0.5f, 0.5f, 0.5f, 0, rotY, 3);

        // 2. BODY (Use Texture 5 - Stone as "Shirt")
        drawBox(shader, x, y - 0.75f, z, 0.5f, 1.0f, 0.25f, 0, rotY, 5);

        // 3. ARMS
        // Right Arm (Swings opposite to Left Leg)
        drawLimb(shader, x, y - 0.75f, z, 0.25f, 1.0f, 0.25f, swing, rotY, 0.4f, 1);
        // Left Arm
        drawLimb(shader, x, y - 0.75f, z, 0.25f, 1.0f, 0.25f, -swing, rotY, -0.4f, 1);

        // 4. LEGS
        // Right Leg
        drawLimb(shader, x, y - 1.75f, z, 0.25f, 1.0f, 0.25f, -swing, rotY, 0.15f, 4);
        // Left Leg
        drawLimb(shader, x, y - 1.75f, z, 0.25f, 1.0f, 0.25f, swing, rotY, -0.15f, 4);
    }

    private void drawLimb(Shader s, float x, float y, float z, float w, float h, float d, float rotX, float rotY, float offsetX, int tex) {
        model.identity().translate(x, y, z)
             .rotate((float)Math.toRadians(-rotY), 0, 1, 0) // Match body rotation
             .translate(offsetX, 0.25f, 0) // Move to shoulder/hip pivot
             .rotate((float)Math.toRadians(rotX), 1, 0, 0) // Swing
             .translate(0, -0.5f, 0) // Move geometry down so it pivots at top
             .scale(w, h, d);
        
        s.setUniform("model", model);
        s.setUniform("texOffset", tex * 0.125f);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    private void drawBox(Shader s, float x, float y, float z, float w, float h, float d, float rotX, float rotY, int tex) {
        model.identity().translate(x, y, z)
             .rotate((float)Math.toRadians(-rotY), 0, 1, 0)
             .scale(w, h, d);
        s.setUniform("model", model);
        s.setUniform("texOffset", tex * 0.125f);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }
}