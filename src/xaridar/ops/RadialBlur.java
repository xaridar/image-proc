//package xaridar.ops;
//
//import xaridar.FileInfo;
//import xaridar.PixelColor;
//import xaridar.args.*;
//
//import java.io.IOException;
//import java.util.List;
//
//public class RadialBlur implements ImageOperation {
//    @Override
//    public String getName() {
//        return "radialblur";
//    }
//
//    @Override
//    public ParamsList<?> getParams() {
//        return new ParamsList<>(
//                new ParamsList.Param("samples", "s", List.of(new NumberParam(4, true))),
//                new ParamsList.Param("blur-distance", "d", List.of(new NumberParam()))
//        );
//    }
//
//    @Override
//    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
//        int samples = (int) args.get("samples");
//        double dist = (double) args.get("blur-distance");
//        return OpUtil.multiprocess(images, image -> {
//            int[][] out = new int[image.getWidth()][image.getHeight()];
//            double[] center = new double[]{ image.getWidth() / 2f - 0.5f, image.getHeight() / 2f - 0.5f };
//            for (int x = 0; x < image.getWidth(); x++) {
//                for (int y = 0; y < image.getHeight(); y++) {
////                    double distFromCenterX = x - center[0] / image.getWidth();
////                    double distFromCenterY = y - center[1] / image.getHeight();
////                    double angle = Math.atan2(distFromCenterY, distFromCenterX);
////                    double[] vals = new double[]{0,0,0,0};
////                    for (int i = -samples / 2; i < samples / 2; i++) {
////                        double currDistMod = ((double) (i + samples / 2) / samples) * dist;
////                        double newDistFromCenterX = currDistMod + distFromCenterX;
////                        double newDistFromCenterY = currDistMod + distFromCenterY;
////
////                        double yComp = newDistFromCenterY * Math.sin(angle);
////                        double xComp = newDistFromCenterX * Math.cos(angle);
////                        int xPixel = OpUtil.clamp((int) (x + Math.round(xComp)), 0, image.getWidth() - 1);
////                        int yPixel = OpUtil.clamp((int) (y + Math.round(yComp)), 0, image.getHeight() - 1);
////                        PixelColor col = PixelColor.fromRGBA(image.data[xPixel][yPixel]);
////                        vals[0] += (double) col.getRed() / samples;
////                        vals[1] += (double) col.getGreen() / samples;
////                        vals[2] += (double) col.getBlue() / samples;
////                        vals[3] += (double) col.getAlpha() / samples;
////                    }
////                    PixelColor outColor = new PixelColor(vals);
////                    out[x][y] = outColor.toRGBA();
//
//                }
//            }
//            return image.withData(out);
//        });
//    }
//}
