package com.vsf.wisemen.graphics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CLGFile {

    public List<CLGFileEntry> entries = new ArrayList<>();

    public CLGFile(){
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
            writer.flush();
            writer.close();
        } catch (Exception ignored) {

        }

    }


}
