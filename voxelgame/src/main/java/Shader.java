import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import org.lwjgl.system.MemoryStack;

public class Shader {
    public int programId;

    private final String vertexSource = "#version 330 core\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "layout (location = 1) in vec2 aTexCoord;\n" +
            "layout (location = 2) in float aFace;\n" +
            "out vec2 TexCoord;\n" +
            "uniform mat4 model;\n" +
            "uniform mat4 view;\n" +
            "uniform mat4 projection;\n" +
            "uniform float texSide;\n" +
            "uniform float texTop;\n" +
            "uniform float texBottom;\n" +
            "void main() {\n" +
            "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
            "    float index = texSide;\n" +
            "    if (aFace > 0.5) index = texTop;\n" +
            "    if (aFace > 1.5) index = texBottom;\n" +
            "    TexCoord = vec2((aTexCoord.x + index) * 0.0625, aTexCoord.y);\n" +
            "}";

    private final String fragmentSource = "#version 330 core\n" +
            "out vec4 FragColor;\n" +
            "in vec2 TexCoord;\n" +
            "uniform sampler2D ourTexture;\n" +
            "uniform vec4 colorTint;\n" +
            "void main() {\n" +
            "    vec4 texColor = texture(ourTexture, TexCoord);\n" +
            "    if(texColor.a < 0.1) discard;\n" +
            "    FragColor = texColor * colorTint;\n" +
            "}";

    public Shader() {
        int v = glCreateShader(GL_VERTEX_SHADER); glShaderSource(v, vertexSource); glCompileShader(v);
        checkCompile(v);
        int f = glCreateShader(GL_FRAGMENT_SHADER); glShaderSource(f, fragmentSource); glCompileShader(f);
        checkCompile(f);
        programId = glCreateProgram(); glAttachShader(programId, v); glAttachShader(programId, f); glLinkProgram(programId);
    }

    public void bind() { glUseProgram(programId); }

    public void setUniform(String name, Matrix4f value) {
        int loc = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(loc, false, value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String name, float val) { glUniform1f(glGetUniformLocation(programId, name), val); }
    public void setUniform(String name, float r, float g, float b, float a) { glUniform4f(glGetUniformLocation(programId, name), r, g, b, a); }

    private void checkCompile(int shader) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) 
            throw new RuntimeException("Shader Error: " + glGetShaderInfoLog(shader));
    }
}
