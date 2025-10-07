package Group2.Car.Rental.System.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    // Upload directory path - you can configure this in application.properties
    private final Path uploadPath = Paths.get("src/main/resources/static/uploads/images").toAbsolutePath().normalize();

    public ImageUploadController() {
        try {
            // Create upload directory if it doesn't exist
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @PostMapping("/vehicle-image")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<Map<String, Object>> uploadVehicleImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("Vehicle image upload requested. File: " + (file != null ? file.getOriginalFilename() : "null"));
        
        try {
            // Validate file
            if (file.isEmpty()) {
                System.out.println("File is empty");
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            System.out.println("File content type: " + contentType);
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Only image files are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (5MB limit)
            System.out.println("File size: " + file.getSize() + " bytes");
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "File size must be less than 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = "vehicle_" + UUID.randomUUID().toString() + fileExtension;
            System.out.println("Generated filename: " + uniqueFilename);

            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            System.out.println("Saving to: " + targetLocation.toString());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return success response with file URL
            String fileUrl = "/api/upload/images/" + uniqueFilename;
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileName", uniqueFilename);
            response.put("fileUrl", fileUrl);
            
            System.out.println("Upload successful. File URL: " + fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("Upload failed: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/package-image")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<Map<String, Object>> uploadPackageImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("Package image upload requested. File: " + (file != null ? file.getOriginalFilename() : "null"));
        
        try {
            // Validate file
            if (file.isEmpty()) {
                System.out.println("File is empty");
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            System.out.println("File content type: " + contentType);
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Only image files are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (5MB limit)
            System.out.println("File size: " + file.getSize() + " bytes");
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "File size must be less than 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = "package_" + UUID.randomUUID().toString() + fileExtension;
            System.out.println("Generated filename: " + uniqueFilename);

            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            System.out.println("Saving to: " + targetLocation.toString());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return success response with file URL
            String fileUrl = "/api/upload/images/" + uniqueFilename;
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileName", uniqueFilename);
            response.put("fileUrl", fileUrl);
            
            System.out.println("Upload successful. File URL: " + fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("Upload failed: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            System.out.println("Attempting to serve image: " + filePath.toString());
            System.out.println("Resource exists: " + resource.exists());
            System.out.println("Resource readable: " + resource.isReadable());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "image/jpeg"; // Default to jpeg for images
                }
                
                System.out.println("Serving image with content type: " + contentType);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .body(resource);
            } else {
                System.out.println("Image not found or not readable: " + filename);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL for image: " + filename);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            System.out.println("IO error serving image: " + filename + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}