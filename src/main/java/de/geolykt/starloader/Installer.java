package de.geolykt.starloader;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

public class Installer extends JFrame {

    public static final String SL_JAR = "/maven/de/geolykt/starloader/2.1.0/starloader-2.1.0-all.jar";
    public static final String SL_MIRROR = "geolykt.de";
    public static final String SLC_WINDOWS = "/SLCubed-windows-x64.exe";
    public static final String SLC_MIRRIOR = "files.geolykt.de";

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2017610672107928535L;

    protected JLabel infoLabel = new JLabel("Choose galimulator home directory");
    protected JButton okbutton = new JButton("OK");
    protected JFileChooser fileChooserGali;
    protected TableLayout layout = new TableLayout(new double[] {0.5, 0.5}, new double[] {0.5, 0.5});

    public Installer() {
        super("Starloader Installer");
        this.setLayout(layout);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(infoLabel, new TableLayoutConstraints(0, 0, 0, 0));
        this.add(okbutton, new TableLayoutConstraints(0, 1, 1, 1));
        okbutton.addMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showJarFileChooser();
            }
        });
        this.pack();
    }

    public void showJarFileChooser() {
        if (fileChooserGali == null) {
            fileChooserGali = new JFileChooser(Utils.getGameDir(Utils.STEAM_GALIMULATOR_APPNAME));
            fileChooserGali.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooserGali.setVisible(true);
        }
        if (fileChooserGali.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            acceptJarFileChooser();
        }
    }

    public void illegalFolder() {
        PopupFactory factory = PopupFactory.getSharedInstance();
        JPanel panel = new JPanel();
        JButton button = new JButton("ok");
        panel.add(button);
        JLabel text = new JLabel("Invalid directory, please select the current galimulator install.");
        panel.add(text);
        Dimension d = getToolkit().getScreenSize();
        Popup popup = factory.getPopup(null, panel, d.width / 2 - 200, d.height / 2); // Perfectionists will hate this line
        button.addMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.hide();
            }
        });
        popup.show();
        fileChooserGali = null;
    }

    public void deleteAllItems(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteAllItems(f);
            }
            try {
                Files.delete(f.toPath());
            } catch (IOException e) {
                System.err.println("Cannot delete file: " + f.toString());
                e.printStackTrace();
            }
        }
    }

    private void finishInstall() {
        this.setVisible(false);
        PopupFactory factory = PopupFactory.getSharedInstance();
        JPanel panel = new JPanel();
        JButton button = new JButton("ok");
        panel.add(button);
        JLabel text = new JLabel("Finished installing without any issues. You can now click 'ok' to exit the installer.");
        panel.add(text);
        Dimension d = getToolkit().getScreenSize();
        Popup popup = factory.getPopup(null, panel, d.width / 2 - 200, d.height / 2); // Perfectionists will hate this line
        button.addMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
        popup.show();
    }

    public void installerError() {
        PopupFactory factory = PopupFactory.getSharedInstance();
        JPanel panel = new JPanel();
        JButton button = new JButton("ok");
        panel.add(button);
        JLabel text = new JLabel("Something went wrong in the installation. Consider installing manually.");
        panel.add(text);
        Dimension d = getToolkit().getScreenSize();
        Popup popup = factory.getPopup(null, panel, d.width / 2 - 200, d.height / 2); // Perfectionists will hate this line
        button.addMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.hide();
            }
        });
        popup.show();
    }

    public void acceptJarFileChooser() {
        File galiFolder = fileChooserGali.getSelectedFile();
        if (!galiFolder.exists()) {
            illegalFolder();
            return;
        }
        System.out.println("Installing Starloader for the " + Utils.OPERATING_SYSTEM + " os.");
        try {
            System.out.println("Downloading starloader launcher jar.");
            FileUtils.copyURLToFile(new URL("https", SL_MIRROR, SL_JAR), new File(galiFolder, "starloader.jar"));
            System.out.println("Finished downloading starloader launcher jar.");
        } catch (IOException e) {
            e.printStackTrace();
            installerError();
        }
        try {
            if (Utils.OPERATING_SYSTEM.toLowerCase(Locale.ROOT).startsWith("win")) {
                System.out.println("Downloading SLCubed distribution.");
                File binary = new File(galiFolder, "galimulator-windows-64bit.exe");
                binary.delete();
                FileUtils.copyURLToFile(new URL("https", SL_MIRROR, SL_JAR), binary);
                System.out.println("Finished downloading SLCubed.");
            } else if (Utils.OPERATING_SYSTEM.toLowerCase(Locale.ROOT).equals("linux")) {
                // Assume that a hashbang suffices
                File binary = new File(galiFolder, "galimulator-linux-64bit");
                binary.delete();
                try (FileWriter fr = new FileWriter(binary)) {
                    fr.write("#!/bin/sh\njava -jar starloader.jar\n"); // Our 37b replacement for a 81kb file. Intelligent problem solving on our part.
                }
                binary.setExecutable(true);
                System.out.println("Replaced binary");
            } else {
                // hashbang should also suffice on macos, but I have no idea what the natives are called there
                System.out.println("Did NOT replace the binaries. Steam integration not done.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            installerError();
        }
        System.out.println("Done. Have a great day.");
        finishInstall();
    }

    public static void main(String[] args) {
        Installer installer = new Installer();
        installer.setVisible(true);
    }
}
