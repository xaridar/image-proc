//package xaridar.ops;
//
//import xaridar.FileInfo;
//import xaridar.args.*;
//
//import java.io.IOException;
//import java.util.List;
//
//public class GaussianNoise implements ImageOperation {
//    @Override
//    public String getName() {
//        return "gaussian-noise";
//    }
//
//    @Override
//    public ParamsList<?> getParams() {
//        return new ParamsList<>(
//                new ParamsList.Param("mean", "m", List.of(new NumberParam(0))),
//                new ParamsList.Param("standard-deviation", "sd", List.of(new NumberParam(0))));
//    }
//
//    @Override
//    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
//        return null;
//    }
//}
