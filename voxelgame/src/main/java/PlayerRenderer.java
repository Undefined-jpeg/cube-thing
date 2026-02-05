import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

public class PlayerRenderer {
    private Matrix4f model = new Matrix4f();

    public void render(Shader shader, float x, float y, float z, float rotY, boolean walking) {
        float time = (float)org.lwjgl.glfw.GLFW.glfwGetTime();
        float swing = walking ? (float)Math.sin(time * 10) * 20.0f : 0.0f;

        // HEAD (Planks: 5)
        drawBox(shader, x, y, z, 0.5f, 0.5f, 0.5f, 0, rotY, 5);

        // BODY (Stone: 6)
        drawBox(shader, x, y - 0.75f, z, 0.5f, 1.0f, 0.25f, 0, rotY, 6);

        // ARMS (Grass-side: 1)
        drawLimb(shader, x, y - 0.75f, z, 0.25f, 1.0f, 0.25f, swing, rotY, 0.4f, 1);
        drawLimb(shader, x, y - 0.75f, z, 0.25f, 1.0f, 0.25f, -swing, rotY, -0.4f, 1);

        // LEGS (Cobble: 7)
        drawLimb(shader, x, y - 1.75f, z, 0.25f, 1.0f, 0.25f, -swing, rotY, 0.15f, 7);
        drawLimb(shader, x, y - 1.75f, z, 0.25f, 1.0f, 0.25f, swing, rotY, -0.15f, 7);
    }

    private void drawLimb(Shader s, float x, float y, float z, float w, float h, float d, float rotX, float rotY, float offsetX, int tex) {
        model.identity().translate(x, y, z)
             .rotate((float)Math.toRadians(-rotY), 0, 1, 0)
             .translate(offsetX, 0.25f, 0)
             .rotate((float)Math.toRadians(rotX), 1, 0, 0)
             .translate(0, -0.5f, 0)
             .scale(w, h, d);
        
        s.setUniform("model", model);
        CubeMesh.draw(s, (float)tex, (float)tex, (float)tex);
    }

    private void drawBox(Shader s, float x, float y, float z, float w, float h, float d, float rotX, float rotY, int tex) {
        model.identity().translate(x, y, z)
             .rotate((float)Math.toRadians(-rotY), 0, 1, 0)
             .scale(w, h, d);
        s.setUniform("model", model);
        CubeMesh.draw(s, (float)tex, (float)tex, (float)tex);
    }
}
