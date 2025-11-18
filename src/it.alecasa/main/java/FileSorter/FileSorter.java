package FileSorter;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.Mp4Directory;

public class FileSorter {

    private Properties properties = new Properties();

    private static String SOURCE_FOLDER_PATH ;
    private static String DESTINATION_FOLDER_PATH;
    private static String ERROR_LOG_PATH;


    public static void main(String[] args) {


        if (!readFromProperties()){
            return;
        }

        System.out.println(SOURCE_FOLDER_PATH);


        //String sourceFolderPath = "C:\\Users\\aleca\\Desktop\\BCK OP6\\2025\\02"; // Modifica questo percorso
        Path sourceDir = Paths.get(SOURCE_FOLDER_PATH);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path filePath : stream) {
                System.out.println("processing: "+filePath);
                if (Files.isRegularFile(filePath)) {
                    try {
                        Date fileDate = getOriginalDate(filePath);
                        if (fileDate != null) {
                            SimpleDateFormat folderFormat = new SimpleDateFormat("yyyy/MM");
                            String datePath = folderFormat.format(fileDate);
                            String destinationFolder = DESTINATION_FOLDER_PATH + File.separator + datePath;
                            Path destinationDir = Paths.get(destinationFolder);

                            if (!Files.exists(destinationDir)) {
                                Files.createDirectories(destinationDir);
                            }

                            Path destinationPath = destinationDir.resolve(filePath.getFileName());
                            Files.move(filePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("File spostato: " + filePath.getFileName() + " -> " + destinationFolder);
                        } else {

                            //TODO: se non riescoa leggere la data dai metadata prova ad estrarla dal nome file, solitamente il nome file è la data nel formato yyyyMMdd_hhmmss_SSS.estensionefile
                            logError("Data non disponibile per: " + filePath.getFileName());
                        }
                    } catch (Exception e) {
                        logError("Errore nel file: " + filePath.getFileName() + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logError("Errore nella lettura della cartella: " + SOURCE_FOLDER_PATH + " - " + e.getMessage());
        }
    }

    private static Date getOriginalDate(Path filePath) {
        try {
            if (isImageFile(filePath)) {
                Metadata metadata = ImageMetadataReader.readMetadata(filePath.toFile());
                ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                if (exif != null) {
                    return exif.getDateOriginal();
                }
            } else if (isVideoFile(filePath)) {
                Metadata metadata = ImageMetadataReader.readMetadata(filePath.toFile());
                Mp4Directory mp4Dir = metadata.getFirstDirectoryOfType(Mp4Directory.class);
                if (mp4Dir != null) {
                    return mp4Dir.getDate(Mp4Directory.TAG_CREATION_TIME);
                }
                QuickTimeDirectory qtDir = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);
                if (qtDir != null) {
                    return qtDir.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
                }
            }
        } catch (Exception e) {
            logError("Errore nella lettura dei metadata per: " + filePath.getFileName() + " - " + e.getMessage());
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            return new Date(attrs.creationTime().toMillis());
        } catch (IOException e) {
            logError("Errore nella lettura degli attributi del file: " + filePath.getFileName() + " - " + e.getMessage());
            return null;
        }
    }

    private static boolean isImageFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

    private static boolean isVideoFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".mp4") || fileName.endsWith(".mov") ||
                fileName.endsWith(".avi") || fileName.endsWith(".mkv");
    }

    private static void logError(String message) {
        System.out.println("ERRORE: " + message);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ERROR_LOG_PATH, true))) {
            writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Impossibile scrivere nel file di log: " + e.getMessage());
        }
    }

    private static boolean readFromProperties(){

        try {

            String propertiesFileName = "moveAndSortFiles.properties";

            String currentDir = System.getProperty("user.dir");

            File propertiesFile = new File(currentDir, propertiesFileName);

            if (!propertiesFile.exists()) {
                logError("Failed to load moveAndSortFiles.properties");
                return false;
            }


            Properties appProperties = new Properties();
            appProperties.load(new FileInputStream(propertiesFile));

            if (!appProperties.isEmpty()){

                SOURCE_FOLDER_PATH = appProperties.getProperty("source.path");
                DESTINATION_FOLDER_PATH = appProperties.getProperty("destination.path");
                ERROR_LOG_PATH = appProperties.getProperty("logs.path");
            }else{
                logError("Failed to load moveAndSortFiles.properties");
                return false;
            }

            return true;
        }catch(Exception e){
            logError("Failed to load moveAndSortFiles.properties");
            return false;
        }


    }




}
