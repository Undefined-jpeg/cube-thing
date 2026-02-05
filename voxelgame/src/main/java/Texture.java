import java.nio.ByteBuffer;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

public class Texture {
    private int id;
    
    public Texture() {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        int blockSize = 16;
        int numSlots = 16;
        int width = blockSize * numSlots;
        int height = blockSize;    
        ByteBuffer atlasBuffer = BufferUtils.createByteBuffer(width * height * 4);

        // Fill with black/transparent initially
        for (int i = 0; i < atlasBuffer.capacity(); i++) atlasBuffer.put(i, (byte)0);

        String[] textures = {
            "grass-top.png",    // 0
            "grass-side.png",   // 1
            "dirt.png",         // 2
            "log-side.png",     // 3
            "log-top.png",      // 4
            "planks.png",       // 5
            "stone.png",        // 6
            "stone1.png",       // 7
            "glass.png",        // 8
            "torch.png",        // 9
            "placeholder.png",  // 10
            "stone1.png"        // 11 (TNT)
        };

        for (int i = 0; i < textures.length; i++) {
            loadToAtlas(textures[i], atlasBuffer, i, blockSize, width);
        }

        atlasBuffer.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, atlasBuffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    private void loadToAtlas(String fileName, ByteBuffer atlas, int slot, int blockSize, int atlasWidth) {
        try {
            InputStream is = getClass().getResourceAsStream("/textures/" + fileName);
            if (is == null) {
                System.err.println("Texture not found: " + fileName);
                return;
            }

            ByteBuffer encoded = BufferUtils.createByteBuffer(16 * 1024);
            try (ReadableByteChannel rbc = Channels.newChannel(is)) {
                while (rbc.read(encoded) != -1) {
                    if (encoded.remaining() == 0) {
                        ByteBuffer newBuffer = BufferUtils.createByteBuffer(encoded.capacity() * 2);
                        encoded.flip();
                        newBuffer.put(encoded);
                        encoded = newBuffer;
                    }
                }
            }
            encoded.flip();

            int[] w = {0}, h = {0}, comp = {0};
            ByteBuffer image = STBImage.stbi_load_from_memory(encoded, w, h, comp, 4);
            if (image == null) {
                System.err.println("Failed to load image: " + fileName + " - " + STBImage.stbi_failure_reason());
                return;
            }

            for (int y = 0; y < Math.min(blockSize, h[0]); y++) {
                for (int x = 0; x < Math.min(blockSize, w[0]); x++) {
                    int atlasX = (slot * blockSize) + x;
                    int atlasY = y;
                    int atlasIdx = (atlasY * atlasWidth + atlasX) * 4;
                    int imgIdx = (y * w[0] + x) * 4;
                    
                    atlas.put(atlasIdx, image.get(imgIdx));
                    atlas.put(atlasIdx + 1, image.get(imgIdx + 1));
                    atlas.put(atlasIdx + 2, image.get(imgIdx + 2));
                    atlas.put(atlasIdx + 3, image.get(imgIdx + 3));
                }
            }
            STBImage.stbi_image_free(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bind() { glBindTexture(GL_TEXTURE_2D, id); }
}
