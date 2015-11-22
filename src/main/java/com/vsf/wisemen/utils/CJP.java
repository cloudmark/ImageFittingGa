package com.vsf.wisemen.utils;

import com.vsf.wisemen.graphics.ImageFile;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

public class CJP {
    public int width = -1;
    public int height = -1;
    public final List<String> lines = new ArrayList();

    public CJP() {
    }

    public void addLine(String line) {
        if(!line.trim().isEmpty()) {
            this.lines.add(line.trim().toUpperCase());
        }

    }

    private Color[] getColorLine(int idx) {
        Color[] colours = new Color[this.width];
        String line = (String)this.lines.get(idx);
        String[] colourStrings = line.split(" ");

        for(int i = 0; i < this.width; ++i) {
            String r = colourStrings[i].substring(0, 2);
            String g = colourStrings[i].substring(2, 4);
            String b = colourStrings[i].substring(4, 6);
            colours[i] = new Color(Integer.parseInt(r, 16), Integer.parseInt(g, 16), Integer.parseInt(b, 16));
        }

        return colours;
    }

    public void saveAsPNG(String pngFilename) {
        BufferedImage image = new BufferedImage(this.width, this.height, 1);
        int[] colData = new int[this.width * this.height];

        for(int e = 0; e < this.height; ++e) {
            Color[] coloursInLine = this.getColorLine(e);

            for(int i = 0; i < coloursInLine.length; ++i) {
                colData[e * this.width + i] = coloursInLine[i].getRed() << 16 | coloursInLine[i].getGreen() << 8 | coloursInLine[i].getBlue();
            }
        }

        image.setRGB(0, 0, this.width, this.height, colData, 0, this.width);

        try {
            ImageIO.write(image, "PNG", new File(pngFilename));
        } catch (IOException var7) {
            var7.printStackTrace();
        }

    }

    public void loadFromPNG(String pngFilename) {
        BufferedImage img = null;

        try {
            File e = new File(pngFilename);
            if(!e.exists()) {
                throw new IllegalArgumentException("File [" + pngFilename + "] does not exist!");
            }

            img = ImageIO.read(e);
            this.lines.clear();
            this.width = img.getWidth();
            this.height = img.getHeight();
            int[] rgb = img.getRGB(0, 0, this.width, this.height, (int[])null, 0, this.width);

            for(int i = 0; i < rgb.length; i += this.width) {
                StringBuilder sb = new StringBuilder();

                for(int j = i; j < i + this.width; ++j) {
                    int alpha = rgb[j] >> 24 & 255;
                    int red = rgb[j] >> 16 & 255;
                    int green = rgb[j] >> 8 & 255;
                    int blue = rgb[j] & 255;
                    if(alpha == 0) {
                        throw new UnsupportedOperationException("Transparency info in png file " + pngFilename + ".  Unsupported operation.");
                    }

                    sb.append(String.format("%02X", new Object[]{Integer.valueOf(red)}));
                    sb.append(String.format("%02X", new Object[]{Integer.valueOf(green)}));
                    sb.append(String.format("%02X", new Object[]{Integer.valueOf(blue)}));
                    sb.append(" ");
                }

                sb.setLength(sb.length() - 1);
                this.addLine(sb.toString());
            }
        } catch (IOException var12) {
            var12.printStackTrace();
        }

    }

    public void read(String cjpFilename) {
        File cjpFile = new File(cjpFilename);
        if(!cjpFile.exists()) {
            throw new IllegalArgumentException("File [" + cjpFilename + "] does not exist!");
        } else {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(cjpFile));
                this.width = Integer.valueOf(reader.readLine()).intValue();
                this.height = Integer.valueOf(reader.readLine()).intValue();
                this.lines.clear();

                for(String e = reader.readLine(); e != null; e = reader.readLine()) {
                    this.addLine(e);
                }
            } catch (FileNotFoundException var17) {
                var17.printStackTrace();
            } catch (NumberFormatException var18) {
                var18.printStackTrace();
            } catch (IOException var19) {
                var19.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException var16) {
                        var16.printStackTrace();
                    }
                }

            }
        }
    }

    public void write(String cjpFilename) {
        File cjpFile = new File(cjpFilename);
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(cjpFile));
            writer.write(Integer.toString(this.width));
            writer.newLine();
            writer.write(Integer.toString(this.height));
            writer.newLine();
            Iterator var5 = this.lines.iterator();

            while(var5.hasNext()) {
                String e = (String)var5.next();
                writer.write(e);
                writer.newLine();
            }
        } catch (IOException var14) {
            var14.printStackTrace();
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException var13) {
                    var13.printStackTrace();
                }
            }

        }

    }
}
