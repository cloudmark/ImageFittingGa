package com.vsf.wisemen.graphics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class CLGFile {

    public ArrayList<CLGFileEntry> entries;

    public CLGFile(ArrayList<CLGFileEntry> entries){
        this.entries = entries;
    }

    public void print(String filename) {

        File clgFile = new File(filename);
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(clgFile));
            for (int i = 0; i < entries.size(); i++) {
                writer.write(entries.get(i).print());
                writer.newLine();
            }
        } catch (Exception ignored) {

        }

    }


}
