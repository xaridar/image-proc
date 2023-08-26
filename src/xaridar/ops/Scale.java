package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Scale implements ImageOperation {
    @Override
    public String getName() {
        return "scale";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new UnnamedArgsInfo("dimensions", new NumberParam(1, true), 1, 2, "width and height in pixels, or just one if a square is desired, of the number of pixels to scale all input images to"),
                new ParamsList.Param("keep-aspect-ratio", "k", Collections.emptyList(), true, "true to keep the aspect ratio of input; false to instead specify 1 dimension for a square output (only if fill-mode is set to 'stretch')"),
                new ParamsList.Param("calculate-short", "c", Collections.emptyList(), true, "only valid when one dimension is provided; true to specify this dimension as the long dimension, false to leave as width"),
                new ParamsList.Param("fill-mode", "f", List.of(new EnumParam("crop-start", "crop-center", "crop-end", "stretch", "start", "center", "end")), List.of("stretch"), "defines the fill mode: crop-start, crop-center, crop-end, stretch, start, center, or end")
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        List<Integer> dimensions = ((List<Integer>) args.get("dimensions"));
        boolean keepAspectRatio = args.exists("keep-aspect-ratio");
        boolean calculateShort = args.exists("calculate-short");
        String fillMode = (String) args.get("fill-mode");

        if (calculateShort && dimensions.size() != 1) throw new ArgError("scale: --calculate-short flag can only be used if one dimension is specified");
        if (keepAspectRatio && !fillMode.equals("stretch")) throw new ArgError("scale: only 'stretch' fill mode can be used when the aspect ratio is maintained using --keep-aspect-ratio");
        return OpUtil.multiprocess(images, image -> {
            int outW, outH, w, h, startX = 0, startY = 0;
            float ar = (float) image.getWidth() / image.getHeight();
            if (dimensions.size() == 2) {
                outW = dimensions.get(0);
                outH = dimensions.get(1);
            } else if (!keepAspectRatio) {
                outW = outH = dimensions.get(0);
            } else {
                if (!calculateShort || image.getWidth() >= image.getHeight()) {
                    outW = dimensions.get(0);
                    outH = (int) (1 / (ar / outW));
                } else {
                    outH = dimensions.get(0);
                    outW = (int) (outH * ar);
                }
            }

            switch (fillMode) {
                case "stretch":
                    w = outW;
                    h = outH;
                    break;
                case "crop-start":
                    if (outW > outH) {
                        w = outW;
                        h = (int) (1 / (ar / w));
                    } else {
                        h = outH;
                        w = (int) (h * ar);
                    }
                    break;
                case "crop-center":
                    if (outW > outH) {
                        w = outW;
                        h = (int) (1 / (ar / w));
                        startX = 0;
                        startY = (h - outH) / 2;
                    } else {
                        h = outH;
                        w = (int) (h * ar);
                        startX = (w - outW) / 2;
                    }
                    break;
                case "crop-end":
                    if (outW > outH) {
                        w = outW;
                        h = (int) (1 / (ar / w));
                        startY = (h - outH);
                    } else {
                        h = outH;
                        w = (int) (h * ar);
                        startX = (w - outW);
                    }
                    break;
                case "start":
                    if (image.getWidth() < image.getHeight()) {
                        h = outH;
                        w = (int) (outH * ar);
                    } else if (image.getWidth() == image.getHeight()) {
                        w = h = Math.min(outW, outH);
                    } else {
                        w = outW;
                        h = (int) (1 / (ar / outW));
                    }
                    break;
                case "center":
                    if (image.getWidth() < image.getHeight()) {
                        h = outH;
                        w = (int) (outH * ar);
                        startX = (outW - w) / 2;
                    } else if (image.getWidth() == image.getHeight()) {
                        w = h = Math.min(outW, outH);
                        if (outW < outH) startY = (outH - h) / 2;
                        else startX = (outW - w) / 2;
                    } else {
                        w = outW;
                        h = (int) (1 / (ar / outW));
                        startY = (outH - h) / 2;
                    }
                    break;
                case "end":
                    if (image.getWidth() < image.getHeight()) {
                        h = outH;
                        w = (int) (outH * ar);
                        startX = (w - outW);
                    } else if (image.getWidth() == image.getHeight()) {
                        w = h = Math.min(outW, outH);
                        if (outW < outH) startY = (outH - h);
                        else startX = (outW - w);
                    } else {
                        w = outW;
                        h = (int) (1 / (ar / outW));
                        startY = (h - outH);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + fillMode);
            }

            int[][] out = new int[outW][outH];
            BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    img.setRGB(x, y, image.data[x][y]);
                }
            }
            Image newImg = img.getScaledInstance(w, h, 0);
            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            scaled.getGraphics().drawImage(newImg, 0, 0, null);

            for (int y = 0; y < outH; y++) {
                for (int x = 0; x < outW; x++) {
                    int imageX = x, imageY = y;
                    if (fillMode.startsWith("crop")) {
                        imageX += startX;
                        imageY += startY;
                    } else if (!fillMode.equals("stretch")) {
                        imageX -= startX;
                        imageY -= startY;
                    }
                    if (imageX < 0 || imageX >= w || imageY < 0 || imageY >= h) out[x][y] = PixelColor.transparent().toRGBA();
                    else out[x][y] = scaled.getRGB(imageX, imageY);
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "scales all input images to a specified dimension, either by stretching the image, placing the image in a larger frame, or cropping the image to fit";
    }
}
