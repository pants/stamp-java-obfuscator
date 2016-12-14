package wtf.pants.stamp;


import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.apache.commons.cli.*;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Spacks
 */
public class Stamp {

    private final File inputFile, outputFile;
    private final String[] libs;
    private final String[] exclusions;

    private ObfuscatorManager obfuscatorManager;

    @Getter
    private ClassCollector collector;

    public Stamp(File inputFile, File outputFile, String[] libs, String[] exclusions) {
        this.collector = new ClassCollector();
        this.obfuscatorManager = new ObfuscatorManager(this);

        this.inputFile = inputFile;
        this.outputFile = outputFile;

        this.libs = libs;
        this.exclusions = exclusions;
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

                    if(mainClassMap.isObfuscated()) {
                        final String newLine = line.replace(mainClass, mainClassMap.getObfClassName()) + "\n";
                        b.write(newLine.getBytes());
                    }
                    else {
                        b.write((line + "\n").getBytes());
                    }
                } else {
                    b.write((line + "\n").getBytes());
                }
            } catch (ClassMapNotFoundException e) {
                Log.error("Main class does not seem to have been mapped");
                System.exit(0);
            } catch (IOException e) {
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

            byte[] classBytes = c.getName().endsWith(".class") ? obfuscatedBytes : bytes;
            ZipUtils.addFileToZip(zipOutputStream, cn.name + ".class", classBytes);
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
            mappingHandler.mapClasses(inputFile, exclusions);

            System.out.println("\n\n----------------------------------");
            Log.info("Obfuscating bytecode...");
            obfuscateJar(inputFile, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addCliOptions(Options options){
        options.addOption(
                new Option("i", "input", true, "Input file to obfuscate"));

        options.addOption(
                new Option("o", "output", true, "File to output to after obfuscation"));

        options.addOption(
                new Option("lib", "libraries", true, "Libraries that the input file uses (separator: ';')"));

        options.addOption(
                new Option("x", "exclude", true, "Packages to exclude (separator: ';')"));

        final Option helpOpt = new Option("help", false, "Displays possible arguments");
        helpOpt.setOptionalArg(true);
        options.addOption(helpOpt);
    }

    public static void main(String[] args) {
        final String usageMsg = "java -jar stamp.jar -i INPUT.jar";

        final Options options = new Options();
        addCliOptions(options);

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter helpFormatter = new HelpFormatter();

        final CommandLine cli;

        try {
            cli = parser.parse(options, args);
        } catch (ParseException e) {
            Log.error(e.getMessage());
            helpFormatter.printHelp(usageMsg, options);
            return;
        }

        if(cli.hasOption("-help")){
            helpFormatter.printHelp("java -jar stamp.jar", options, true);
            return;
        }

        String inputFilename = cli.getOptionValue("input");
        String outputFilename = cli.getOptionValue("output");

        if(inputFilename == null){
            helpFormatter.printHelp(usageMsg, options);
            return;
        }

        if (!inputFilename.endsWith(".jar")) {
            inputFilename += ".jar";
        }

        if (outputFilename == null) {
            outputFilename = "Obfuscated_" + inputFilename;
        }

        final File file = new File(inputFilename);

        if(!file.exists()){
            Log.error("Input file doesn't exist: %s", inputFilename);
            return;
        }

        final String libVal = cli.getOptionValue("libraries");
        final String exVal = cli.getOptionValue("exclude");

        //Correctly splits the excluded files and libs into an array
        final String[] libs = libVal != null ? libVal.split(";") : null;
        final String[] exclude = exVal != null ? exVal.split(";") : null;

        Log.info("Obfuscating '%s' and outputting to '%s'", inputFilename, outputFilename);
        Stamp instance = new Stamp(file, new File(outputFilename), libs, exclude);
        instance.start();
    }

}
