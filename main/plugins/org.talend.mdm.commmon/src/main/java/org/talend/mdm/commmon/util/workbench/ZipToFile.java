/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.workbench;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DOC aiming class global comment. Detailled comment
 */
public class ZipToFile {

    public static final int BUFFER = 1024;// buf size

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipToFile.class);

    public static void deleteDirectory(File dir) {
        // modified by honghb ,fix bug 21552
        if (!dir.exists() || dir.isFile()) {
            return;
        }
        // end
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                deleteDirectory(entries[i]);
            } else {
                entries[i].delete();
            }
        }
        dir.delete();
    }

    /**
     *
     * DOC aiming Comment method "zipFile".
     *
     * @param baseDir
     * @param zipFile
     * @throws Exception
     */
    public static void zipFile(String baseDir, String zipFile) throws Exception {
        List fileList = getSubFiles(new File(baseDir));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry ze = null;
        byte[] buf = new byte[BUFFER];
        int readLen = 0;
        for (int i = 0; i < fileList.size(); i++) {
            File f = (File) fileList.get(i);
            ze = new ZipEntry(getAbsFileName(baseDir, f));
            ze.setSize(f.length());
            ze.setTime(f.lastModified());
            zos.putNextEntry(ze);
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            while ((readLen = is.read(buf, 0, BUFFER)) != -1) {
                zos.write(buf, 0, readLen);
            }
            is.close();
        }
        zos.close();
    }

    /**
     *
     * DOC aiming Comment method "getAbsFileName".
     *
     * @param baseDir
     * @param realFileName
     * @return
     */
    private static String getAbsFileName(String baseDir, File realFileName) {
        File real = realFileName;
        File base = new File(baseDir);
        String ret = real.getName();
        while (true) {
            real = real.getParentFile();
            if (real == null) {
                break;
            }
            if (real.equals(base)) {
                break;
            } else {
                ret = real.getName() + "/" + ret; //$NON-NLS-1$
            }
        }
        return ret;
    }

    /**
     *
     * DOC aiming Comment method "getSubFiles".
     *
     * @param baseDir
     * @return
     */
    private static List getSubFiles(File baseDir) {
        List ret = new ArrayList();
        File[] tmp = baseDir.listFiles();
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].isFile()) {
                ret.add(tmp[i]);
            }
            if (tmp[i].isDirectory()) {
                ret.addAll(getSubFiles(tmp[i]));
            }
        }
        return ret;
    }

    /**
     *
     * DOC aiming Comment method "unZipFile".
     *
     * @param zipfile
     * @param unzipdir
     * @throws IOException
     * @throws Exception
     */
    public static void unZipFile(String zipfile, String unzipdir) throws IOException {
        File unzipF = new File(unzipdir);
        if (!unzipF.exists()) {
            unzipF.mkdirs();
        }
        ZipFile zfile = null;
        try {
            zfile = new ZipFile(zipfile);
            Enumeration zList = zfile.entries();
            ZipEntry ze = null;
            byte[] buf = new byte[1024];
            while (zList.hasMoreElements()) {
                ze = (ZipEntry) zList.nextElement();
                if (ze.isDirectory()) {
                    File f = new File(unzipdir + ze.getName());
                    f.mkdirs();
                    continue;
                }
                // OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(unzipdir,
                // ze.getName())));
                unzipdir = unzipdir.replace('\\', '/');
                if (!unzipdir.endsWith("/")) { //$NON-NLS-1$
                    unzipdir = unzipdir + "/"; //$NON-NLS-1$
                }
                String filename = unzipdir + ze.getName();
                File zeF = new File(filename);
                if (!zeF.getParentFile().exists()) {
                    zeF.getParentFile().mkdirs();
                }
                OutputStream os = null;
                InputStream is = null;
                try {
                    os = new BufferedOutputStream(new FileOutputStream(zeF));
                    is = new BufferedInputStream(zfile.getInputStream(ze));
                    int readLen = 0;
                    while ((readLen = is.read(buf, 0, 1024)) != -1) {
                        os.write(buf, 0, readLen);
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (os != null) {
                            os.close();
                        }
                    } catch (Exception e) {
                    }

                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            if (zfile != null) {
                try {
                    zfile.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void removeTalendLibsFromBarFile(File barFile) {
        String tmpfolder = System.getProperty("user.dir") + "/tmpfolder";

        try {
            ZipToFile.unZipFile(barFile.getAbsolutePath(), tmpfolder);
            File talendFolder = new File(tmpfolder + "/provided-libs/talend");
            if (talendFolder.exists()) {
                ZipToFile.deleteDirectory(talendFolder);
            }

            ZipToFile.zipFile(tmpfolder, barFile.getAbsolutePath());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            File tmp = new File(tmpfolder);
            if (tmp.exists()) {
                ZipToFile.deleteDirectory(tmp);
            }
        }
    }
}
