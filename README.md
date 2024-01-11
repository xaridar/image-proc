# ImageProc

ImageProc is a image processing pipeline toolbox built for the parallel processing of large numbers of images. It allows a user to provide a single command to specify image input and output, and applies the desired operations.

These operations may be applied sequentially after preceding operations, or at the same time through parallel processing. Through several [pipeline functions](#pipeline-functions), complex trees of image processing can be defined and executed without any additional input from the user.

The speed of some operations defined in ImageProc is slow, but the purpose of the tool is to allow a single input from a user to fully define complex image processing tasks of many inputs.

ImageProc is executed by following the executable filename with whatever arguments are desired; eg:

```
$ java -jar image-proc read test.jpg $ brightness 50 $ write test2.jpg
```

Individual operations and their usage are specified within the program, and can be listed by using `java -jar image-proc.jar help`. These operations are specified modularly, and new operations can be added very easily using the framework provided.

## Pipeline Functions

Pipeline functions are as follows:

-   `$` - Separates operations to be processed on all inputs in sequence.
-   `;` - Separates operations to be processed in parallel on the same input, with each parallel chain producing separate outputs.
-   `~` - Returns both the output of the previous operation and that same output after the following operation has been applied.
-   `+` - Used to separate n operations, where some integer multiple m of n inputs are provided; m inputs will be passed to each operation to be output.
-   `()` - Parentheses can be used to specify precedence and operation grouping/nesting.
