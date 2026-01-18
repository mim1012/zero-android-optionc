package com.sec.android.app.sbrowser.engine;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String TAG = FileManager.class.getSimpleName();

    static public byte[] getBytesFromFile(File file) {
        byte[] buffer = null;

        try {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return buffer;
    }

    public void chownSubFiles(String group, String owner, String path) {
        List<FileInfo> fileInfoList = getFileInfoList(path);

        for (FileInfo fileInfo : fileInfoList) {
            chown(group, owner, path + "/" + fileInfo.fileName);
            Log.d(TAG, "chown file: " + path + "/" + fileInfo.fileName);
        }
    }

    public void chown(String group, String owner, String path) {
        String cmd = "chown -R " + group + ":" + owner + " " + path;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void copySubFiles(String src, String dst) {
        List<FileInfo> fileInfoList = getFileInfoList(src);
        File dstFile = new File(dst);

        // 복사할 곳에 경로를 미리 만들어 준다.
        if (!dstFile.exists()) {
            dstFile.mkdirs();
        }

        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.fileName.contains("lib") || fileInfo.fileName.contains("cache")) {
                continue;
            }

            copy(src + "/" + fileInfo.fileName, dst);
            Log.d(TAG, "copy file: " + src + "/" + fileInfo.fileName + " -> " + dst + "/" + fileInfo.fileName);
        }
    }

    public void copy(String src, String dst) {
        String cmd = "cp -Rf " + src + " " + dst;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void moveSubFiles(String src, String dst) {
        List<FileInfo> fileInfoList = getFileInfoList(src);
        File dstFile = new File(dst);

        // 복사할 곳에 경로를 미리 만들어 준다.
        if (!dstFile.exists()) {
            mkdir(dst);
        }

        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.fileName.contains("lib") || fileInfo.fileName.contains("cache")) {
                continue;
            }

            move(src + "/" + fileInfo.fileName, dst);
            Log.d(TAG, "move file: " + src + "/" + fileInfo.fileName + " -> " + dst + "/" + fileInfo.fileName);
        }
    }

    public void move(String src, String dst) {
        File dstDir = new File(dst);
        File parentDir = dstDir.getParentFile();

        if (!parentDir.exists()) {
            Log.d(TAG, "Create dest parent dir: " + parentDir.getPath());
            mkdir(parentDir.getPath());
        }

        String cmd = "mv -f " + src + " " + dst;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void mkdir(String dir) {
        String cmd = "mkdir -p " + dir;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * path 삭제.
     *
     * @param path
     */
    public void delete(String path) {
        String cmd = "rm -rf " + path;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * path 안에 있는 파일 및 디렉토리 삭제.
     * 단. 데이터 폴더의 경우 lib 폴더는 삭제하지 않는다.
     *
     * @param path
     */
    public void deleteSubFiles(String path) {
        Log.d(TAG, "deleteSubFiles: " + path);
        List<FileInfo> fileInfoList = getFileInfoList(path);

        // 약간 부실함. 개선 필요.
        boolean isDataPath = path.contains("/data/data");

        for (FileInfo fileInfo : fileInfoList) {
            if (isDataPath && (fileInfo.fileName.contains("lib") || fileInfo.fileName.contains("cache"))) {
                continue;
            }

            delete(path + "/" + fileInfo.fileName);
            Log.d(TAG, "delete file: " + path + "/" + fileInfo.fileName);
        }
    }

    public List<FileInfo> getFileInfoList(String path) {
//        long start = System.currentTimeMillis();
        List<FileInfo> fileInfoList = new ArrayList<>();

        try {
            String command = "ls -l ";
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

            outputStream.writeBytes(command + path + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();

            InputStreamReader streamReader = new InputStreamReader(process.getInputStream());
            BufferedReader in = new BufferedReader(streamReader);
            String line;

            while ((line = in.readLine()) != null) {
                FileInfo fileInfo = createFileInfo(line.split("\\s+"));

                if (fileInfo != null) {
                    fileInfoList.add(fileInfo);
                }
            }

            in.close();
            streamReader.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        long execTime = System.currentTimeMillis() - start;
//        Log.d(TAG, "time: " + execTime);

        return fileInfoList;
    }

    public List<FileInfo> getFileInfoList(String path, String grep) {
//        long start = System.currentTimeMillis();
        List<FileInfo> fileInfoList = new ArrayList<>();

        try {
            String command = "ls -l " + path + " | grep " + grep;
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

            outputStream.writeBytes(command + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();

            InputStreamReader streamReader = new InputStreamReader(process.getInputStream());
            BufferedReader in = new BufferedReader(streamReader);
            String line;

            while ((line = in.readLine()) != null) {
                FileInfo fileInfo = createFileInfo(line.split("\\s+"));

                if (fileInfo != null) {
                    fileInfoList.add(fileInfo);
                }
            }

            in.close();
            streamReader.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        long execTime = System.currentTimeMillis() - start;
//        Log.d(TAG, "time: " + execTime);

        return fileInfoList;
    }

    private FileInfo createFileInfo(String... args) {
        FileInfo fi = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (args.length == 6) {
                fi = new FileInfo(args[0],args[1],args[2],args[3] +" "+ args[4],args[5]);
            } else if (args.length == 7) {
                fi = new FileInfo(args[0],args[1],args[2],args[3],args[4] +" "+ args[5],args[6]);
            }
        } else {
            if (args.length == 8) {
                fi = new FileInfo(args[0],args[2],args[3],args[4],args[5] +" "+ args[6],args[7]);
            }
        }

        return fi;
    }

    public class FileInfo {
        public String permissions;
        public String owner;
        public String group;
        public String size;
        public String date;
        public String fileName;

        private FileInfo(String permissions, String owner, String group, String size, String date, String fileName) {
            this.permissions = permissions;
            this.owner = owner;
            this.group = group;
            this.size = size;
            this.date = date;
            this.fileName = fileName;
        }

        private FileInfo(String permissions, String owner, String group, String date, String fileName) {
            this.permissions = permissions;
            this.owner = owner;
            this.group = group;
            this.date = date;
            this.fileName = fileName;
        }
    }

    public void test() {
        String path = "/data/data/com.sec.android.app.sbrowser";

        long start = System.currentTimeMillis();
        BufferedWriter out;
        BufferedReader in;
        String test = "";
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        try {
            String command = "ls -l";
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

            outputStream.writeBytes("cd " + path + "\n");
            outputStream.writeBytes(command + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();

//            Process proc = Runtime.getRuntime().exec("su ls -l");
//            out = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while((line = in.readLine()) != null) {
                fileInfoList.add(createFileInfo(line.split("\\s+")));
            }
            process.waitFor();
            in.close();
//            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long execTime = System.currentTimeMillis() - start;
        System.out.println(execTime);

//        FileInfo s = fileInfoList.get(0);
//        Log.d(TAG, s.date);
    }
}
