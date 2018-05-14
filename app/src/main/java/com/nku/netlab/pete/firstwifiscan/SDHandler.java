package com.nku.netlab.pete.firstwifiscan;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SDHandler {
    private static String DIRECTORY_ROOT = "FirstWiFi";
    private static String FILE_EXTENTION = "csv";
    private String m_rootFilePath;

    public SDHandler() {
        m_rootFilePath = initRootDirectory();
    }
    private String initRootDirectory(){
        return String.format("%s/%s", Environment.getExternalStorageDirectory().toString(), DIRECTORY_ROOT);
    }

    private void doCreateFile(File file) throws IOException {
        if (!file.exists()) {
            File folder = file.getParentFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file.createNewFile();
        }
    }

    public boolean doWriteToFile(final String sensorID, final String sensorValues) {
        boolean writeFlag = false;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String sensorFilePath = String.format("%s/%s_%s.%s", m_rootFilePath, format.format(new Date()), sensorID, FILE_EXTENTION);
        try {
            File sensorFile = new File(sensorFilePath);
            doCreateFile(sensorFile);
            FileWriter fstream = new FileWriter(sensorFile);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(sensorValues);
            out.flush();
            out.close();
            writeFlag = true;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            writeFlag = false;
        }
        return writeFlag;
    }
}
