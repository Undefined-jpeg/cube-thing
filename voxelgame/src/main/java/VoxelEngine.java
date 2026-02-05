import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryUtil.NULL;

public class VoxelEngine {
    private long window;
    private static final int WIDTH = 1000, HEIGHT = 800;
    
    private World world = new World();
    private List<ItemEntity> droppedItems = new ArrayList<>();
    private Map<Vector3f, Boolean> doorStates = new HashMap<>();

    private Inventory inventory = new Inventory();
    private boolean isInventoryOpen = false;
    private int selectedSlot = 0; 
    
    private boolean thirdPerson = false;
    private float lastF5 = 0.0f;

    private int vao, vbo;
    private int vertexCount = 0;
    private boolean meshDirty = true;

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
        for(int x = -16; x < 16; x++) {
            for(int z = -16; z < 16; z++) world.setBlock(x, -2, z, 0);
        }
    }

    private void updateMesh() {
        List<Float> vertices = new ArrayList<>();
        Map<Vector3i, Integer> blocks = world.getBlockMap();

        for (Map.Entry<Vector3i, Integer> entry : blocks.entrySet()) {
            Vector3i pos = entry.getKey();
            int id = entry.getValue();
            if (id == 8 || id == 9) continue;

            BlockData.Block info = BlockData.get(id);
            addBlockToMesh(vertices, pos.x, pos.y, pos.z, info);
        }

        float[] vArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) vArray[i] = vertices.get(i);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vArray, GL_STATIC_DRAW);
        vertexCount = vertices.size() / 7;
        meshDirty = false;
    }

    private void addBlockToMesh(List<Float> v, int x, int y, int z, BlockData.Block info) {
        if (!world.isOpaque(x, y, z + 1)) addFace(v, x, y, z, 0, info.texSide, 0);
        if (!world.isOpaque(x, y, z - 1)) addFace(v, x, y, z, 1, info.texSide, 0);
        if (!world.isOpaque(x - 1, y, z)) addFace(v, x, y, z, 2, info.texSide, 0);
        if (!world.isOpaque(x + 1, y, z)) addFace(v, x, y, z, 3, info.texSide, 0);
        if (!world.isOpaque(x, y - 1, z)) addFace(v, x, y, z, 4, info.texBottom, 2);
        if (!world.isOpaque(x, y + 1, z)) addFace(v, x, y, z, 5, info.texTop, 1);
    }

    private void addFace(List<Float> v, int x, int y, int z, int face, int tex, int faceType) {
        float b = world.isLit(x, y + 1, z) ? 1.0f : 0.5f;
        if (faceType == 0) b *= 0.8f;
        if (faceType == 2) b *= 0.6f;

        float[][] faceVerts = {
            { -0.5f,-0.5f, 0.5f, 0,0,  0.5f,-0.5f, 0.5f, 1,0,  0.5f, 0.5f, 0.5f, 1,1,  0.5f, 0.5f, 0.5f, 1,1, -0.5f, 0.5f, 0.5f, 0,1, -0.5f,-0.5f, 0.5f, 0,0 },
            {  0.5f,-0.5f,-0.5f, 0,0, -0.5f,-0.5f,-0.5f, 1,0, -0.5f, 0.5f,-0.5f, 1,1, -0.5f, 0.5f,-0.5f, 1,1,  0.5f, 0.5f,-0.5f, 0,1,  0.5f,-0.5f,-0.5f, 0,0 },
            { -0.5f,-0.5f,-0.5f, 0,0, -0.5f,-0.5f, 0.5f, 1,0, -0.5f, 0.5f, 0.5f, 1,1, -0.5f, 0.5f, 0.5f, 1,1, -0.5f, 0.5f,-0.5f, 0,1, -0.5f,-0.5f,-0.5f, 0,0 },
            {  0.5f,-0.5f, 0.5f, 0,0,  0.5f,-0.5f,-0.5f, 1,0,  0.5f, 0.5f,-0.5f, 1,1,  0.5f, 0.5f,-0.5f, 1,1,  0.5f, 0.5f, 0.5f, 1,1,  0.5f,-0.5f, 0.5f, 0,0 },
            { -0.5f,-0.5f,-0.5f, 0,1,  0.5f,-0.5f,-0.5f, 1,1,  0.5f,-0.5f, 0.5f, 1,0,  0.5f,-0.5f, 0.5f, 1,0, -0.5f,-0.5f, 0.5f, 0,0, -0.5f,-0.5f,-0.5f, 0,1 },
            { -0.5f, 0.5f, 0.5f, 0,0,  0.5f, 0.5f, 0.5f, 1,0,  0.5f, 0.5f,-0.5f, 1,1,  0.5f, 0.5f,-0.5f, 1,1, -0.5f, 0.5f,-0.5f, 0,1, -0.5f, 0.5f, 0.5f, 0,0 }
        };

        float[] fv = faceVerts[face];
        for (int i = 0; i < 6; i++) {
            v.add(fv[i * 5] + x);
            v.add(fv[i * 5 + 1] + y);
            v.add(fv[i * 5 + 2] + z);
            v.add(fv[i * 5 + 3]);
            v.add(fv[i * 5 + 4]);
            v.add((float)faceType);
            v.add(b);
        }
    }

    private void loop() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

        Texture atlas = new Texture();
        Shader shader = new Shader();
        HUD hud = new HUD();
        Camera camera = new Camera(window);
        PlayerRenderer playerRenderer = new PlayerRenderer();

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0); glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES); glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 7 * Float.BYTES, 5 * Float.BYTES); glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 7 * Float.BYTES, 6 * Float.BYTES); glEnableVertexAttribArray(3);

        Matrix4f model = new Matrix4f();
        float lastFrame = 0.0f, lastClick = 0.0f, lastToggle = 0.0f, lastDrop = 0.0f;

        while (!glfwWindowShouldClose(window)) {
            float time = (float)glfwGetTime();
            float delta = time - lastFrame;
            lastFrame = time;

            if (meshDirty) updateMesh();

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

            if (!isInventoryOpen) {
                camera.update(window, delta, world);
                for(int i=0; i<8; i++) if(glfwGetKey(window, GLFW_KEY_1 + i) == GLFW_PRESS) selectedSlot = i;
                
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS && time - lastClick > 0.2f) { 
                    Vector3f pos = raycast(camera, true);
                    Vector3f hitBlock = raycast(camera, false);

                    if (hitBlock != null) {
                        int hitId = world.getBlock((int)hitBlock.x, (int)hitBlock.y, (int)hitBlock.z);
                        if (hitId == 9) {
                            toggleDoor(hitBlock.x, hitBlock.y, hitBlock.z, !doorStates.getOrDefault(hitBlock, false));
                            lastClick = time;
                        } else if (BlockData.get(hitId).isTNT) {
                            explode((int)hitBlock.x, (int)hitBlock.y, (int)hitBlock.z);
                            lastClick = time;
                        }
                    }

                    if (time - lastClick > 0.2f && pos != null) {
                        ItemStack stack = inventory.items[selectedSlot];
                        if (stack != null && !collidesWithPlayer(pos, camera.position)) {
                            if (stack.type == 9) { // Door
                                world.setBlock((int)pos.x, (int)pos.y, (int)pos.z, 9);
                                world.setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, 9);
                            } else if (stack.type == 8) { // Torch
                                int meta = 0;
                                if (hitBlock != null) {
                                    if (pos.z < hitBlock.z) meta = 1;
                                    else if (pos.z > hitBlock.z) meta = 2;
                                    else if (pos.x < hitBlock.x) meta = 3;
                                    else if (pos.x > hitBlock.x) meta = 4;
                                }
                                world.setBlock((int)pos.x, (int)pos.y, (int)pos.z, 8, meta);
                            } else {
                                world.setBlock((int)pos.x, (int)pos.y, (int)pos.z, stack.type);
                            }
                            inventory.consume(selectedSlot);
                            meshDirty = true;
                        }
                    }
                    lastClick = time;
                }
                
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS && time - lastClick > 0.2f) { 
                    Vector3f pos = raycast(camera, false);
                    if (pos != null) {
                        int type = world.getBlock((int)pos.x, (int)pos.y, (int)pos.z);
                        droppedItems.add(new ItemEntity(pos, new Vector3f(0,2,0), type));
                        world.setBlock((int)pos.x, (int)pos.y, (int)pos.z, -1);
                        meshDirty = true;
                    }
                    lastClick = time;
                }
            } else {
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS && time - lastClick > 0.2f) {
                    double[] mx = new double[1], my = new double[1];
                    glfwGetCursorPos(window, mx, my);
                    int slot = getSlotAtMouse((float)mx[0], (float)my[0], WIDTH, HEIGHT);
                    if (slot != -1) inventory.swap(slot);
                    lastClick = time;
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

                model.identity();
                shader.setUniform("model", model);
                shader.setUniform("texSide", 0.0f);
                shader.setUniform("texTop", 0.0f);
                shader.setUniform("texBottom", 0.0f);
                shader.setUniform("colorTint", 1.0f, 1.0f, 1.0f, 1.0f);
                glBindVertexArray(vao);
                glDrawArrays(GL_TRIANGLES, 0, vertexCount);

                renderSpecialBlocks(shader, world, model);

                if (thirdPerson) {
                    boolean isWalking = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
                    playerRenderer.render(shader, camera.position.x, camera.position.y - 1.0f, camera.position.z, camera.getYaw(), isWalking);
                }

                // Render Held Item (First Person)
                if (!isInventoryOpen && !thirdPerson) {
                    ItemStack held = inventory.items[selectedSlot];
                    if (held != null) {
                        BlockData.Block info = BlockData.get(held.type);
                        glClear(GL_DEPTH_BUFFER_BIT); 
                        model.identity().translate(0.6f, -0.7f, -1.0f).rotate((float)Math.toRadians(35), 0, 1, 0).scale(0.4f);
                        shader.setUniform("model", model);
                        shader.setUniform("view", new Matrix4f().identity());
                        CubeMesh.draw(shader, (float)info.texSide, (float)info.texTop, (float)info.texBottom);
                    }
                }

                // Render Dropped Items
                for(ItemEntity item : droppedItems) {
                    BlockData.Block info = BlockData.get(item.type);
                    model.identity().translate(item.position).rotate((float)glfwGetTime(), 0, 1, 0).scale(0.25f);
                    shader.setUniform("model", model);
                    CubeMesh.draw(shader, (float)info.texSide, (float)info.texTop, (float)info.texBottom);
                }

                double[] mx = new double[1], my = new double[1];
                glfwGetCursorPos(window, mx, my);
                hud.render(shader, inventory, selectedSlot, WIDTH, HEIGHT, isInventoryOpen, (float)mx[0], (float)my[0]);
            }
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void renderSpecialBlocks(Shader shader, World world, Matrix4f model) {
        Map<Vector3i, Integer> blocks = world.getBlockMap();
        for (Map.Entry<Vector3i, Integer> entry : blocks.entrySet()) {
            Vector3i pos = entry.getKey();
            int id = entry.getValue();
            if (id != 8 && id != 9) continue;

            BlockData.Block info = BlockData.get(id);
            model.identity().translate(pos.x, pos.y, pos.z);

            if (id == 8) { // TORCH
                int meta = world.getMeta(pos.x, pos.y, pos.z);
                if (meta == 1) model.translate(0, 0.2f, 0.35f).rotate((float)Math.toRadians(-20), 1, 0, 0); // North
                else if (meta == 2) model.translate(0, 0.2f, -0.35f).rotate((float)Math.toRadians(20), 1, 0, 0); // South
                else if (meta == 3) model.translate(0.35f, 0.2f, 0).rotate((float)Math.toRadians(20), 0, 0, 1); // West
                else if (meta == 4) model.translate(-0.35f, 0.2f, 0).rotate((float)Math.toRadians(-20), 0, 0, 1); // East
                model.scale(0.12f, 0.6f, 0.12f); // Slim and short
            } else if (id == 9) { // DOOR
                if (doorStates.getOrDefault(new Vector3f(pos.x, pos.y, pos.z), false)) {
                    model.translate(-0.45f, 0, 0.45f).rotate((float)Math.toRadians(90), 0, 1, 0);
                }
                model.scale(0.15f, 1.0f, 1.0f);
            }

            shader.setUniform("model", model);
            shader.setUniform("colorTint", 1.0f, 1.0f, 1.0f, 1.0f);
            CubeMesh.draw(shader, (float)info.texSide, (float)info.texTop, (float)info.texBottom);
        }
    }

    private void explode(int x, int y, int z) {
        world.setBlock(x, y, z, -1);
        int radius = 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    float dist = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (dist <= radius) {
                        int bx = x + dx, by = y + dy, bz = z + dz;
                        int id = world.getBlock(bx, by, bz);
                        if (id != -1) {
                            float toughness = BlockData.get(id).toughness;
                            if (toughness < 20.0f) {
                                world.setBlock(bx, by, bz, -1);
                            }
                        }
                    }
                }
            }
        }
        meshDirty = true;
    }

    private void toggleDoor(float x, float y, float z, boolean open) {
        doorStates.put(new Vector3f(x, y, z), open);
    }

    private int getSlotAtMouse(float mx, float my, int width, int height) {
        float glMouseY = height - my;
        int slotSize = 40, gap = 5;
        int startX = (width / 2) - ((slotSize + gap) * 8 / 2);
        
        if (glMouseY >= 20 && glMouseY <= 20+slotSize) {
            int col = (int)(mx - startX) / (slotSize+gap);
            if (col >= 0 && col < 8) return col;
        }

        int invX = (width / 2) - 150;
        int invY = (height / 2) - 150;

        if (glMouseY >= invY && glMouseY <= invY + 3 * (slotSize + gap)) {
            if (mx >= invX && mx <= invX + 8 * (slotSize + gap)) {
                int col = (int)(mx - invX) / (slotSize + gap);
                int row = (int)(glMouseY - invY) / (slotSize + gap);
                if (col >= 0 && col < 8 && row >= 0 && row < 3) return 8 + ((2-row) * 8) + col;
            }
        }

        int armorX = invX - 50;
        if (mx >= armorX && mx <= armorX + slotSize) {
            if (glMouseY >= invY && glMouseY <= invY + 4 * (slotSize + gap)) {
                int row = (int)(glMouseY - invY) / (slotSize + gap);
                if (row >= 0 && row < 4) return 32 + (3 - row);
            }
        }

        return -1;
    }

    private boolean collidesWithPlayer(Vector3f blockPos, Vector3f playerPos) {
        return Math.abs(blockPos.x - playerPos.x) < 0.7f &&
               Math.abs(blockPos.z - playerPos.z) < 0.7f &&
               blockPos.y > playerPos.y - 2.0f && blockPos.y < playerPos.y + 0.5f;
    }

    private Vector3f raycast(Camera cam, boolean placing) {
        Vector3f rayStart = new Vector3f(cam.position);
        Vector3f rayDir = cam.getDirection();
        for(float d = 0; d < 6.0f; d += 0.05f) {
            Vector3f point = new Vector3f(rayStart).add(new Vector3f(rayDir).mul(d));
            int bx = Math.round(point.x), by = Math.round(point.y), bz = Math.round(point.z);
            if (world.getBlock(bx, by, bz) != -1) {
                if (placing) {
                     Vector3f prev = new Vector3f(rayStart).add(new Vector3f(rayDir).mul(d - 0.05f));
                     return new Vector3f(Math.round(prev.x), Math.round(prev.y), Math.round(prev.z));
                }
                return new Vector3f(bx, by, bz);
            }
        }
        return null;
    }
    
    public static void main(String[] args) { new VoxelEngine().run(); }
}
