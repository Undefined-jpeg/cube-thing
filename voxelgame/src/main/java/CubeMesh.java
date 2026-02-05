import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class CubeMesh {
    private static int vao = -1, vbo = -1;

    public static void draw(Shader shader, float texSide, float texTop, float texBottom) {
        if (vao == -1) init();

        shader.setUniform("texSide", texSide);
        shader.setUniform("texTop", texTop);
        shader.setUniform("texBottom", texBottom);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    private static void init() {
        float[] vertices = {
            // Front
            -0.5f,-0.5f, 0.5f, 0,0, 0, 1.0f,  0.5f,-0.5f, 0.5f, 1,0, 0, 1.0f,  0.5f, 0.5f, 0.5f, 1,1, 0, 1.0f,
             0.5f, 0.5f, 0.5f, 1,1, 0, 1.0f, -0.5f, 0.5f, 0.5f, 0,1, 0, 1.0f, -0.5f,-0.5f, 0.5f, 0,0, 0, 1.0f,
            // Back
             0.5f,-0.5f,-0.5f, 0,0, 0, 1.0f, -0.5f,-0.5f,-0.5f, 1,0, 0, 1.0f, -0.5f, 0.5f,-0.5f, 1,1, 0, 1.0f,
            -0.5f, 0.5f,-0.5f, 1,1, 0, 1.0f,  0.5f, 0.5f,-0.5f, 0,1, 0, 1.0f,  0.5f,-0.5f,-0.5f, 0,0, 0, 1.0f,
            // Left
            -0.5f,-0.5f,-0.5f, 0,0, 0, 1.0f, -0.5f,-0.5f, 0.5f, 1,0, 0, 1.0f, -0.5f, 0.5f, 0.5f, 1,1, 0, 1.0f,
            -0.5f, 0.5f, 0.5f, 1,1, 0, 1.0f, -0.5f, 0.5f,-0.5f, 0,1, 0, 1.0f, -0.5f,-0.5f,-0.5f, 0,0, 0, 1.0f,
            // Right
             0.5f,-0.5f, 0.5f, 0,0, 0, 1.0f,  0.5f,-0.5f,-0.5f, 1,0, 0, 1.0f,  0.5f, 0.5f,-0.5f, 1,1, 0, 1.0f,
             0.5f, 0.5f,-0.5f, 1,1, 0, 1.0f,  0.5f, 0.5f, 0.5f, 0,1, 0, 1.0f,  0.5f,-0.5f, 0.5f, 0,0, 0, 1.0f,
            // Bottom
            -0.5f,-0.5f,-0.5f, 0,1, 2, 1.0f,  0.5f,-0.5f,-0.5f, 1,1, 2, 1.0f,  0.5f,-0.5f, 0.5f, 1,0, 2, 1.0f,
             0.5f,-0.5f, 0.5f, 1,0, 2, 1.0f, -0.5f,-0.5f, 0.5f, 0,0, 2, 1.0f, -0.5f,-0.5f,-0.5f, 0,1, 2, 1.0f,
            // Top
            -0.5f, 0.5f, 0.5f, 0,0, 1, 1.0f,  0.5f, 0.5f, 0.5f, 1,0, 1, 1.0f,  0.5f, 0.5f,-0.5f, 1,1, 1, 1.0f,
             0.5f, 0.5f,-0.5f, 1,1, 1, 1.0f, -0.5f, 0.5f,-0.5f, 0,1, 1, 1.0f, -0.5f, 0.5f, 0.5f, 0,0, 1, 1.0f
        };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0); glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES); glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 7 * Float.BYTES, 5 * Float.BYTES); glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 7 * Float.BYTES, 6 * Float.BYTES); glEnableVertexAttribArray(3);
    }
}
