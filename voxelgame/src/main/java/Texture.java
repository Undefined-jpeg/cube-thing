import java.nio.ByteBuffer;

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
        int width = blockSize * 16; // Increased width for more blocks
        int height = blockSize;    
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte r=0, g=0, b=0, a=(byte)255;
                
                int blockType = x / blockSize; 
                int px = x % blockSize; int py = y % blockSize;
                int noise = (int)(Math.random() * 15); 

                switch (blockType) {
                    case 0: r=(byte)(30+noise); g=(byte)(160+noise); b=(byte)(30+noise); break; // Grass
                    case 1: r=(byte)(80+noise); g=(byte)(50+noise); b=(byte)(20+noise); break; // Dirt
                    case 2: r=(byte)60; g=(byte)40; b=(byte)10; break; // Log
                    case 3: r=(byte)180; g=(byte)130; b=(byte)70; break; // Planks
                    case 4: r=(byte)100; g=(byte)100; b=(byte)100; break; // Cobble
                    case 5: r=(byte)120; g=(byte)120; b=(byte)120; break; // Stone
                    case 6: if((px+py)%3!=0){r=30;g=120;b=30;}else{a=0;} break; // Leaves
                    case 7: r=(byte)220; g=(byte)240; b=(byte)255; a=(byte)100; // Glass
                            if(px==0||py==0) a=(byte)255; break;
                    
                    case 8: // TORCH (Yellow center, wood stick)
                        if (px > 6 && px < 10 && py < 10) { // Stick
                            r=(byte)100; g=(byte)60; b=(byte)20;
                        } else if (px > 6 && px < 10 && py >= 10) { // Flame
                            r=(byte)255; g=(byte)200; b=(byte)50;
                        } else {
                            a = 0; // Transparent air around torch
                        }
                        break;
                        
                    case 9: // DOOR (Wood with window)
                        if (px < 2 || px > 13 || py < 1 || py > 14) { // Frame
                            r=(byte)100; g=(byte)60; b=(byte)20;
                        } else if (py > 9 && px > 4 && px < 11) { // Window
                            r=(byte)150; g=(byte)200; b=(byte)255; a=(byte)150;
                        } else { // Door panel
                            r=(byte)140; g=(byte)90; b=(byte)40;
                        }
                        break;
                }
                buffer.put(r).put(g).put(b).put(a);
            }
        }
        buffer.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }
    public void bind() { glBindTexture(GL_TEXTURE_2D, id); }
}