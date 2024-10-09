import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClassesFromData {
    public static void main(String[] args) {
        String inputFilePath = null;
        String outputDirPath = null;
        String direction = "--";  // Default arrow
        boolean mirror = false;   // Flag for mirror option

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("-i".equals(args[i]) || "--input".equals(args[i])) {
                inputFilePath = args[i + 1];
            } else if ("-o".equals(args[i]) || "--output".equals(args[i])) {
                outputDirPath = args[i + 1];
            } else if ("-d".equals(args[i]) || "--direction".equals(args[i])) {
                String dirValue = args[i + 1];
                if ("up".equals(dirValue) || "down".equals(dirValue) || "left".equals(dirValue) || "right".equals(dirValue)) {
                    direction = "-" + dirValue + "-";
                } else {
                    System.err.println("Invalid value for -d. Accepted values are: up, down, left, right.");
                    return;
                }
            } else if ("-m".equals(args[i]) || "--mirror".equals(args[i])) {
                mirror = true;  // Set mirror flag
            }
        }

        if (inputFilePath == null || outputDirPath == null) {
            System.err.println("Usage: java -jar classes-from-data.jar -i <input.json/xml> -o <output-directory> [-d <direction>] [-m]");
            return;
        }

        try {
            // Read the input file
            String fileContent = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            JSONObject json = null;

            // Check if the input is an XML or JSON file
            if (inputFilePath.endsWith(".json")) {
                json = new JSONObject(new JSONTokener(fileContent));
            } else if (inputFilePath.endsWith(".xml")) {
                // Convert XML to JSON
                json = XML.toJSONObject(fileContent);
            } else {
                System.err.println("Unsupported file format. Please provide a .json or .xml file.");
                return;
            }

            // Get the filename without extension
            String fileNameWithoutExt = new File(inputFilePath).getName().replaceFirst("[.][^.]+$", "");

            // Ensure output directory exists
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Generate class diagram content
            StringBuilder classDiagram = new StringBuilder();
            classDiagram.append("/' CLASSES '/\n\n");
            classDiagram.append("    frame \"" + fileNameWithoutExt + "\" {\n\n");
            
            // Update here to generate Root as an empty string
            generateClassDiagram(json, capitalize(fileNameWithoutExt), capitalize(fileNameWithoutExt) + "Root", classDiagram, null, mirror);  // Pass mirror flag
            classDiagram.append("    }\n");

            // Generate connections after class definitions
            classDiagram.append("\n/' CONNECTIONS '/\n\n");
            generateConnections(json, capitalize(fileNameWithoutExt) + "Root", classDiagram, direction, mirror);  // Pass the direction and mirror flag

            // Write output to a .txt file
            FileWriter writer = new FileWriter(outputDirPath + File.separator + fileNameWithoutExt + ".txt");
            writer.write(classDiagram.toString());
            writer.close();

            System.out.println("Successfully created " + fileNameWithoutExt + ".txt in " + outputDirPath);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    // Method to generate class diagram for a JSONObject
    public static void generateClassDiagram(JSONObject json, String filename, String className, StringBuilder classDiagram, String parentProperty, boolean mirror) {
        // Start class definition for the current class
        String displayName = parentProperty == null ? " " : parentProperty;
        classDiagram.append("        class ").append(className).append(" as \"").append(displayName).append("\" {\n");

        // To hold fields in order
        StringBuilder fields = new StringBuilder();
        // Iterate over the keys in the JSONObject
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.opt(key);  // Use opt() to handle null values

            // Determine field type and add to fields
            if (value instanceof JSONObject) {
                fields.append("            <color:orange>Object</color> ").append(key).append("\n");
            } else if (value instanceof String) {
                fields.append("            <color:green>String</color> ").append(key).append("\n");
            } else if (value instanceof Integer) {
                fields.append("            <color:blue>Integer</color> ").append(key).append("\n");
            } else if (value instanceof Boolean) {
                fields.append("            <color:red>Boolean</color> ").append(key).append("\n");
            } else if (value == JSONObject.NULL) {
                fields.append("            <color:gray>Null</color> ").append(key).append("\n");
            } else if (value instanceof JSONArray) {
                fields.append("            <color:purple>").append(determineArrayType((JSONArray) value)).append("</color> ").append(key).append("\n");
            }

            // Add a separator line for class properties
            if (keys.hasNext()) {
                fields.append("            ---\n");
            }
        }

        // Append the fields to the class definition
        classDiagram.append(fields.toString());
        classDiagram.append("        }\n\n");

        // Generate nested class definitions recursively
        keys = json.keys(); // Reset the iterator for the next part
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                // Use the property key as the alias for nested objects
                generateClassDiagram((JSONObject) value, filename, className + capitalize(key), classDiagram, key, mirror);  // Pass the key as the alias
            } else if (value instanceof JSONArray) {
                // For arrays, use "as \"_\"" for the object type
                for (int i = 0; i < ((JSONArray) value).length(); i++) {
                    Object arrayElement = ((JSONArray) value).get(i);
                    if (arrayElement instanceof JSONObject) {
                        generateClassDiagram((JSONObject) arrayElement, filename, className + capitalize(key) + i, classDiagram, " ", mirror);  // Pass " " for array elements
                    }
                }
            }
        }

        // If mirroring is enabled, add the mirrored class
        if (mirror && !(className.startsWith(filename) && className.endsWith("Root"))) {
            classDiagram.append("        class Mirror").append(className).append(" as \"").append(displayName).append("\" {\n");
            classDiagram.append(fields.toString());  // Copy fields to the mirror class
            classDiagram.append("        }\n\n");
        }
    }

    // Method to generate connections between classes
    public static void generateConnections(JSONObject json, String className, StringBuilder connections, String direction, boolean mirror) {
        // Iterate over the keys in the JSONObject
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);

            if (value instanceof JSONObject) {
                // Create a connection between the class and its nested object with the specified direction
                connections.append("    ").append(className).append("::").append(key)
                           .append(" ").append(direction).append(" ").append(className + capitalize(key)).append("\n");
                // Recursively create connections for the nested object
                generateConnections((JSONObject) value, className + capitalize(key), connections, direction, mirror);

                // If mirroring is enabled, add a connection to the mirror class
                if (mirror) {
                    connections.append("    ").append(className).append("::").append(key)
                               .append(" -up- Mirror").append(className + capitalize(key)).append("\n");
                }
            } else if (value instanceof JSONArray) {
                // Handle connections for arrays
                for (int i = 0; i < ((JSONArray) value).length(); i++) {
                    Object arrayElement = ((JSONArray) value).get(i);
                    if (arrayElement instanceof JSONObject) {
                        connections.append("    ").append(className).append("::").append(key)
                                   .append(" ").append(direction).append(" ").append(className + capitalize(key) + i).append("\n");
                        // Recursively create connections for the array element
                        generateConnections((JSONObject) arrayElement, className + capitalize(key) + i, connections, direction, mirror);

                        // If mirroring is enabled, add a connection to the mirror class
                        if (mirror) {
                            connections.append("    ").append(className).append("::").append(key)
                                       .append(" -up- Mirror").append(className + capitalize(key) + i).append("\n");
                        }
                    }
                }
            }
        }
    }

    // Utility method to capitalize the first letter of a string
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Utility method to determine the type of elements in a JSONArray
    public static String determineArrayType(JSONArray jsonArray) {
        if (jsonArray.isEmpty()) {
            return "Empty Array";
        }
        Object firstElement = jsonArray.get(0);
        if (firstElement instanceof JSONObject) {
            return "Object[ ]";
        } else if (firstElement instanceof String) {
            return "String[ ]";
        } else if (firstElement instanceof Integer) {
            return "Integer[ ]";
        } else if (firstElement instanceof Boolean) {
            return "Boolean[ ]";
        } else {
            return "Mixed Array";
        }
    }
}