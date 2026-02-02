import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryUtil.NULL;

public class VoxelEngine {
    private long window;
    private static final int WIDTH = 1000, HEIGHT = 800;
    
    private List<Vector4f> blocks = new ArrayList<>();
    private List<ItemEntity> droppedItems = new ArrayList<>();
    
    private Map<Vector3f, Boolean> doorStates = new HashMap<>();

    private Inventory inventory = new Inventory();
    private boolean isInventoryOpen = false;
    private int selectedSlot = 0; 
    
    private boolean thirdPerson = false;
    private float lastF5 = 0.0f;

    public void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Minecraft Clone", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create window");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        
        // Floor
        for(int x = -8; x < 8; x++) {
            for(int z = -8; z < 8; z++) blocks.add(new Vector4f(x, -2, z, 0));
        }
    }

    private void loop() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.5f, 0.8f, 1.0f, 1.0f); // Sky Blue

        Texture atlas = new Texture();
        Shader shader = new Shader();
        HUD hud = new HUD();
        Camera camera = new Camera(window);
        PlayerRenderer playerRenderer = new PlayerRenderer();

        // Mesh (Cube) with Face IDs (0=Side, 1=Top, 2=Bottom)
        float[] vertices = {
            // BACK (Side)
            -0.5f,-0.5f,-0.5f, 0,0, 0,  0.5f,-0.5f,-0.5f, 1,0, 0,  0.5f, 0.5f,-0.5f, 1,1, 0,  0.5f, 0.5f,-0.5f, 1,1, 0, -0.5f, 0.5f,-0.5f, 0,1, 0, -0.5f,-0.5f,-0.5f, 0,0, 0,
            // FRONT (Side)
            -0.5f,-0.5f, 0.5f, 0,0, 0,  0.5f,-0.5f, 0.5f, 1,0, 0,  0.5f, 0.5f, 0.5f, 1,1, 0,  0.5f, 0.5f, 0.5f, 1,1, 0, -0.5f, 0.5f, 0.5f, 0,1, 0, -0.5f,-0.5f, 0.5f, 0,0, 0,
            // LEFT (Side)
            -0.5f, 0.5f, 0.5f, 1,0, 0, -0.5f, 0.5f,-0.5f, 1,1, 0, -0.5f,-0.5f,-0.5f, 0,1, 0, -0.5f,-0.5f,-0.5f, 0,1, 0, -0.5f,-0.5f, 0.5f, 0,0, 0, -0.5f, 0.5f, 0.5f, 1,0, 0,
            // RIGHT (Side)
             0.5f, 0.5f, 0.5f, 1,0, 0,  0.5f, 0.5f,-0.5f, 1,1, 0,  0.5f,-0.5f,-0.5f, 0,1, 0,  0.5f,-0.5f,-0.5f, 0,1, 0,  0.5f,-0.5f, 0.5f, 0,0, 0,  0.5f, 0.5f, 0.5f, 1,0, 0,
            // BOTTOM
            -0.5f,-0.5f,-0.5f, 0,1, 2,  0.5f,-0.5f,-0.5f, 1,1, 2,  0.5f,-0.5f, 0.5f, 1,0, 2,  0.5f,-0.5f, 0.5f, 1,0, 2, -0.5f,-0.5f, 0.5f, 0,0, 2, -0.5f,-0.5f,-0.5f, 0,1, 2,
            // TOP
            -0.5f, 0.5f,-0.5f, 0,1, 1,  0.5f, 0.5f,-0.5f, 1,1, 1,  0.5f, 0.5f, 0.5f, 1,0, 1,  0.5f, 0.5f, 0.5f, 1,0, 1, -0.5f, 0.5f, 0.5f, 0,0, 1, -0.5f, 0.5f,-0.5f, 0,1, 1
        };

        int vao = glGenVertexArrays();
        int vbo = org.lwjgl.opengl.GL15.glGenBuffers();
        glBindVertexArray(vao);
        org.lwjgl.opengl.GL15.glBindBuffer(GL_ARRAY_BUFFER, vbo);
        org.lwjgl.opengl.GL15.glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0); glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES); glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * Float.BYTES, 5 * Float.BYTES); glEnableVertexAttribArray(2);

        Matrix4f model = new Matrix4f();
        float lastFrame = 0.0f, lastClick = 0.0f, lastToggle = 0.0f, lastDrop = 0.0f;

        while (!glfwWindowShouldClose(window)) {
            float time = (float)glfwGetTime();
            float delta = time - lastFrame;
            lastFrame = time;

            String title = String.format("Pos: %d %d %d", (int)camera.position.x, (int)camera.position.y, (int)camera.position.z);
            glfwSetWindowTitle(window, title);

            if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS && time - lastToggle > 0.3f) {
                isInventoryOpen = !isInventoryOpen;
                glfwSetInputMode(window, GLFW_CURSOR, isInventoryOpen ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_DISABLED);
                lastToggle = time;
            }
            if (glfwGetKey(window, GLFW_KEY_F5) == GLFW_PRESS && time - lastF5 > 0.3f) {
                thirdPerson = !thirdPerson;
                lastF5 = time;
            }

            if (!isInventoryOpen && glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS && time - lastDrop > 0.2f) {
                ItemStack stack = inventory.items[selectedSlot];
                if (stack != null) { 
                    inventory.consume(selectedSlot);
                    Vector3f dropPos = new Vector3f(camera.position).add(0, -0.5f, 0);
                    Vector3f dropVel = camera.getDirection().mul(5.0f);
                    droppedItems.add(new ItemEntity(dropPos, dropVel, stack.type));
                }
                lastDrop = time;
            }

            if (!isInventoryOpen) {
                camera.update(window, delta, blocks);
                for(int i=0; i<8; i++) if(glfwGetKey(window, GLFW_KEY_1 + i) == GLFW_PRESS) selectedSlot = i;
                
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS && time - lastClick > 0.2f) { 
                    Vector3f pos = raycast(camera, true);
                    Vector3f hitBlock = raycast(camera, false);

                    if (hitBlock != null) {
                        for(Vector4f b : blocks) {
                            if (b.w == 9 && (int)b.x == (int)hitBlock.x && (int)b.y == (int)hitBlock.y && (int)b.z == (int)hitBlock.z) {
                                toggleDoor(b.x, b.y, b.z, !doorStates.getOrDefault(new Vector3f(b.x, b.y, b.z), false));
                                lastClick = time;
                                break;
                            }
                        }
                    }

                    if (time - lastClick > 0.2f && pos != null) {
                        // COLLISION CHECK
                        boolean collides = false;
                        if (Math.abs(pos.x - camera.position.x) < 0.7f &&
                            Math.abs(pos.z - camera.position.z) < 0.7f &&
                            pos.y > camera.position.y - 2.0f && pos.y < camera.position.y + 0.5f) {
                            collides = true;
                        }

                        if (!collides) {
                            ItemStack stack = inventory.items[selectedSlot];
                            if (stack != null) {
                                if (stack.type == 9) {
                                    blocks.add(new Vector4f(pos.x, pos.y, pos.z, 9));
                                    blocks.add(new Vector4f(pos.x, pos.y + 1, pos.z, 9));
                                    doorStates.put(new Vector3f(pos.x, pos.y, pos.z), false);
                                    doorStates.put(new Vector3f(pos.x, pos.y + 1, pos.z), false);
                                    inventory.consume(selectedSlot);
                                } else {
                                    blocks.add(new Vector4f(pos, stack.type));
                                    inventory.consume(selectedSlot);
                                }
                            }
                        }
                    }
                    lastClick = time;
                }
                
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS && time - lastClick > 0.2f) { 
                    Vector3f pos = raycast(camera, false);
                    if (pos != null) {
                        for(int i=0; i<blocks.size(); i++) {
                             Vector4f b = blocks.get(i);
                             if((int)b.x == (int)pos.x && (int)b.y == (int)pos.y && (int)b.z == (int)pos.z) {
                                 droppedItems.add(new ItemEntity(new Vector3f(b.x, b.y, b.z), new Vector3f(0,2,0), (int)b.w));
                                 if (b.w == 9) { // Door half breakage
                                     for(int j=0; j<blocks.size(); j++) {
                                         Vector4f o = blocks.get(j);
                                         if (i!=j && o.w == 9 && (int)o.x == (int)b.x && (int)o.z == (int)b.z && Math.abs((int)o.y - (int)b.y) == 1) {
                                             blocks.remove(j);
                                             doorStates.remove(new Vector3f(o.x, o.y, o.z));
                                             if (j < i) i--;
                                             break;
                                         }
                                     }
                                 }
                                 blocks.remove(i); 
                                 doorStates.remove(new Vector3f(b.x, b.y, b.z));
                                 break; 
                             }
                        }
                    }
                    lastClick = time;
                }
            } else {
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS && time - lastClick > 0.2f) {
                    double[] mx = new double[1], my = new double[1];
                    glfwGetCursorPos(window, mx, my);
                    int slot = getSlotAtMouse((float)mx[0], (float)my[0], WIDTH, HEIGHT);
                    inventory.swap(slot);
                    lastClick = time;
                }
            }

            Iterator<ItemEntity> it = droppedItems.iterator();
            while(it.hasNext()) {
                ItemEntity item = it.next();
                item.update(delta, blocks);
                if (item.pickupDelay <= 0 && item.position.distance(camera.position) < 2.0f) {
                    if (inventory.add(item.type)) it.remove(); 
                }
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            shader.bind();
            atlas.bind();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                Matrix4f view = camera.getViewMatrix();
                if(thirdPerson) view.translate(0, 0, -4.0f);
                shader.setUniform("projection", new Matrix4f().perspective((float)Math.toRadians(110.0f), (float)WIDTH/HEIGHT, 0.1f, 100.0f));
                shader.setUniform("view", view);

                glBindVertexArray(vao);
                
                for(Vector4f b : blocks) {
                    int id = (int)b.w;
                    BlockData.Block info = BlockData.get(id);
                    float brightness = 0.7f;
                    if (id == 8) brightness = 1.0f; 
                    else {
                        for(Vector4f check : blocks) {
                            if (check.w == 8) {
                                float dist = new Vector3f(b.x, b.y, b.z).distance(new Vector3f(check.x, check.y, check.z));
                                if (dist < 6.0f) { brightness = 1.0f; break; }
                            }
                        }
                    }
                    shader.setUniform("colorTint", brightness, brightness, brightness, 1.0f);

                    model.identity().translate(b.x, b.y, b.z);
                    if (id == 9) {
                         if (doorStates.getOrDefault(new Vector3f(b.x, b.y, b.z), false)) {
                             model.translate(-0.45f, 0, 0.45f).rotate((float)Math.toRadians(90), 0, 1, 0);
                         }
                         model.scale(0.15f, 1.0f, 1.0f);
                    }
                    if (id == 8) model.scale(0.2f, 0.8f, 0.2f);

                    shader.setUniform("model", model);
                    shader.setUniform("texSide", (float)info.texSide);
                    shader.setUniform("texTop", (float)info.texTop);
                    shader.setUniform("texBottom", (float)info.texBottom);
                    glDrawArrays(GL_TRIANGLES, 0, 36);
                }

                shader.setUniform("colorTint", 1.0f, 1.0f, 1.0f, 1.0f);
                for(ItemEntity item : droppedItems) {
                    BlockData.Block info = BlockData.get(item.type);
                    model.identity().translate(item.position).rotate((float)glfwGetTime(), 0, 1, 0).scale(0.25f);
                    shader.setUniform("model", model);
                    shader.setUniform("texSide", (float)info.texSide);
                    shader.setUniform("texTop", (float)info.texTop);
                    shader.setUniform("texBottom", (float)info.texBottom);
                    glDrawArrays(GL_TRIANGLES, 0, 36);
                }

                if (thirdPerson) {
                    boolean isWalking = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
                    playerRenderer.render(shader, camera.position.x, camera.position.y - 1.0f, camera.position.z, camera.getYaw(), isWalking);
                }

                if (!isInventoryOpen && !thirdPerson) {
                    ItemStack held = inventory.items[selectedSlot];
                    if (held != null) {
                        BlockData.Block info = BlockData.get(held.type);
                        glClear(GL_DEPTH_BUFFER_BIT); 
                        model.identity().translate(0.6f, -0.7f, -1.0f).rotate((float)Math.toRadians(35), 0, 1, 0).scale(0.4f);
                        shader.setUniform("model", model);
                        shader.setUniform("view", new Matrix4f().identity());
                        shader.setUniform("texSide", (float)info.texSide);
                        shader.setUniform("texTop", (float)info.texTop);
                        shader.setUniform("texBottom", (float)info.texBottom);
                        glDrawArrays(GL_TRIANGLES, 0, 36);
                    }
                }

                double[] mx = new double[1], my = new double[1];
                glfwGetCursorPos(window, mx, my);
                hud.render(shader, inventory, selectedSlot, WIDTH, HEIGHT, isInventoryOpen, (float)mx[0], (float)my[0]);
            }
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void toggleDoor(float x, float y, float z, boolean open) {
        for(Vector4f b : blocks) {
            if (b.w == 9 && (int)b.x == (int)x && (int)b.z == (int)z && Math.abs((int)b.y - (int)y) <= 1) {
                doorStates.put(new Vector3f(b.x, b.y, b.z), open);
            }
        }
    }

    private int getSlotAtMouse(float mx, float my, int width, int height) {
        float invY = height - my;
        int slotSize = 40, gap = 5;
        int startX = (width / 2) - ((slotSize + gap) * 8 / 2);
        
        if (invY >= 20 && invY <= 20+slotSize) {
            int col = (int)(mx - startX) / (slotSize+gap);
            if (col >= 0 && col < 8) return col;
        }
        if (invY >= 100 && invY <= 100 + (slotSize+gap)*3) {
            int col = (int)(mx - startX) / (slotSize+gap);
            int row = (int)(invY - 100) / (slotSize+gap);
            if (col >= 0 && col < 8 && row >= 0 && row < 3) return 8 + ((2-row) * 8) + col;
        }
        return -1;
    }
    
    private Vector3f raycast(Camera cam, boolean placing) {
        Vector3f rayStart = new Vector3f(cam.position);
        Vector3f rayDir = cam.getDirection();
        for(float d = 0; d < 6.0f; d += 0.05f) {
            Vector3f point = new Vector3f(rayStart).add(new Vector3f(rayDir).mul(d));
            int bx = Math.round(point.x), by = Math.round(point.y), bz = Math.round(point.z);
            for(Vector4f block : blocks) {
                if((int)block.x == bx && (int)block.y == by && (int)block.z == bz) {
                    if (placing) {
                         Vector3f prev = new Vector3f(rayStart).add(new Vector3f(rayDir).mul(d - 0.05f));
                         return new Vector3f(Math.round(prev.x), Math.round(prev.y), Math.round(prev.z));
                    }
                    return new Vector3f(block.x, block.y, block.z);
                }
            }
        }
        return null;
    }
    
    public static void main(String[] args) { new VoxelEngine().run(); }
}
