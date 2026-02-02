import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;

public class Camera {
    public Vector3f position = new Vector3f(0, 5, 0);
    public Vector3f front = new Vector3f(0, 0, -1);
    public Vector3f up = new Vector3f(0, 1, 0);

    public float verticalVelocity = 0.0f;
    private float gravity = -20.0f;
    private float jumpStrength = 7.0f;
    
    private float playerWidth = 0.3f;
    private float playerHeight = 1.8f;

    private float yaw = -90.0f;
    private float pitch = 0.0f;
    private boolean firstMouse = true;
    private double lastX, lastY;

    public Camera(long window) {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }
    
    // THIS IS THE NEW METHOD YOU NEEDED
    public float getYaw() {
        return yaw;
    }

    public void update(long window, float deltaTime, List<Vector4f> blocks) {
        // 1. ROTATION
        double[] xpos = new double[1]; double[] ypos = new double[1];
        glfwGetCursorPos(window, xpos, ypos);
        if (firstMouse) { lastX = xpos[0]; lastY = ypos[0]; firstMouse = false; }
        
        float xoffset = (float)(xpos[0] - lastX) * 0.1f;
        float yoffset = (float)(lastY - ypos[0]) * 0.1f;
        lastX = xpos[0]; lastY = ypos[0];

        yaw += xoffset; pitch += yoffset;
        if (pitch > 89.0f) pitch = 89.0f; else if (pitch < -89.0f) pitch = -89.0f;

        front.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.normalize();

        // 2. MOVEMENT (With Collision)
        float speed = 5.0f * deltaTime;
        Vector3f forwardFlat = new Vector3f(front.x, 0, front.z).normalize();
        Vector3f rightFlat = new Vector3f(front).cross(up).normalize();
        Vector3f moveDir = new Vector3f(0,0,0);

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) moveDir.add(forwardFlat);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) moveDir.sub(forwardFlat);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) moveDir.sub(rightFlat);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) moveDir.add(rightFlat);
        
        if (moveDir.length() > 0) moveDir.normalize().mul(speed);

        // Move X and check collision
        position.x += moveDir.x;
        if(checkCollision(blocks)) position.x -= moveDir.x;

        // Move Z and check collision
        position.z += moveDir.z;
        if(checkCollision(blocks)) position.z -= moveDir.z;

        // 3. GRAVITY & JUMP
        verticalVelocity += gravity * deltaTime;
        position.y += verticalVelocity * deltaTime;
        
        if (checkCollision(blocks)) {
            position.y -= verticalVelocity * deltaTime;
            verticalVelocity = 0;
            if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                verticalVelocity = jumpStrength;
            }
        }
    }
    
    private boolean checkCollision(List<Vector4f> blocks) {
        for(Vector4f block : blocks) {
            boolean xOverlap = position.x + playerWidth > block.x - 0.5f && position.x - playerWidth < block.x + 0.5f;
            boolean zOverlap = position.z + playerWidth > block.z - 0.5f && position.z - playerWidth < block.z + 0.5f;
            boolean yOverlap = position.y > block.y - 0.5f && (position.y - playerHeight) < block.y + 0.5f;

            if (xOverlap && yOverlap && zOverlap) return true;
        }
        return false;
    }

    public Vector3f getDirection() { return new Vector3f(front); }
}