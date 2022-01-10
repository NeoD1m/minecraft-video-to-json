import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {
        String inPath = "src/main/java/in/";
        String outPath = "src/main/java/out/";
        SafetyStuff safetyStuff = new SafetyStuff();

        float luminanceThreshold = 0.4f;

        List<int[][]> listOfFrames = new ArrayList<>();
        List<int[][]> listOfDifferences = new ArrayList<>();

        int baseHeight = 0;
        int baseWidth = 0;

        File file = new File(inPath);
        boolean isFirstTime = true;
        for (final File fileEntry : Objects.requireNonNull(file.listFiles())) {
            BufferedImage pic = ImageIO.read(new File(fileEntry.getAbsolutePath()));
            if (isFirstTime) {
                baseHeight = pic.getHeight();
                baseWidth = pic.getWidth();
                isFirstTime = false;
            }
            safetyStuff.checkResolution(baseHeight,baseWidth,pic,fileEntry);

            int[][] frame = new int[baseWidth][baseHeight];
            for (int x=0; x< pic.getWidth();x++){
                for(int y=0;y<pic.getHeight();y++){
                    int color = pic.getRGB(x, y);

                    // extract each color component
                    int red   = (color >>> 16) & 0xFF;
                    int green = (color >>>  8) & 0xFF;
                    int blue  = (color >>>  0) & 0xFF;

                    // calc luminance in range 0.0 to 1.0; using SRGB luminance constants
                    float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
                    // rotate the image 90 degrees
                    if (luminance >= luminanceThreshold) frame[y][x] = 1; else frame[y][x] = 0;

                }
            }
            listOfFrames.add(frame);
        }



        for(int[][] frame : listOfFrames){
            int[][] finalFrame = new int[baseWidth][baseHeight];
            for (int x=0; x < frame.length - 1;x++) {
                for (int y = 0; y < frame[0].length - 1; y++) {
                    if (frame[x][y] != frame[x+1][y+1]) finalFrame[x][y] = 1; else finalFrame[x][y] = 0;
                }
            }
            listOfDifferences.add(finalFrame);
        }


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter myWriter = new FileWriter(outPath+"video.json");
        String category = gson.toJson(listOfDifferences);
        myWriter.write(category);
        myWriter.close();

        // true russian
        boolean точкаОстановки = true;
    }
}

class SafetyStuff{

    public void checkResolution(int baseHeight,int baseWidth,BufferedImage pic,File fileEntry) throws IOException{
        if (baseHeight != pic.getHeight() || baseWidth != pic.getWidth())
            System.out.println("[ WRONG RESOLUTION IDIOT ] expected height is: " +
                    baseHeight + " found: " + pic.getHeight() + " expected width is: " + baseWidth + " found: " + pic.getWidth() + " at " + fileEntry.getName());
    }
}