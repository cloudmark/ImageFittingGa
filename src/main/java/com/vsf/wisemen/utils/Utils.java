package com.vsf.wisemen.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Utils {
    public Utils() {
    }

    public static int[][] fileToArray(String cjpFile) {
        Scanner cjpFileScanner = null;

        try {
            cjpFileScanner = new Scanner(new File(cjpFile));
        } catch (Exception var15) {
            var15.printStackTrace();
            System.err.println("Oops! Couldn\'t find the file at " + cjpFile);
            System.exit(8);
        }

        int w = 0;
        int h = 0;

        try {
            w = cjpFileScanner.nextInt();
            h = cjpFileScanner.nextInt();
        } catch (Exception var14) {
            var14.printStackTrace();
            System.err.println("Oops! Couldn\'t find width or height in " + cjpFile);
            System.exit(9);
        }

        try {
            int[][] ex = new int[h][];

            for(int i = 0; i < h; ++i) {
                int[] line = new int[w];

                for(int j = 0; j < w; ++j) {
                    line[j] = cjpFileScanner.nextInt(16);
                }

                ex[i] = line;
            }

            int[][] var9 = ex;
            return var9;
        } catch (Exception var16) {
            var16.printStackTrace();
            System.err.println("Oops! Something\'s wrong with the contents of file " + cjpFile);
            System.exit(9);
        } finally {
            if(cjpFileScanner != null) {
                cjpFileScanner.close();
            }

        }

        return null;
    }

    public static String to6Hex(int num) {
        String hex = Integer.toHexString(num);
        return ("000000" + hex).substring(hex.length());
    }

    public static void arrayToCJPFile(String fileName, int[][] image) {
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(fileName);
        } catch (Exception var7) {
            System.err.println("Oops can\'t save at " + fileName);
            System.exit(9);
        }

        int height = image.length;
        int width = image[0].length;
        pw.println(width);
        pw.println(height);

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                pw.print(to6Hex(image[i][j]) + " ");
            }

            pw.println();
        }

        pw.close();
    }

    public static int[][] initialiseArray(int height, int width) {
        int[][] array = new int[height][];

        for(int i = 0; i < height; ++i) {
            array[i] = new int[width];

            for(int j = 0; j < width; ++j) {
                array[i][j] = 0;
            }
        }

        return array;
    }

    public static int[][] collageToArray(String collageFile, String compositionFile, int maxCollagePieces, int height, int width) {
        int[][] targetCanvas = initialiseArray(height, width);
        Scanner collageFileScanner = null;

        try {
            try {
                collageFileScanner = (new Scanner(new File(collageFile))).useDelimiter("\\s*,\\s*|\\s+");
            } catch (Exception var27) {
                System.err.println("Oops! Couldn\'t find the collage file at " + collageFile);
                return null;
            }

            int piece;
            for(piece = 0; piece < maxCollagePieces && collageFileScanner.hasNext(); ++piece) {
                try {
                    int ex = collageFileScanner.nextInt();
                    String cjpComposingImage = getFilenameFromId(ex, compositionFile);
                    int xc = collageFileScanner.nextInt();
                    int yc = collageFileScanner.nextInt();
                    int w = collageFileScanner.nextInt();
                    int h = collageFileScanner.nextInt();
                    int xt = collageFileScanner.nextInt();
                    int yt = collageFileScanner.nextInt();
                    int[][] cjpComposingImageContents = fileToArray(cjpComposingImage);

                    for(int i = 0; i < h; ++i) {
                        for(int j = 0; j < w; ++j) {
                            try {
                                if(i + yc < cjpComposingImageContents.length && j + xc < cjpComposingImageContents[i + yc].length && i + yt < targetCanvas.length && j + xt < targetCanvas[i + yt].length) {
                                    targetCanvas[i + yt][j + xt] = cjpComposingImageContents[i + yc][j + xc];
                                }
                            } catch (Exception var25) {
                                System.err.println("Ignoring out of bounds...");
                                var25.printStackTrace();
                            }
                        }
                    }
                } catch (Exception var26) {
                    var26.printStackTrace();
                    System.err.println("Oops something is wrong ... contact judges on mailing list");
                    System.exit(10);
                }
            }

            if(collageFileScanner.hasNext()) {
                System.err.println("Too many rectangle definitions in collage file (" + (piece + 1) + " rectangles in file " + collageFile + " , " + maxCollagePieces + " allowed)");
                System.exit(6);
            }

            return targetCanvas;
        } finally {
            if(collageFileScanner != null) {
                collageFileScanner.close();
            }

        }
    }

    private static String getFilenameFromId(int id, String compositionFile) {
        ArrayList list = new ArrayList();

        try {
            BufferedReader e = new BufferedReader(new FileReader(compositionFile));

            String str;
            while((str = e.readLine()) != null) {
                if(!str.trim().isEmpty()) {
                    list.add(str.trim());
                }
            }
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        if(id >= list.size()) {
            System.err.println("Composition image with id " + id + " not found in " + compositionFile + ". " + list.size() + " composition images available.");
            System.exit(6);
        }

        return (String)list.get(id);
    }

    public static void collageToCJP(String collageFile, String compositionFile, int maxCollagePieces, int height, int width, String cjpFile) {
        int[][] collage = collageToArray(collageFile, compositionFile, maxCollagePieces, height, width);
        if(collage == null) {
            System.err.println("Something went wrong while processing collage file - CJP file not generated");
        } else {
            arrayToCJPFile(cjpFile, collage);
        }
    }

    public static void collageToPNG(String collageFile, String compositionFile, int maxCollagePieces, int height, int width, String pngFile) {
        int[][] collage = collageToArray(collageFile, compositionFile, maxCollagePieces, height, width);
        if(collage == null) {
            System.err.println("Something went wrong while processing collage file - PNG file not generated");
        } else {
            String tmpFile = "tmp.cjp";
            arrayToCJPFile("tmp.cjp", collage);
            CJP cjp = new CJP();
            cjp.read("tmp.cjp");
            cjp.saveAsPNG(pngFile);
            (new File("tmp.cjp")).delete();
        }
    }

    public static double scoringFunction(int colour1, int colour2) {
        String hex1 = to6Hex(colour1);
        int r1 = Integer.parseInt(hex1.substring(0, 2), 16);
        int g1 = Integer.parseInt(hex1.substring(2, 4), 16);
        int b1 = Integer.parseInt(hex1.substring(4, 6), 16);
        String hex2 = to6Hex(colour2);
        int r2 = Integer.parseInt(hex2.substring(0, 2), 16);
        int g2 = Integer.parseInt(hex2.substring(2, 4), 16);
        int b2 = Integer.parseInt(hex2.substring(4, 6), 16);
        return Math.sqrt(Math.pow((double)(r1 - r2), 2.0D) + Math.pow((double)(g1 - g2), 2.0D) + Math.pow((double)(b1 - b2), 2.0D));
    }

    public static double calcScore(int maxCollagePieces, String compositionFile, String targetImage, String collageFile) {
        int[][] targetContents = fileToArray(targetImage);
        if(targetContents == null) {
            return -1.0D;
        } else {
            int height = targetContents.length;
            int width = targetContents[0].length;
            int[][] collageContents = collageToArray(collageFile, compositionFile, maxCollagePieces, height, width);
            if(collageContents == null) {
                return -1.0D;
            } else {
                double score = 0.0D;

                for(int i = 0; i < height; ++i) {
                    for(int j = 0; j < width; ++j) {
                        score += scoringFunction(collageContents[i][j], targetContents[i][j]);
                    }
                }

                return score;
            }
        }
    }
}
