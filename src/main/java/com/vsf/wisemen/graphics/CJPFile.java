package com.vsf.wisemen.graphics;

import com.vsf.ga.functions.Tuple;
import com.vsf.wisemen.utils.CJP;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CJPFile extends ImageFile {

    private int row;
    private int column;
    private int factor;
    private Pixel[][] matrix;
    private int[][] pastes;
    private ImageFile parent;
    public Pixel background = new Pixel(0, 0, 0);
    public String sourceFilename;


    public CJPFile(int width, int height){
        this.row = height;
        this.column = width;
        this.parent = null;
        this.factor = 1;
        this.matrix = new Pixel[width][height];
        for(int x = 0; x < width; x++)
            for(int y = 0; y< height; y++)
                this.matrix[x][y] = background;

        this.pastes = new int[width][height];
        for(int x = 0; x < width; x++)
            for(int y = 0; y< height; y++)
                this.pastes[x][y] = 0;

    }

    public CJPFile(){
        this(0,0);
    }

    public CJPFile(ImageFile parent, int factor) {
        super();
        this.parent = parent;
        this.factor = factor;
    }


    @Override
    public ImageFile getParent() {
        return this.parent;
    }

    @Override
    public Pixel getPixel(int x, int y) {
        try {
            if ((x > this.column) || (y > this.row)) return background;
            else return matrix[x][y];
        } catch (Exception e) {
            throw new NotImplementedException();
        }
    }

    public CJPFile getRectangle(int x0, int y0, int x1, int y1) {
        if (x1 >= this.column) x1 = this.column - 1;
        if (y1 >= this.row) y1 = this.row - 1;
        if ((x0 > x1) || (y0 > y1))
            throw new NotImplementedException();
        CJPFile cjp = new CJPFile(null, 1);
        cjp.column = x1 - x0 + 1;
        cjp.row = y1 - y0 + 1;
        cjp.matrix = new Pixel[cjp.column][cjp.row];
        for (int i = 0; i < cjp.column; i++) {
            System.arraycopy(this.matrix[i + x0], y0, cjp.matrix[i], 0, cjp.row);
        }

        return cjp;

    }


    public ArrayList<Pixel> getRectanglePixels(int x0, int y0, int x1, int y1){
        ArrayList<Pixel> answer = new ArrayList<Pixel>();
        if (x1 >= this.column) x1 = this.column-1;
        if (y1 >= this.row) y1 = this.row-1;
        if ((x0 > x1) || (y0 > y1)) throw new NotImplementedException();
        for (int i = 0; i<=x1-x0; i++)
        {
            for (int j = 0; j <= y1-y0; j++){
                answer.add(matrix[x0+i][y0+j]);
            }
        }
        return answer;

    }

    public static Pixel AveragePixel(List<Pixel> pixels) {
        int allred = 0;
        int allblue = 0;
        int allgreen = 0;
        for (Pixel pixel : pixels) {
            allred += pixel.r;
            allgreen += pixel.g;
            allblue += pixel.b;

        }
        //System.out.println("size of pixels: "+pixels.size());
        return new Pixel(allred / pixels.size(), allgreen / pixels.size(), allblue / pixels.size());
    }

    @Override
    public ImageFile subsample(int factor) {
        CJPFile subcjp = new CJPFile(this, factor);
        subcjp.parent = this;
        subcjp.sourceFilename = sourceFilename;
        subcjp.column = (int) Math.ceil(this.column / (factor + 0.0));
        subcjp.row = (int) Math.ceil(this.row / (factor + 0.0));
        subcjp.matrix = new Pixel[subcjp.column][subcjp.row];
        for (int i = 0; i < subcjp.column; i++) {
            for (int j = 0; j < subcjp.row; j++) {
                //System.out.println("getRectagle in subsample: i:"+i+" j:"+j+" factor: "+factor);
                subcjp.matrix[i][j] = AveragePixel(this.getRectanglePixels(i * factor, j * factor, i * factor + (factor - 1), j * factor + (factor - 1)));
            }
        }
        return subcjp;
    }

    @Override
    public List<Pixel> getPixelsFromPyramid(int x, int y, int stepsback) {
        if (parent == null) return new ArrayList<Pixel>() {{
            add(getPixel(x, y));
        }};
        if (stepsback == 0) return new ArrayList<Pixel>() {{
            add(getPixel(x, y));
        }};
        List<Pixel> answer = new ArrayList<Pixel>();
        for (int i = x * factor; i < x * factor + (factor); i++) {
            for (int j = y * factor; j < y * factor + (factor); j++) {
                answer.addAll(this.parent.getPixelsFromPyramid(i, j, stepsback - 1));
            }
        }
        return answer;
    }

    @Override
    public int getWidth() {
        return this.column;
    }

    @Override
    public int getHeight() {
        return this.row;
    }

    @Override
    public ImageFile read(String filename) {
        this.sourceFilename = filename;
        CJP cjp = new CJP();
        cjp.read(filename);
        this.row = cjp.height;
        this.column = cjp.width;
        this.matrix = new Pixel[column][row];
        List<String> raw = new ArrayList<>();
        for (int i = 0; i < cjp.lines.size(); i++) {
            raw.addAll(Arrays.asList(cjp.lines.get(i).split(" ")));
        }
        int cnt = 0;
        String curString;
        System.out.println(raw.size());
        System.out.println(this.column * this.row);
        if (raw.size() != this.column * this.row) throw new NotImplementedException();
        for (int j = 0; j < this.row; j++) {
            for (int i = 0; i < this.column; i++) {
                curString = raw.get(cnt);
                int r = Integer.parseInt(curString.substring(0, 2), 16);
                int g = Integer.parseInt(curString.substring(2, 4), 16);
                int b = Integer.parseInt(curString.substring(4, 6), 16);
                this.matrix[i][j] = new Pixel(r, g, b);
                cnt++;
            }
        }
        this.parent = null;
        this.factor = 1;
        return this;
    }

    @Override
    public void write(String filename) {
        File cjpFile = new File(filename);
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(cjpFile));
            writer.write(Integer.toString(this.column));
            writer.newLine();
            writer.write(Integer.toString(this.row));
            writer.newLine();
            for (int j = 0; j < this.row; j++) {
                for (int i = 0; i < this.column; i++) {
                    writer.write(this.matrix[i][j].toHex() + " ");
                }
                writer.newLine();
            }
            writer.close();

        } catch (Exception ignored) {

        }
    }

    public void paste(int x, int y, CJPFile img){
        for (int i = 0; i < img.column; i++) {
            for (int j = 0; j < img.row; j++) {
                this.matrix[x+i][y+j] = img.matrix[i][j];
                if (!this.matrix[x+i][y+j].equals(img.matrix[i][j])){
                    pastes[x+i][y+j] += 1;
                }
            }
        }
    }

    @Override
    public Tuple<Integer, Double> overlapPercentage(){
        int atLeast1 = 0;
        int moreThan1 = 0;
        for(int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if(this.pastes[x][y] != 0){
                    if (this.pastes[x][y] >= 1) atLeast1 ++;
                    if (this.pastes[x][y] >= 2) moreThan1++;
                }
            }
        }
        double overlap = (atLeast1 == 0) ? 0:  (moreThan1 / atLeast1);
        return new Tuple<>(moreThan1, overlap);
    }

    // TODO: Fix Parameter Flips
    @Override
    public void compose(int tx, int ty, CJPFile compositeFile, int sx, int sy, int width, int height){
        CJPFile rectangle = compositeFile.getRectangle(sx,sy,sx+width-1,sy+height-1);
        this.paste(tx, ty, rectangle);
    }


    private Color[] getColorLine(int idx) {
        Color[] colours = new Color[this.getWidth()];
        for(int i = 0; i < this.getWidth(); i++) {
            Pixel p = getPixel(i, idx);
            colours[i] = new Color(p.r, p.g, p.b);
        }
        return colours;
    }


    public void saveAsPNG(String pngFilename) {
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), 1);
        int[] colData = new int[this.getWidth() * this.getHeight()];

        for(int e = 0; e < this.getHeight(); ++e) {
            Color[] coloursInLine = this.getColorLine(e);

            for(int i = 0; i < coloursInLine.length; ++i) {
                colData[e * this.getWidth() + i] = coloursInLine[i].getRed() << 16 | coloursInLine[i].getGreen() << 8 | coloursInLine[i].getBlue();
            }
        }

        image.setRGB(0, 0, this.getWidth(), this.getHeight(), colData, 0, this.getWidth());

        try {
            ImageIO.write(image, "PNG", new File(pngFilename));
        } catch (IOException var7) {
            var7.printStackTrace();
        }

    }

    public CLGFile quantize(int maxColours){
        return null;
    }

    public static void main(String[] args) {


        System.out.println("!!!!!!!!!!!!!!!!beda!!!!!!!!!!!!!!!!");
        CJPFile cjp = new CJPFile();
        cjp.read("C:\\Users\\james\\Desktop\\GOC\\pikachu.cjp");
        CJPFile rectangleToPrint = cjp.getRectangle(100, 100, 200, 205);
        rectangleToPrint.write("C:\\Users\\james\\Desktop\\GOC\\rectangleToPrint.cjp");
        cjp.compose(0,0,rectangleToPrint,0,0,100,100);
        cjp.write("C:\\Users\\james\\Desktop\\GOC\\PikaShift.cjp");
        System.out.println("Pixel (4,7) is: " + cjp.getPixel(100, 100).toString());
        List<Pixel> rectanglePixels = cjp.getRectanglePixels(100, 100, 200, 205);

        for (int i = 0; i < rectanglePixels.size(); i++) {
            System.out.println("Pixel " + i + " of rectanglePixels: " + rectanglePixels.get(i).toString());
        }
        System.out.println("Average Pixel: " + AveragePixel(rectanglePixels).toString());

        ImageFile cjpsub20 = cjp.subsample(12);
        cjpsub20.write("C:\\Users\\james\\Desktop\\GOC\\subsample12.cjp");
        System.out.println("Generated subsample12.cjp file");
        List<Pixel> pyramidPixels = cjpsub20.getPixelsFromPyramid(0, 0, 1);
        //for (int i = 0; i < pyramidPixels.size(); i++){
        //    System.out.println("Pixel "+i+" of pyramidPixels: "+pyramidPixels.get(i).toString());
        //}


    }

}