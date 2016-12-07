package wtf.pants.stamp;


import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import wtf.pants.stamp.mapping.ClassCollector;
import wtf.pants.stamp.mapping.MappingManager;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.obfuscator.ObfuscatorManager;
import wtf.pants.stamp.util.Log;
import wtf.pants.stamp.util.ZipUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Spacks
 */
public class Stamp {

    private static Stamp instance;

    private final File inputFile, outputFile;

    private ObfuscatorManager obfuscatorManager;

    @Getter
    private ClassCollector collector;

    public Stamp(File inputFile, File outputFile) {
        this.collector = new ClassCollector();
        this.obfuscatorManager = new ObfuscatorManager(this);

        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    private byte[] modifyManifestFile(byte[] bytes) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        final ByteArrayOutputStream b = new ByteArrayOutputStream(bytes.length);

        bufferedReader.lines().forEach(line -> {
            try {
                if (line.contains("Class: ")) {
                    final String mainClass = line.split(": ")[1];
                    Log.log("Found main class: %s", mainClass);
                    final ClassMap mainClassMap = collector.getClassMap(mainClass.replace(".", "/"));

                    final String newLine = line.replace(mainClass, mainClassMap.getObfClassName()) + "\n";
                    b.write(newLine.getBytes());
                } else {
                    b.write((line + "\n").getBytes());
                }
            } catch (ClassMapNotFoundException e) {
                Log.error("Main class does not seem to have been mapped");
                System.exit(0);
            } catch (IOException e){
                Log.error("Error writing manifest file");
                System.exit(0);
            }
        });

        return b.toByteArray();
    }

    private void handleZipEntry(ZipOutputStream zipOutputStream, ZipFile zipFile, ZipEntry c) throws IOException {
        if (c.getName().endsWith("/")) {
            return;
        }

        final InputStream is = zipFile.getInputStream(c);
        final byte[] bytes = ByteStreams.toByteArray(is);

        Log.log("\t> %s", c.getName());
        if (c.getName().endsWith(".class")) {
            final ClassReader cr = new ClassReader(bytes);
            final ClassNode cn = new ClassNode();

            final byte[] obfuscatedBytes = obfuscatorManager.obfuscate(cr, cn);

            Log.info("Saving: %s    (Old Name: %s)", cn.name, c.getName());

            ZipUtils.addFileToZip(zipOutputStream, cn.name + ".class",
                    c.getName().endsWith(".class") ? obfuscatedBytes : bytes);
        } else if (c.getName().endsWith("MANIFEST.MF")) {
            ZipUtils.addFileToZip(zipOutputStream, c.getName(), modifyManifestFile(bytes));
        } else {
            ZipUtils.addFileToZip(zipOutputStream, c.getName(), bytes);
        }

        is.close();
    }

    private void obfuscateJar(File inputFile, File outputFile) throws IOException {
        final FileOutputStream fos = new FileOutputStream(outputFile, false);
        final ZipOutputStream zipOutputStream = new ZipOutputStream(fos);
        final ZipFile zipFile = new ZipFile(inputFile);

        zipFile.stream().forEach(c -> {
            try {
                handleZipEntry(zipOutputStream, zipFile, c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        zipOutputStream.close();
        zipFile.close();
    }

    private void start() {
        Log.DEBUG = true;
        try {
            Log.info("Mapping classes...");
            MappingManager mappingHandler = new MappingManager(collector);
            mappingHandler.mapClasses(inputFile);

            System.out.println("\n\n----------------------------------");
            Log.info("Obfuscating bytecode...");
            obfuscateJar(inputFile, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            final String inputFilename = args[0] + (args[0].toLowerCase().endsWith(".jar") ? "" : ".jar");

            String outputFilename = "Obfuscated_" + inputFilename;

            if (args.length >= 2) {
                outputFilename = args[1] + (inputFilename.toLowerCase().endsWith(".jar") ? "" : ".jar");
            }

            final File file = new File(inputFilename);

            if (file.exists()) {
                Log.info("Obfuscating '%s' and outputting to '%s'", inputFilename, outputFilename);
                instance = new Stamp(file, new File(outputFilename));
                instance.start();
            } else {
                Log.error("'%s' does not exist!", inputFilename);
            }

        } else {
            System.out.println("Invalid format. --help for more info\n-\tjava -jar FILENAME.jar [input filename] <output filename>");
        }
    }

}
