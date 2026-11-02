package se233.project2.view;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import se233.project2.exception.AssetLoadException;

import java.util.ArrayDeque;
import java.util.Deque;

public class SpriteSheet {

    private final Image sourceImage;
    private final int bgR;
    private final int bgG;
    private final int bgB;
    private final int tolerance;

    public SpriteSheet(Image sourceImage) {
        this(sourceImage, 34);
    }

    public SpriteSheet(Image sourceImage, int tolerance) {
        if (sourceImage == null || sourceImage.isError()) {
            throw new AssetLoadException("SpriteSheet", "Source image for SpriteSheet is null or invalid");
        }
        this.sourceImage = sourceImage;
        this.tolerance = tolerance;

        PixelReader reader = sourceImage.getPixelReader();
        int argb = reader.getArgb(5, 5);
        this.bgR = (argb >> 16) & 0xFF;
        this.bgG = (argb >> 8) & 0xFF;
        this.bgB = argb & 0xFF;
    }


    public WritableImage getSprite(int x, int y, int width, int height) {
        int pad = 2;
        int imgW = (int) sourceImage.getWidth();
        int imgH = (int) sourceImage.getHeight();

        int sx = Math.max(0, x - pad);
        int sy = Math.max(0, y - pad);
        int ex = Math.min(imgW, x + width + pad);
        int ey = Math.min(imgH, y + height + pad);
        int w = ex - sx;
        int h = ey - sy;
        if (w <= 0 || h <= 0) {
            throw new AssetLoadException("SpriteSheet", "Invalid crop dimensions: " + width + "x" + height);
        }

        PixelReader reader = sourceImage.getPixelReader();
        int[][] argbGrid = new int[h][w];
        boolean[][] isBg = new boolean[h][w];
        Deque<int[]> queue = new ArrayDeque<>();

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                argbGrid[py][px] = reader.getArgb(sx + px, sy + py);
                if (px == 0 || py == 0 || px == w - 1 || py == h - 1) {
                    if (matchesBackground(argbGrid[py][px], tolerance + 8)) {
                        isBg[py][px] = true;
                        queue.add(new int[]{px, py});
                    }
                }
            }
        }

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cx = cur[0];
            int cy = cur[1];
            for (int d = 0; d < 4; d++) {
                int nx = cx + dx[d];
                int ny = cy + dy[d];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !isBg[ny][nx]) {
                    if (matchesBackground(argbGrid[ny][nx], tolerance)) {
                        isBg[ny][nx] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }

        int outXOffset = x - sx;
        int outYOffset = y - sy;
        int outW = Math.min(width, w - outXOffset);
        int outH = Math.min(height, h - outYOffset);

        WritableImage out = new WritableImage(outW, outH);
        PixelWriter writer = out.getPixelWriter();
        for (int py = 0; py < outH; py++) {
            for (int px = 0; px < outW; px++) {
                int gx = px + outXOffset;
                int gy = py + outYOffset;
                if (isBg[gy][gx]) {
                    writer.setColor(px, py, Color.TRANSPARENT);
                } else {
                    writer.setArgb(px, py, argbGrid[gy][gx] | 0xFF000000);
                }
            }
        }
        return out;
    }

    //use chroma key, balls,laser,bricks
    public WritableImage getParticleSprite(int x, int y, int width, int height) {
        int imgW = (int) sourceImage.getWidth();
        int imgH = (int) sourceImage.getHeight();
        int sx = Math.max(0, Math.min(x, imgW - 1));
        int sy = Math.max(0, Math.min(y, imgH - 1));
        int w = Math.min(width, imgW - sx);
        int h = Math.min(height, imgH - sy);

        PixelReader reader = sourceImage.getPixelReader();
        WritableImage out = new WritableImage(w, h);
        PixelWriter writer = out.getPixelWriter();

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int argb = reader.getArgb(sx + px, sy + py);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int maxDiff = Math.max(Math.abs(r - bgR), Math.max(Math.abs(g - bgG), Math.abs(b - bgB)));
                int saturation = Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));

                if (maxDiff <= tolerance && saturation <= 22) {
                    writer.setColor(px, py, Color.TRANSPARENT);
                } else if (maxDiff <= tolerance + 14 && saturation <= 28) {
                    int alpha = Math.min(255, Math.max(0, (maxDiff - tolerance) * 18));
                    writer.setArgb(px, py, (alpha << 24) | (r << 16) | (g << 8) | b);
                } else {
                    writer.setArgb(px, py, 0xFF000000 | (r << 16) | (g << 8) | b);
                }
            }
        }
        return out;
    }

    private boolean matchesBackground(int argb, int tol) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int maxDiff = Math.max(Math.abs(r - bgR), Math.max(Math.abs(g - bgG), Math.abs(b - bgB)));
        int saturation = Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));
        return maxDiff <= tol && saturation <= 24;
    }

    public Image getSourceImage() {
        return sourceImage;
    }
}
