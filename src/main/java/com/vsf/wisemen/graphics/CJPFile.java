package com.vsf.wisemen.graphics;

import com.vsf.wisemen.utils.CJP;
import lombok.Data;
import org.hibernate.cfg.NotYetImplementedException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

@Data
public class CJPFile implements ImageFile {
    
    private CJPFile parent;
    private int row;
    private int column;
    private List<String> raw;
    private int factor;
    private Pixel[][] matrix;



    public CJPFile(CJPFile parent, int factor){
        this.parent = parent;
        this.factor = factor;


    }

    public CJPFile(){
    }



    @Override
    public Pixel getPixel(int x, int y){
        try {
            if ((x > this.column) || (y > this.row)) return new Pixel(0, 0, 0);
            else return matrix[x][y];
        } catch(Exception e) {
            throw new NotImplementedException();
        }
    }

    public CJPFile getRect(int x0, int y0, int x1, int y1){
        if (x1 >= this.column) x1 = this.column-1;
        if (y1 >= this.row) y1 = this.row-1;
        if ((x0 > x1) || (y0 > y1)) throw new NotImplementedException();
        CJPFile cjp = new CJPFile(null,1);
        cjp.column = x1-x0+1;
        cjp.row = y1-y0+1;
        cjp.matrix = new Pixel[cjp.column][cjp.row];
        for (int i=0; i<cjp.column; i++){
            for (int j=0; j<cjp.row; j++){
                cjp.matrix[i][j] = this.matrix[i+x0][j+y0];
            }
        }

        return cjp;

    }


    public ArrayList<Pixel> getRectangle(int x0, int y0, int x1, int y1){
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


    public static Pixel AveragePixel(List<Pixel> pixels){
        int allred = 0;
        int allblue = 0;
        int allgreen = 0;
        for (int i = 0; i < pixels.size(); i++)
        {
            allred += pixels.get(i).r;
            allgreen += pixels.get(i).g;
            allblue += pixels.get(i).b;;
        }
        return new Pixel(allred/pixels.size(),allgreen/pixels.size(),allblue/pixels.size());
    }

    @Override
    public CJPFile subsample(int factor){
        CJPFile subcjp = new CJPFile(this,factor);
        subcjp.parent = this;
        subcjp.column = (int)Math.ceil(this.column/(factor+0.0));
        subcjp.row = (int)Math.ceil(this.row/(factor+0.0));
        subcjp.matrix = new Pixel[subcjp.column][subcjp.row];
        for (int i = 0; i<subcjp.column; i++)
        {
            for (int j =0; j < subcjp.row; j++)
            {
                subcjp.matrix[j][i] = AveragePixel(this.getRectangle(i*factor,j*factor,i*factor+(factor-1),j*factor+(factor-1)));
            }
        }
        return subcjp;
    }

    @Override
    public List<Pixel> getPixelsFromPyramid(int x, int y, int stepsback){
        if (parent == null) return new ArrayList<Pixel>(){{add(getPixel(x,y));}};
        if (stepsback == 0) return new ArrayList<Pixel>(){{add(getPixel(x,y));}};
        List<Pixel> answer = new ArrayList<Pixel>();
        for (int i = x*factor; i < x*factor+(factor); i++){
            for (int j = y*factor; j < y*factor+(factor);  j++)
            {
                answer.addAll(this.parent.getPixelsFromPyramid(i,j,stepsback-1));
            }
        }
        return answer;
    }

    @Override
    public void read(String filename) {
        CJP cjp = new CJP();
        cjp.read(filename);
        this.row = cjp.height;
        this.column = cjp.width;
        this.matrix = new Pixel[column][row];
        this.raw = new ArrayList<>();
        for (int i=0; i<cjp.lines.size(); i++) {
            raw.addAll(Arrays.asList(cjp.lines.get(i).split(" ")));
        }
        int cnt = 0;
        String curString;
        System.out.println(raw.size());
        System.out.println(this.column*this.row);
        if (raw.size() != this.column * this.row) throw new NotImplementedException();
        for (int j = 0; j<this.row; j++){
            for (int i = 0; i<this.column; i++){
                curString = raw.get(cnt);
                int r = Integer.parseInt(curString.substring(0,2), 16);
                int g = Integer.parseInt(curString.substring(2,4), 16);
                int b = Integer.parseInt(curString.substring(4,6), 16);
                this.matrix[i][j] = new Pixel(r,g,b);
                cnt++;
            }
        }
        this.parent = null;
        this.factor = 1;



    }

    @Override
    public void write(String filename) {
        File cjpFile = new File(filename);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(cjpFile));
            writer.write(Integer.toString(this.column));
            writer.newLine();
            writer.write(Integer.toString(this.row));
            writer.newLine();
            for (int j = 0; j < this.row; j++){
                for (int i = 0; i < this.column; i++){
                    writer.write(this.matrix[i][j].toHex()+" ");
                }
                writer.newLine();
            }
            writer.close();

        } catch (Exception e){

        }
    }

    @Override
    public double score(int sx, int sy, CJPFile sampleCJP, int tx, int ty, int width, int height) {

        double ans  = 0.0;
        if (sx + width > sampleCJP.column) width = sampleCJP.column-sx;
        if (sy + height > sampleCJP.row) height = sampleCJP.row-sy;

        if (tx + width > this.column) width = this.column-tx;
        if (ty + height > this.row) height = this.row-ty;

        int r1;
        int r2;
        int g1;
        int g2;
        int b1;
        int b2;

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                r1 = this.matrix[tx+j][ty+i].r;
                r2 = sampleCJP.matrix[sx+j][sy+i].r;
                g1 = this.matrix[tx+j][ty+i].g;
                g2 = sampleCJP.matrix[sx+j][sy+i].g;
                b1 = this.matrix[tx+j][ty+i].b;
                b2 = sampleCJP.matrix[sx+j][sy+i].b;
                ans += Math.sqrt(Math.pow((double)(r1 - r2), 2.0D) + Math.pow((double)(g1 - g2), 2.0D) + Math.pow((double)(b1 - b2), 2.0D));

            }
        }

        return ans;
    }

    public static void main(String[] args) {




        System.out.println("!!!!!!!!!!!!!!!!beda!!!!!!!!!!!!!!!!");
        CJPFile cjp = new CJPFile();
        cjp.read("C:\\Users\\james\\Desktop\\GOC\\pikachu.cjp");
        CJPFile rectangleToPrint = cjp.getRect(100,100,200,205);
        rectangleToPrint.write("C:\\Users\\james\\Desktop\\GOC\\rectangleToPrint.cjp");
        cjp.write("C:\\Users\\james\\Desktop\\GOC\\originalPika.cjp");
        System.out.println("Pixel (4,7) is: "+cjp.getPixel(100,100).toString());
        List<Pixel> rectanglePixels = cjp.getRectangle(100,100,200,205);

        for (int i = 0; i < rectanglePixels.size(); i++){
            System.out.println("Pixel "+i+" of rectanglePixels: "+rectanglePixels.get(i).toString());
        }
        System.out.println("Average Pixel: "+AveragePixel(rectanglePixels).toString());
        CJPFile cjpsub20 = cjp.subsample(12);
        cjpsub20.write("C:\\Users\\james\\Desktop\\GOC\\subsample12.cjp");
        System.out.println("Generated subsample12.cjp file");
        List<Pixel> pyramidPixels = cjpsub20.getPixelsFromPyramid(0,0,1);
        //for (int i = 0; i < pyramidPixels.size(); i++){
        //    System.out.println("Pixel "+i+" of pyramidPixels: "+pyramidPixels.get(i).toString());
        //}






    }

}
